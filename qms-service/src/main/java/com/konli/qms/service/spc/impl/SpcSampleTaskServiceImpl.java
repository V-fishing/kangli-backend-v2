package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.entity.SpcAlarm;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcParamProduct;
import com.konli.qms.domain.spc.entity.SpcSampleTask;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.domain.fia.mapper.FiaInspStdItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.domain.spc.mapper.SpcAlarmMapper;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.spc.mapper.SpcParamProductMapper;
import com.konli.qms.domain.spc.mapper.SpcSampleTaskMapper;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import com.konli.qms.service.spc.CpkGateService;
import com.konli.qms.service.spc.FiaChartTypeResolver;
import com.konli.qms.service.spc.SpcSampleTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 抽样批次任务实现。
 * 自动带出标准:工单×料号×工序 → FIA 标准项 → SpcParam(fiaStdItemId)。
 * 录满目标批次数自动结案并算 CPK 软告警(跌破门槛不卡停产)。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpcSampleTaskServiceImpl implements SpcSampleTaskService {

    private final SpcSampleTaskMapper spcSampleTaskMapper;
    private final SpcParamMapper spcParamMapper;
    private final SpcParamProductMapper spcParamProductMapper;
    private final SpcSubgroupMapper spcSubgroupMapper;
    private final SpcAlarmMapper spcAlarmMapper;
    private final FiaInspStdItemMapper fiaInspStdItemMapper;
    private final FiaInspStdMapper fiaInspStdMapper;
    private final CpkGateService cpkGateService;

    @Override
    @Transactional
    public SpcSampleTask create(String orgId, String woNo, String partNo, String procName,
                                String productName, Integer targetCount, String operatorId) {
        // targetCount<=0 视为"不限":可一直录,不自动结案(保持采集中)
        if (targetCount == null || targetCount < 1) {
            targetCount = 0;
        }
        String paramId = resolveParamId(orgId, partNo, procName);
        SpcSampleTask task = buildTask(orgId, woNo, partNo, procName, productName,
                targetCount, paramId, null, null, null, null, null, null, operatorId);
        spcSampleTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public List<SpcSampleTask> createBatch(String orgId, String woNo, String partNo, String procName,
                                           String productName, Integer targetCount, List<String> paramIds,
                                           List<String> fiaStdItemIds,
                                           String triggerType, String category, String supplierId,
                                           String supplierName, Boolean isUrgent, String remark, String operatorId) {
        if (targetCount == null || targetCount < 1) {
            targetCount = 0;
        }
        // 收集最终要建任务的 spc_param id:
        // 1) 已存在的 spc_param.id(兼容历史调用)
        // 2) FIA 检验标准项 id → 自动派生/复用 spc_param(完全分离于 spc_param 维护,参数来源即 FIA 标准库)
        List<String> ids = new java.util.ArrayList<>();
        if (paramIds != null) {
            for (String pid : paramIds) {
                if (pid != null && !pid.isBlank()) ids.add(pid);
            }
        }
        if (fiaStdItemIds != null) {
            for (String itemId : fiaStdItemIds) {
                if (itemId == null || itemId.isBlank()) continue;
                String derivedId = resolveParamFromStdItem(itemId, orgId, procName, woNo);
                if (derivedId != null) ids.add(derivedId);
            }
        }
        // 两者皆空时回退单参数自动匹配(兼容旧调用)
        if (ids.isEmpty()) {
            ids.add(resolveParamId(orgId, partNo, procName));
        }
        List<SpcSampleTask> created = new java.util.ArrayList<>();
        for (String pid : ids) {
            if (pid == null || pid.isBlank()) continue;
            try {
                SpcParam param = spcParamMapper.selectById(pid);
                if (param == null) {
                    throw new BusinessException(400, "未找到 SPC 参数[" + pid + "],无法创建抽样任务");
                }
                // 该参数是否已绑定当前产品料号;未绑定则复制出一条"产品专属"参数,保证抽样列表按料号独立成行
                String effectiveParamId = resolveParamForProduct(param, partNo, productName, category, orgId, woNo);
                SpcSampleTask task = buildTask(orgId, woNo, partNo, procName, productName,
                        targetCount, effectiveParamId, triggerType, category, supplierId, supplierName, isUrgent, remark, operatorId);
                spcSampleTaskMapper.insert(task);
                created.add(task);
            } catch (BusinessException be) {
                throw be;
            } catch (Exception e) {
                throw new BusinessException(500, "创建抽样任务失败(param=" + pid + "): " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }
        if (created.isEmpty()) {
            throw new BusinessException(400, "未选择任何有效 SPC 参数,无法创建抽样任务");
        }
        return created;
    }

    /** 构造并填充抽样任务公共字段(不含 insert)。 */
    private SpcSampleTask buildTask(String orgId, String woNo, String partNo, String procName,
                                    String productName, Integer targetCount, String paramId,
                                    String triggerType, String category, String supplierId,
                                    String supplierName, Boolean isUrgent, String remark, String operatorId) {
        SpcSampleTask task = new SpcSampleTask();
        task.setOrgId(orgId);
        task.setWoNo(woNo);
        task.setPartNo(partNo);
        task.setProcName(procName);
        task.setProductName(productName);
        task.setParamId(paramId);
        task.setTargetCount(targetCount);
        task.setCurrentCount(0);
        task.setStatus("采集中");
        task.setReleased(false);
        task.setAlarmFlag(false);
        task.setTriggerType(triggerType);
        task.setCategory(category);
        task.setSupplierId(supplierId);
        task.setSupplierName(supplierName);
        task.setUrgent(isUrgent);
        task.setRemark(remark);
        task.setCreatedBy(operatorId);
        return task;
    }

    @Override
    public List<SpcSampleTask> list(String orgId, String woNo, String partNo, String status) {
        LambdaQueryWrapper<SpcSampleTask> w = new LambdaQueryWrapper<>();
        if (orgId != null && !orgId.isBlank()) {
            w.eq(SpcSampleTask::getOrgId, orgId);
        }
        if (woNo != null && !woNo.isBlank()) {
            w.like(SpcSampleTask::getWoNo, woNo);
        }
        if (partNo != null && !partNo.isBlank()) {
            w.like(SpcSampleTask::getPartNo, partNo);
        }
        if (status != null && !status.isBlank()) {
            w.eq(SpcSampleTask::getStatus, status);
        }
        w.orderByDesc(SpcSampleTask::getCreatedAt);
        return spcSampleTaskMapper.selectList(w);
    }

    @Override
    public SpcSampleTask get(String id) {
        SpcSampleTask t = spcSampleTaskMapper.selectById(id);
        if (t == null) {
            throw new BusinessException(404, "抽样任务不存在");
        }
        return t;
    }

    @Override
    @Transactional
    public SpcSampleTask incCount(String taskId) {
        SpcSampleTask task = get(taskId);
        if ("已结案".equals(task.getStatus())) {
            return task;
        }
        int next = (task.getCurrentCount() == null ? 0 : task.getCurrentCount()) + 1;
        task.setCurrentCount(next);
        // 仅当 targetCount>0 且录满目标数时才自动结案并算 CPK 软告警;targetCount=0 视为不限,保持采集中可一直录
        if (task.getTargetCount() != null && task.getTargetCount() > 0 && next >= task.getTargetCount()) {
            task.setStatus("已结案");
            spcSampleTaskMapper.updateById(task);
            // 录满自动结案,触发 CPK 软告警(不卡停产)
            calcAndSetReleased(taskId);
            return get(taskId);
        }
        spcSampleTaskMapper.updateById(task);
        return task;
    }

    @Override
    @Transactional
    public void calcAndSetReleased(String taskId) {
        SpcSampleTask task = get(taskId);
        SpcParam param = spcParamMapper.selectById(task.getParamId());
        if (param == null) {
            return;
        }
        // 仅算该任务下 ROUTINE 子组
        List<SpcSubgroup> subgroups = spcSubgroupMapper.selectList(
                new LambdaQueryWrapper<SpcSubgroup>()
                        .eq(SpcSubgroup::getParamId, task.getParamId())
                        .eq(SpcSubgroup::getSampleTaskId, taskId)
                        .eq(SpcSubgroup::getStage, "ROUTINE")
                        .orderByDesc(SpcSubgroup::getSubgroupTime));
        if (subgroups.isEmpty()) {
            return;
        }
        BigDecimal cpk = computeCpk(param, subgroups);
        task.setCpk(cpk);
        boolean ok = cpkGateService.isSufficient(task.getOrgId(), cpk);
        task.setReleased(ok);
        if (!ok) {
            // 软告警:写告警记录(不卡停产)
            task.setAlarmFlag(true);
            writeAlarm(task, param, cpk);
        }
        spcSampleTaskMapper.updateById(task);
    }

    /** 仅算该任务子组的 CPK(组内 σ within,复用 d2 因子;无规格限或样本<10 返回 null)。 */
    private BigDecimal computeCpk(SpcParam param, List<SpcSubgroup> subgroups) {
        List<BigDecimal> xbars = subgroups.stream().map(SpcSubgroup::getXbar).filter(Objects::nonNull).toList();
        List<BigDecimal> ranges = subgroups.stream().map(SpcSubgroup::getRangeR).filter(Objects::nonNull).toList();
        if (xbars.isEmpty() || ranges.isEmpty()) {
            return null;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal x : xbars) sum = sum.add(x);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(xbars.size()), 6, RoundingMode.HALF_UP);
        BigDecimal avgRange = BigDecimal.ZERO;
        for (BigDecimal r : ranges) avgRange = avgRange.add(r);
        avgRange = avgRange.divide(BigDecimal.valueOf(ranges.size()), 6, RoundingMode.HALF_UP);
        int n = (param.getSubgroupSize() == null || param.getSubgroupSize() <= 0) ? 5 : param.getSubgroupSize();
        double d2 = d2Factor(n);
        double sigmaWithin = avgRange.doubleValue() / d2;
        BigDecimal usl = param.getSpecUpper();
        BigDecimal lsl = param.getSpecLower();
        if (usl == null || lsl == null || sigmaWithin == 0.0 || xbars.size() < 10) {
            return null;
        }
        BigDecimal k = param.getSigmaK() != null ? param.getSigmaK() : BigDecimal.valueOf(3);
        double uslMean = usl.doubleValue() - mean.doubleValue();
        double meanLsl = mean.doubleValue() - lsl.doubleValue();
        double minDelta = Math.min(uslMean, meanLsl);
        double val = minDelta / (k.doubleValue() * sigmaWithin);
        return BigDecimal.valueOf(val).setScale(4, RoundingMode.HALF_UP);
    }

    private void writeAlarm(SpcSampleTask task, SpcParam param, BigDecimal cpk) {
        try {
            SpcAlarm alarm = new SpcAlarm();
            alarm.setOrgId(task.getOrgId());
            alarm.setCode("SPT-" + System.currentTimeMillis());
            alarm.setParamId(task.getParamId());
            alarm.setParamName(param.getParamName());
            alarm.setCurrentValue(cpk);
            alarm.setTriggeredRule("抽样结案CPK软告警");
            alarm.setLevel("预警");
            alarm.setAlarmTime(LocalDateTime.now());
            alarm.setStatus("待确认");
            alarm.setWoNo(task.getWoNo());
            alarm.setBatchNo(task.getPartNo());
            spcAlarmMapper.insert(alarm);
        } catch (Exception e) {
            log.warn("[抽样CPK软告警] 写告警失败(忽略): {}", e.getMessage());
        }
    }

    @Override
    public String resolveParamId(String orgId, String partNo, String procName) {
        if (procName == null || procName.isBlank()) {
            throw new BusinessException(400, "工序不能为空,无法自动带出标准");
        }
        LambdaQueryWrapper<SpcParam> w = new LambdaQueryWrapper<SpcParam>()
                .eq(SpcParam::getProcName, procName)
                .eq(SpcParam::getIsActive, true)
                .eq(SpcParam::getChartable, true);
        if (orgId != null && !orgId.isBlank()) {
            // 优先同组织;同组织无匹配时退回通用参数
            w.eq(SpcParam::getOrgId, orgId);
        }
        w.last("LIMIT 1");
        SpcParam param = spcParamMapper.selectOne(w);
        if (param == null) {
            // 退回无组织限定匹配,保证可带出标准
            param = spcParamMapper.selectOne(
                    new LambdaQueryWrapper<SpcParam>()
                            .eq(SpcParam::getProcName, procName)
                            .eq(SpcParam::getIsActive, true)
                            .eq(SpcParam::getChartable, true)
                            .last("LIMIT 1"));
        }
        if (param == null) {
            throw new BusinessException(400, "未找到工序[" + procName + "]对应的 SPC 参数,请先在标准库生成 SPC 参数");
        }
        return param.getId();
    }

    /**
     * 解析"产品专属"参数:同一工序参数可能被多个产品复用,但抽样列表按产品(料号)分组展示。
     * 若该参数已通过 spc_param_product 绑定当前 partNo,直接复用;否则以原参数为模板复制一条
     * 专属参数并绑定当前产品,使新料号在「产品抽样 SPC」视图中独立成行、可独立录入子组。
     */
    private String resolveParamForProduct(SpcParam template, String partNo, String productName,
                                          String category, String orgId, String woNo) {
        if (partNo == null || partNo.isBlank()) {
            return template.getId(); // 无料号则直接复用
        }
        // 标准派生参数(fiaStdItemId 锚定)本身即抽样参数:不复制,直接绑定产品后复用,
        // 避免复制出"产品专属"参数导致原始标准参数成为无任务引用的 SAMPLE 孤儿(泄漏/崩溃)
        if (template.getFiaStdItemId() != null && !template.getFiaStdItemId().isBlank()) {
            upsertProductBinding(template.getId(), template.getOrgId(), partNo, productName, category, orgId);
            return template.getId();
        }
        // 已绑定该料号?
        Long bound = spcParamProductMapper.selectCount(
                new LambdaQueryWrapper<SpcParamProduct>()
                        .eq(SpcParamProduct::getParamId, template.getId())
                        .eq(SpcParamProduct::getPartNo, partNo));
        if (bound != null && bound > 0) {
            return template.getId();
        }
        // 复制专属参数(org 沿用模板参数所属组织,避免 org_id NOT NULL 约束;ROOT 场景下模板本身带具体分公司 org)
        SpcParam np = new SpcParam();
        np.setOrgId(template.getOrgId() != null ? template.getOrgId() : orgId);
        np.setParamName(template.getParamName());
        np.setProcName(template.getProcName());
        np.setProcessId(template.getProcessId());
        np.setChartable(template.getChartable());
        np.setUnit(template.getUnit());
        np.setSpecLower(template.getSpecLower());
        np.setSpecUpper(template.getSpecUpper());
        np.setSpecText(template.getSpecText());
        np.setTargetValue(template.getTargetValue());
        np.setSubgroupSize(template.getSubgroupSize());
        np.setCollectFreq(template.getCollectFreq());
        np.setChartType(template.getChartType());
        np.setSigmaMethod(template.getSigmaMethod());
        np.setSigmaK(template.getSigmaK());
        np.setCpkPeriod(template.getCpkPeriod());
        np.setSupplierId(template.getSupplierId());
        np.setIsActive(true);
        np.setParamSource("SAMPLE");   // 抽样任务产品专属参数,归属产品抽样 SPC 视图
        // 带入抽样工单号,供控制图页按工单聚拢抽样参数(与首件参数 srcWoNo 口径一致)
        if (StringUtils.hasText(woNo)) np.setSrcWoNo(woNo);
        spcParamMapper.insert(np);
        // 绑定产品
        SpcParamProduct pp = new SpcParamProduct();
        pp.setOrgId(np.getOrgId());
        pp.setParamId(np.getId());
        pp.setProductName(productName != null && !productName.isBlank() ? productName : partNo);
        pp.setPartNo(partNo);
        pp.setKind(category != null && !category.isBlank() ? category : "material");
        spcParamProductMapper.insert(pp);
        return np.getId();
    }

    /** 给参数绑定产品(幂等:已绑定该料号则跳过)。标准派生参数复用此逻辑避免复制出孤儿。 */
    private void upsertProductBinding(String paramId, String orgId, String partNo,
                                      String productName, String category, String fallbackOrg) {
        Long bound = spcParamProductMapper.selectCount(
                new LambdaQueryWrapper<SpcParamProduct>()
                        .eq(SpcParamProduct::getParamId, paramId)
                        .eq(SpcParamProduct::getPartNo, partNo));
        if (bound != null && bound > 0) return;
        SpcParamProduct pp = new SpcParamProduct();
        pp.setOrgId(orgId != null && !orgId.isBlank() ? orgId : fallbackOrg);
        pp.setParamId(paramId);
        pp.setProductName(productName != null && !productName.isBlank() ? productName : partNo);
        pp.setPartNo(partNo);
        pp.setKind(category != null && !category.isBlank() ? category : "material");
        spcParamProductMapper.insert(pp);
    }

    /**
     * 由 FIA 检验标准项派生 spc_param:以 fiaStdItemId + orgId 为去重锚,已存在则复用,
     * 否则按检验项(名称/单位/规格限/可制图判定)与所属标准(procName)新建一条标准参数。
     * 返回对应 spc_param 的 id(供 createBatch 循环建任务)。
     */
    private String resolveParamFromStdItem(String fiaStdItemId, String orgId, String procNameFallback, String woNo) {
        // 已存在锚定该检验项的参数则直接复用
        SpcParam exist = spcParamMapper.selectOne(
                new LambdaQueryWrapper<SpcParam>()
                        .eq(SpcParam::getFiaStdItemId, fiaStdItemId)
                        .eq(orgId != null && !orgId.isBlank(), SpcParam::getOrgId, orgId)
                        .last("LIMIT 1"));
        if (exist != null) {
            // 若该参数来源非抽样(如首件标准库生成),被抽样任务复用时统一归入抽样视图
            boolean dirty = false;
            if (!"SAMPLE".equals(exist.getParamSource())) {
                exist.setParamSource("SAMPLE");
                dirty = true;
            }
            // 同步标准项最新推荐图类型(避免复用历史写死默认值),与新建分支对齐
            FiaInspStdItem liveItem = fiaInspStdItemMapper.selectById(fiaStdItemId);
            if (liveItem != null) {
                boolean liveChartable = "数值".equals(liveItem.getValueType());
                List<String> recRaw = StringUtils.hasText(liveItem.getChartTypes())
                        ? FiaChartTypeResolver.parse(liveItem.getChartTypes())
                        : FiaChartTypeResolver.defaultChartTypes(liveItem.getValueType());
                List<String> rec = FiaChartTypeResolver.normalizeToBasic(recRaw);
                if (rec.isEmpty()) rec = List.of(liveChartable ? "Xbar" : "记录");
                String newChart = FiaChartTypeResolver.primaryChartType(rec);
                String newCands = FiaChartTypeResolver.join(rec);
                if (!newChart.equals(exist.getChartType()) || !newCands.equals(exist.getChartCandidates())) {
                    exist.setChartType(newChart);
                    exist.setChartCandidates(newCands);
                    dirty = true;
                }
            }
            if (dirty) spcParamMapper.updateById(exist);
            return exist.getId();
        }
        FiaInspStdItem item = fiaInspStdItemMapper.selectById(fiaStdItemId);
        if (item == null) return null;
        FiaInspStd std = item.getStdId() != null ? fiaInspStdMapper.selectById(item.getStdId()) : null;
        String procName = (std != null && std.getProcName() != null) ? std.getProcName() : procNameFallback;
        // 可制图判定:数值型才可制图(与 listByStd 一致)
        boolean chartable = "数值".equals(item.getValueType());
        SpcParam np = new SpcParam();
        // org 兜底: 传入 orgId → 标准 org → 检验项 org(避免 org_id NOT NULL 约束)
        String realOrg = (orgId != null && !orgId.isBlank()) ? orgId
                : (std != null && std.getOrgId() != null ? std.getOrgId()
                : (item.getOrgId() != null ? item.getOrgId() : null));
        np.setOrgId(realOrg);
        np.setParamName(item.getItemName());
        np.setProcName(procName);
        // 工序字典 id 从标准带出,保持与 SPC 工序字典同源
        if (std != null && std.getSpcProcessId() != null) np.setProcessId(std.getSpcProcessId());
        np.setChartable(chartable);
        np.setUnit(item.getUnit());
        // 优先用检验项显式上下限;为空则从 标准值±公差 推算(真实数据多数只填 std_value+tolerance)
        BigDecimal[] lr = resolveSpecBounds(item.getLowerLimit(), item.getUpperLimit(),
                item.getStdValue(), item.getTolerance());
        np.setSpecLower(lr[0]);
        np.setSpecUpper(lr[1]);
        np.setSpecText(item.getStdValue());
        // 目标值取标准中心值(stdValue);数值型才解析,非数值留空
        if (chartable && item.getStdValue() != null && !item.getStdValue().isBlank()) {
            try {
                np.setTargetValue(new BigDecimal(item.getStdValue().trim()));
            } catch (NumberFormatException ignored) {
                np.setTargetValue(null);
            }
        }
        np.setFiaStdItemId(item.getId());
        np.setIsActive(true);
        np.setParamSource("SAMPLE");   // 抽样任务流程派生,归属产品抽样 SPC 视图
        // 带入抽样工单号,供控制图页按工单聚拢抽样参数(与首件参数 srcWoNo 口径一致)
        if (StringUtils.hasText(woNo)) np.setSrcWoNo(woNo);
        // 控制图类型按标准项推荐集合带入: 取主图写 chartType、全集写 chartCandidates,
        // 与首件任务 fromFiaTask 对齐; 统一为基础图码体系, 兼容历史组合码。
        List<String> recommendedRaw = StringUtils.hasText(item.getChartTypes())
                ? FiaChartTypeResolver.parse(item.getChartTypes())
                : FiaChartTypeResolver.defaultChartTypes(item.getValueType());
        List<String> recommended = FiaChartTypeResolver.normalizeToBasic(recommendedRaw);
        if (recommended.isEmpty()) recommended = List.of(chartable ? "Xbar" : "记录");
        np.setChartType(FiaChartTypeResolver.primaryChartType(recommended));
        np.setChartCandidates(FiaChartTypeResolver.join(recommended));
        // 补全 NOT NULL 列,避免插入失败
        if (np.getCollectFreq() == null) np.setCollectFreq("每批");
        if (np.getChartType() == null) np.setChartType(chartable ? "Xbar" : "记录");
        if (np.getSubgroupSize() == null) np.setSubgroupSize(5);
        if (np.getSigmaMethod() == null) np.setSigmaMethod("within");
        if (np.getSigmaK() == null) np.setSigmaK(new java.math.BigDecimal("3"));
        spcParamMapper.insert(np);
        if (np.getId() == null) {
            throw new BusinessException(500, "派生 SPC 参数失败: 主键未生成(item=" + fiaStdItemId + ")");
        }
        return np.getId();
    }

    /**
     * 解析规格上下限:显式 lower/upper 优先;为空时由 stdValue±tolerance 推算。
     * tolerance 支持常见写法:±X、+X/-Y、X~Y、X-Y、X/Y。
     */
    private BigDecimal[] resolveSpecBounds(BigDecimal lower, BigDecimal upper,
                                           String stdValue, String tolerance) {
        if (lower != null && upper != null) return new BigDecimal[]{lower, upper};
        BigDecimal base = null;
        if (stdValue != null && !stdValue.isBlank()) {
            try {
                base = new BigDecimal(stdValue.trim());
            } catch (NumberFormatException ignored) {
                base = null;
            }
        }
        BigDecimal[] parsed = parseTolerance(tolerance);
        // 显式上下限优先补齐缺的一侧
        BigDecimal lo = lower != null ? lower : (parsed != null ? parsed[0] : null);
        BigDecimal hi = upper != null ? upper : (parsed != null ? parsed[1] : null);
        // 仍缺则从 基准值±公差 推算
        if (base != null) {
            if (lo == null && parsed != null && parsed[2] != null) lo = base.add(parsed[2]);
            if (hi == null && parsed != null && parsed[3] != null) hi = base.add(parsed[3]);
        }
        return new BigDecimal[]{lo, hi};
    }

    /**
     * 解析公差字符串。返回 [绝对下界, 绝对上界, 相对下偏, 相对上偏]。
     * 相对偏差用于配合基准值推算;绝对界直接可用。
     */
    private BigDecimal[] parseTolerance(String tolerance) {
        if (tolerance == null || tolerance.isBlank() || "-".equals(tolerance.trim())) return null;
        String t = tolerance.trim().replaceAll("\\s+", "");
        try {
            // ±X
            if (t.startsWith("±") || t.startsWith("+/-")) {
                BigDecimal half = new BigDecimal(t.replace("±", "").replace("+/-", ""));
                return new BigDecimal[]{null, null, half.negate(), half};
            }
            // +X/-Y
            if (t.contains("/")) {
                String[] parts = t.split("/");
                BigDecimal loOff = parseOffset(parts[0]);
                BigDecimal hiOff = parseOffset(parts[1]);
                if (loOff != null && hiOff != null) {
                    return new BigDecimal[]{null, null, loOff, hiOff};
                }
            }
            // X~Y 或 X-Y (非 ±、非纯负号开头)
            if (t.contains("~") || (t.contains("-") && !t.startsWith("-"))) {
                String[] parts = t.split("[~-]");
                if (parts.length == 2) {
                    BigDecimal lo = new BigDecimal(parts[0].trim());
                    BigDecimal hi = new BigDecimal(parts[1].trim());
                    return new BigDecimal[]{lo, hi, null, null};
                }
            }
            // 单数字当作对称公差
            BigDecimal single = new BigDecimal(t);
            return new BigDecimal[]{null, null, single.negate(), single};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 解析带 + / - 前缀的偏移量,如 "+0.1"、"-0.2"。 */
    private BigDecimal parseOffset(String s) {
        try {
            String v = s.trim();
            if (v.startsWith("+") || v.startsWith("-")) return new BigDecimal(v);
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double d2Factor(int n) {
        return switch (n) {
            case 2 -> 1.128;
            case 3 -> 1.693;
            case 4 -> 2.059;
            case 5 -> 2.326;
            case 6 -> 2.534;
            case 7 -> 2.704;
            case 8 -> 2.847;
            case 9 -> 2.970;
            case 10 -> 3.078;
            default -> 2.326;
        };
    }
}
