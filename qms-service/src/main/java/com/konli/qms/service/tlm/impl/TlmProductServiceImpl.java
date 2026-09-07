package com.konli.qms.service.tlm.impl;

import com.konli.qms.domain.tlm.vo.TlmProductCandidate;
import com.konli.qms.domain.tlm.vo.TlmProductDetail;
import com.konli.qms.service.tlm.TlmProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** 工装关联产品: 真实来源为 MES 检验表(qms.material_inspection / qms.finished_goods_inspection)。 */
@Service
@RequiredArgsConstructor
public class TlmProductServiceImpl implements TlmProductService {

    private final JdbcTemplate jdbcTemplate;

    /** MES 落地宽表所在 schema(与 QMS 自身的 ops 不同);由 qms.mes.schema 配置,禁止硬编码。 */
    @Value("${qms.mes.schema:qms}")
    private String mesSchema;

    private String materialSql() {
        return "SELECT material_code AS material_code, material_name AS product_name, spec_model AS spec_model, 'MATERIAL' AS kind "
                + "FROM " + mesSchema + ".material_inspection WHERE material_code IS NOT NULL AND material_code <> '' ";
    }

    private String semiSql() {
        return "SELECT material_code AS material_code, product_name AS product_name, model_spec AS spec_model, 'SEMI' AS kind "
                + "FROM " + mesSchema + ".finished_goods_inspection WHERE category='半成品' AND material_code IS NOT NULL AND material_code <> '' ";
    }

    private String finishedSql() {
        return "SELECT material_code AS material_code, product_name AS product_name, model_spec AS spec_model, 'FINISHED' AS kind "
                + "FROM " + mesSchema + ".finished_goods_inspection WHERE category='成品' AND material_code IS NOT NULL AND material_code <> '' ";
    }

    private static final RowMapper<TlmProductCandidate> CAND_ROW = (rs, i) -> {
        TlmProductCandidate c = new TlmProductCandidate();
        c.setMaterialCode(rs.getString("material_code"));
        c.setProductName(rs.getString("product_name"));
        c.setSpecModel(rs.getString("spec_model"));
        c.setKind(rs.getString("kind"));
        return c;
    };

    @Override
    public List<TlmProductCandidate> candidates(String keyword, String kind) {
        List<String> parts = new ArrayList<>();
        if (!StringUtils.hasText(kind) || "MATERIAL".equals(kind)) parts.add(materialSql());
        if (!StringUtils.hasText(kind) || "SEMI".equals(kind)) parts.add(semiSql());
        if (!StringUtils.hasText(kind) || "FINISHED".equals(kind)) parts.add(finishedSql());

        StringBuilder sql = new StringBuilder("SELECT DISTINCT ON (material_code) material_code, product_name, spec_model, kind FROM (")
                .append(String.join(" UNION ALL ", parts)).append(") t");
        List<Object> args = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" WHERE (material_code ILIKE ? OR product_name ILIKE ? OR COALESCE(spec_model,'') ILIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            args.add(kw); args.add(kw); args.add(kw);
        }
        sql.append(" ORDER BY material_code, kind LIMIT 200");
        return jdbcTemplate.query(sql.toString(), CAND_ROW, args.toArray());
    }

    @Override
    public List<TlmProductDetail> detail(String materialCode) {
        if (!StringUtils.hasText(materialCode)) return new ArrayList<>();
        String sql = "SELECT * FROM ("
                + "SELECT 'MATERIAL' AS kind, '" + mesSchema + ".material_inspection' AS source_table, material_code, material_name AS product_name, "
                + "spec_model, material_batch_no AS batch_no, supplier_name, inspection_result, "
                + "CAST(inspection_date AS VARCHAR) AS inspection_date, NULL AS production_order_no, "
                + "CAST(submitted_qty AS VARCHAR) AS qty, unit, inspector, plant_name "
                + "FROM " + mesSchema + ".material_inspection WHERE material_code = ? "
                + "UNION ALL "
                + "SELECT 'SEMI' AS kind, '" + mesSchema + ".finished_goods_inspection' AS source_table, material_code, product_name, "
                + "model_spec, prod_batch_or_sn AS batch_no, NULL AS supplier_name, inspection_result, "
                + "CAST(production_date AS VARCHAR) AS inspection_date, production_order_no, "
                + "CAST(inspected_qty AS VARCHAR) AS qty, unit, inspector_name AS inspector, plant_name "
                + "FROM " + mesSchema + ".finished_goods_inspection WHERE category='半成品' AND material_code = ? "
                + "UNION ALL "
                + "SELECT 'FINISHED' AS kind, '" + mesSchema + ".finished_goods_inspection' AS source_table, material_code, product_name, "
                + "model_spec, prod_batch_or_sn AS batch_no, NULL AS supplier_name, inspection_result, "
                + "CAST(production_date AS VARCHAR) AS inspection_date, production_order_no, "
                + "CAST(inspected_qty AS VARCHAR) AS qty, unit, inspector_name AS inspector, plant_name "
                + "FROM " + mesSchema + ".finished_goods_inspection WHERE category='成品' AND material_code = ? "
                + ") x ORDER BY inspection_date DESC NULLS LAST LIMIT 200";
        return jdbcTemplate.query(sql, (rs, i) -> {
            TlmProductDetail d = new TlmProductDetail();
            d.setKind(rs.getString("kind"));
            d.setSourceTable(rs.getString("source_table"));
            d.setMaterialCode(rs.getString("material_code"));
            d.setProductName(rs.getString("product_name"));
            d.setSpecModel(rs.getString("spec_model"));
            d.setBatchNo(rs.getString("batch_no"));
            d.setSupplierName(rs.getString("supplier_name"));
            d.setInspectionResult(rs.getString("inspection_result"));
            d.setInspectionDate(rs.getString("inspection_date"));
            d.setProductionOrderNo(rs.getString("production_order_no"));
            d.setQty(rs.getString("qty"));
            d.setUnit(rs.getString("unit"));
            d.setInspector(rs.getString("inspector"));
            d.setPlantName(rs.getString("plant_name"));
            return d;
        }, materialCode, materialCode, materialCode);
    }
}
