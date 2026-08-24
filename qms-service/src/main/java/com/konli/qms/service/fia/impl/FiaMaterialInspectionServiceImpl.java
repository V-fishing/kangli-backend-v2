package com.konli.qms.service.fia.impl;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.fia.FiaMaterialInspectionService;
import com.konli.qms.service.fia.MaterialInspectionQuery;
import com.konli.qms.service.fia.dto.MaterialInspectionCreateRequest;
import com.konli.qms.service.fia.dto.MaterialInspectionUpdateRequest;
import com.konli.qms.service.fia.dto.MaterialInspectionVO;
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
 * 物料检验(来料检验)独立模块实现:JdbcTemplate 直读直写 MES qms.material_inspection
 * (62 列全 text、无主键;以 record_no 作为稳定行定位键——新建行由应用生成 MI- 前缀唯一号,
 * MES 推送行以其业务记录编号定位,规避 ctid 随 VACUUM/行移动漂移)。
 * 不复用首件主表(fia_task)、不建检验项明细表、不接审批流。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FiaMaterialInspectionServiceImpl implements FiaMaterialInspectionService {

    private final JdbcTemplate jdbcTemplate;

    private static final String TABLE = "qms.material_inspection";

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

    /** 按物料编码从 MES 来料历史带出物料名称/型号规格/单位/供应商;带出失败静默降级留空。 */
    private void fillFromMaterial(MaterialInspectionCreateRequest req) {
        String partNo = req.getMaterialCode();
        if (partNo == null || partNo.isBlank()) {
            return;
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT material_name, spec_model, unit, supplier_name, supplier_code"
                            + " FROM " + TABLE
                            + " WHERE is_deleted = '0' AND material_code = ?"
                            + " ORDER BY updated_at DESC NULLS LAST LIMIT 1",
                    partNo);
            if (!rows.isEmpty()) {
                Map<String, Object> r = rows.get(0);
                if (req.getMaterialName() == null || req.getMaterialName().isBlank()) {
                    req.setMaterialName(blankToNull(str(r.get("material_name"))));
                }
                if (req.getSpecModel() == null || req.getSpecModel().isBlank()) {
                    req.setSpecModel(blankToNull(str(r.get("spec_model"))));
                }
                if (req.getUnit() == null || req.getUnit().isBlank()) {
                    req.setUnit(blankToNull(str(r.get("unit"))));
                }
                if (req.getSupplierName() == null || req.getSupplierName().isBlank()) {
                    req.setSupplierName(blankToNull(str(r.get("supplier_name"))));
                }
                if (req.getSupplierCode() == null || req.getSupplierCode().isBlank()) {
                    req.setSupplierCode(blankToNull(str(r.get("supplier_code"))));
                }
            }
        } catch (Exception e) {
            log.warn("[MATERIAL] 按物料带出名称/型号/单位/供应商失败 partNo={}: {}", partNo, e.getMessage());
        }
    }

    @Override
    public List<String> listMesMaterialCodes(String orgId, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT material_code FROM " + TABLE
                        + " WHERE is_deleted = '0' AND material_code IS NOT NULL AND material_code <> ''");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND material_code ILIKE ?");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(" ORDER BY material_code ASC LIMIT 300");
        try {
            return jdbcTemplate.queryForList(sql.toString(), String.class, args.toArray());
        } catch (Exception e) {
            log.warn("[MATERIAL] 查询物料编码下拉失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public PageResult<MaterialInspectionVO> page(MaterialInspectionQuery query) {
        StringBuilder where = new StringBuilder(" WHERE is_deleted = '0'");
        List<Object> args = new ArrayList<>();
        if (query.getSupplierName() != null && !query.getSupplierName().isBlank()) {
            where.append(" AND supplier_name ILIKE ?");
            args.add("%" + query.getSupplierName().trim() + "%");
        }
        if (query.getMaterialCode() != null && !query.getMaterialCode().isBlank()) {
            where.append(" AND material_code ILIKE ?");
            args.add("%" + query.getMaterialCode().trim() + "%");
        }
        if (query.getInspectionRequestNo() != null && !query.getInspectionRequestNo().isBlank()) {
            where.append(" AND inspection_request_no ILIKE ?");
            args.add("%" + query.getInspectionRequestNo().trim() + "%");
        }
        if (query.getRecordNo() != null && !query.getRecordNo().isBlank()) {
            where.append(" AND record_no ILIKE ?");
            args.add("%" + query.getRecordNo().trim() + "%");
        }
        if (query.getInspectionResult() != null && !query.getInspectionResult().isBlank()) {
            where.append(" AND inspection_result = ?");
            args.add(query.getInspectionResult());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String kw = "%" + query.getKeyword().trim() + "%";
            where.append(" AND (material_code ILIKE ? OR material_name ILIKE ? OR supplier_name ILIKE ?"
                    + " OR material_batch_no ILIKE ? OR record_no ILIKE ? OR material_barcode ILIKE ?)");
            for (int i = 0; i < 6; i++) {
                args.add(kw);
            }
        }
        int page = Math.max(query.getPage(), 1);
        int size = query.getSize() <= 0 ? 20 : Math.min(query.getSize(), 200);
        int offset = (page - 1) * size;

        Long total = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM " + TABLE + where, Long.class, args.toArray());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM " + TABLE + where + " ORDER BY created_at DESC NULLS LAST LIMIT ? OFFSET ?",
                append(args, size, offset).toArray());

        List<MaterialInspectionVO> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            MaterialInspectionVO vo = mapToVo(r);
            vo.setId(str(r.get("record_no")));
            list.add(vo);
        }
        return new PageResult<>(list, total == null ? 0L : total, page, size);
    }

    @Override
    public MaterialInspectionVO get(String id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM " + TABLE + " WHERE record_no = ? AND is_deleted = '0' LIMIT 1", id);
        if (rows.isEmpty()) {
            throw new BusinessException(404, "物料检验记录不存在: " + id);
        }
        MaterialInspectionVO vo = mapToVo(rows.get(0));
        vo.setId(id);
        return vo;
    }

    @Override
    public String create(MaterialInspectionCreateRequest req) {
        if (req.getMaterialCode() == null || req.getMaterialCode().isBlank()) {
            throw new BusinessException(400, "物料编码不能为空");
        }
        // 物料名称/型号规格/单位/供应商按物料带出(前端未填时)
        fillFromMaterial(req);

        String now = LocalDateTime.now().toString();
        String user = currentUserId();
        String recordNo = genRecordNo(req.getMaterialCode());

        jdbcTemplate.update(
                "INSERT INTO " + TABLE
                        + " (record_no, material_code, material_name, spec_model, material_batch_no,"
                        + "  material_barcode, supplier_name, supplier_code, material_category,"
                        + "  inspection_category, unit, is_customer_supplied, is_urgent,"
                        + "  plant_code, plant_name, remark, purchase_order, inbound_no, arrival_date,"
                        + "  receiving_no, po_line_no, receiving_line_no, shelf_life_days,"
                        + "  is_valid, is_deleted, created_by, updated_by, created_at, updated_at)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '1', '0', ?, ?, ?, ?)",
                recordNo,
                req.getMaterialCode(),
                blankToNull(req.getMaterialName()),
                blankToNull(req.getSpecModel()),
                blankToNull(req.getMaterialBatchNo()),
                blankToNull(req.getMaterialBarcode()),
                blankToNull(req.getSupplierName()),
                blankToNull(req.getSupplierCode()),
                blankToNull(req.getMaterialCategory()),
                blankToNull(req.getInspectionCategory()),
                blankToNull(req.getUnit()),
                req.getIsCustomerSupplied() != null && req.getIsCustomerSupplied() ? "1" : "0",
                req.getIsUrgent() != null && req.getIsUrgent() ? "1" : "0",
                blankToNull(req.getPlantCode()),
                blankToNull(req.getPlantName()),
                blankToNull(req.getRemark()),
                blankToNull(req.getPurchaseOrder()),
                blankToNull(req.getInboundNo()),
                blankToNull(req.getArrivalDate()),
                blankToNull(req.getReceivingNo()),
                blankToNull(req.getPoLineNo()),
                blankToNull(req.getReceivingLineNo()),
                blankToNull(req.getShelfLifeDays()),
                user, user, now, now);
        log.info("[MATERIAL] 新建物料检验单 material_code={}, supplier_name={}, record_no={}",
                req.getMaterialCode(), req.getSupplierName(), recordNo);

        // 直接返回 record_no 作为行定位键(应用生成且唯一,规避 MES 表无主键下 ctid 不稳定的问题)
        return recordNo;
    }

    @Override
    public void updateInspection(String id, MaterialInspectionUpdateRequest req) {
        MaterialInspectionVO exist = get(id);
        jdbcTemplate.update(
                "UPDATE " + TABLE
                        + " SET inspector = COALESCE(?, inspector),"
                        + "  inspection_result = COALESCE(?, inspection_result),"
                        + "  inspection_request_no = COALESCE(?, inspection_request_no),"
                        + "  mes_inspection_no = COALESCE(?, mes_inspection_no),"
                        + "  inspection_date = COALESCE(?, inspection_date),"
                        + "  judgement_date = COALESCE(?, judgement_date),"
                        + "  inspection_end_date = COALESCE(?, inspection_end_date),"
                        + "  defect_desc = COALESCE(?, defect_desc),"
                        + "  handling_method = COALESCE(?, handling_method),"
                        + "  purchase_order = COALESCE(?, purchase_order),"
                        + "  inbound_no = COALESCE(?, inbound_no),"
                        + "  arrival_date = COALESCE(?, arrival_date),"
                        + "  receiving_no = COALESCE(?, receiving_no),"
                        + "  po_line_no = COALESCE(?, po_line_no),"
                        + "  receiving_line_no = COALESCE(?, receiving_line_no),"
                        + "  shelf_life_days = COALESCE(?, shelf_life_days),"
                        + "  updated_by = ?, updated_at = now()"
                        + " WHERE record_no = ? AND is_deleted = '0'",
                blankToNull(req.getInspector()),
                blankToNull(req.getInspectionResult()),
                blankToNull(req.getInspectionRequestNo()),
                blankToNull(req.getMesInspectionNo()),
                blankToNull(req.getInspectionDate()),
                blankToNull(req.getJudgementDate()),
                blankToNull(req.getInspectionEndDate()),
                blankToNull(req.getDefectDesc()),
                blankToNull(req.getHandlingMethod()),
                blankToNull(req.getPurchaseOrder()),
                blankToNull(req.getInboundNo()),
                blankToNull(req.getArrivalDate()),
                blankToNull(req.getReceivingNo()),
                blankToNull(req.getPoLineNo()),
                blankToNull(req.getReceivingLineNo()),
                blankToNull(req.getShelfLifeDays()),
                currentUserId(), id);
        log.info("[MATERIAL] 保存检验汇总 material_code={}, result={}",
                exist.getMaterialCode(), req.getInspectionResult());
    }

    @Override
    public void updateSignoff(String id, MaterialInspectionUpdateRequest req) {
        get(id); // 存在性校验
        jdbcTemplate.update(
                "UPDATE " + TABLE
                        + " SET submitted_qty = ?, qualified_qty = ?, unqualified_qty = ?, loss_qty = ?, unit = ?,"
                        + "  reviewer = COALESCE(?, reviewer), review_date = COALESCE(?, review_date),"
                        + "  unqualified_review = COALESCE(?, unqualified_review),"
                        + "  unqualified_final_status = COALESCE(?, unqualified_final_status),"
                        + "  unqualified_review_no = COALESCE(?, unqualified_review_no),"
                        + "  judge = COALESCE(?, judge), submitter = COALESCE(?, submitter),"
                        + "  submit_date = COALESCE(?, submit_date), reinspect_remark = COALESCE(?, reinspect_remark),"
                        + "  signature_user = COALESCE(?, signature_user), signature_time = COALESCE(?, signature_time),"
                        + "  signature_reason = COALESCE(?, signature_reason), remark = COALESCE(?, remark),"
                        + "  updated_by = ?, updated_at = now()"
                        + " WHERE record_no = ? AND is_deleted = '0'",
                toStr(req.getSubmittedQty()),
                toStr(req.getQualifiedQty()),
                toStr(req.getUnqualifiedQty()),
                toStr(req.getLossQty()),
                blankToNull(req.getUnit()),
                blankToNull(req.getReviewer()),
                blankToNull(req.getReviewDate()),
                blankToNull(req.getUnqualifiedReview()),
                blankToNull(req.getUnqualifiedFinalStatus()),
                blankToNull(req.getUnqualifiedReviewNo()),
                blankToNull(req.getJudge()),
                blankToNull(req.getSubmitter()),
                blankToNull(req.getSubmitDate()),
                blankToNull(req.getReinspectRemark()),
                blankToNull(req.getSignatureUser()),
                blankToNull(req.getSignatureTime()),
                blankToNull(req.getSignatureReason()),
                blankToNull(req.getRemark()),
                currentUserId(), id);
        log.info("[MATERIAL] 保存数量信息与审核签核 record_no={}", id);
    }

    @Override
    public void softDelete(String id) {
        MaterialInspectionVO exist = get(id);
        jdbcTemplate.update(
                "UPDATE " + TABLE + " SET is_deleted = '1', updated_by = ?, updated_at = now()"
                        + " WHERE record_no = ? AND is_deleted = '0'",
                currentUserId(), id);
        log.info("[MATERIAL] 软删物料检验单 material_code={}", exist.getMaterialCode());
    }

    // ---- 内部工具 ----

    private String genRecordNo(String materialCode) {
        return "MI-" + materialCode + "-" + System.currentTimeMillis();
    }

    private String toStr(BigDecimal b) {
        return b == null ? null : b.toPlainString();
    }

    private List<Object> append(List<Object> args, Object... more) {
        List<Object> all = new ArrayList<>(args);
        for (Object o : more) all.add(o);
        return all;
    }

    private MaterialInspectionVO mapToVo(Map<String, Object> r) {
        MaterialInspectionVO vo = new MaterialInspectionVO();
        vo.setProcessNo(str(r.get("process_no")));
        vo.setFormVersion(str(r.get("form_version")));
        vo.setIsCustomerSupplied(str(r.get("is_customer_supplied")));
        vo.setMemo(str(r.get("memo")));
        vo.setMaterialCategory(str(r.get("material_category")));
        vo.setIsValid(str(r.get("is_valid")));
        vo.setReviewStatus(str(r.get("review_status")));
        vo.setSignatureStatus(str(r.get("signature_status")));
        vo.setIsUrgent(str(r.get("is_urgent")));
        vo.setDataRecordFlag(str(r.get("data_record_flag")));
        vo.setIsInvalid(str(r.get("is_invalid")));
        vo.setReportGenerated(str(r.get("report_generated")));
        vo.setRecordNo(str(r.get("record_no")));
        vo.setPurchaseOrder(str(r.get("purchase_order")));
        vo.setInboundNo(str(r.get("inbound_no")));
        vo.setInspectionRequestNo(str(r.get("inspection_request_no")));
        vo.setMesInspectionNo(str(r.get("mes_inspection_no")));
        vo.setInspectionDate(str(r.get("inspection_date")));
        vo.setJudgementDate(str(r.get("judgement_date")));
        vo.setInspector(str(r.get("inspector")));
        vo.setInspectionResult(str(r.get("inspection_result")));
        vo.setSupplierName(str(r.get("supplier_name")));
        vo.setSupplierCode(str(r.get("supplier_code")));
        vo.setMaterialCode(str(r.get("material_code")));
        vo.setMaterialName(str(r.get("material_name")));
        vo.setSpecModel(str(r.get("spec_model")));
        vo.setMaterialBatchNo(str(r.get("material_batch_no")));
        vo.setQualifiedQty(toDecimal(r.get("qualified_qty")));
        vo.setUnqualifiedQty(toDecimal(r.get("unqualified_qty")));
        vo.setSubmittedQty(toDecimal(r.get("submitted_qty")));
        vo.setLossQty(toDecimal(r.get("loss_qty")));
        vo.setUnit(str(r.get("unit")));
        vo.setDefectDesc(str(r.get("defect_desc")));
        vo.setHandlingMethod(str(r.get("handling_method")));
        vo.setUnqualifiedFinalStatus(str(r.get("unqualified_final_status")));
        vo.setUnqualifiedReview(str(r.get("unqualified_review")));
        vo.setUnqualifiedReviewNo(str(r.get("unqualified_review_no")));
        vo.setInspectionCategory(str(r.get("inspection_category")));
        vo.setArrivalDate(str(r.get("arrival_date")));
        vo.setReceivingNo(str(r.get("receiving_no")));
        vo.setPoLineNo(str(r.get("po_line_no")));
        vo.setReceivingLineNo(str(r.get("receiving_line_no")));
        vo.setShelfLifeDays(str(r.get("shelf_life_days")));
        vo.setReinspectRemark(str(r.get("reinspect_remark")));
        vo.setJudge(str(r.get("judge")));
        vo.setInspectionEndDate(str(r.get("inspection_end_date")));
        vo.setReviewer(str(r.get("reviewer")));
        vo.setReviewDate(str(r.get("review_date")));
        vo.setSubmitter(str(r.get("submitter")));
        vo.setSubmitDate(str(r.get("submit_date")));
        vo.setRemark(str(r.get("remark")));
        vo.setExtId(str(r.get("ext_id")));
        vo.setLastModifiedBy(str(r.get("last_modified_by")));
        vo.setSignatureUser(str(r.get("signature_user")));
        vo.setSignatureTime(str(r.get("signature_time")));
        vo.setSignatureReason(str(r.get("signature_reason")));
        vo.setPlantCode(str(r.get("plant_code")));
        vo.setPlantName(str(r.get("plant_name")));
        vo.setCreatedBy(str(r.get("created_by")));
        vo.setUpdatedBy(str(r.get("updated_by")));
        vo.setIsDeleted(str(r.get("is_deleted")));
        vo.setVersion(str(r.get("version")));
        vo.setCreatedAt(str(r.get("created_at")));
        vo.setUpdatedAt(str(r.get("updated_at")));
        vo.setMaterialBarcode(str(r.get("material_barcode")));
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
