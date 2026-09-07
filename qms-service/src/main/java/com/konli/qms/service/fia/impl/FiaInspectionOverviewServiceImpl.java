package com.konli.qms.service.fia.impl;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.service.fia.FiaInspectionOverviewService;
import com.konli.qms.service.fia.dto.MergedInspectionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 完工检验 + 物料检验 合并视图实现:
 * UNION ALL 直读 MES qms.finished_goods_inspection 与 qms.material_inspection,
 * 仅取列表展示所需公共列,统一按 created_at 倒序分页。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FiaInspectionOverviewServiceImpl implements FiaInspectionOverviewService {

    private final JdbcTemplate jdbcTemplate;

    /** MES 落地宽表全名(schema 由 qms.mes.schema 配置, 默认 qms),禁止硬编码。 */
    @Value("${qms.mes.schema:qms}.finished_goods_inspection")
    private String finishTable;
    @Value("${qms.mes.schema:qms}.material_inspection")
    private String materialTable;

    @Override
    public PageResult<MergedInspectionVO> page(int page, int size, String materialCode, String keyword) {
        List<Object> finishArgs = new ArrayList<>();
        List<Object> materialArgs = new ArrayList<>();

        StringBuilder finishWhere = new StringBuilder(" is_deleted = '0'");
        StringBuilder materialWhere = new StringBuilder(" is_deleted = '0'");
        if (materialCode != null && !materialCode.isBlank()) {
            finishWhere.append(" AND material_code ILIKE ?");
            finishArgs.add("%" + materialCode.trim() + "%");
            materialWhere.append(" AND material_code ILIKE ?");
            materialArgs.add("%" + materialCode.trim() + "%");
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            finishWhere.append(" AND (report_no ILIKE ? OR production_order_no ILIKE ?"
                    + " OR material_code ILIKE ? OR product_name ILIKE ?)");
            for (int i = 0; i < 4; i++) {
                finishArgs.add(kw);
            }
            materialWhere.append(" AND (record_no ILIKE ? OR material_code ILIKE ? OR material_name ILIKE ?"
                    + " OR supplier_name ILIKE ? OR material_batch_no ILIKE ?)");
            for (int i = 0; i < 5; i++) {
                materialArgs.add(kw);
            }
        }

        String finishSql = "SELECT 'finish' AS src_type, report_no AS row_id, report_no, NULL AS record_no,"
                + " production_order_no, material_code, product_name, NULL AS material_name,"
                + " NULL AS supplier_name, NULL AS material_batch_no, category, inspection_result,"
                + " signature_user, qc_reviewer, NULL AS reviewer, inspected_qty, created_at"
                + " FROM " + finishTable + " WHERE " + finishWhere;
        String materialSql = "SELECT 'material' AS src_type, record_no AS row_id, NULL AS report_no, record_no,"
                + " NULL AS production_order_no, material_code, NULL AS product_name, material_name,"
                + " supplier_name, material_batch_no, NULL AS category, inspection_result,"
                + " signature_user, NULL AS qc_reviewer, reviewer, submitted_qty, created_at"
                + " FROM " + materialTable + " WHERE " + materialWhere;
        String merged = "SELECT * FROM (" + finishSql + " UNION ALL " + materialSql + ") m";

        Long total = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM (" + finishSql + " UNION ALL " + materialSql + ") m",
                Long.class, mergedArgs(finishArgs, materialArgs));

        int pg = Math.max(page, 1);
        int sz = size <= 0 ? 20 : Math.min(size, 200);
        int offset = (pg - 1) * sz;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                merged + " ORDER BY m.created_at DESC NULLS LAST LIMIT ? OFFSET ?",
                mergedArgs(finishArgs, materialArgs, sz, offset));

        List<MergedInspectionVO> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            list.add(mapToVo(r));
        }
        return new PageResult<>(list, total == null ? 0L : total, pg, sz);
    }

    private MergedInspectionVO mapToVo(Map<String, Object> r) {
        MergedInspectionVO vo = new MergedInspectionVO();
        vo.setSrcType(str(r.get("src_type")));
        vo.setId(str(r.get("row_id")));
        vo.setReportNo(str(r.get("report_no")));
        vo.setRecordNo(str(r.get("record_no")));
        vo.setProductionOrderNo(str(r.get("production_order_no")));
        vo.setMaterialCode(str(r.get("material_code")));
        vo.setProductName(str(r.get("product_name")));
        vo.setMaterialName(str(r.get("material_name")));
        vo.setSupplierName(str(r.get("supplier_name")));
        vo.setMaterialBatchNo(str(r.get("material_batch_no")));
        vo.setCategory(str(r.get("category")));
        vo.setInspectionResult(str(r.get("inspection_result")));
        vo.setSignatureUser(str(r.get("signature_user")));
        vo.setQcReviewer(str(r.get("qc_reviewer")));
        vo.setReviewer(str(r.get("reviewer")));
        vo.setInspectedQty(str(r.get("inspected_qty")));
        vo.setCreatedAt(str(r.get("created_at")));
        return vo;
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Object[] mergedArgs(List<Object> finishArgs, List<Object> materialArgs, Object... more) {
        List<Object> all = new ArrayList<>(finishArgs);
        all.addAll(materialArgs);
        for (Object o : more) {
            all.add(o);
        }
        return all.toArray();
    }
}
