package com.konli.qms.service.fia.impl;

import com.konli.qms.service.fia.dto.FinishInspectionCreateRequest;
import com.konli.qms.service.fia.dto.FinishInspectionUpdateRequest;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.fia.FiaFinishInspectionService;
import com.konli.qms.service.fia.FinishInspectionQuery;
import com.konli.qms.service.fia.dto.EligibleFirstArticleVO;
import com.konli.qms.service.fia.dto.FinishInspectionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 完工检验独立模块实现:JdbcTemplate 直读直写 MES qms.finished_goods_inspection
 * (40 列全 text、无主键;以应用生成的 report_no 作为稳定行定位键,规避 ctid 随 VACUUM/行移动漂移)。
 * 不复用首件主表(fia_task)、不建检验项明细表、不接审批流。
 * 首件绑定仅按 production_order_no 查询 fia_task 展示(软提示,不新增存储)。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FiaFinishInspectionServiceImpl implements FiaFinishInspectionService {

    private final JdbcTemplate jdbcTemplate;

    private static final String TABLE = "qms.finished_goods_inspection";

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    /** 当前登录用户 id(用于 created_by/updated_by 归属隔离,与 MES 推送数据区分)。 */
    private String currentUserId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "system" : u.userId();
    }

    /** 按物料编码从物料主数据带出型号规格/单位;带出失败静默降级留空。 */
    private void fillFromMaterial(FinishInspectionCreateRequest req) {
        String partNo = req.getMaterialCode();
        if (partNo == null || partNo.isBlank()) {
            return;
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT spec_model, unit FROM ops.sqm_material"
                            + " WHERE is_deleted = false AND part_no = ?"
                            + " ORDER BY updated_at DESC LIMIT 1",
                    partNo);
            if (!rows.isEmpty()) {
                Map<String, Object> r = rows.get(0);
                if (req.getModelSpec() == null || req.getModelSpec().isBlank()) {
                    String spec = blankToNull(str(r.get("spec_model")));
                    if (spec != null) req.setModelSpec(spec);
                }
                if (req.getPlantCode() == null || req.getPlantCode().isBlank()) {
                    String unit = blankToNull(str(r.get("unit")));
                    if (unit != null) req.setUnit(unit);
                }
            }
        } catch (Exception e) {
            log.warn("[FINISH] 按物料带出型号规格/单位失败 partNo={}: {}", partNo, e.getMessage());
        }
    }

    @Override
    public List<String> listMesProductionOrders(String orgId, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT production_order_no FROM " + TABLE
                        + " WHERE is_deleted = '0' AND production_order_no IS NOT NULL AND production_order_no <> ''");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND production_order_no ILIKE ?");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY production_order_no ASC LIMIT 300");
        try {
            return jdbcTemplate.queryForList(sql.toString(), String.class, args.toArray());
        } catch (Exception e) {
            log.warn("[FINISH] 查询生产订单号下拉失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public PageResult<FinishInspectionVO> page(FinishInspectionQuery query) {
        StringBuilder where = new StringBuilder(" WHERE is_deleted = '0'");
        List<Object> args = new ArrayList<>();
        if (query.getCategory() != null && !query.getCategory().isBlank()) {
            where.append(" AND category = ?");
            args.add(query.getCategory());
        }
        if (query.getProductionOrderNo() != null && !query.getProductionOrderNo().isBlank()) {
            where.append(" AND production_order_no ILIKE ?");
            args.add("%" + query.getProductionOrderNo().trim() + "%");
        }
        if (query.getMaterialCode() != null && !query.getMaterialCode().isBlank()) {
            where.append(" AND material_code ILIKE ?");
            args.add("%" + query.getMaterialCode().trim() + "%");
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String kw = "%" + query.getKeyword().trim() + "%";
            where.append(" AND (production_order_no ILIKE ? OR material_code ILIKE ? OR product_name ILIKE ? OR prod_batch_or_sn ILIKE ?)");
            args.add(kw);
            args.add(kw);
            args.add(kw);
            args.add(kw);
        }
        int page = Math.max(query.getPage(), 1);
        int size = query.getSize() <= 0 ? 20 : Math.min(query.getSize(), 200);
        int offset = (page - 1) * size;

        Long total = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM " + TABLE + where, Long.class, args.toArray());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM " + TABLE + where + " ORDER BY created_at DESC NULLS LAST LIMIT ? OFFSET ?",
                append(args, size, offset).toArray());

        List<FinishInspectionVO> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            FinishInspectionVO vo = mapToVo(r);
            vo.setId(str(r.get("report_no")));
            list.add(vo);
        }
        return new PageResult<>(list, total == null ? 0L : total, page, size);
    }

    @Override
    public FinishInspectionVO get(String id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM " + TABLE + " WHERE report_no = ? AND is_deleted = '0' LIMIT 1", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404, "完工检验记录不存在: " + id);
        }
        FinishInspectionVO vo = mapToVo(rows.get(0));
        vo.setId(id);
        return vo;
    }

    @Override
    public String create(FinishInspectionCreateRequest req) {
        if (req.getProductionOrderNo() == null || req.getProductionOrderNo().isBlank()) {
            throw new BusinessException(400, "生产订单号不能为空");
        }
        if (req.getMaterialCode() == null || req.getMaterialCode().isBlank()) {
            throw new BusinessException(400, "物料编码不能为空");
        }
        // 型号规格/单位按物料带出(前端未填时)
        fillFromMaterial(req);

        String now = LocalDateTime.now().toString();
        String user = currentUserId();
        String cat = req.getCategory() != null && !req.getCategory().isBlank() ? req.getCategory() : "成品";
        String reportNo = genReportNo(req.getProductionOrderNo());

        jdbcTemplate.update(
                "INSERT INTO " + TABLE
                        + " (report_no, production_order_no, material_code, product_name, model_spec,"
                        + "  prod_batch_or_sn, production_date, category, is_urgent, is_entrusted,"
                        + "  plant_code, plant_name, unit, is_valid, is_deleted, created_by, updated_by, created_at, updated_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '1', '0', ?, ?, ?, ?)",
                reportNo,
                req.getProductionOrderNo(),
                req.getMaterialCode(),
                blankToNull(req.getProductName()),
                blankToNull(req.getModelSpec()),
                blankToNull(req.getProdBatchOrSn()),
                blankToNull(req.getProductionDate()),
                cat,
                req.getIsUrgent() != null && req.getIsUrgent() ? "1" : "0",
                req.getIsEntrusted() != null && req.getIsEntrusted() ? "1" : "0",
                blankToNull(req.getPlantCode()),
                blankToNull(req.getPlantName()),
                blankToNull(req.getUnit()),
                user, user, now, now);
        log.info("[FINISH] 新建完工检验单 production_order_no={}, material_code={}, report_no={}",
                req.getProductionOrderNo(), req.getMaterialCode(), reportNo);

        // 直接返回 report_no 作为行定位键(应用生成且唯一,规避 MES 表无主键下 ctid 不稳定的问题)
        return reportNo;
    }

    @Override
    public void updateInspection(String id, FinishInspectionUpdateRequest req) {
        FinishInspectionVO exist = get(id);
        jdbcTemplate.update(
                "UPDATE " + TABLE
                        + " SET inspector_name = COALESCE(?, inspector_name),"
                        + "  inspection_result = COALESCE(?, inspection_result),"
                        + "  inspection_request_no = COALESCE(?, inspection_request_no),"
                        + "  expiry_date = COALESCE(?, expiry_date),"
                        + "  drug_reg_no = COALESCE(?, drug_reg_no),"
                        + "  perf_test_method = COALESCE(?, perf_test_method),"
                        + "  perf_sample_batch_no = COALESCE(?, perf_sample_batch_no),"
                        + "  updated_by = ?, updated_at = now()"
                        + " WHERE report_no = ? AND is_deleted = '0'",
                blankToNull(req.getInspectorName()),
                blankToNull(req.getInspectionResult()),
                blankToNull(req.getInspectionRequestNo()),
                blankToNull(req.getExpiryDate()),
                blankToNull(req.getDrugRegNo()),
                blankToNull(req.getPerfTestMethod()),
                blankToNull(req.getPerfSampleBatchNo()),
                currentUserId(), id);
        log.info("[FINISH] 保存检验汇总 production_order_no={}, result={}",
                exist.getProductionOrderNo(), req.getInspectionResult());
    }

    @Override
    public void updateSignoff(String id, FinishInspectionUpdateRequest req) {
        get(id); // 存在性校验
        jdbcTemplate.update(
                "UPDATE " + TABLE
                        + " SET submitted_qty = ?, inspected_qty = ?, qualified_qty = ?, unqualified_qty = ?, unit = ?,"
                        + "  qc_review = COALESCE(?, qc_review), qc_reviewer = ?, qc_review_time = ?,"
                        + "  mgr_approval = COALESCE(?, mgr_approval), mgr_representative = ?,"
                        + "  mgr_approval_time = ?, signature_user = ?, signature_time = ?, signature_reason = ?, updated_by = ?, updated_at = now()"
                        + " WHERE report_no = ? AND is_deleted = '0'",
                toStr(req.getSubmittedQty()),
                toStr(req.getInspectedQty()),
                toStr(req.getQualifiedQty()),
                toStr(req.getUnqualifiedQty()),
                blankToNull(req.getUnit()),
                blankToNull(req.getQcReview()),
                blankToNull(req.getQcReviewer()),
                blankToNull(req.getQcReviewTime()),
                blankToNull(req.getMgrApproval()),
                blankToNull(req.getMgrRepresentative()),
                blankToNull(req.getMgrApprovalTime()),
                blankToNull(req.getSignatureUser()),
                blankToNull(req.getSignatureTime()),
                blankToNull(req.getSignatureReason()),
                currentUserId(), id);
        log.info("[FINISH] 保存审核签核 production_order_no={}", id);
    }

    @Override
    public void softDelete(String id) {
        FinishInspectionVO exist = get(id);
        jdbcTemplate.update(
                "UPDATE " + TABLE + " SET is_deleted = '1', updated_by = ?, updated_at = now()"
                        + " WHERE report_no = ? AND is_deleted = '0'",
                currentUserId(), id);
        log.info("[FINISH] 软删完工检验单 production_order_no={}", exist.getProductionOrderNo());
    }

    @Override
    public List<EligibleFirstArticleVO> listBoundFirstArticles(String id) {
        FinishInspectionVO vo = get(id);
        String orderNo = vo.getProductionOrderNo();
        if (orderNo == null || orderNo.isBlank()) {
            return new ArrayList<>();
        }
        // 首件软绑定: 按生产订单号(=首件 wo_no)查 fia_task 中同单且非完工检验的首件(软提示,可跳过)
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, code, wo_no, product_name, part_no, proc_name, overall_judge, status, created_at"
                        + " FROM ops.fia_task"
                        + " WHERE is_deleted = false AND wo_no = ? AND (trigger_type IS NULL OR trigger_type <> '完工检验')"
                        + " ORDER BY created_at DESC LIMIT 20",
                orderNo);
        List<EligibleFirstArticleVO> vos = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            EligibleFirstArticleVO a = new EligibleFirstArticleVO();
            a.setTaskId(str(r.get("id")));
            a.setCode(str(r.get("code")));
            a.setWoNo(str(r.get("wo_no")));
            a.setProductName(str(r.get("product_name")));
            a.setPartNo(str(r.get("part_no")));
            a.setProcName(str(r.get("proc_name")));
            a.setOverallJudge(str(r.get("overall_judge")));
            a.setStatus(str(r.get("status")));
            Object ca = r.get("created_at");
            if (ca instanceof java.sql.Timestamp ts) {
                a.setCreatedAt(ts.toLocalDateTime());
            } else if (ca instanceof java.time.LocalDateTime ldt) {
                a.setCreatedAt(ldt);
            } else {
                a.setCreatedAt(null);
            }
            vos.add(a);
        }
        return vos;
    }

    // ---- 内部工具 ----

    private String genReportNo(String orderNo) {
        return "FG-" + orderNo + "-" + System.currentTimeMillis();
    }

    private String toStr(BigDecimal b) {
        return b == null ? null : b.toPlainString();
    }

    private List<Object> append(List<Object> args, Object... more) {
        List<Object> all = new ArrayList<>(args);
        for (Object o : more) all.add(o);
        return all;
    }

    private FinishInspectionVO mapToVo(Map<String, Object> r) {
        FinishInspectionVO vo = new FinishInspectionVO();
        vo.setReportNo(str(r.get("report_no")));
        vo.setInspectionRequestNo(str(r.get("inspection_request_no")));
        vo.setProductionOrderNo(str(r.get("production_order_no")));
        vo.setMaterialCode(str(r.get("material_code")));
        vo.setProductName(str(r.get("product_name")));
        vo.setModelSpec(str(r.get("model_spec")));
        vo.setProdBatchOrSn(str(r.get("prod_batch_or_sn")));
        vo.setProductionDate(str(r.get("production_date")));
        vo.setExpiryDate(str(r.get("expiry_date")));
        vo.setSubmittedQty(toDecimal(r.get("submitted_qty")));
        vo.setInspectedQty(toDecimal(r.get("inspected_qty")));
        vo.setQualifiedQty(toDecimal(r.get("qualified_qty")));
        vo.setUnqualifiedQty(toDecimal(r.get("unqualified_qty")));
        vo.setUnit(str(r.get("unit")));
        vo.setInspectorName(str(r.get("inspector_name")));
        vo.setInspectionResult(str(r.get("inspection_result")));
        vo.setDrugRegNo(str(r.get("drug_reg_no")));
        vo.setPerfTestMethod(str(r.get("perf_test_method")));
        vo.setPerfSampleBatchNo(str(r.get("perf_sample_batch_no")));
        vo.setQcReviewer(str(r.get("qc_reviewer")));
        vo.setQcReviewTime(str(r.get("qc_review_time")));
        vo.setMgrRepresentative(str(r.get("mgr_representative")));
        vo.setMgrApprovalTime(str(r.get("mgr_approval_time")));
        vo.setSignatureUser(str(r.get("signature_user")));
        vo.setSignatureTime(str(r.get("signature_time")));
        vo.setSignatureReason(str(r.get("signature_reason")));
        vo.setQcReview(str(r.get("qc_review")));
        vo.setMgrApproval(str(r.get("mgr_approval")));
        vo.setIsUrgent(str(r.get("is_urgent")));
        vo.setIsEntrusted(str(r.get("is_entrusted")));
        vo.setIsValid(str(r.get("is_valid")));
        vo.setCategory(str(r.get("category")));
        vo.setPlantCode(str(r.get("plant_code")));
        vo.setPlantName(str(r.get("plant_name")));
        vo.setCreatedBy(str(r.get("created_by")));
        vo.setUpdatedBy(str(r.get("updated_by")));
        vo.setCreatedAt(str(r.get("created_at")));
        vo.setUpdatedAt(str(r.get("updated_at")));
        return vo;
    }

    private BigDecimal toDecimal(Object o) {
        if (o == null) return null;
        try {
            return new BigDecimal(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
