package com.konli.qms.service.sqm.impl;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.sqm.SqmMaterialBindingService;
import com.konli.qms.service.sqm.dto.MaterialBindingCreateRequest;
import com.konli.qms.service.sqm.dto.MaterialBindingUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MES 关键物料绑定管理实现: JdbcTemplate 直写 qms.critical_material_binding。
 * 表无主键, 行定位键采用 (product_barcode, material_barcode, category) 组合。
 */
@Service
@RequiredArgsConstructor
public class SqmMaterialBindingServiceImpl implements SqmMaterialBindingService {

    private static final String TABLE = "qms.critical_material_binding";

    private final JdbcTemplate jdbcTemplate;

    /** 业务列(可人工维护, 不含审计列与 MES 推送列)。 */
    private static final String[] BUSINESS_COLS = {
            "work_order_no", "product_material_no", "product_name", "work_order_qty",
            "material_code", "material_name", "spec_model", "process_code", "process_name",
            "plant_code", "plant_name", "remark"
    };

    private String currentUserId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "system" : u.userId();
    }

    @Override
    public PageResult<Map<String, Object>> list(String keyword, String category, String isActive,
                                                int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE is_deleted = '0'");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (product_barcode ILIKE ? OR material_barcode ILIKE ?"
                    + " OR product_name ILIKE ? OR material_name ILIKE ? OR material_code ILIKE ?)");
            String k = "%" + keyword.trim() + "%";
            args.add(k); args.add(k); args.add(k); args.add(k); args.add(k);
        }
        if (category != null && !category.isBlank()) {
            where.append(" AND category = ?");
            args.add(category);
        }
        if (isActive != null && !isActive.isBlank()) {
            where.append(" AND is_active = ?");
            args.add(isActive);
        }

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + TABLE + where, Long.class, args.toArray());
        int offset = (page - 1) * size;
        String dataSql = "SELECT * FROM " + TABLE + where
                + " ORDER BY updated_at DESC NULLS LAST, created_at DESC NULLS LAST"
                + " LIMIT ? OFFSET ?";
        List<Object> dataArgs = new ArrayList<>(args);
        dataArgs.add(size);
        dataArgs.add(offset);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql, dataArgs.toArray());
        return new PageResult<Map<String, Object>>(rows, (total == null ? 0 : total), page, size);
    }

    @Override
    public List<Map<String, Object>> candidates(String role, String keyword, int limit) {
        if (role == null || role.isBlank()) {
            throw new BusinessException("缺少候选角色(role=child/parent)");
        }
        if (limit <= 0) limit = 50;
        if (limit > 200) limit = 200;
        List<Map<String, Object>> out = new ArrayList<>();
        boolean child = "child".equalsIgnoreCase(role);
        boolean parent = "parent".equalsIgnoreCase(role);
        if (child || parent) {
            // 父级: 成品/半成品 finished_goods_inspection; 子件中的「半成品」也来自该表
            out.addAll(searchFinished(keyword, limit, child ? "半成品" : null));
        }
        if (child) {
            // 子件中的「来料」来自 material_inspection
            out.addAll(searchMaterial(keyword, limit));
        }
        return out;
    }

    /** 从 finished_goods_inspection 搜候选(成品/半成品)。onlyCategory 非空则过滤(如只取半成品当子件)。 */
    private List<Map<String, Object>> searchFinished(String keyword, int limit, String onlyCategory) {
        List<Map<String, Object>> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT prod_batch_or_sn AS barcode, material_code AS code, product_name AS name,"
                        + " production_order_no AS work_order_no, category FROM qms.finished_goods_inspection WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (prod_batch_or_sn ILIKE ? OR material_code ILIKE ? OR product_name ILIKE ?)");
            String k = "%" + keyword.trim() + "%";
            args.add(k); args.add(k); args.add(k);
        }
        if (onlyCategory != null && !onlyCategory.isBlank()) {
            sql.append(" AND category = ?");
            args.add(onlyCategory);
        }
        sql.append(" AND prod_batch_or_sn IS NOT NULL AND prod_batch_or_sn <> '' LIMIT ?");
        args.add(limit);
        for (Map<String, Object> r : jdbcTemplate.queryForList(sql.toString(), args.toArray())) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("barcode", str(r.get("barcode")));
            m.put("code", str(r.get("code")));
            m.put("name", str(r.get("name")));
            m.put("workOrderNo", str(r.get("work_order_no")));
            m.put("category", str(r.get("category")));
            m.put("srcType", "finished");
            out.add(m);
        }
        return out;
    }

    /** 从 material_inspection 搜候选(来料子件)。 */
    private List<Map<String, Object>> searchMaterial(String keyword, int limit) {
        List<Map<String, Object>> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT material_barcode AS barcode, material_code AS code, material_name AS name,"
                        + " spec_model AS spec FROM qms.material_inspection WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (material_barcode ILIKE ? OR material_code ILIKE ? OR material_name ILIKE ?)");
            String k = "%" + keyword.trim() + "%";
            args.add(k); args.add(k); args.add(k);
        }
        sql.append(" AND material_barcode IS NOT NULL AND material_barcode <> '' LIMIT ?");
        args.add(limit);
        for (Map<String, Object> r : jdbcTemplate.queryForList(sql.toString(), args.toArray())) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("barcode", str(r.get("barcode")));
            m.put("code", str(r.get("code")));
            m.put("name", str(r.get("name")));
            m.put("spec", str(r.get("spec")));
            m.put("category", "来料");
            m.put("srcType", "material");
            out.add(m);
        }
        return out;
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    @Override
    @Transactional
    public Map<String, Object> create(MaterialBindingCreateRequest req) {
        if (req.getProductBarcode() == null || req.getProductBarcode().isBlank()) {
            throw new BusinessException("父实例条码(product_barcode)必填");
        }
        if (req.getMaterialBarcode() == null || req.getMaterialBarcode().isBlank()) {
            throw new BusinessException("子件条码(material_barcode)必填");
        }
        if (req.getCategory() == null || req.getCategory().isBlank()) {
            throw new BusinessException("子件类别(category)必填");
        }
        // 幂等: 同定位键已存在(未软删)则不允许重复插入
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + TABLE
                        + " WHERE product_barcode = ? AND material_barcode = ? AND category = ? AND is_deleted = '0'",
                Integer.class, req.getProductBarcode(), req.getMaterialBarcode(), req.getCategory());
        if (cnt != null && cnt > 0) {
            throw new BusinessException("该绑定关系已存在(父条码+子条码+类别), 请勿重复新增");
        }

        String now = "now()";
        String user = currentUserId();
        StringBuilder cols = new StringBuilder(
                "category, product_barcode, product_material_no, product_name, work_order_no, work_order_qty,"
                        + " material_barcode, material_code, material_name, spec_model, process_code, process_name,"
                        + " is_active, deactivate_operator, deactivate_time, remark, plant_code, plant_name,"
                        + " created_by, updated_by, is_deleted, version, created_at, updated_at");
        StringBuilder vals = new StringBuilder(
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '1', NULL, NULL, ?, ?, ?, ?, ?, '0', 1, " + now + ", " + now);
        Object[] params = {
                req.getCategory(), req.getProductBarcode(), trimToNull(req.getProductMaterialNo()),
                trimToNull(req.getProductName()), trimToNull(req.getWorkOrderNo()), trimToNull(req.getWorkOrderQty()),
                req.getMaterialBarcode(), trimToNull(req.getMaterialCode()), trimToNull(req.getMaterialName()),
                trimToNull(req.getSpecModel()), trimToNull(req.getProcessCode()), trimToNull(req.getProcessName()),
                trimToNull(req.getRemark()), trimToNull(req.getPlantCode()), trimToNull(req.getPlantName()),
                user, user
        };
        jdbcTemplate.update("INSERT INTO " + TABLE + " (" + cols + ") VALUES (" + vals + ")", params);
        return fetchRow(req.getProductBarcode(), req.getMaterialBarcode(), req.getCategory());
    }

    @Override
    @Transactional
    public Map<String, Object> update(String productBarcode, String materialBarcode, String category,
                                      MaterialBindingUpdateRequest req) {
        Map<String, Object> existing = fetchRow(productBarcode, materialBarcode, category);
        if (existing == null) {
            throw new BusinessException("绑定关系不存在或已删除");
        }
        // 动态 SET: 仅更新非空的传入字段(COALESCE 保留原值)
        List<String> sets = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        for (String col : BUSINESS_COLS) {
            Object v = getField(req, col);
            if (v != null && !String.valueOf(v).isBlank()) {
                sets.add(col + " = ?");
                params.add(String.valueOf(v).trim());
            }
        }
        // category 允许改(业务列), 但定位键保持不变
        if (req.getCategory() != null && !req.getCategory().isBlank()
                && !req.getCategory().equals(category)) {
            sets.add("category = ?");
            params.add(req.getCategory().trim());
        }
        if (sets.isEmpty()) {
            return existing;
        }
        sets.add("updated_by = ?");
        sets.add("updated_at = now()");
        params.add(currentUserId());
        params.add(productBarcode);
        params.add(materialBarcode);
        params.add(category);
        jdbcTemplate.update("UPDATE " + TABLE + " SET " + String.join(", ", sets)
                + " WHERE product_barcode = ? AND material_barcode = ? AND category = ? AND is_deleted = '0'",
                params.toArray());
        return fetchRow(productBarcode, materialBarcode,
                sets.stream().anyMatch(s -> s.startsWith("category =")) ? req.getCategory().trim() : category);
    }

    @Override
    @Transactional
    public void deactivate(String productBarcode, String materialBarcode, String category) {
        int n = jdbcTemplate.update(
                "UPDATE " + TABLE + " SET is_active = '0', deactivate_operator = ?, deactivate_time = now(),"
                        + " updated_by = ?, updated_at = now()"
                        + " WHERE product_barcode = ? AND material_barcode = ? AND category = ? AND is_deleted = '0'",
                currentUserId(), currentUserId(), productBarcode, materialBarcode, category);
        if (n == 0) {
            throw new BusinessException("绑定关系不存在或已删除");
        }
    }

    @Override
    @Transactional
    public void delete(String productBarcode, String materialBarcode, String category) {
        int n = jdbcTemplate.update(
                "UPDATE " + TABLE + " SET is_deleted = '1', updated_by = ?, updated_at = now()"
                        + " WHERE product_barcode = ? AND material_barcode = ? AND category = ? AND is_deleted = '0'",
                currentUserId(), productBarcode, materialBarcode, category);
        if (n == 0) {
            throw new BusinessException("绑定关系不存在或已删除");
        }
    }

    private Map<String, Object> fetchRow(String productBarcode, String materialBarcode, String category) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM " + TABLE
                        + " WHERE product_barcode = ? AND material_barcode = ? AND category = ? AND is_deleted = '0'",
                productBarcode, materialBarcode, category);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Object getField(MaterialBindingUpdateRequest req, String col) {
        switch (col) {
            case "work_order_no": return req.getWorkOrderNo();
            case "product_material_no": return req.getProductMaterialNo();
            case "product_name": return req.getProductName();
            case "work_order_qty": return req.getWorkOrderQty();
            case "material_code": return req.getMaterialCode();
            case "material_name": return req.getMaterialName();
            case "spec_model": return req.getSpecModel();
            case "process_code": return req.getProcessCode();
            case "process_name": return req.getProcessName();
            case "plant_code": return req.getPlantCode();
            case "plant_name": return req.getPlantName();
            case "remark": return req.getRemark();
            default: return null;
        }
    }

    private String trimToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
