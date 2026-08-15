package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaApproval;
import com.konli.qms.domain.fia.entity.FiaArchivedReport;
import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.domain.fia.entity.FiaSignConfig;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.entity.FiaTaskLog;
import com.konli.qms.domain.fia.mapper.FiaArchivedReportMapper;
import com.konli.qms.domain.fia.mapper.FiaInspItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.domain.fia.mapper.FiaTaskLogMapper;
import com.konli.qms.domain.fia.mapper.FiaTaskMapper;
import com.konli.qms.service.fia.dto.ProductSearchResult;
import com.konli.qms.service.fia.dto.ProductTreeNode;
import com.konli.qms.service.fia.dto.TaskStdItemVo;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcSpecStandard;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.spc.mapper.SpcSpecStandardMapper;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.fia.FiaApprovalService;
import com.konli.qms.service.fia.FiaTaskService;
import static com.konli.qms.common.enums.QmsEnums.*;
import com.konli.qms.service.fia.FiaWoLockService;
import com.konli.qms.service.ncm.NcmDefectRecordService;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.service.sqm.SqmTraceService;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.mapper.TlmToolingMapper;
import com.konli.qms.service.fia.SignConfigService;
import com.konli.qms.domain.fia.dto.PreviewJudgeRequest;
import com.konli.qms.domain.fia.dto.PreviewJudgeResult;
import com.konli.qms.domain.fia.dto.StdTraceResult;
import com.konli.qms.service.fia.dto.FiaTaskVo;
import com.konli.qms.service.spc.SpcParamService;
import com.konli.qms.service.spc.SpcSubgroupService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FiaTaskServiceImpl implements FiaTaskService {

    private final FiaTaskMapper fiaTaskMapper;
    private final FiaInspItemMapper fiaInspItemMapper;
    private final FiaInspStdMapper fiaInspStdMapper;
    private final FiaInspStdItemMapper fiaInspStdItemMapper;
    private final FiaArchivedReportMapper fiaArchivedReportMapper;
    private final FiaTaskLogMapper fiaTaskLogMapper;
    private final SignConfigService signConfigService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final SpcParamMapper spcParamMapper;
    private final SpcSpecStandardMapper spcSpecStandardMapper;
    private final SqmSupplierMapper sqmSupplierMapper;
    private final SpcParamService spcParamService;
    private final SpcSubgroupService spcSubgroupService;
    private final FiaApprovalService fiaApprovalService;
    private final FiaWoLockService fiaWoLockService;
    private final JdbcTemplate jdbcTemplate;
    private final NcmDefectRecordService ncmDefectRecordService;
    private final SqmTraceService sqmTraceService;
    private final NotificationService notificationService;
    private final TlmToolingMapper tlmToolingMapper;

    @Override
        public List<FiaTask> list(String orgId, String status, String woNo, String productName, String partNo, String procName, String triggerType) {
        LambdaQueryWrapper<FiaTask> w = new LambdaQueryWrapper<FiaTask>();
        if (orgId != null && !orgId.isEmpty() && !"all".equals(orgId)) {
            w.eq(FiaTask::getOrgId, orgId);
        }
        if (status != null && !status.trim().isEmpty()) {
            w.eq(FiaTask::getStatus, status);
        }
        if (woNo != null && !woNo.trim().isEmpty()) {
            w.like(FiaTask::getWoNo, woNo.trim());
        }
        if (productName != null && !productName.trim().isEmpty()) {
            w.like(FiaTask::getProductName, productName.trim());
        }
        if (partNo != null && !partNo.trim().isEmpty()) {
            w.like(FiaTask::getPartNo, partNo.trim());
        }
        if (procName != null && !procName.trim().isEmpty()) {
            w.like(FiaTask::getProcName, procName.trim());
        }
        if (triggerType != null && !triggerType.trim().isEmpty()) {
            w.eq(FiaTask::getTriggerType, triggerType.trim());
        }
        w.orderByDesc(FiaTask::getCreatedAt);
        return fiaTaskMapper.selectList(w);
    }

    @Override
    public PageResult<FiaTask> listPage(String orgId, String status, String woNo, String productName, String partNo, String procName, String triggerType, int page, int size) {
        LambdaQueryWrapper<FiaTask> w = new LambdaQueryWrapper<FiaTask>();
        if (orgId != null && !orgId.isEmpty() && !"all".equals(orgId)) {
            w.eq(FiaTask::getOrgId, orgId);
        }
        if (status != null && !status.trim().isEmpty()) {
            w.eq(FiaTask::getStatus, status);
        }
        if (woNo != null && !woNo.trim().isEmpty()) {
            w.like(FiaTask::getWoNo, woNo.trim());
        }
        if (productName != null && !productName.trim().isEmpty()) {
            w.like(FiaTask::getProductName, productName.trim());
        }
        if (partNo != null && !partNo.trim().isEmpty()) {
            w.like(FiaTask::getPartNo, partNo.trim());
        }
        if (procName != null && !procName.trim().isEmpty()) {
            w.like(FiaTask::getProcName, procName.trim());
        }
        if (triggerType != null && !triggerType.trim().isEmpty()) {
            w.eq(FiaTask::getTriggerType, triggerType.trim());
        }
        w.orderByDesc(FiaTask::getCreatedAt);
        IPage<FiaTask> ip = fiaTaskMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    /**
     * 产品→工序 二级树(去重汇总):从 FiaTask 当前组织的任务聚合产品(按 productName 去重)
     * 与每个产品的工序列表,供列表筛选构建树。复用 list() 以继承一致的数据权限(org switch / dataScope)。
     */
    @Override
    public List<ProductTreeNode> listProductTree(String orgId) {
        // 复用 list():其内部已按 orgId / dataScope / 组织切换 正确处理权限,避免自建查询被拦截器二次改写
        List<FiaTask> rows = this.list(orgId, null, null, null, null, null, null);
        Map<String, ProductTreeNode> byName = new LinkedHashMap<>();
        for (FiaTask t : rows) {
            String name = t.getProductName();
            if (name == null || name.isEmpty()) continue;
            ProductTreeNode node = byName.get(name);
            if (node == null) {
                node = new ProductTreeNode(name, t.getPartNo(), t.getCategory(), new ArrayList<>());
                byName.put(name, node);
            }
            if (t.getProcName() != null && !t.getProcName().isEmpty() && !node.getProcNames().contains(t.getProcName())) {
                node.getProcNames().add(t.getProcName());
            }
        }
        return new ArrayList<>(byName.values());
    }

    @Override
    public List<FiaTask> listBySource(String source) {
        return fiaTaskMapper.selectList(
                new LambdaQueryWrapper<FiaTask>().eq(FiaTask::getSource, source));
    }

    @Override
    public FiaTaskVo get(String id) {
        FiaTask task = null;
        // 优先按主键(UUID)查询；若传入的是校验单号(code,如 FA-...),PostgreSQL 的 uuid 列
        // 会把非法字符串当作 uuid 解析而抛 invalid input syntax,这里捕获后回退按 code 查询。
        try {
            task = fiaTaskMapper.selectById(id);
        } catch (Exception ignored) {
            task = null;
        }
        if (task == null) {
            task = fiaTaskMapper.selectOne(
                    new LambdaQueryWrapper<FiaTask>().eq(FiaTask::getCode, id));
        }
        FiaTaskVo vo = new FiaTaskVo();
        vo.setTask(task);
        if (task != null) {
            vo.setItems(fiaInspItemMapper.selectList(
                    new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, task.getId()).orderByAsc(FiaInspItem::getSeq)));
        }
        return vo;
    }

    /** 校验标准：优先按 UUID 主键查询；传入编码(如 STD-001)时按 code 兜底，避免向 UUID 列传入非法值导致 500 */
    private static final Pattern UUID_RE =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private static void blankToNull(java.util.function.Supplier<String> getter, java.util.function.Consumer<String> setter) {
        String v = getter.get();
        if (v != null && v.isBlank()) setter.accept(null);
    }

    private static final Pattern NUM_RE = Pattern.compile("-?\\d+(?:\\.\\d+)?");
    private static final Pattern TOL_PM_RE = Pattern.compile("±\\s*(\\d+(?:\\.\\d+)?)");
    private static final Pattern TOL_RANGE_RE = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[~\\-]\\s*(\\d+(?:\\.\\d+)?)");

    private FiaInspStd resolveInspStd(String stdId) {
        if (stdId == null || stdId.isBlank()) {
            return null;
        }
        FiaInspStd std = isUuid(stdId) ? fiaInspStdMapper.selectById(stdId) : null;
        if (std == null) {
            std = fiaInspStdMapper.selectOne(
                    new LambdaQueryWrapper<FiaInspStd>().eq(FiaInspStd::getCode, stdId));
        }
        return std;
    }

    private static boolean isUuid(String s) {
        return s != null && UUID_RE.matcher(s).matches();
    }

    @Override
    public FiaInspStd matchStd(String orgId, String partNo, String supplierId, String procName) {
        if (partNo == null || partNo.isBlank()) {
            return null;
        }
        // 工序已指定: 必须以「物料编码 + 工序」双字段精确匹配,匹配不到即返回 null,
        // 不允许退回「仅物料编码」或通用默认标准,避免工序形同虚设、错配其他工序的标准。
        if (procName != null && !procName.isBlank()) {
            return fiaInspStdMapper.selectOne(stdQuery(orgId, partNo, procName));
        }
        // 工序未指定(兼容旧调用/自动任务): 退回「仅物料编码」宽松匹配,保证不漏料。
        FiaInspStd std = fiaInspStdMapper.selectOne(stdQuery(orgId, partNo, null));
        if (std != null) {
            return std;
        }
        // 兜底: 通用默认标准(仅工序为空时生效,保证来料全量覆盖)
        return fiaInspStdMapper.selectOne(
                new LambdaQueryWrapper<FiaInspStd>()
                        .eq(FiaInspStd::getOrgId, orgId)
                        .eq(FiaInspStd::getStatus, "生效")
                        .eq(FiaInspStd::getIsDefault, true)
                        .eq(FiaInspStd::getIsDeleted, false)
                        .last("LIMIT 1"));
    }

    private LambdaQueryWrapper<FiaInspStd> stdQuery(String orgId, String partNo, String procName) {
        LambdaQueryWrapper<FiaInspStd> w = new LambdaQueryWrapper<>();
        w.eq(FiaInspStd::getOrgId, orgId)
                .eq(FiaInspStd::getStatus, "生效")
                .eq(FiaInspStd::getPartNo, partNo)
                .eq(FiaInspStd::getIsDeleted, false);
        if (procName != null && !procName.isBlank()) {
            w.eq(FiaInspStd::getProcName, procName);
        }
        w.last("LIMIT 1");
        return w;
    }

    @Override
    public List<ProductSearchResult> searchProduct(String orgId, String keyword, String category) {
        List<ProductSearchResult> results = new ArrayList<>();
        Set<String> seenPartNos = new HashSet<>();
        // orgId 可能为虚拟根(如 "ROOT",非 UUID),此时按 org 过滤会触发 uuid 类型转换 500;
        // 集团管理员视角下跳过 org 过滤,退化为跨组织检索产品料号。
        boolean orgFilter = isValidUuid(orgId);

        // 1) 从 FiaTask 历史查找已出现的产品
        LambdaQueryWrapper<FiaTask> taskW = new LambdaQueryWrapper<FiaTask>()
                .eq(orgFilter, FiaTask::getOrgId, orgId)
                .and(w -> w.like(FiaTask::getPartNo, keyword).or().like(FiaTask::getProductName, keyword));
        if (category != null && !category.isBlank()) {
            taskW.eq(FiaTask::getCategory, category);
        }
        taskW.orderByDesc(FiaTask::getCreatedAt).last("LIMIT 10");
        List<FiaTask> tasks = fiaTaskMapper.selectList(taskW);
        for (FiaTask t : tasks) {
            String pn = t.getPartNo() != null ? t.getPartNo() : t.getProductName();
            if (pn != null && !pn.isBlank() && seenPartNos.add(pn)) {
                ProductSearchResult r = new ProductSearchResult();
                r.setPartNo(t.getPartNo());
                r.setProductName(t.getProductName() != null && !t.getProductName().isBlank()
                        ? t.getProductName() : t.getPartNo());
                r.setExists(true);
                r.setCategory(t.getCategory());  // 物料/半成品/成品
                // 匹配历史供应商
                if (t.getSupplierId() != null) {
                    SqmSupplier sup = sqmSupplierMapper.selectById(t.getSupplierId());
                    r.setMatchedSupplierId(t.getSupplierId());
                    r.setMatchedSupplierName(sup != null ? sup.getName() : null);
                }
                results.add(r);
            }
        }

        // 2) 从 FiaInspStd 查找标准库中的产品，产品名/品类以来料追溯节点(成品/半成品/物料)为权威来源实时关联
        LambdaQueryWrapper<FiaInspStd> stdW = new LambdaQueryWrapper<FiaInspStd>()
                .eq(orgFilter, FiaInspStd::getOrgId, orgId)
                .eq(FiaInspStd::getIsDeleted, false)
                .and(w -> w.like(FiaInspStd::getPartNo, keyword).or().like(FiaInspStd::getMaterial, keyword));
        stdW.orderByDesc(FiaInspStd::getCreatedAt).last("LIMIT 10");
        List<FiaInspStd> stds = fiaInspStdMapper.selectList(stdW);
        for (FiaInspStd s : stds) {
            String pn = s.getPartNo() != null ? s.getPartNo() : s.getMaterial();
            if (pn != null && !pn.isBlank() && seenPartNos.add(pn)) {
                ProductSearchResult r = new ProductSearchResult();
                r.setPartNo(pn);
                String stdMat = s.getMaterial();
                // 以来料追溯三源表(material_inspection/finished_goods_inspection/critical_material_binding)为权威来源补全产品名与品类
                ProductInfo info = resolveFromSourceTables(orgId, stdMat != null ? stdMat : pn);
                if (info != null) {
                    r.setProductName(info.name());
                    r.setCategory(info.category());
                } else {
                    // 三源表均无匹配时，产品名兜底取标准库 material 字段(而非料号)，避免返回空名
                    r.setProductName(stdMat != null && !stdMat.isBlank() ? stdMat : pn);
                    r.setCategory(s.getCategory());
                }
                r.setExists(true);
                results.add(r);
            }
        }

        // 3) 补充供应商信息：优先以来料追溯 material_inspection 关联 sqm_supplier(ven_code=supplier_code,
        //    这是最权威的真实来料供应商)，其次回退到历史首件任务反查。
        for (ProductSearchResult r : results) {
            boolean needSupplier = r.isExists() && (r.getMatchedSupplierId() == null || r.getMatchedSupplierId().isBlank());
            boolean needCategory = r.isExists() && (r.getCategory() == null || r.getCategory().isBlank());
            if (needSupplier || needCategory) {
                // 3a) 优先从 material_inspection 关联供应商主数据
                if (needSupplier) {
                    SqmSupplier sup = resolveSupplierByMaterialCode(r.getPartNo());
                    if (sup != null) {
                        r.setMatchedSupplierId(sup.getId());
                        r.setMatchedSupplierName(sup.getName());
                    }
                }
                // 3b) 仍缺失则回退到历史首件任务反查(确保老数据兼容)
                if ((needSupplier && (r.getMatchedSupplierId() == null || r.getMatchedSupplierId().isBlank()))
                        || needCategory) {
                    LambdaQueryWrapper<FiaTask> histW = new LambdaQueryWrapper<FiaTask>()
                            .eq(orgFilter, FiaTask::getOrgId, orgId)
                            .eq(FiaTask::getPartNo, r.getPartNo())
                            .orderByDesc(FiaTask::getCreatedAt)
                            .last("LIMIT 1");
                    FiaTask hist = fiaTaskMapper.selectList(histW).stream().findFirst().orElse(null);
                    if (hist != null) {
                        if (needCategory && hist.getCategory() != null) {
                            r.setCategory(hist.getCategory());
                        }
                        if (needSupplier && (r.getMatchedSupplierId() == null || r.getMatchedSupplierId().isBlank())
                                && hist.getSupplierId() != null) {
                            SqmSupplier sup = sqmSupplierMapper.selectById(hist.getSupplierId());
                            r.setMatchedSupplierId(hist.getSupplierId());
                            r.setMatchedSupplierName(sup != null ? sup.getName() : null);
                        }
                    }
                }
            }
        }

        return results;
    }

    /**
     * 按 material_code 从 material_inspection 关联 sqm_supplier 解析真实来料供应商。
     * material_inspection.supplier_code = sqm_supplier.ven_code (MES 供应商编号, 全局唯一)。
     */
    private SqmSupplier resolveSupplierByMaterialCode(String materialCode) {
        if (materialCode == null || materialCode.isBlank()) return null;
        String sql = "SELECT s.id, s.org_id, s.name FROM qms.material_inspection mi "
                + "JOIN ops.sqm_supplier s ON s.ven_code = mi.supplier_code "
                + "WHERE mi.is_deleted='0' AND mi.material_code = ? LIMIT 1";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, materialCode);
        if (rows.isEmpty()) return null;
        Map<String, Object> row = rows.get(0);
        SqmSupplier sup = new SqmSupplier();
        sup.setId(str(row.get("id")));
        sup.setName(str(row.get("name")));
        return sup;
    }

    /** 产品名与品类解析结果（来自来料追溯三源表） */
    private record ProductInfo(String name, String category) {}

    /**
     * 以来料追溯三源表为权威来源解析产品名与品类（方案B：节点权威=三源表，sqm_trace_node 已清空）。
     * 按 material_code 同时命中 material_inspection / finished_goods_inspection / critical_material_binding，
     * 取真实 material_name / product_name 作为产品名，按源表类型映射品类：
     *   material_inspection -> material
     *   finished_goods_inspection category='成品' -> product / '半成品' -> semi
     *   critical_material_binding -> material
     * 优先级沿用 product(成品) > semi > material，保证同一料号多表命中时取最具体品类。
     */
    private ProductInfo resolveFromSourceTables(String orgId, String materialCode) {
        if (materialCode == null || materialCode.isBlank()) {
            return null;
        }
        // 三源表为 MES 同步的全局追溯数据,按 material_code 唯一关联,不按 org_id 过滤(与 SqmTraceServiceImpl 源表查询一致)

        // 1) 来料 material_inspection：material_code + material_name
        String sqlIncoming = "SELECT mi.material_name AS name FROM qms.material_inspection mi"
                + " WHERE mi.is_deleted='0' AND mi.material_code = ?";
        // 2) 成品/半成品 finished_goods_inspection：material_code + product_name + category
        String sqlFinished = "SELECT fi.product_name AS name, fi.category AS cat FROM qms.finished_goods_inspection fi"
                + " WHERE fi.is_deleted='0' AND fi.category IN ('成品','半成品') AND fi.material_code = ?";
        // 3) 关键件 critical_material_binding：material_code（取关联产品名，无独立名称列时回退料号）
        String sqlCritical = "SELECT cb.product_barcode AS name FROM qms.critical_material_binding cb"
                + " WHERE cb.is_deleted='0' AND cb.material_code = ?";

        Object[] argsIn = new Object[]{materialCode};
        Object[] argsFin = new Object[]{materialCode};
        Object[] argsCrit = new Object[]{materialCode};

        // 成品（最优先）
        List<Map<String, Object>> finished = jdbcTemplate.queryForList(sqlFinished, argsFin);
        if (finished != null && !finished.isEmpty()) {
            Map<String, Object> row = finished.get(0);
            String cat = "成品".equals(String.valueOf(row.get("cat"))) ? "product" : "semi";
            String name = str(row.get("name"));
            if (name != null && !name.isBlank()) {
                return new ProductInfo(name, cat);
            }
        }
        // 半成品已并入上面 finished 查询，此处只需处理来料与关键件（优先级 semi > material 已由 finished 先行覆盖）

        // 来料
        List<Map<String, Object>> incoming = jdbcTemplate.queryForList(sqlIncoming, argsIn);
        if (incoming != null && !incoming.isEmpty()) {
            String name = str(incoming.get(0).get("name"));
            if (name != null && !name.isBlank()) {
                return new ProductInfo(name, "material");
            }
        }

        // 关键件（归物料类首件）
        List<Map<String, Object>> critical = jdbcTemplate.queryForList(sqlCritical, argsCrit);
        if (critical != null && !critical.isEmpty()) {
            String name = str(critical.get(0).get("name"));
            if (name != null && !name.isBlank()) {
                return new ProductInfo(name, "material");
            }
        }
        return null;
    }

    @Override
    public String generateWoNo(String orgId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "WO-" + today + "-";
        // 同天内最大序号查询(行锁防并发)
        String sql = "SELECT wo_no FROM ops.fia_task WHERE wo_no LIKE ? AND org_id = ? ORDER BY wo_no DESC LIMIT 1 FOR UPDATE";
        List<String> rows;
        try {
            rows = jdbcTemplate.queryForList(sql, String.class, prefix + "%", orgId);
        } catch (Exception e) {
            // 若 FOR UPDATE 在只读事务失败，退化为无锁查询
            rows = jdbcTemplate.queryForList(
                    "SELECT wo_no FROM ops.fia_task WHERE wo_no LIKE ? AND org_id = ? ORDER BY wo_no DESC LIMIT 1",
                    String.class, prefix + "%", orgId);
        }
        int seq = 1;
        if (!rows.isEmpty()) {
            String lastWo = rows.get(0);
            String numPart = lastWo.substring(prefix.length());
            try {
                seq = Integer.parseInt(numPart) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return prefix + String.format("%03d", seq);
    }

    @Override
    public List<TaskStdItemVo> getTaskStdItems(String taskId) {
        String realId = resolveTaskId(taskId);
        if (realId == null) {
            return Collections.emptyList();
        }
        List<FiaInspItem> items = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, realId).orderByAsc(FiaInspItem::getSeq));
        List<TaskStdItemVo> vos = new ArrayList<>();
        for (FiaInspItem it : items) {
            TaskStdItemVo vo = new TaskStdItemVo();
            vo.setItemId(it.getId());
            vo.setStdItemId(it.getStdItemId());
            vo.setItemName(it.getItemName());
            vo.setStdValue(it.getStdValue());
            vo.setTolerance(it.getTolerance());
            vo.setUnit(it.getUnit());
            vo.setIsCtq(it.getIsCtq());
            // 查找关联的 SPC 参数
            if (it.getStdItemId() != null) {
                List<SpcParam> fkParams = spcParamMapper.selectList(
                        new LambdaQueryWrapper<SpcParam>()
                                .eq(SpcParam::getFiaStdItemId, it.getStdItemId())
                                .eq(SpcParam::getIsActive, true)
                                .last("LIMIT 1"));
                if (!fkParams.isEmpty()) {
                    vo.setSpcParamId(fkParams.get(0).getId());
                    vo.setSpcParamName(fkParams.get(0).getParamName());
                }
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    @Transactional
    public FiaTask create(FiaTask task) {
        // 默认 source=FACTORY (产线首件), 若未显式设置
        if (task.getSource() == null || task.getSource().isBlank()) {
            task.setSource("FACTORY");
        }
        // 前端可能传空字符串,修复为空字符串的 UUID 字段为 null
        blankToNull(task::getSupplierId, task::setSupplierId);
        blankToNull(task::getLotId, task::setLotId);
        blankToNull(task::getInspectorId, task::setInspectorId);

        // -- 供应商规则:按 category 处理 --
        String cat = task.getCategory();
        if ("semi".equals(cat) || "product".equals(cat)) {
            // 半成品/成品:统一绑定"工厂自产"(真实供应商主数据,非字符串字面量),前端不可编辑
            com.konli.qms.domain.sqm.entity.SqmSupplier factorySelf = sqmSupplierMapper.selectOne(
                    new LambdaQueryWrapper<com.konli.qms.domain.sqm.entity.SqmSupplier>()
                            .eq(com.konli.qms.domain.sqm.entity.SqmSupplier::getName, "工厂自产")
                            .last("limit 1"));
            if (factorySelf == null || factorySelf.getId() == null) {
                throw new BusinessException(400, "系统未维护'工厂自产'供应商,请先创建名称为'工厂自产'的供应商");
            }
            task.setSupplierId(factorySelf.getId());
        } else if ("material".equals(cat)) {
            // 物料类:新产品前端必选供应商(从供应商管理模块选);旧产品自动匹配
            if (task.getSupplierId() == null || task.getSupplierId().isBlank()) {
                // 尝试自动匹配:查历史任务中相同料号的供应商
                if (task.getPartNo() != null && !task.getPartNo().isBlank()) {
                    FiaTask hist = fiaTaskMapper.selectOne(
                            new LambdaQueryWrapper<FiaTask>()
                                    .eq(FiaTask::getPartNo, task.getPartNo())
                                    .eq(FiaTask::getOrgId, task.getOrgId())
                                    .isNotNull(FiaTask::getSupplierId)
                                    .orderByDesc(FiaTask::getCreatedAt)
                                    .last("LIMIT 1"));
                    if (hist != null && hist.getSupplierId() != null) {
                        task.setSupplierId(hist.getSupplierId());
                    }
                }
            }
            // 物料类必须要有供应商(新产品前端必选,后端兜底校验)
            if (task.getSupplierId() == null || task.getSupplierId().isBlank()) {
                throw new BusinessException(400, "物料类首件检验必须选择供应商");
            }
        }

        // 产线名称:前端首件创建页未收集,为空时兜底为默认产线,避免 line_name NOT NULL 约束报错
        if (task.getLineName() == null || task.getLineName().isBlank()) {
            task.setLineName("FIA-Demo-Line");
        }

        FiaInspStd std = resolveInspStd(task.getStdId());
        // 自动匹配:stdId 为空时按(物料+工序)从标准库匹配
        if (std == null && task.getProductName() != null && task.getProcName() != null) {
            std = fiaInspStdMapper.selectOne(
                    new LambdaQueryWrapper<FiaInspStd>()
                            .eq(FiaInspStd::getMaterial, task.getProductName())
                            .eq(FiaInspStd::getProcName, task.getProcName())
                            .eq(FiaInspStd::getStatus, "生效"));
            if (std != null) task.setStdId(std.getId());
        }
        // 来料批次驱动:按 物料编码 + 供应商 + 工序 自动匹配标准库
        if (std == null && task.getPartNo() != null && !task.getPartNo().isBlank()) {
            std = matchStd(task.getOrgId(), task.getPartNo(), task.getSupplierId(), task.getProcName());
            if (std != null) task.setStdId(std.getId());
        }
        if (std == null) {
            throw new BusinessException(400, "检验标准不存在(物料=" + task.getProductName()
                    + ",工序=" + task.getProcName() + ",物料编码=" + task.getPartNo() + ")");
        }
        task.setCode("FA-" + System.currentTimeMillis());
        task.setStdVersion(std.getStdVersion());
        // 工单号:前端不收集,create 时自动生成(WO-日期-序号),避免 wo_no NOT NULL 约束
        if (task.getWoNo() == null || task.getWoNo().isBlank()) {
            task.setWoNo(generateWoNo(task.getOrgId()));
        }
        if (task.getAql() == null) task.setAql(std.getAql());
        if (task.getStatus() == null) {
            task.setStatus(FiaTaskStatus.PENDING);
        }
        task.setIsOverdue(false);
        task.setSlaDueAt(LocalDateTime.now().plusHours(2));
        fiaTaskMapper.insert(task);

        List<FiaInspStdItem> allStdItems = fiaInspStdItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspStdItem>().eq(FiaInspStdItem::getStdId, task.getStdId()).orderByAsc(FiaInspStdItem::getSeq));
        // 按前端选中的标准项过滤(空=全量,向后兼容);同时以 stdId 归属二次校验,防止越权引用其它标准项
        List<FiaInspStdItem> stdItems;
        List<String> selIds = task.getStdItemIds();
        if (selIds != null && !selIds.isEmpty()) {
            Set<String> selSet = new HashSet<>(selIds);
            stdItems = allStdItems.stream()
                    .filter(si -> selSet.contains(si.getId()) && si.getStdId().equals(task.getStdId()))
                    .sorted(Comparator.comparing(FiaInspStdItem::getSeq))
                    .collect(Collectors.toList());
            // 容错:若所选 ID 与标准项主键一个都匹配不上(如 SPC 参数未回填 fia_std_item_id 关联),
            // 回退为全量标准项,避免创建出"零检验项"的任务。
            if (stdItems.isEmpty()) {
                log.info("[FIA] stdItemIds 与标准项无匹配({}, std_id={}),回退全量标准项", selIds.size(), task.getStdId());
                stdItems = allStdItems;
            }
        } else {
            stdItems = allStdItems;
        }
        for (FiaInspStdItem si : stdItems) {
            FiaInspItem it = new FiaInspItem();
            it.setOrgId(task.getOrgId());
            it.setTaskId(task.getId());
            it.setSeq(si.getSeq());
            it.setItemName(si.getItemName());
            it.setIsCtq(si.getIsCtq());
            it.setStdValue(si.getStdValue());
            it.setTolerance(si.getTolerance());
            it.setUnit(si.getUnit());
            it.setStdItemId(si.getId());
            it.setJudge("-");
            fiaInspItemMapper.insert(it);
        }
        log(task, 1, "创建任务", "系统");
        log(task, 2, "标准调取(" + std.getCode() + ")", "系统");
        // SR-FIA-022/026:首件任务创建即锁定工单(首件未完成),在制品待处理、禁止流转
        try {
            fiaWoLockService.lockOnCreate(task.getOrgId(), task.getWoNo(), task.getCode());
        } catch (Exception e) {
            log.warn("[FIA] 工单锁定失败 woNo={}: {}", task.getWoNo(), e.getMessage());
        }
        // SR-FIA-006 前置:推送待检通知给检验员/班组长(写 notification_log)
        try {
            notifyPending(task);
        } catch (Exception e) {
            log.warn("[FIA] 待检通知写入失败 taskId={}: {}", task.getId(), e.getMessage());
        }
        return task;
    }

    /**
     * SPC 量产监控严重异常回环:停线整改后重新开工,自动创建一条首件检验任务。
     * 触发类型固定为"停线重启",复用 create 全主流程(工单锁定+标准匹配+待检通知)。
     * 异常由调用方 try-catch,不阻断 SPC 主流程。
     */
    @Override
    @Transactional
    public FiaTask createFromSetup(String orgId, String woNo, String partNo, String procName,
                                   String productName, String lineName, String remark) {
        FiaTask task = new FiaTask();
        task.setOrgId(orgId);
        task.setWoNo(woNo);
        task.setPartNo(partNo);
        task.setProcName(procName);
        task.setProductName(productName != null && !productName.isBlank() ? productName : partNo);
        task.setLineName(lineName != null && !lineName.isBlank() ? lineName : "FIA-Demo-Line");
        // 触发类型:停线重启(对应首件触发类型枚举)
        task.setTriggerType("停线重启");
        task.setSource("FACTORY");
        task.setRemark(remark != null && !remark.isBlank() ? remark : "SPC 量产监控报警级异常触发停线整改后自动重开首件");
        // 反查历史任务的供应商/标准,保证标准匹配链路可用
        if (partNo != null && !partNo.isBlank()) {
            FiaTask hist = fiaTaskMapper.selectOne(
                    new LambdaQueryWrapper<FiaTask>()
                            .eq(FiaTask::getPartNo, partNo)
                            .eq(FiaTask::getOrgId, orgId)
                            .orderByDesc(FiaTask::getCreatedAt)
                            .last("LIMIT 1"));
            if (hist != null) {
                if (hist.getSupplierId() != null) task.setSupplierId(hist.getSupplierId());
                if (hist.getCategory() != null) task.setCategory(hist.getCategory());
            }
        }
        return create(task);
    }

    /**
     * 工装触发首件检验任务(source=TOOLING)。
     * 通过 tooling.productCode(即 partNo) + procName 调用 matchStd 定位 FIA 标准，
     * woNo 为空时自动生成，复用 create 全流程(工单锁定+标准匹配+待检通知+SPC联动)。
     * batchNo 为空时兜底生成(自动触发场景无人工批次)，前端人工创建时强制必填。
     */
    @Override
    @Transactional
    public FiaTask createFromTooling(String orgId, String toolId, String woNo, String partNo,
                                     String procName, String productName, String lineName,
                                     String triggerType, String batchNo, String supplierId, String remark) {
        // 前置校验: 必须有产品编码和工序名称才能匹配标准
        if (partNo == null || partNo.isBlank()) {
            throw new BusinessException(400, "工装缺少产品编码(product_code)，无法匹配 FIA 检验标准");
        }
        if (procName == null || procName.isBlank()) {
            throw new BusinessException(400, "工装缺少工序名称(proc_name)，无法匹配 FIA 检验标准");
        }
        // 匹配标准: 以 productCode 作 partNo，procName 作工序名
        FiaInspStd std = matchStd(orgId, partNo, null, procName);
        if (std == null) {
            throw new BusinessException(400,
                    String.format("未找到检验标准(产品编码=%s, 工序=%s)，请先在 FIA 标准库中创建对应标准", partNo, procName));
        }

        // 批次号: 人工创建必填(前端校验)，自动触发场景兜底生成，确保与生产批次绑定可追溯
        String finalBatchNo = (batchNo != null && !batchNo.isBlank()) ? batchNo
                : String.format("TOOLING-%s-%d", toolId == null ? "NA" : toolId.substring(0, Math.min(8, toolId.length())), System.currentTimeMillis());

        FiaTask task = new FiaTask();
        task.setOrgId(orgId);
        task.setToolId(toolId);
        task.setWoNo(woNo);
        task.setPartNo(partNo);
        task.setProcName(procName);
        task.setProductName(productName != null && !productName.isBlank() ? productName : partNo);
        task.setLineName(lineName != null && !lineName.isBlank() ? lineName : "FIA-Demo-Line");
        task.setTriggerType(triggerType != null && !triggerType.isBlank() ? triggerType : "工装维修后");
        task.setSource("TOOLING");
        task.setStdId(std.getId());
        task.setBatchNo(finalBatchNo);
        if (supplierId != null && !supplierId.isBlank()) task.setSupplierId(supplierId);
        task.setRemark(remark != null && !remark.isBlank() ? remark
                : String.format("工装首件检验(触发类型:%s)", task.getTriggerType()));

        return create(task);
    }

    /**
     * 该工装是否存在「待处理」的工装首件任务(source=TOOLING 且未完成)。
     * 用于工装台账「待首件」强提醒判定。
     */
    @Override
    public boolean hasPendingToolingFirst(String toolId) {
        if (toolId == null || toolId.isBlank()) return false;
        long cnt = fiaTaskMapper.selectCount(new LambdaQueryWrapper<FiaTask>()
                .eq(FiaTask::getToolId, toolId)
                .eq(FiaTask::getSource, "TOOLING")
                .notIn(FiaTask::getStatus, "已完成", "已作废"));
        return cnt > 0;
    }

    /** 推送待检通知给检验员/班组长(使用 NotificationService 标准接口)。 */
    private void notifyPending(FiaTask task) {
        String content = String.format("首件检验待检:校验单 %s,工单 %s,产线 %s,工序 %s,SLA %s",
                task.getCode(), task.getWoNo(), task.getLineName(), task.getProcName(),
                task.getSlaDueAt() != null ? task.getSlaDueAt().toString() : "-");
        notificationService.notify("fia", "fia_task_created",
                "首件检验待检提醒", content, "fia_task", task.getCode(), "/fia/tasks");
    }

    /** 将前端传入的标识(可能是主键 UUID,也可能是校验单号 code)解析为真实主键;解析失败返回 null */
    private String resolveTaskId(String input) {
        if (input == null) return null;
        FiaTask t;
        // 主键是 PG uuid 类型,传入 code(如 FA-...)会被当作 uuid 解析而抛异常,这里捕获后回退按 code 查。
        try {
            t = fiaTaskMapper.selectById(input);
        } catch (Exception ignored) {
            t = null;
        }
        if (t == null) {
            t = fiaTaskMapper.selectOne(
                    new LambdaQueryWrapper<FiaTask>().eq(FiaTask::getCode, input));
        }
        return t != null ? t.getId() : null;
    }

    @Override
    @Transactional
    public void enterResults(String taskId, List<FiaInspItem> items) {
        String realId = resolveTaskId(taskId);
        if (realId == null) {
            throw new BusinessException(400, "任务不存在");
        }
        // 批量取标准项规则,避免 N+1
        Set<String> stdIds = items.stream().map(FiaInspItem::getStdItemId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, FiaInspStdItem> stdItemMap = stdIds.isEmpty() ? Collections.emptyMap() :
                fiaInspStdItemMapper.selectBatchIds(stdIds).stream()
                        .collect(Collectors.toMap(FiaInspStdItem::getId, s -> s, (a, b) -> a));
        for (FiaInspItem it : items) {
            // 系统按标准规则自动判定:可匹配则覆盖,不可匹配则保留前端人工 judge
            String sysJudge = null;
            if (it.getStdItemId() != null) {
                sysJudge = computeItemJudge(stdItemMap.get(it.getStdItemId()), it.getMeasuredValue());
            }
            FiaInspItem upd = new FiaInspItem();
            upd.setId(it.getId());
            upd.setMeasuredValue(it.getMeasuredValue());
            upd.setJudge(sysJudge != null ? sysJudge
                    : (it.getJudge() == null || it.getJudge().isBlank() ? "-" : it.getJudge()));
            fiaInspItemMapper.updateById(upd);
        }
        // 录入后刷新检验项统计与合格率
        computeStats(realId);
        // 仅在首次录入(待检)时置为FiaTaskStatus.IN_PROGRESS;已处于签名流转中(待复核/待批准)的任务补充录入时
        // 保持当前状态,避免把状态错误地重置回FiaTaskStatus.IN_PROGRESS而打断分级签名。
        FiaTask task = fiaTaskMapper.selectById(realId);
        if (task != null && (task.getStatus() == null || FiaTaskStatus.PENDING.equals(task.getStatus()))) {
            task.setStatus(FiaTaskStatus.IN_PROGRESS);
            fiaTaskMapper.updateById(task);
        }
        log(fiaTaskMapper.selectById(realId), 3, "检验录入", currentOperator());
    }

    @Override
    @Transactional
    public void signInspector(String taskId, String password, String itemId) {
        String realId = resolveTaskId(taskId);
        FiaTask task = realId == null ? null : fiaTaskMapper.selectById(realId);
        if (task == null) {
            throw new BusinessException(400, "任务不存在");
        }
        verifyPassword(task.getOrgId(), password);
        // 逐项签名(sign_granularity=逐项签名):itemId 非空时仅记日志,不改 task 状态;
        // 所有项签完后由调用方再发起一次整单签名(itemId 为空)触发状态流转。
        if (itemId != null && !itemId.isEmpty()) {
            log(task, 4, "检验人签名-项" + itemId, currentOperator());
            return;
        }
        // 整单签名:原逻辑(设 inspectorId + 状态 -> 待复核)
        if (!FiaTaskStatus.IN_PROGRESS.equals(task.getStatus())) {
            throw new BusinessException(400, "任务状态不允许检验签名(需为进行中)");
        }
        task.setInspectorId(currentOperator());
        task.setStatus(FiaTaskStatus.WAIT_REVIEW);
        fiaTaskMapper.updateById(task);
        log(task, 4, "检验人签名", currentOperator());
    }

    @Override
    @Transactional
    public void signReviewer(String taskId, String password, String itemId) {
        String realId = resolveTaskId(taskId);
        FiaTask task = realId == null ? null : fiaTaskMapper.selectById(realId);
        if (task == null) {
            throw new BusinessException(400, "任务不存在");
        }
        verifyPassword(task.getOrgId(), password);
        // 逐项签名:itemId 非空时仅记日志,不改 task 状态
        if (itemId != null && !itemId.isEmpty()) {
            log(task, 5, "复核人签名-项" + itemId, currentOperator());
            return;
        }
        // 整单签名:原逻辑
        if (!FiaTaskStatus.WAIT_REVIEW.equals(task.getStatus())) {
            throw new BusinessException(400, "任务状态不允许复核签名(需为待复核)");
        }
        task.setReviewerId(currentOperator());
        task.setReviewedAt(LocalDateTime.now());
        FiaSignConfig config = signConfigService.get(task.getOrgId());
        if (config != null && "三级".equals(config.getSignNodes())) {
            task.setStatus("待批准");
            fiaTaskMapper.updateById(task);
            log(task, 5, "复核人签名(待批准)", currentOperator());
        } else if (needsApproval(task)) {
            // 需审批路径:签名成功后创建审批单,任务置FiaTaskStatus.IN_APPROVAL挂起
            createApprovalForTask(task);
            task.setStatus(FiaTaskStatus.IN_APPROVAL);
            fiaTaskMapper.updateById(task);
            log(task, 5, "复核人签名-待审批", currentOperator());
        } else {
            completeAndArchive(task, 5, "复核人签名");
        }
    }

    @Override
    @Transactional
    public void signApprover(String taskId, String password) {
        String realId = resolveTaskId(taskId);
        FiaTask task = realId == null ? null : fiaTaskMapper.selectById(realId);
        if (task == null || !"待批准".equals(task.getStatus())) {
            throw new BusinessException(400, "任务不存在或任务状态不允许批准签名(需为待批准)");
        }
        verifyPassword(task.getOrgId(), password);
        task.setApproverId(currentOperator());
        task.setApprovedAt(LocalDateTime.now());
        if (needsApproval(task)) {
            // 需审批路径:签名成功后创建审批单,任务置FiaTaskStatus.IN_APPROVAL挂起
            createApprovalForTask(task);
            task.setStatus(FiaTaskStatus.IN_APPROVAL);
            fiaTaskMapper.updateById(task);
            log(task, 6, "批准人签名-待审批", currentOperator());
        } else {
            completeAndArchive(task, 6, "批准人签名");
        }
    }

    /**
     * 处置/放行路径:退货/返工/让步接收/紧急放行/豁免开工。
     * 仅记录处置路径到任务,审批单在签名成功后由 signReviewer/signApprover 创建,
     * 确保签名失败不会留下孤儿审批单。
     */
    @Override
    @Transactional
    public void setDisposition(String taskId, String disposition, String remark) {
        String realId = resolveTaskId(taskId);
        if (realId == null) {
            throw new BusinessException(400, "首件任务不存在");
        }
        FiaTask task = fiaTaskMapper.selectById(realId);
        // 按 source 校验 disposition 枚举
        Set<String> allowed;
        if ("SUPPLIER".equals(task.getSource())) {
            allowed = Set.of(SupplierDisposition.ACCEPT, FactoryDisposition.RETURN, FactoryDisposition.CONCESSION, SupplierDisposition.SORT);
        } else {
            allowed = Set.of(FactoryDisposition.RETURN, FactoryDisposition.REWORK, FactoryDisposition.CONCESSION, FactoryDisposition.EMERGENCY, FactoryDisposition.EXEMPTION);
        }
        if (disposition == null || !allowed.contains(disposition)) {
            throw new BusinessException(400, "处置路径 [" + disposition + "] 不适用于 source=" + task.getSource() + "，允许: " + String.join("/", allowed));
        }
        task.setDisposition(disposition);
        task.setRemark(remark);
        fiaTaskMapper.updateById(task);

        int seq = fiaTaskLogMapper.selectCount(
                new LambdaQueryWrapper<FiaTaskLog>().eq(FiaTaskLog::getTaskId, realId)
        ).intValue() + 1;
        log(task, seq, "处置-" + disposition, currentOperator());
    }

    /** 是否需质量主管审批的处置/放行路径 */
    private boolean needsApproval(FiaTask task) {
        return task.getDisposition() != null
                && APPROVAL_DISPOSITIONS.contains(task.getDisposition());
    }

    /** 签名成功后创建审批单(先清去重,再建新单),确保无孤儿审批记录 */
    private void createApprovalForTask(FiaTask task) {
        fiaApprovalService.removePendingByTask(task.getId());
        FiaApproval approval = new FiaApproval();
        approval.setOrgId(task.getOrgId());
        approval.setApprovalType(task.getDisposition());
        approval.setWoNo(task.getWoNo());
        approval.setTaskId(task.getId());
        String remark = task.getRemark();
        approval.setReason(remark == null || remark.isBlank() ? (task.getDisposition() + "申请") : remark);
        approval.setApplicantId(currentOperator());
        approval.setStatus("待审批");
        approval.setApplyAt(LocalDateTime.now());
        fiaApprovalService.create(approval);
    }

    private static final Set<String> APPROVAL_DISPOSITIONS = Set.of(FactoryDisposition.CONCESSION, FactoryDisposition.EMERGENCY, FactoryDisposition.EXEMPTION);

    /**
     * 审批通过后的放行:归档 + 首件CTQ数据写入SPC基准(仅判定合格时同步)。
     * 仅当任务处于FiaTaskStatus.IN_APPROVAL才执行,幂等安全。
     */
    @Override
    @Transactional
    public void releaseAfterApproval(String taskId) {
        FiaTask task = fiaTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(400, "首件任务不存在");
        }
        if (!FiaTaskStatus.IN_APPROVAL.equals(task.getStatus())) {
            return; // 幂等:非审批中(已放行/已驳回)直接返回,避免重复归档
        }
        task.setStatus(FiaTaskStatus.COMPLETED);
        task.setSubmittedAt(LocalDateTime.now());
        task.setOverallJudge(computeJudge(task.getId()));
        computeStats(task.getId());
        fiaTaskMapper.updateById(task);
        archive(task);
        log(task, 7, "审批通过-放行归档", "系统");
        log(task, 8, "归档报告", "系统");
        // 审批结果通知
        try {
            String judgeText = task.getOverallJudge() != null ? task.getOverallJudge() : "已判定";
            notificationService.notify("fia", "fia_task_released",
                    "首件检验审批通过-已放行",
                    String.format("校验单 %s(工单 %s) 审批通过,判定:%s,已自动放行归档。",
                            task.getCode(), task.getWoNo(), judgeText),
                    "fia_task", task.getCode(), "/fia/tasks");
        } catch (Exception e) {
            log.warn("[FIA] 审批放行通知失败: {}", e.getMessage());
        }
        // 审批放行:FIA->SPC 参数生成(一):任务完成即派生 SPC 参数并绑定产品(幂等),保证可进采集
        try {
            spcParamService.ensureFromFiaTask(task.getId());
        } catch (Exception e) {
            log.warn("FIA->SPC 参数生成失败(审批放行), taskId={}: {}", task.getId(), e.getMessage(), e);
        }
        // 审批放行:仅合格时将CTQ数据写入SPC基准
        if (InspResult.PASS.equals(task.getOverallJudge())) {
            try {
                syncToSpc(task);
            } catch (Exception e) {
                log.warn("FIA->SPC 联动失败(审批放行), taskId={}: {}", task.getId(), e.getMessage(), e);
            }
            // 审批放行亦触发 FIA->来料追溯 联动(合格免审直录物料表)
            try {
                syncToTrace(task, 10);
            } catch (Exception e) {
                log.warn("FIA->来料追溯 联动失败(审批放行), taskId={}: {}", task.getId(), e.getMessage(), e);
            }
        }
        // SR-FIA-025:放行审批通过 -> 工单解锁(紧急放行/让步接收/豁免 留痕 + 追溯标签)
        try {
            FiaApproval ap = fiaApprovalService.list(null, null, null).stream()
                    .filter(a -> task.getId().equals(a.getTaskId()) && "已通过".equals(a.getStatus()))
                    .findFirst().orElse(null);
            String approverId = ap != null ? ap.getApproverId() : null;
            String reason = ap != null ? ap.getApproveOpinion() : null;
            String traceTag = "REL-" + task.getCode() + "-" + (System.currentTimeMillis() % 100000);
            fiaWoLockService.unlockByApproval(task.getOrgId(), task.getWoNo(), approverId, reason, traceTag, task.getCode());
            log(task, 9, "工单放行解锁-" + task.getDisposition(), "系统");
        } catch (Exception e) {
            log.warn("[FIA] 放行解锁联动失败 taskId={}: {}", task.getId(), e.getMessage());
        }
    }

    /**
     * 审批驳回:任务置FiaTaskStatus.REJECTED,不放行不归档。仅FiaTaskStatus.IN_APPROVAL可驳回,幂等安全。
     */
    @Override
    @Transactional
    public void rejectTask(String taskId) {
        FiaTask task = fiaTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(400, "首件任务不存在");
        }
        if (!FiaTaskStatus.IN_APPROVAL.equals(task.getStatus())) {
            return;
        }
        task.setStatus(FiaTaskStatus.REJECTED);
        fiaTaskMapper.updateById(task);
        log(task, 7, "审批驳回-不放行", "系统");
    }

    @Override
    public FiaArchivedReport getArchive(String taskId) {
        String realId = resolveTaskId(taskId);
        if (realId == null) return null;
        return fiaArchivedReportMapper.selectOne(
                new LambdaQueryWrapper<FiaArchivedReport>().eq(FiaArchivedReport::getTaskId, realId));
    }

    @Override
    public List<Map<String, Object>> listArchives() {
        CompanyContext.CurrentUser _u = CompanyContext.get();
        boolean isAdmin = CompanyContext.isAdmin();
        String orgId = (_u != null) ? _u.orgId() : null;
        // 注意:root 的 JWT orgId="ROOT"(非 uuid),不能直接用于 uuid 列过滤,故管理员跳过该条件。
        LambdaQueryWrapper<FiaArchivedReport> qw = new LambdaQueryWrapper<FiaArchivedReport>()
                .orderByDesc(FiaArchivedReport::getArchiveDate);
        if (!isAdmin && orgId != null && !orgId.isBlank() && !"ROOT".equals(orgId)) {
            qw.eq(FiaArchivedReport::getOrgId, orgId);
        }
        List<FiaArchivedReport> reports = fiaArchivedReportMapper.selectList(qw);
        if (reports.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> taskIds = reports.stream().map(FiaArchivedReport::getTaskId).collect(Collectors.toSet());
        List<FiaTask> tasks = fiaTaskMapper.selectBatchIds(taskIds);
        Map<String, FiaTask> taskMap = tasks.stream()
                .collect(Collectors.toMap(FiaTask::getId, t -> t, (a, b) -> a));

        Set<String> userIds = new HashSet<>();
        Set<String> stdIds = new HashSet<>();
        for (FiaTask t : tasks) {
            if (t.getInspectorId() != null) userIds.add(t.getInspectorId());
            if (t.getReviewerId() != null) userIds.add(t.getReviewerId());
            if (t.getStdId() != null) stdIds.add(t.getStdId());
        }
        Map<String, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (SysUser u : sysUserMapper.selectBatchIds(userIds)) {
                userNameMap.put(u.getId(), u.getRealName());
            }
        }
        Map<String, String> stdCodeMap = new HashMap<>();
        if (!stdIds.isEmpty()) {
            for (FiaInspStd s : fiaInspStdMapper.selectBatchIds(stdIds)) {
                stdCodeMap.put(s.getId(), s.getCode());
            }
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Map<String, Object>> res = new ArrayList<>();
        for (FiaArchivedReport r : reports) {
            FiaTask t = taskMap.get(r.getTaskId());
            Map<String, Object> m = new HashMap<>();
            m.put("rpt", r.getReportNo());
            m.put("wo", r.getWoNo() != null ? r.getWoNo() : (t != null ? t.getWoNo() : ""));
            m.put("prod", t != null ? t.getProductName() : "");
            m.put("proc", t != null ? t.getProcName() : "");
            m.put("d", r.getArchiveDate() != null ? r.getArchiveDate().format(df) : "");
            m.put("retainUntil", r.getRetentionUntil() != null ? r.getRetentionUntil().format(df) : "");
            m.put("std", t != null && t.getStdId() != null
                    ? stdCodeMap.getOrDefault(t.getStdId(), t.getStdId()) + " v" + (t.getStdVersion() != null ? t.getStdVersion() : "")
                    : "");
            m.put("aql", t != null ? t.getAql() : "");
            m.put("sample", t != null
                    ? (t.getSampleSize() != null ? t.getSampleSize() : "") + "/" + (t.getSampleCount() != null ? t.getSampleCount() : "")
                    : "");
            m.put("batch", t != null ? t.getBatchNo() : "");
            m.put("inspector", t != null && t.getInspectorId() != null ? userNameMap.getOrDefault(t.getInspectorId(), t.getInspectorId()) : "");
            m.put("reviewer", t != null && t.getReviewerId() != null ? userNameMap.getOrDefault(t.getReviewerId(), t.getReviewerId()) : "");
            m.put("conclusion", t != null ? t.getOverallJudge() : "");
            m.put("st", r.getStatus());
            m.put("perm", "质量/审计可阅");
            m.put("hash", r.getReportHash());
            m.put("taskId", r.getTaskId());
            res.add(m);
        }
        return res;
    }

    @Override
    public List<Map<String, Object>> getTaskLog(String taskId) {
        CompanyContext.CurrentUser _u = CompanyContext.get();
        boolean isAdmin = CompanyContext.isAdmin();
        String orgId = (_u != null) ? _u.orgId() : null;
        String realId = resolveTaskId(taskId);
        if (realId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<FiaTaskLog> qw = new LambdaQueryWrapper<FiaTaskLog>()
                .eq(FiaTaskLog::getTaskId, realId)
                .orderByAsc(FiaTaskLog::getNodeSeq);
        // 管理员(dataScope=all)看全部公司;普通用户按 org_id 过滤。
        // 注意:root 的 JWT orgId="ROOT"(非 uuid),不能直接用于 uuid 列过滤,故管理员跳过该条件。
        if (!isAdmin && orgId != null) {
            qw.eq(FiaTaskLog::getOrgId, orgId);
        }
        List<FiaTaskLog> logs = fiaTaskLogMapper.selectList(qw);
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> res = new ArrayList<>();
        for (FiaTaskLog l : logs) {
            Map<String, Object> m = new HashMap<>();
            m.put("node", l.getNodeName());
            m.put("t", l.getOpTime() != null ? l.getOpTime().format(tf) : "");
            m.put("o", l.getOperator());
            m.put("done", l.getIsDone());
            res.add(m);
        }
        return res;
    }

    // ---- 可配置签名:密码校验 + 锁定 ----
    private void verifyPassword(String orgId, String password) {
        FiaSignConfig config = signConfigService.get(orgId);
        if (config == null || config.getSignMethods() == null) {
            return;
        }
        if (!Arrays.asList(config.getSignMethods()).contains("password")) {
            return; // password 不在配置方式内(手写/CA),免密
        }
        String userId = currentOperator();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(400, "账号已锁定,请稍后再试");
        }
        if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            int fail = (user.getFailCount() == null ? 0 : user.getFailCount()) + 1;
            int lockAfter = config.getLockAfterFail() == null ? 3 : config.getLockAfterFail();
            SysUser upd = new SysUser();
            upd.setId(userId);
            upd.setFailCount(fail);
            if (fail >= lockAfter) {
                int lockMin = config.getLockMinutes() == null ? 5 : config.getLockMinutes();
                upd.setLockUntil(LocalDateTime.now().plusMinutes(lockMin));
                sysUserMapper.updateById(upd);
                throw new BusinessException(400, "密码错误次数过多,已锁定" + lockMin + "分钟");
            }
            sysUserMapper.updateById(upd);
            throw new BusinessException(400, "密码错误(剩余" + (lockAfter - fail) + "次)");
        }
        if (user.getFailCount() != null && user.getFailCount() > 0) {
            SysUser upd = new SysUser();
            upd.setId(userId);
            upd.setFailCount(0);
            upd.setLockUntil(null);
            sysUserMapper.updateById(upd);
        }
    }

    private void completeAndArchive(FiaTask task, int logSeq, String logName) {
        task.setStatus(FiaTaskStatus.COMPLETED);
        task.setSubmittedAt(LocalDateTime.now());
        task.setOverallJudge(computeJudge(task.getId()));
        computeStats(task.getId());
        // 拦截生产:首件不合格时设 disposition=拦截(记录级,不阻断 MES;一期不接 MES)
        if (!InspResult.PASS.equals(task.getOverallJudge())) {
            if (task.getDisposition() == null || task.getDisposition().isEmpty()) {
                task.setDisposition("拦截");
            }
        }
        fiaTaskMapper.updateById(task);
        archive(task);
        log(task, logSeq, logName, currentOperator());
        log(task, logSeq + 1, "归档报告", "系统");
        // 拦截生产日志 + 告警(归档日志之后)
        if (!InspResult.PASS.equals(task.getOverallJudge())) {
            log(task, logSeq + 2, "拦截生产-首件不合格", "系统");
            log.warn("FIA拦截生产: code={}, woNo={}, judge={}", task.getCode(), task.getWoNo(), task.getOverallJudge());
            // 不合格告警通知
            try {
                notificationService.notify("fia", "fia_task_rejected",
                        "首件检验不合格告警",
                        String.format("校验单 %s(工单 %s,产线 %s) 判定不合格(%s),请及时处理。",
                                task.getCode(), task.getWoNo(), task.getLineName(), task.getOverallJudge()),
                        "fia_task", task.getCode(), "/fia/tasks");
            } catch (Exception e) {
                log.warn("[FIA] 不合格告警通知失败: {}", e.getMessage());
            }
        }
        // FIA->SPC 联动(一):任务完成即由首件检验项派生 SPC 参数并绑定产品(幂等),
        // 保证签字通过后即可进入 SPC 数据采集;无论合格与否均生成,异常只 log 不阻断主流程。
        try {
            spcParamService.ensureFromFiaTask(task.getId());
        } catch (Exception e) {
            log.warn("FIA->SPC 参数生成失败, taskId={}: {}", task.getId(), e.getMessage(), e);
        }
        // FIA->SPC 联动(二):仅合格时同步 CTQ 数值到 SPC,异常只 log 不阻断主流程
        if (InspResult.PASS.equals(task.getOverallJudge())) {
            try {
                syncToSpc(task);
            } catch (Exception e) {
                log.warn("FIA->SPC 联动失败, taskId={}: {}", task.getId(), e.getMessage(), e);
            }
            // FIA->来料追溯 联动:合格免审直录物料表(同产品幂等复用,不重复录入)
            try {
                syncToTrace(task, logSeq + 5);
            } catch (Exception e) {
                log.warn("FIA->来料追溯 联动失败, taskId={}: {}", task.getId(), e.getMessage(), e);
            }
        }
        // SR-FIA-024:合格完成 -> 自动解锁工单;SR-FIA-022:不合格 -> 强化锁定(首件不合格)
        try {
            if (InspResult.PASS.equals(task.getOverallJudge())) {
                fiaWoLockService.unlockAutoInNewTx(task.getOrgId(), task.getWoNo(), task.getCode());
                log(task, logSeq + 3, "工单自动解锁", "系统");
            } else {
                fiaWoLockService.lockOnFailInNewTx(task.getOrgId(), task.getWoNo(), task.getCode());
                log(task, logSeq + 3, "工单锁定-首件不合格", "系统");
                // FIA→NCM 联动:首件不合格自动创建不良记录
                try {
                    NcmDefectRecord def = new NcmDefectRecord();
                    def.setOrgId(task.getOrgId());
                    def.setWoNo(task.getWoNo());
                    def.setProcessCode(task.getProcName());
                    def.setDefectCount(1);
                    def.setBatchTotal(1);
                    def.setSource("首件检验");
                    def.setDefectDictCode("D001");
                    def.setSeverity("一般");
                    ncmDefectRecordService.create(def);
                    log(task, logSeq + 4, "FIA→NCM联动-不良记录", "系统");
                    log.info("[FIA→NCM] 首件不合格 {} 自动创建不良记录", task.getCode());
                } catch (Exception ex) {
                    log.warn("[FIA→NCM] 联动失败 taskId={}: {}", task.getId(), ex.getMessage());
                }
                // FIA→TLM 联动:工装首件不合格时回写工装锁定
                if ("TOOLING".equals(task.getSource()) && task.getToolId() != null) {
                    try {
                        TlmTooling tooling = tlmToolingMapper.selectById(task.getToolId());
                        if (tooling != null && !Boolean.TRUE.equals(tooling.getLocked())) {
                            tooling.setLocked(true);
                            tlmToolingMapper.updateById(tooling);
                            log(task, logSeq + 5, "工装锁定-首件不合格", "系统");
                            log.info("[FIA→TLM] 工装 {} 首件不合格,已锁定", tooling.getToolNo());
                        }
                        // 推送工装预警通知
                        try {
                            notificationService.notify("tlm", "tool_first_article_fail",
                                    "工装首件不合格预警",
                                    String.format("工装 %s(编号 %s) 首件检验不合格(%s),已自动锁定,请及时处理。",
                                            tooling != null ? tooling.getToolName() : "",
                                            tooling != null ? tooling.getToolNo() : "",
                                            task.getOverallJudge()),
                                    "tlm_tooling", task.getToolId(), "/tlm/tooling/" + task.getToolId());
                        } catch (Exception ne) {
                            log.warn("[FIA→TLM] 工装预警通知失败: {}", ne.getMessage());
                        }
                    } catch (Exception ex) {
                        log.warn("[FIA→TLM] 工装锁定回写失败 toolId={}: {}", task.getToolId(), ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[FIA] 工单锁定/解锁联动失败 taskId={}: {}", task.getId(), e.getMessage());
        }
    }

    /**
     * FIA->SPC 联动:FIA 任务合格时,将 CTQ 检验项的数值型测量值同步到 SPC 参数,
     * 触发 SPC 子组录入 + WECO 判异。
     * 优先通过 SpcParam.fiaStdItemId 精确匹配;无关联时回退到 procName 匹配(向后兼容)。
     * 异常由调用方 try-catch,不阻断 FIA 主流程。
     */
    private void syncToSpc(FiaTask task) {
        // 查 FIA 任务的 CTQ 项
        List<FiaInspItem> ctqItems = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>()
                        .eq(FiaInspItem::getTaskId, task.getId())
                        .eq(FiaInspItem::getIsCtq, true));
        if (ctqItems.isEmpty()) {
            return;
        }
        // 优先:按 fiaStdItemId 精确匹配(每个 CTQ 项单独匹配对应的 SPC 参数)
        boolean fkMatched = false;
        for (FiaInspItem ctqItem : ctqItems) {
            if (ctqItem.getStdItemId() == null || ctqItem.getStdItemId().isBlank()) {
                continue;
            }
            List<SpcParam> fkParams = spcParamMapper.selectList(
                    new LambdaQueryWrapper<SpcParam>()
                            .eq(SpcParam::getFiaStdItemId, ctqItem.getStdItemId())
                            .eq(SpcParam::getIsActive, true));
            if (fkParams.isEmpty()) {
                continue;
            }
            // 解析该 CTQ 项的测量值
            BigDecimal value = null;
            try {
                value = ctqItem.getMeasuredValue() == null ? null
                        : new BigDecimal(ctqItem.getMeasuredValue().trim());
            } catch (Exception ignored) {
            }
            if (value == null) {
                continue;
            }
            for (SpcParam param : fkParams) {
                try {
                    SpcSubgroup sg = new SpcSubgroup();
                    sg.setOrgId(task.getOrgId());
                    sg.setParamId(param.getId());
                    sg.setSubgroupTime(LocalDateTime.now());
                    sg.setWoNo(task.getWoNo());
                    sg.setBatchNo(task.getBatchNo());
                    sg.setDataSource(task.getSource() != null ? task.getSource().toLowerCase() : "fia");
                    sg.setTaskId(task.getId());
                    sg.setProductCode(task.getPartNo());
                    spcSubgroupService.createInNewTx(sg, List.of(value));
                    fkMatched = true;
                } catch (Exception e) {
                    log.warn("FIA->SPC FK联动:SPC参数 {} 创建子组失败: {}", param.getId(), e.getMessage(), e);
                }
            }
        }
        if (fkMatched) {
            return; // FK 精确匹配成功,不再走后续匹配
        }
        // 获取检验标准,后续标准线匹配和 procName 兜底都需要
        FiaInspStd std = fiaInspStdMapper.selectById(task.getStdId());
        if (std == null || std.getProcName() == null || std.getProcName().isBlank()) {
            return;
        }
        // 优先级2:通过标准线(material + procName)精准匹配 SPC 参数
        if (std.getMaterial() != null && !std.getMaterial().isBlank()) {
            List<SpcSpecStandard> specStandards = spcSpecStandardMapper.selectList(
                    new LambdaQueryWrapper<SpcSpecStandard>()
                            .eq(SpcSpecStandard::getMaterial, std.getMaterial())
                            .eq(SpcSpecStandard::getProcName, std.getProcName())
                            .eq(SpcSpecStandard::getOrgId, task.getOrgId()));
            if (!specStandards.isEmpty()) {
                List<String> specStandardIds = specStandards.stream()
                        .map(SpcSpecStandard::getId).toList();
                List<SpcParam> stdLineParams = spcParamMapper.selectList(
                        new LambdaQueryWrapper<SpcParam>()
                                .in(SpcParam::getSpecStandardId, specStandardIds)
                                .eq(SpcParam::getIsActive, true));
                if (!stdLineParams.isEmpty()) {
                    List<BigDecimal> values = ctqItems.stream()
                            .map(i -> {
                                try {
                                    return i.getMeasuredValue() == null ? null : new BigDecimal(i.getMeasuredValue().trim());
                                } catch (Exception e) {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .toList();
                    if (!values.isEmpty()) {
                        for (SpcParam param : stdLineParams) {
                            try {
                                SpcSubgroup sg = new SpcSubgroup();
                                sg.setOrgId(task.getOrgId());
                                sg.setParamId(param.getId());
                                sg.setSubgroupTime(LocalDateTime.now());
                                sg.setWoNo(task.getWoNo());
                                sg.setBatchNo(task.getBatchNo());
                                sg.setDataSource(task.getSource() != null ? task.getSource().toLowerCase() : "fia");
                                sg.setTaskId(task.getId());
                                sg.setProductCode(task.getPartNo());
                                spcSubgroupService.createInNewTx(sg, values);
                            } catch (Exception e) {
                                log.warn("FIA->SPC 标准线联动:SPC参数 {} 创建子组失败: {}", param.getId(), e.getMessage(), e);
                            }
                        }
                    }
                    return; // 标准线匹配成功,不再走 procName 兜底
                }
            }
        }
        // 优先级3兜底:按 procName 匹配(向后兼容未关联标准线/检验标准的 SPC 参数)
        List<SpcParam> params = spcParamMapper.selectList(
                new LambdaQueryWrapper<SpcParam>()
                        .eq(SpcParam::getProcName, std.getProcName())
                        .eq(SpcParam::getIsActive, true));
        if (params.isEmpty()) {
            return;
        }
        List<BigDecimal> values = ctqItems.stream()
                .map(i -> {
                    try {
                        return i.getMeasuredValue() == null ? null : new BigDecimal(i.getMeasuredValue().trim());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        if (values.isEmpty()) {
            return;
        }
        for (SpcParam param : params) {
            try {
                SpcSubgroup sg = new SpcSubgroup();
                sg.setOrgId(task.getOrgId());
                sg.setParamId(param.getId());
                sg.setSubgroupTime(LocalDateTime.now());
                sg.setWoNo(task.getWoNo());
                sg.setBatchNo(task.getBatchNo());
                sg.setDataSource(task.getSource() != null ? task.getSource().toLowerCase() : "fia");
                sg.setStage("FIRST");
                sg.setTaskId(task.getId());
                sg.setProductCode(task.getPartNo());
                spcSubgroupService.createInNewTx(sg, values);
            } catch (Exception e) {
                log.warn("FIA->SPC 联动:SPC 参数 {} 创建子组失败: {}", param.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 按 setup(工单 + 物料 + 工序)查询最新一条首件任务,供量产监控前置校验使用。
     * 至少提供一个有效条件(woNo/partNo/procName),否则返回 null。
     */
    @Override
    public FiaTask findLatestBySetup(String orgId, String woNo, String partNo, String procName) {
        boolean hasAny = (woNo != null && !woNo.isBlank())
                || (partNo != null && !partNo.isBlank())
                || (procName != null && !procName.isBlank());
        if (!hasAny) {
            return null;
        }
        LambdaQueryWrapper<FiaTask> w = new LambdaQueryWrapper<FiaTask>();
        if (orgId != null && !orgId.isBlank() && !"all".equals(orgId)) {
            w.eq(FiaTask::getOrgId, orgId);
        }
        if (woNo != null && !woNo.isBlank()) w.eq(FiaTask::getWoNo, woNo);
        if (partNo != null && !partNo.isBlank()) w.eq(FiaTask::getPartNo, partNo);
        if (procName != null && !procName.isBlank()) w.eq(FiaTask::getProcName, procName);
        w.orderByDesc(FiaTask::getCreatedAt).last("LIMIT 1");
        return fiaTaskMapper.selectOne(w);
    }

    /**
     * FIA->来料追溯 联动:首件合格后,将同产品(同 partNo + supplier + org,无则按
     * partName 兜底)免审直接登记到来料追溯模块的物料表(自动建 incoming 根节点),
     * 不再走 IQC 重复抽检/审核抽样。已存在同产品批次时直接复用,实现"后续相同产品免审
     * 直录"。异常由调用方 try-catch,不阻断 FIA 主流程。
     */
    private void syncToTrace(FiaTask task, int baseSeq) {
        if (!InspResult.PASS.equals(task.getOverallJudge())) {
            return;
        }
        String partNo = task.getPartNo();
        String partName = task.getProductName();
        if ((partNo == null || partNo.isBlank()) && (partName == null || partName.isBlank())) {
            return; // 无产品标识,无法登记
        }
        // 来料批次要求 part_no 非空:缺失时用 partName 兜底,避免非空约束冲突导致复核签名整笔回滚
        if (partNo == null || partNo.isBlank()) {
            partNo = partName;
        }
        // 同产品已登记过来料追溯:免审直接关联,不重复建批次
        SqmIncomingLot exist = sqmTraceService.findExistingLot(
                task.getOrgId(), partNo, partName, task.getSupplierId());
        if (exist != null) {
            log(task, baseSeq, "FIA→来料追溯(免审复用已有批次)", "系统");
            return;
        }
        SqmIncomingLot lot = new SqmIncomingLot();
        lot.setOrgId(task.getOrgId());
        lot.setPartNo(partNo);
        lot.setPartName(partName);
        lot.setSupplierId(task.getSupplierId());
        lot.setLotNo("FIA-" + (task.getCode() != null ? task.getCode() : String.valueOf(System.currentTimeMillis())));
        lot.setQty(task.getSampleCount() != null ? new BigDecimal(task.getSampleCount()) : BigDecimal.ONE);
        lot.setUnit("PCS");
        lot.setIncomingDate(LocalDate.now());
        lot.setInspectResult(InspResult.PASS);
        lot.setInspectType("正常");
        lot.setIqcPass(true);
        sqmTraceService.createLot(lot);
        log(task, baseSeq, "FIA→来料追溯(免审直录物料表)", "系统");
    }

    private String computeJudge(String taskId) {
        List<FiaInspItem> items = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, taskId));
        boolean anyCtqFail = items.stream().anyMatch(i -> Boolean.TRUE.equals(i.getIsCtq()) && InspResult.FAIL.equals(i.getJudge()));
        boolean anyFail = items.stream().anyMatch(i -> InspResult.FAIL.equals(i.getJudge()));
        if (anyCtqFail) {
            return InspResult.FAIL;
        }
        return anyFail ? "警告" : InspResult.PASS;
    }

    /**
     * 依据标准项规则计算单条检验项判定:合格/不合格;不可匹配(缺规则/缺实测/非数值/枚举未定义)返回 null。
     * 数值型:stdValue±tolerance(支持 ±X、单值 X 视作±X、L~U / L-U 区间)。
     * 枚举型:实测命中 passValues → 合格;命中其它 enumValues → 不合格;否则不可匹配(人工兜底)。
     */
    private static BigDecimal parseMeasured(String measuredValue) {
        try {
            return new BigDecimal(measuredValue.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String computeItemJudge(FiaInspStdItem rule, String measuredValue) {
        if (rule == null || measuredValue == null || measuredValue.isBlank()) {
            return null;
        }
        String vt = rule.getValueType();
        if ("numeric".equals(vt) || "数值".equals(vt)) {
            // 标准值兼容:纯数字(中心值) / ≥X / ≤X / =X(单侧界限,无显式公差时以该界限为上下限)
            String sv = rule.getStdValue() != null ? rule.getStdValue().trim() : "";
            BigDecimal center = null;
            BigDecimal oneSidedLower = null;
            BigDecimal oneSidedUpper = null;
            if (NUM_RE.matcher(sv).matches()) {
                center = new BigDecimal(sv);
            } else if (sv.startsWith("≥") || sv.startsWith(">=")) {
                oneSidedLower = new BigDecimal(sv.startsWith("≥") ? sv.substring(1) : sv.substring(2).trim());
            } else if (sv.startsWith("≤") || sv.startsWith("<=")) {
                oneSidedUpper = new BigDecimal(sv.startsWith("≤") ? sv.substring(1) : sv.substring(2).trim());
            } else if (sv.startsWith("=")) {
                center = new BigDecimal(sv.substring(1).trim());
            }
            String tol = rule.getTolerance();
            if (tol == null || tol.isBlank()) {
                // 无显式公差:若标准值本身是单侧界限(≥X/≤X),则以此为准做单边界定
                if (oneSidedLower != null) {
                    BigDecimal mv = parseMeasured(measuredValue);
                    if (mv == null) return null;
                    return mv.compareTo(oneSidedLower) >= 0 ? InspResult.PASS : InspResult.FAIL;
                }
                if (oneSidedUpper != null) {
                    BigDecimal mv = parseMeasured(measuredValue);
                    if (mv == null) return null;
                    return mv.compareTo(oneSidedUpper) <= 0 ? InspResult.PASS : InspResult.FAIL;
                }
                return null;
            }
            String t = tol.trim();
            BigDecimal lower, upper;
            Matcher mPm = TOL_PM_RE.matcher(t);
            if (mPm.find()) {
                BigDecimal half = new BigDecimal(mPm.group(1));
                BigDecimal c = center != null ? center : BigDecimal.ZERO;
                lower = c.subtract(half);
                upper = c.add(half);
            } else {
                Matcher mRange = TOL_RANGE_RE.matcher(t);
                if (mRange.find()) {
                    lower = new BigDecimal(mRange.group(1));
                    upper = new BigDecimal(mRange.group(2));
                } else if (NUM_RE.matcher(t).matches()) {
                    BigDecimal half = new BigDecimal(t);
                    BigDecimal c = center != null ? center : BigDecimal.ZERO;
                    lower = c.subtract(half);
                    upper = c.add(half);
                } else {
                    return null;
                }
            }
            BigDecimal mv;
            try {
                mv = new BigDecimal(measuredValue.trim());
            } catch (Exception e) {
                return null;
            }
            return (mv.compareTo(lower) >= 0 && mv.compareTo(upper) <= 0) ? InspResult.PASS : InspResult.FAIL;
        } else if ("enum".equals(vt) || "枚举".equals(vt)) {
            if (rule.getPassValues() == null || rule.getPassValues().trim().isEmpty()) {
                return null;
            }
            Set<String> pass = Arrays.stream(rule.getPassValues().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
            if (pass.isEmpty()) {
                return null;
            }
            String mv = measuredValue.trim();
            if (pass.contains(mv)) {
                return InspResult.PASS;
            }
            if (rule.getEnumValues() != null && !rule.getEnumValues().trim().isEmpty()) {
                Set<String> enums = Arrays.stream(rule.getEnumValues().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
                if (enums.contains(mv)) {
                    return InspResult.FAIL;
                }
            }
            return null; // 实测不在枚举定义内 → 不可匹配,人工兜底
        }
        return null; // 文本型/空 → 人工兜底
    }

    /** 统计检验项总数/合格数/不合格数,并计算合格率(=合格数/总数)写回 fia_task。 */
    private void computeStats(String taskId) {
        List<FiaInspItem> items = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, taskId));
        int total = items.size();
        int pass = 0, fail = 0;
        for (FiaInspItem it : items) {
            if (InspResult.PASS.equals(it.getJudge())) {
                pass++;
            } else if (InspResult.FAIL.equals(it.getJudge())) {
                fail++;
            }
        }
        BigDecimal rate = total > 0
                ? BigDecimal.valueOf(pass).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        FiaTask upd = new FiaTask();
        upd.setId(taskId);
        upd.setItemTotal(total);
        upd.setPassCount(pass);
        upd.setFailCount(fail);
        upd.setPassRate(rate);
        fiaTaskMapper.updateById(upd);
    }

    @Override
    public List<PreviewJudgeResult> previewJudge(String taskId, PreviewJudgeRequest req) {
        String realId = resolveTaskId(taskId);
        if (realId == null) {
            throw new BusinessException(400, "任务不存在");
        }
        List<FiaInspItem> taskItems = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, realId));
        Map<String, FiaInspItem> itemMap = taskItems.stream()
                .collect(Collectors.toMap(FiaInspItem::getId, i -> i, (a, b) -> a));
        Set<String> stdIds = taskItems.stream().map(FiaInspItem::getStdItemId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, FiaInspStdItem> stdMap = stdIds.isEmpty() ? Collections.emptyMap() :
                fiaInspStdItemMapper.selectBatchIds(stdIds).stream()
                        .collect(Collectors.toMap(FiaInspStdItem::getId, s -> s, (a, b) -> a));
        List<PreviewJudgeResult> res = new ArrayList<>();
        if (req.getItems() != null) {
            for (PreviewJudgeRequest.PreviewJudgeItem pi : req.getItems()) {
                FiaInspItem ti = itemMap.get(pi.getId());
                String measured = pi.getMeasuredValue() != null ? pi.getMeasuredValue()
                        : (ti != null ? ti.getMeasuredValue() : null);
                String judge = null;
                boolean matchable = false;
                if (ti != null && ti.getStdItemId() != null) {
                    judge = computeItemJudge(stdMap.get(ti.getStdItemId()), measured);
                    matchable = judge != null;
                }
                PreviewJudgeResult r = new PreviewJudgeResult();
                r.setId(pi.getId());
                r.setJudge(judge);
                r.setMatchable(matchable);
                r.setAutoJudged(matchable);
                res.add(r);
            }
        }
        return res;
    }

    /**
     * 标准引用追溯:列出引用该标准(stdId)的首件任务;itemId 非空时精确到引用该标准项的任务。
     * 组织隔离沿用 CompanyContext;命中检验项按 std_item_id 过滤(精确到项时只剩该项)。
     */
    @Override
    public StdTraceResult traceStd(String stdId, String itemId) {
        StdTraceResult res = new StdTraceResult();
        res.setStdId(stdId);
        List<FiaTask> tasks;
        if (itemId != null && !itemId.isBlank()) {
            Set<String> taskIds = fiaInspItemMapper.selectList(
                            new LambdaQueryWrapper<FiaInspItem>().select(FiaInspItem::getTaskId).eq(FiaInspItem::getStdItemId, itemId))
                    .stream().map(FiaInspItem::getTaskId).collect(Collectors.toSet());
            tasks = taskIds.isEmpty() ? Collections.emptyList()
                    : fiaTaskMapper.selectList(new LambdaQueryWrapper<FiaTask>().in(FiaTask::getId, taskIds));
        } else {
            tasks = fiaTaskMapper.selectList(new LambdaQueryWrapper<FiaTask>().eq(FiaTask::getStdId, stdId));
        }
        // 组织隔离
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = u != null && !"all".equals(u.dataScope()) ? u.orgId() : null;
        if (orgId != null) {
            tasks = tasks.stream().filter(t -> orgId.equals(t.getOrgId())).collect(Collectors.toList());
        }
        // 收集该标准下的标准项 id(精确到项时只取该项),用于过滤命中检验项
        Set<String> stdItemIds = new HashSet<>();
        if (itemId != null && !itemId.isBlank()) {
            stdItemIds.add(itemId);
        } else {
            stdItemIds.addAll(fiaInspStdItemMapper.selectList(
                            new LambdaQueryWrapper<FiaInspStdItem>().select(FiaInspStdItem::getId).eq(FiaInspStdItem::getStdId, stdId))
                    .stream().map(FiaInspStdItem::getId).collect(Collectors.toSet()));
        }
        List<StdTraceResult.StdTraceTask> list = new ArrayList<>();
        for (FiaTask t : tasks) {
            List<FiaInspItem> items = stdItemIds.isEmpty() ? Collections.emptyList()
                    : fiaInspItemMapper.selectList(
                            new LambdaQueryWrapper<FiaInspItem>()
                                    .eq(FiaInspItem::getTaskId, t.getId())
                                    .in(FiaInspItem::getStdItemId, stdItemIds));
            StdTraceResult.StdTraceTask tt = new StdTraceResult.StdTraceTask();
            tt.setTask(t);
            tt.setItems(items);
            list.add(tt);
        }
        res.setTasks(list);
        return res;
    }

    private void archive(FiaTask task) {
        List<FiaInspItem> items = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, task.getId()));
        StringBuilder sb = new StringBuilder(task.getCode()).append('|').append(task.getWoNo());
        for (FiaInspItem i : items) {
            sb.append('|').append(i.getItemName()).append(':').append(i.getMeasuredValue()).append('(').append(i.getJudge()).append(')');
        }
        FiaArchivedReport r = new FiaArchivedReport();
        r.setOrgId(task.getOrgId());
        r.setReportNo("AR-" + task.getCode());
        r.setTaskId(task.getId());
        r.setWoNo(task.getWoNo());
        r.setArchiveDate(LocalDate.now());
        r.setStatus("已归档");
        r.setReportHash(sha256(sb.toString()));
        r.setRetentionUntil(LocalDate.now().plusYears(15));
        // 生成归档 PDF(openhtmltopdf),失败回退占位
        r.setPdfRef(generatePdf(task, items, r));
        fiaArchivedReportMapper.insert(r);
    }

    /**
     * 用 openhtmltopdf 从 HTML 模板生成归档 PDF,存本地 logs/reports/ 目录(一期不接 MinIO)。
     * 生成失败则回退 placeholder://,不阻断归档主流程。
     */
    private String generatePdf(FiaTask task, List<FiaInspItem> items, FiaArchivedReport r) {
        try {
            String html = buildReportHtml(task, items, r);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            builder.toStream(os);
            builder.run();
            byte[] pdfBytes = os.toByteArray();
            String fileName = "fia-report-" + task.getCode() + ".pdf";
            Path pdfPath = Paths.get("logs", "reports", fileName);
            Files.createDirectories(pdfPath.getParent());
            Files.write(pdfPath, pdfBytes);
            return pdfPath.toString();
        } catch (Exception e) {
            log.warn("FIA 归档 PDF 生成失败,回退占位, taskId={}: {}", task.getId(), e.getMessage(), e);
            return "placeholder://" + task.getId();
        }
    }

    /** 构建归档报告 HTML(内联样式,不依赖外部 CSS;openhtmltopdf 渲染)。 */
    private String buildReportHtml(FiaTask task, List<FiaInspItem> items, FiaArchivedReport r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>")
          .append("body{font-family:'SimSun',serif;font-size:12px;color:#000;margin:24px;}")
          .append("h1{text-align:center;font-size:20px;margin:8px 0 16px;}")
          .append("h2{font-size:14px;margin:12px 0 4px;border-left:4px solid #333;padding-left:6px;}")
          .append("table{border-collapse:collapse;width:100%;margin:4px 0;}")
          .append("th,td{border:1px solid #333;padding:4px 6px;text-align:left;font-size:11px;}")
          .append("th{background:#eee;}")
          .append(".info{margin:2px 0;}")
          .append(".label{display:inline-block;width:110px;font-weight:bold;}")
          .append("</style></head><body>");
        // 标题
        sb.append("<h1>首件检验归档报告</h1>");
        // 报告元信息
        sb.append("<div class=\"info\"><span class=\"label\">报告编号:</span>").append(str(r.getReportNo())).append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">归档日期:</span>").append(str(r.getArchiveDate())).append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">留存至:</span>").append(str(r.getRetentionUntil())).append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">报告哈希:</span>").append(str(r.getReportHash())).append("</div>");
        // 任务信息
        sb.append("<h2>任务信息</h2>");
        sb.append("<table>");
        appendRow(sb, "任务编号", str(task.getCode()), "工单号", str(task.getWoNo()));
        appendRow(sb, "产品名称", str(task.getProductName()), "工序名称", str(task.getProcName()));
        appendRow(sb, "产线", str(task.getLineName()), "批次号", str(task.getBatchNo()));
        appendRow(sb, "触发类型", str(task.getTriggerType()), "是否紧急", Boolean.TRUE.equals(task.getIsUrgent()) ? "是" : "否");
        appendRow(sb, "标准版本", str(task.getStdVersion()), "AQL", str(task.getAql()));
        appendRow(sb, "任务状态", str(task.getStatus()), "综合判定", str(task.getOverallJudge()));
        appendRow(sb, "处置", str(task.getDisposition()), "提交时间", str(task.getSubmittedAt()));
        sb.append("</table>");
        // 检验项目
        sb.append("<h2>检验项目</h2>");
        sb.append("<table><tr><th>序号</th><th>检验项</th><th>CTQ</th><th>标准值</th><th>公差</th><th>单位</th><th>测量值</th><th>判定</th></tr>");
        for (FiaInspItem i : items) {
            sb.append("<tr>")
              .append("<td>").append(str(i.getSeq())).append("</td>")
              .append("<td>").append(str(i.getItemName())).append("</td>")
              .append("<td>").append(Boolean.TRUE.equals(i.getIsCtq()) ? "是" : "").append("</td>")
              .append("<td>").append(str(i.getStdValue())).append("</td>")
              .append("<td>").append(str(i.getTolerance())).append("</td>")
              .append("<td>").append(str(i.getUnit())).append("</td>")
              .append("<td>").append(str(i.getMeasuredValue())).append("</td>")
              .append("<td>").append(str(i.getJudge())).append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");
        // 签名
        sb.append("<h2>签名</h2>");
        sb.append("<table><tr><th>角色</th><th>签名人</th><th>时间</th></tr>");
        sb.append("<tr><td>检验人</td><td>").append(str(task.getInspectorId())).append("</td><td></td></tr>");
        sb.append("<tr><td>复核人</td><td>").append(str(task.getReviewerId())).append("</td><td>").append(str(task.getReviewedAt())).append("</td></tr>");
        sb.append("<tr><td>批准人</td><td>").append(str(task.getApproverId())).append("</td><td>").append(str(task.getApprovedAt())).append("</td></tr>");
        sb.append("</table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, String l1, String v1, String l2, String v2) {
        sb.append("<tr><td><b>").append(l1).append("</b></td><td>").append(v1).append("</td>")
          .append("<td><b>").append(l2).append("</b></td><td>").append(v2).append("</td></tr>");
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    /** 判断字符串是否为合法 UUID(org_id 列为 uuid 类型,非法值直接查会 500) */
    private static boolean isValidUuid(String s) {
        if (s == null || s.isBlank()) return false;
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder h = new StringBuilder();
            for (byte x : b) {
                h.append(String.format("%02x", x));
            }
            return h.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void log(FiaTask task, int seq, String nodeName, String operator) {
        FiaTaskLog l = new FiaTaskLog();
        l.setOrgId(task.getOrgId());
        l.setTaskId(task.getId());
        l.setNodeSeq(seq);
        l.setNodeName(nodeName);
        l.setOpTime(LocalDateTime.now());
        l.setOperator(operator);
        l.setOpType("系统".equals(operator) ? "系统" : "人工");
        l.setIsDone(true);
        fiaTaskLogMapper.insert(l);
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }
}
