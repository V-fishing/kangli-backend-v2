package com.konli.qms.service.spc.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.mapper.FiaInspStdItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.domain.fia.mapper.FiaInspItemMapper;
import com.konli.qms.domain.fia.mapper.FiaTaskMapper;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcParamProduct;
import com.konli.qms.domain.spc.entity.SpcProcess;
import com.konli.qms.domain.spc.entity.SpcSpecStandard;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.spc.mapper.SpcParamProductMapper;
import com.konli.qms.domain.spc.mapper.SpcProcessMapper;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.spc.mapper.SpcSpecStandardMapper;
import com.konli.qms.service.spc.SpcParamService;
import com.konli.qms.service.spc.FiaChartTypeResolver;
import com.konli.qms.service.support.OrgIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpcParamServiceImpl implements SpcParamService {

    private final SpcParamMapper spcParamMapper;
    private final FiaInspStdItemMapper fiaInspStdItemMapper;
    private final FiaInspStdMapper fiaInspStdMapper;
    private final SpcSpecStandardMapper spcSpecStandardMapper;
    private final SpcProcessMapper spcProcessMapper;
    private final OrgIdResolver orgIdResolver;
    private final FiaTaskMapper fiaTaskMapper;
    private final FiaInspItemMapper fiaInspItemMapper;
    private final SpcParamProductMapper spcParamProductMapper;

    @Override
    public List<SpcParam> list(String productName, String procName, String paramSource) {
        List<SpcParam> params = spcParamMapper.selectList(null);
        // 填充 fiaStdItemName(展示用)
        List<String> itemIds = params.stream().map(SpcParam::getFiaStdItemId).filter(StringUtils::hasText).distinct().collect(Collectors.toList());
        if (!itemIds.isEmpty()) {
            Map<String, FiaInspStdItem> itemMap = fiaInspStdItemMapper.selectBatchIds(itemIds).stream()
                    .collect(Collectors.toMap(FiaInspStdItem::getId, i -> i));
            Map<String, FiaInspStd> stdMap = fiaInspStdMapper.selectBatchIds(
                    itemMap.values().stream().map(FiaInspStdItem::getStdId).filter(StringUtils::hasText).distinct().collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(FiaInspStd::getId, s -> s));
            for (SpcParam p : params) {
                FiaInspStdItem item = p.getFiaStdItemId() != null ? itemMap.get(p.getFiaStdItemId()) : null;
                if (item != null) {
                    FiaInspStd std = stdMap.get(item.getStdId());
                    String stdCode = std != null ? std.getCode() : item.getStdId();
                    String stdProcName = std != null ? std.getProcName() : "";
                    p.setFiaStdItemName(stdCode + " - " + item.getItemName() + " (" + stdProcName + ")");
                }
            }
        }
        // 填充 specStandardName(展示用)
        List<String> stdIds = params.stream().map(SpcParam::getSpecStandardId).filter(StringUtils::hasText).distinct().collect(Collectors.toList());
        if (!stdIds.isEmpty()) {
            Map<String, SpcSpecStandard> stdMap = spcSpecStandardMapper.selectBatchIds(stdIds).stream()
                    .collect(Collectors.toMap(SpcSpecStandard::getId, s -> s));
            for (SpcParam p : params) {
                SpcSpecStandard std = p.getSpecStandardId() != null ? stdMap.get(p.getSpecStandardId()) : null;
                if (std != null) {
                    p.setSpecStandardName(std.getMaterial() + " - " + std.getProcName());
                }
            }
        }
        // 填充 processName(父级工序展示用)
        List<String> procIds = params.stream().map(SpcParam::getProcessId).filter(StringUtils::hasText).distinct().collect(Collectors.toList());
        if (!procIds.isEmpty()) {
            Map<String, SpcProcess> procMap = spcProcessMapper.selectBatchIds(procIds).stream()
                    .collect(Collectors.toMap(SpcProcess::getId, p -> p));
            for (SpcParam p : params) {
                SpcProcess proc = p.getProcessId() != null ? procMap.get(p.getProcessId()) : null;
                if (proc != null) {
                    p.setProcessName(proc.getProcessName());
                }
            }
        }
        // 填充产品关联(参数↔产品多对多)
        fillProducts(params);
        // 按产品名过滤:选中产品时,返回「绑定该产品的参数 + 未绑定任何产品的通用参数」
        if (StringUtils.hasText(productName)) {
            params = params.stream().filter(p -> {
                List<SpcParamProduct> ps = p.getProducts();
                if (ps == null || ps.isEmpty()) {
                    return true; // 通用参数(未绑定产品)
                }
                return ps.stream().anyMatch(x -> productName.equals(x.getProductName()));
            }).collect(Collectors.toList());
        }
        // 按工序名过滤:选中工序时,仅返回工序名/工艺名匹配的参数(与产品维度取交集)
        if (StringUtils.hasText(procName)) {
            String kw = procName.trim();
            params = params.stream().filter(p -> {
                String pn = p.getProcessName() != null ? p.getProcessName() : (p.getProcName() != null ? p.getProcName() : "");
                return pn.contains(kw);
            }).collect(Collectors.toList());
        }
        // 按来源过滤:选中来源时仅返回对应 paramSource 的参数(与产品/工序维度取交集)
        if (StringUtils.hasText(paramSource)) {
            params = params.stream().filter(p -> paramSource.equals(p.getParamSource())).collect(Collectors.toList());
        }
        return params;
    }

    @Override
    public List<SpcParam> listByStd(String stdId) {
        FiaInspStd std = fiaInspStdMapper.selectById(stdId);
        if (std == null) return List.of();
        // 带出标准下「全部未删除」检验项(不限 CTQ/数值型):文本/外观类虽不可制图,仍允许勾选,
        // 仅标注「不可制图」;已存在对应 SPC 参数的优先用实体,否则用检验项实时构造候选参数
        // (fiaStdItemId=检验项 id,便于前端收集提交后精确对应)。
        List<FiaInspStdItem> items = fiaInspStdItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspStdItem>()
                        .eq(FiaInspStdItem::getStdId, stdId)
                        .eq(FiaInspStdItem::getIsDeleted, false));
        if (items.isEmpty()) return List.of();
        // 已有 SPC 参数(fiaStdItemId 精确匹配)按 检验项id 建索引
        List<SpcParam> all = list(null, null, null);
        Map<String, SpcParam> existByItem = all.stream()
                .filter(p -> p.getIsActive() && p.getFiaStdItemId() != null)
                .collect(Collectors.toMap(SpcParam::getFiaStdItemId, p -> p, (a, b) -> a));
        List<SpcParam> result = new ArrayList<>();
        for (FiaInspStdItem it : items) {
            SpcParam exist = existByItem.get(it.getId());
            if (exist != null) {
                result.add(exist);
            } else {
                // 构造候选参数:仅用于前端勾选,提交后后端按 stdItemId 精确对应标准检验项
                SpcParam cand = new SpcParam();
                cand.setId("");
                cand.setParamName(it.getItemName());
                cand.setUnit(it.getUnit());
                cand.setSpecLower(it.getLowerLimit());
                cand.setSpecUpper(it.getUpperLimit());
                cand.setFiaStdItemId(it.getId());
                cand.setIsActive(true);
                // 可制图判定:数值型(有中心值/公差可算控制限)才可制图;文本/枚举型不可制图。
                // 注意:本库规格多以 std_value+tolerance 字符串存储,lower_limit/upper_limit 多为空,
                // 故以 value_type 为判定依据,而非依赖上下限数值列非空。
                boolean chartable = "数值".equals(it.getValueType());
                cand.setChartable(chartable);
                // 带出工序显示名,避免前端直接展示工序代码
                cand.setProcessName(std.getProcName());
                result.add(cand);
            }
        }
        return result;
    }

    @Override
    public PageResult<SpcParam> listPage(String productName, String procName, String paramSource, String keyword, int page, int size) {
        List<SpcParam> all = list(productName, procName, paramSource);
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim().toLowerCase();
            all = all.stream().filter(p ->
                (p.getParamName() != null && p.getParamName().toLowerCase().contains(kw))
                || (p.getProcessName() != null && p.getProcessName().toLowerCase().contains(kw))
            ).collect(Collectors.toList());
        }
        int total = all.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        List<SpcParam> records = from >= total ? List.of() : all.subList(from, to);
        return new PageResult<>(records, total, page, size);
    }

    @Override
    public SpcParam get(String id) {
        SpcParam p = spcParamMapper.selectById(id);
        if (p != null) {
            fillProducts(List.of(p));
        }
        return p;
    }

    @Override
    @Transactional
    public SpcParam create(SpcParam param) {
        // org_id 解析:前端可能传 org_code(MZ)或 UUID,统一转为 UUID
        param.setOrgId(orgIdResolver.resolve(param.getOrgId()));
        if (param.getIsActive() == null) {
            param.setIsActive(true);
        }
        // 来源默认手动新建(首件标准库/FIA任务生成在 ensureFromFiaTask 内标记 FIA_FIRST)
        if (!StringUtils.hasText(param.getParamSource())) {
            param.setParamSource("MANUAL");
        }
        validateBinding(param);
        fillSpecFromStandard(param);
        fillSpecFromStdItem(param);
        if (!StringUtils.hasText(param.getSpecText())) {
            param.setSpecText("");
        }
        deriveChartable(param);
        spcParamMapper.insert(param);
        upsertProducts(param);
        return param;
    }

    @Override
    @Transactional
    public void update(SpcParam param) {
        validateBinding(param);
        boolean clearSpecStd = !StringUtils.hasText(param.getSpecStandardId());
        boolean clearFiaItem = !StringUtils.hasText(param.getFiaStdItemId());
        fillSpecFromStandard(param);
        fillSpecFromStdItem(param);
        if (!StringUtils.hasText(param.getSpecText())) {
            param.setSpecText("");
        }
        deriveChartable(param);
        spcParamMapper.updateById(param);
        // 用户清空关联选择时,前端传 null,但 MyBatis-Plus 默认跳过 null 字段,需显式置 NULL
        if (clearSpecStd && param.getSpecStandardId() == null) {
            spcParamMapper.update(null,
                new LambdaUpdateWrapper<SpcParam>().eq(SpcParam::getId, param.getId()).set(SpcParam::getSpecStandardId, null));
        }
        if (clearFiaItem && param.getFiaStdItemId() == null) {
            spcParamMapper.update(null,
                new LambdaUpdateWrapper<SpcParam>().eq(SpcParam::getId, param.getId()).set(SpcParam::getFiaStdItemId, null));
        }
        // 同步产品关联(先删后插,保证与表单一致)
        upsertProducts(param);
    }

    @Override
    @Transactional
    public void delete(String id) {
        spcParamMapper.deleteById(id);
    }

    /**
     * 校验 SPC 参数必须绑定关联产品。
     * 关联产品(products)不可为空,否则抛出 BusinessException。
     *
     * 注意:供应商(supplierId)为「物料」维度可选关联,成品/半成品维度本就没有供应商,
     * 故不再对所有参数强制供应商必填(此前对成品/半成品参数误拦截,已取消)。
     * 参数↔产品关联在派生(首件/抽样)与手动新建流程中均有填充,保留该校验。
     */
    private void validateBinding(SpcParam param) {
        List<SpcParamProduct> products = param.getProducts();
        if (products == null || products.isEmpty()) {
            throw new BusinessException("请先绑定关联产品后再保存 SPC 参数");
        }
        // 过滤掉无效(无产品名)的产品行,避免用户误留空行绕过校验
        boolean hasValid = products.stream().anyMatch(p -> p != null && StringUtils.hasText(p.getProductName()));
        if (!hasValid) {
            throw new BusinessException("请先绑定有效的关联产品(产品名称不可为空)后再保存 SPC 参数");
        }
    }

    /** 若 specStandardId 非空,从标准线读取规格界限并填充到 SpcParam 的冗余字段(优先级高于 fillSpecFromStdItem)。 */
    private void fillSpecFromStandard(SpcParam param) {
        if (!StringUtils.hasText(param.getSpecStandardId())) {
            return;
        }
        SpcSpecStandard std = spcSpecStandardMapper.selectById(param.getSpecStandardId());
        if (std == null) {
            return;
        }
        param.setSpecLower(std.getSpecLower());
        param.setSpecUpper(std.getSpecUpper());
        if (std.getTargetValue() != null) {
            param.setTargetValue(std.getTargetValue());
        }
        if (StringUtils.hasText(std.getUnit())) {
            param.setUnit(std.getUnit());
        }
        if (StringUtils.hasText(std.getChartType()) && !StringUtils.hasText(param.getChartType())) {
            // 标准线 chartType 可能为历史组合码(Xbar-R等), 归一为基础图码写入, 与前端单一体系一致
            List<String> basic = FiaChartTypeResolver.normalizeToBasic(FiaChartTypeResolver.parse(std.getChartType()));
            param.setChartType(FiaChartTypeResolver.primaryChartType(basic));
            param.setChartCandidates(FiaChartTypeResolver.join(basic));
        }
    }

    /** 若 fiaStdItemId 非空,从检验标准项读取规格界限并填充到 SpcParam 的冗余字段。
     *  兼容:标准项规格以「std_value(中心值/文本) + tolerance(±公差)」字符串存储,
     *  与 SPC 参数的 spec_lower/spec_upper/target_value(BigDecimal)字段口径不同,需解析转换,
     *  否则审核通过后进入控制图/参数页会出现字段不匹配(如关键尺寸只有目标值缺上下限)。 */
    private void fillSpecFromStdItem(SpcParam param) {
        if (!StringUtils.hasText(param.getFiaStdItemId())) {
            return;
        }
        FiaInspStdItem item = fiaInspStdItemMapper.selectById(param.getFiaStdItemId());
        if (item == null) {
            return;
        }
        param.setSpecLower(item.getLowerLimit());
        param.setSpecUpper(item.getUpperLimit());
        param.setUnit(item.getUnit());
        // 标准项的结构化上下限优先(本库多为空,规格落在 std_value+tolerance)
        SpecTriple parsed = parseSpecText(item.getStdValue(), item.getTolerance(), item.getUnit());
        if (parsed == null) {
            // std_value 非数值型(如文本外观)时尝试直接作为目标值文本
            if (StringUtils.hasText(item.getStdValue())) {
                param.setSpecText(item.getStdValue());
            }
            return;
        }
        if (param.getSpecLower() == null) param.setSpecLower(parsed.lower());
        if (param.getSpecUpper() == null) param.setSpecUpper(parsed.upper());
        if (param.getTargetValue() == null) param.setTargetValue(parsed.target());
        if (!StringUtils.hasText(param.getUnit()) && StringUtils.hasText(parsed.unit())) {
            param.setUnit(parsed.unit());
        }
    }

    /** 派生 chartable:规格上下限或目标值至少有一个非空才可制图,否则标记不可制图(如外观类仅文本)。 */
    private void deriveChartable(SpcParam param) {
        boolean chartable = param.getSpecLower() != null
                || param.getSpecUpper() != null
                || param.getTargetValue() != null;
        param.setChartable(chartable);
    }

    @Override
    public List<SpcParam> ensureFromFiaTask(String taskId) {
        FiaTask task = fiaTaskMapper.selectById(taskId);
        if (task == null) {
            return List.of();
        }
        String orgId = orgIdResolver.resolve(task.getOrgId());
        String processId = resolveProcessId(orgId, task.getProcName());

        List<FiaInspItem> items = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, taskId));
        if (items.isEmpty()) {
            return List.of();
        }

        // 已生成的参数按来源检验项去重,避免重复点击重复建参
        List<String> itemIds = items.stream().map(FiaInspItem::getId).collect(Collectors.toList());
        Map<String, SpcParam> existing = spcParamMapper.selectList(
                        new LambdaQueryWrapper<SpcParam>().in(SpcParam::getSrcItemId, itemIds))
                .stream().collect(Collectors.toMap(SpcParam::getSrcItemId, p -> p, (a, b) -> a));

        List<SpcParam> result = new ArrayList<>();
        for (FiaInspItem item : items) {
            SpcParam ex = existing.get(item.getId());
            if (ex != null) {
                result.add(enrich(ex));
                continue;
            }

            FiaInspStdItem stdItem = StringUtils.hasText(item.getStdItemId())
                    ? fiaInspStdItemMapper.selectById(item.getStdItemId()) : null;
            boolean stdHasLimits = stdItem != null
                    && (stdItem.getLowerLimit() != null || stdItem.getUpperLimit() != null);
            SpecTriple parsed = parseSpecText(item.getStdValue(), item.getTolerance(), item.getUnit());
            // 标准项已显式配置控制图推荐集合(如 文本/外观型配 P 图)时,即使无规格上下限也允许带入,
            // 因为 P 图依赖合格/不合格计数而非上下限;其余完全无规格信息者跳过。
            boolean stdHasChart = stdItem != null && StringUtils.hasText(stdItem.getChartTypes());
            if (!stdHasLimits && parsed == null && !StringUtils.hasText(item.getStdValue()) && !stdHasChart) {
                // 完全无规格信息且未配置控制图(非数值型且无可解析文本)→ 跳过,不生成无意义的 SPC 参数
                continue;
            }

            SpcParam param = new SpcParam();
            param.setOrgId(orgId);
            param.setParamName(item.getItemName());
            param.setProcName(task.getProcName());
            param.setProcessId(processId);
            param.setUnit(item.getUnit());
            // 自动从 FIA 任务带出关联供应商(满足 SPC 参数强制绑定供应商要求)
            param.setSupplierId(task.getSupplierId());
            param.setFiaStdItemId(item.getStdItemId());
            param.setSrcItemId(item.getId());
            param.setSubgroupSize(5);
            // 控制图类型按标准项推荐集合带入: 取主图写 chartType、全集写 chartCandidates,
            // 统一为基础图码体系(Xbar/R/S/I/MR/P/...), 兼容历史组合码(Xbar-R等)经 normalizeToBasic 展开。
            List<String> recommendedRaw = stdItem != null && StringUtils.hasText(stdItem.getChartTypes())
                    ? FiaChartTypeResolver.parse(stdItem.getChartTypes())
                    : FiaChartTypeResolver.defaultChartTypes(stdItem != null ? stdItem.getValueType() : "numeric");
            List<String> recommended = FiaChartTypeResolver.normalizeToBasic(recommendedRaw);
            param.setChartType(FiaChartTypeResolver.primaryChartType(recommended));
            param.setChartCandidates(FiaChartTypeResolver.join(recommended));
            param.setSigmaMethod("within");
            param.setSigmaK(new BigDecimal("3"));
            param.setCollectFreq("每日");
            param.setIsActive(true);
            // 来源标记:工装首件任务(source=TOOLING)派生参数归 TOOLING,其余(产线/来料首件)归 FIA_FIRST;
            // 均归属"首件 SPC"视图,前端按来源筛选可区分工装 SPC。
            param.setParamSource("TOOLING".equalsIgnoreCase(task.getSource()) ? "TOOLING" : "FIA_FIRST");

            // 优先使用标准项的结构化规格;若标准项无结构化规格,回退解析标准值/公差文本
            fillSpecFromStdItem(param);
            if (!StringUtils.hasText(param.getUnit()) && StringUtils.hasText(item.getUnit())) {
                param.setUnit(item.getUnit());
            }
            boolean stillEmpty = param.getSpecLower() == null
                    && param.getSpecUpper() == null && param.getTargetValue() == null;
            // 标准项缺上下限时,用文本公差(如 ±0.05)补全,使与检验项实际规格一致
            boolean missingLimits = (param.getSpecLower() == null || param.getSpecUpper() == null)
                    && parsed != null && (parsed.lower() != null || parsed.upper() != null);
            if ((stillEmpty || missingLimits) && parsed != null) {
                if (param.getSpecLower() == null) param.setSpecLower(parsed.lower());
                if (param.getSpecUpper() == null) param.setSpecUpper(parsed.upper());
                if (param.getTargetValue() == null) param.setTargetValue(parsed.target());
                if (!StringUtils.hasText(param.getUnit()) && StringUtils.hasText(parsed.unit())) {
                    param.setUnit(parsed.unit());
                }
            }
            if (!StringUtils.hasText(param.getSpecText())) {
                param.setSpecText(buildSpecText(item));
            }
            deriveChartable(param);
            // 注:即使 chartable=false(如纯文本外观项,无结构化上下限)也照常生成参数,
            // 仅标注不可制图(控制图不绘制);与前端「不可制图项仍可选、审核通过后进参数页可见」对齐,
            // 避免用户勾选了参数却在 SPC 参数页找不到。
            spcParamMapper.insert(param);
            upsertProductForTask(param, task);
            result.add(enrich(param));
        }
        fillProducts(result);
        return result;
    }

    private String resolveProcessId(String orgId, String procName) {
        if (StringUtils.hasText(procName)) {
            SpcProcess p = spcProcessMapper.selectOne(new LambdaQueryWrapper<SpcProcess>()
                    .eq(SpcProcess::getOrgId, orgId).eq(SpcProcess::getProcessName, procName));
            if (p != null) {
                return p.getId();
            }
        }
        SpcProcess detect = spcProcessMapper.selectOne(new LambdaQueryWrapper<SpcProcess>()
                .eq(SpcProcess::getOrgId, orgId).eq(SpcProcess::getProcessName, "检测"));
        if (detect != null) {
            return detect.getId();
        }
        SpcProcess any = spcProcessMapper.selectOne(new LambdaQueryWrapper<SpcProcess>()
                .eq(SpcProcess::getOrgId, orgId).orderByAsc(SpcProcess::getSortNo).last("LIMIT 1"));
        return any != null ? any.getId() : null;
    }

    private SpcParam enrich(SpcParam p) {
        if (StringUtils.hasText(p.getProcessId())) {
            SpcProcess proc = spcProcessMapper.selectById(p.getProcessId());
            if (proc != null) {
                p.setProcessName(proc.getProcessName());
            }
        }
        return p;
    }

    /** 批量回填参数↔产品关联(一次 IN 查询,无 N+1)。 */
    private void fillProducts(Collection<SpcParam> params) {
        List<String> ids = params.stream().map(SpcParam::getId).filter(StringUtils::hasText).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        List<SpcParamProduct> all = spcParamProductMapper.selectByParamIds(ids);
        Map<String, List<SpcParamProduct>> byParam = all.stream().collect(Collectors.groupingBy(SpcParamProduct::getParamId));
        for (SpcParam p : params) {
            List<SpcParamProduct> list = byParam.get(p.getId());
            p.setProducts(list != null ? list : List.of());
        }
    }

    /** 同步某参数的产品关联:先物理删除旧关联,再按表单插入(保证与提交一致)。 */
    private void upsertProducts(SpcParam param) {
        spcParamProductMapper.deleteByParamId(param.getId());
        List<SpcParamProduct> products = param.getProducts();
        if (products == null || products.isEmpty()) {
            return;
        }
        for (SpcParamProduct pp : products) {
            pp.setId(null);
            pp.setParamId(param.getId());
            pp.setOrgId(param.getOrgId());
            if (!StringUtils.hasText(pp.getKind())) {
                pp.setKind("product");
            }
            spcParamProductMapper.insert(pp);
        }
    }

    /** 由 FIA 任务的产品(名/件号)与分类/来源派生 kind,建立参数↔产品关联。 */
    private void upsertProductForTask(SpcParam param, FiaTask task) {
        if (!StringUtils.hasText(task.getProductName())) {
            return; // 任务无产品名则不建关联
        }
        // 优先用任务分类(物料/半成品/成品),仅三值白名单放行,防止脏值绕过 CHECK 写入关联表
        String cat = task.getCategory();
        String kind;
        if ("material".equals(cat) || "semi".equals(cat) || "product".equals(cat)) {
            kind = cat;
        } else {
            // 老任务未设分类:按来源派生(供应商来料→物料, 产线首件→成品)
            kind = "SUPPLIER".equalsIgnoreCase(task.getSource()) ? "material" : "product";
        }
        SpcParamProduct pp = new SpcParamProduct();
        pp.setOrgId(param.getOrgId());
        pp.setParamId(param.getId());
        pp.setProductName(task.getProductName());
        pp.setPartNo(task.getPartNo());
        pp.setKind(kind);
        spcParamProductMapper.insert(pp);
    }

    private String buildSpecText(FiaInspItem item) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(item.getStdValue())) {
            sb.append("标准值 ").append(item.getStdValue());
        }
        if (StringUtils.hasText(item.getTolerance()) && !"-".equals(item.getTolerance())) {
            sb.append(" 公差 ").append(item.getTolerance());
        }
        return sb.toString().trim();
    }

    /** 从「标准值 + 公差」文本解析结构化规格(不区分标准项/实际检验项,统一口径)。
     *  兼容格式:纯数字(中心值) / ≥95 / ≤95 / =95 / ±0.05(公差,配合中心值算上下限) / 文本(返回 null)。
     *  无规格信息返回 null。 */
    private SpecTriple parseSpecText(String stdValue, String tolerance, String unit) {
        String sv = stdValue != null ? stdValue.trim() : "";
        String tol = tolerance != null ? tolerance.trim() : "";
        if (!StringUtils.hasText(sv) && !StringUtils.hasText(tol)) {
            return null;
        }
        BigDecimal target = null, lower = null, upper = null;
        if (StringUtils.hasText(sv)) {
            try {
                if (sv.matches("^[+\\-]?\\d+(\\.\\d+)?$")) {
                    target = new BigDecimal(sv);
                } else if (sv.startsWith("≥") || sv.startsWith(">=")) {
                    String n = sv.startsWith("≥") ? sv.substring(1) : sv.substring(2);
                    lower = new BigDecimal(n.trim());
                    target = lower;
                } else if (sv.startsWith("≤") || sv.startsWith("<=")) {
                    String n = sv.startsWith("≤") ? sv.substring(1) : sv.substring(2);
                    upper = new BigDecimal(n.trim());
                    target = upper;
                } else if (sv.startsWith("=")) {
                    target = new BigDecimal(sv.substring(1).trim());
                } else {
                    // 非数值文本(如外观描述)→ 无法解析为结构化规格
                    return null;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (StringUtils.hasText(tol) && tol.startsWith("±")) {
            try {
                BigDecimal pm = new BigDecimal(tol.substring(1).trim());
                if (target != null) {
                    lower = target.subtract(pm);
                    upper = target.add(pm);
                } else if (lower != null) {
                    upper = lower.add(pm.multiply(new BigDecimal("2")));
                }
            } catch (NumberFormatException ignored) {
                // 公差不可解析时忽略,不影响已解析出的规格
            }
        }
        if (target == null && lower == null && upper == null) {
            return null;
        }
        return new SpecTriple(lower, upper, target, unit);
    }

    private record SpecTriple(BigDecimal lower, BigDecimal upper, BigDecimal target, String unit) {
    }
}
