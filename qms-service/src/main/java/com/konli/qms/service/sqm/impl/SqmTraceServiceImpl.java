package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmKeyPartSn;
import com.konli.qms.domain.sqm.entity.SqmTraceNode;
import com.konli.qms.domain.sqm.entity.SqmTraceProductDetail;
import com.konli.qms.domain.sqm.entity.SqmTraceRawDetail;
import com.konli.qms.domain.sqm.vo.TraceDirection;
import com.konli.qms.domain.sqm.vo.TraceDirectionNode;
import com.konli.qms.domain.sqm.vo.TraceFullTreeVO;
import com.konli.qms.domain.sqm.vo.TraceLinkRef;
import com.konli.qms.domain.sqm.vo.TraceNodeFullVO;
import com.konli.qms.domain.sqm.vo.TraceNodeSearchVO;
import com.konli.qms.domain.sqm.vo.TraceNodeTreeVO;
import com.konli.qms.domain.sqm.mapper.SqmIncomingLotMapper;
import com.konli.qms.domain.sqm.mapper.SqmKeyPartSnMapper;
import com.konli.qms.domain.sqm.mapper.SqmTraceNodeMapper;
import com.konli.qms.domain.sqm.mapper.SqmTraceProductDetailMapper;
import com.konli.qms.domain.sqm.mapper.SqmTraceRawDetailMapper;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.service.sqm.SqmTraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.konli.qms.domain.sqm.dto.TraceNodeSaveRequest;

@Service
@RequiredArgsConstructor
public class SqmTraceServiceImpl implements SqmTraceService {

    private final SqmIncomingLotMapper sqmIncomingLotMapper;
    private final SqmTraceNodeMapper sqmTraceNodeMapper;
    private final SqmTraceRawDetailMapper sqmTraceRawDetailMapper;
    private final SqmTraceProductDetailMapper sqmTraceProductDetailMapper;
    private final SqmKeyPartSnMapper sqmKeyPartSnMapper;
    private final JdbcTemplate jdbcTemplate;
    private final com.konli.qms.service.support.OrgIdResolver orgIdResolver;

    @Override
    public void saveRelation(String parentBarcode, String childBarcode, String relationType, String orgId) {
        if (parentBarcode == null || childBarcode == null
                || parentBarcode.isBlank() || childBarcode.isBlank()) {
            return;
        }
        // 防自环
        if (parentBarcode.equals(childBarcode)) {
            return;
        }
        if (orgId == null || orgId.isBlank()) {
            orgId = (CompanyContext.get() != null) ? CompanyContext.get().orgId() : null;
        }
        // 幂等跳过:同边已存在则直接返回
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ops.sqm_trace_relation WHERE org_id = ?::uuid"
                        + " AND parent_barcode = ? AND child_barcode = ? AND relation_type = ? AND is_deleted = '0'",
                Integer.class, orgId, parentBarcode, childBarcode, relationType);
        if (cnt != null && cnt > 0) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_trace_relation (org_id, parent_barcode, child_barcode, relation_type, is_deleted, created_at)"
                        + " VALUES (?::uuid, ?, ?, ?, '0', now())",
                orgId, parentBarcode, childBarcode, relationType);
    }

    @Override
    public List<SqmIncomingLot> listLots(String keyword, String supplierId, Boolean iqcPass) {
        LambdaQueryWrapper<SqmIncomingLot> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            w.and(k -> k.like(SqmIncomingLot::getLotNo, kw)
                    .or().like(SqmIncomingLot::getPartNo, kw)
                    .or().like(SqmIncomingLot::getPartName, kw));
        }
        if (supplierId != null && !supplierId.trim().isEmpty()) {
            w.eq(SqmIncomingLot::getSupplierId, supplierId.trim());
        }
        if (iqcPass != null) {
            w.eq(SqmIncomingLot::getIqcPass, iqcPass);
        }
        w.orderByDesc(SqmIncomingLot::getIncomingDate);
        return sqmIncomingLotMapper.selectList(w);
    }

    @Override
    public PageResult<SqmIncomingLot> listLotsPage(String keyword, String supplierId, String orgId, int page, int size) {
        LambdaQueryWrapper<SqmIncomingLot> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            w.and(k -> k.like(SqmIncomingLot::getLotNo, kw)
                    .or().like(SqmIncomingLot::getPartNo, kw)
                    .or().like(SqmIncomingLot::getPartName, kw));
        }
        if (supplierId != null && !supplierId.trim().isEmpty()) {
            w.eq(SqmIncomingLot::getSupplierId, supplierId.trim());
        }
        // 组织隔离: 走查询专用解析器
        // - 普通用户: 强制按登录上下文 orgId 过滤(防越权看别公司)
        // - 管理员: 传了 orgCode/UUID 则解析过滤, 未传则 null(全局视图)
        String resolvedOrg = orgIdResolver.resolveForQuery(orgId);
        if (resolvedOrg != null) {
            w.eq(SqmIncomingLot::getOrgId, resolvedOrg);
        }
        w.orderByDesc(SqmIncomingLot::getIncomingDate);
        IPage<SqmIncomingLot> ip = sqmIncomingLotMapper.selectPage(new Page<>(page, size), w);
        // 批量回填供应商名(sqm_incoming_lot.supplier_id 为外键, supplierName 为非持久化展示字段)
        List<SqmIncomingLot> records = ip.getRecords();
        if (!records.isEmpty()) {
            Map<String, String> supplierNameMap = new HashMap<>();
            try {
                List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                        "SELECT id, name FROM ops.sqm_supplier WHERE is_deleted = false");
                for (Map<String, Object> s : suppliers) {
                    supplierNameMap.put(String.valueOf(s.get("id")), String.valueOf(s.get("name")));
                }
            } catch (EmptyResultDataAccessException ignored) { /* 无供应商时忽略 */ }
            for (SqmIncomingLot lot : records) {
                lot.setSupplierName(lot.getSupplierId() == null ? null : supplierNameMap.get(lot.getSupplierId()));
            }
        }
        return new PageResult<>(records, ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    @Override
    public SqmIncomingLot getLotByLotNo(String lotNo) {
        if (lotNo == null || lotNo.isBlank()) return null;
        return sqmIncomingLotMapper.selectOne(
                new LambdaQueryWrapper<SqmIncomingLot>().eq(SqmIncomingLot::getLotNo, lotNo.trim()));
    }

    @Override
    public java.util.Map<String, Object> getSourceDetail(String sourceType, String key) {
        if (key == null || key.isBlank()) return new HashMap<>();
        String sql;
        switch (sourceType) {
            case "finished":
                sql = "SELECT * FROM qms.finished_goods_inspection WHERE category='成品' AND prod_batch_or_sn = ? LIMIT 1";
                break;
            case "semi":
                sql = "SELECT * FROM qms.finished_goods_inspection WHERE category='半成品' AND prod_batch_or_sn = ? LIMIT 1";
                break;
            case "critical":
                // 绑定表节点可能以 material_barcode(被装子件) / product_barcode(父产品) / work_order_no(工单) / product_material_no(料号聚合) 任一身份命中,
                // 全部 OR 覆盖, 避免子件节点用自身 material_barcode 查不到导致详情为空。
                sql = "SELECT * FROM qms.critical_material_binding WHERE product_barcode = ? OR work_order_no = ? OR material_barcode = ? OR product_material_no = ? LIMIT 1";
                break;
            case "material":
            default:
                // 兼容三类键: 来料条码(material_barcode) / 物料批次号(material_batch_no) / 记录编号(record_no, 即来料批次 lot_no)
                sql = "SELECT * FROM qms.material_inspection WHERE material_barcode = ? OR material_batch_no = ? OR record_no = ? LIMIT 1";
                break;
        }
        try {
            // 按占位符数量动态填充参数(key 重复使用), 兼容 1/2/4 个 ? 的各分支
            int ph = 0;
            for (int i = 0; i < sql.length(); i++) if (sql.charAt(i) == '?') ph++;
            Object[] args = new Object[ph];
            java.util.Arrays.fill(args, key);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
            if (rows.isEmpty()) return new HashMap<>();
            Map<String, Object> src = rows.get(0);
            // 返回数据库原始 snake_case 列名(与 SourceDetailDialog 的字段定义一一对应, 不做 camel 转换)
            Map<String, Object> raw = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : src.entrySet()) {
                raw.put(e.getKey(), e.getValue());
            }
            return raw;
        } catch (EmptyResultDataAccessException e) {
            return new HashMap<>();
        }
    }

    // ====== 方案 B: 源表分页 ======

    @Override
    public PageResult<Map<String, Object>> sourcePage(String type, String keyword, String plantCode, String bizType, int page, int size) {
        if (type == null) type = "all";
        if ("all".equals(type)) {
            // 总表: 分别计数各类型, 返回各表总数(前端按 tab 分别调 material/semi/finished 取数)
            Map<String, Object> counts = new LinkedHashMap<>();
            counts.put("material", countSource("material", null, plantCode));
            counts.put("semi", countSource("semi", null, plantCode));
            counts.put("finished", countSource("finished", null, plantCode));
            List<Map<String, Object>> one = new ArrayList<>();
            one.add(counts);
            return new PageResult<>(one, 1, 1, 1);
        }
        if ("union".equals(type)) {
            // 单张大表全量: 两源表三块 UNION, bizType 可进一步筛选(material/semi/finished)
            return unionPage(keyword, plantCode, bizType, page, size);
        }
        // 单类型分页查询(单表, 统一精简列, 与 union 大表列结构一致)
        String baseSql = sourceBaseSql(type);
        String where = sourceWhere(type, keyword, plantCode);
        String countSql = "SELECT COUNT(*) FROM " + baseSql + " " + where;
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class);
        if (total == null) total = 0;
        int offset = (page - 1) * size;
        String dataSql = singleSelectColumns(type) + " FROM " + baseSql + " " + where
                + " ORDER BY (SELECT 1) LIMIT " + size + " OFFSET " + offset;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql);
        List<Map<String, Object>> camelRows = new ArrayList<>(rows.size());
        for (Map<String, Object> r : rows) {
            Map<String, Object> camel = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : r.entrySet()) {
                camel.put(toCamel(e.getKey()), e.getValue());
            }
            camelRows.add(camel);
        }
        return new PageResult<>(camelRows, total, page, size);
    }

    /**
     * 单表(material/semi/finished)列表统一精简列投影, 与 unionPage 列结构一致:
     * biz_type / biz_key / name / code / batch_no / barcode / qty / unit / node_date / plant_code / plant_name
     */
    private String singleSelectColumns(String type) {
        switch (type) {
            case "material":
                return "SELECT 'material' AS biz_type, COALESCE(material_barcode, material_batch_no) AS biz_key,"
                        + " material_name AS name, material_code AS code, material_batch_no AS batch_no, material_barcode AS barcode,"
                        + " supplier_name AS supplier_name, supplier_code AS supplier_code,"
                        + " inspection_request_no AS inspect_request_no, record_no AS record_no,"
                        + " inspection_result AS inspect_result, inspection_date AS node_date,"
                        + " submitted_qty AS qty, unit AS unit, plant_code AS plant_code, plant_name AS plant_name";
            case "semi":
            case "finished":
                return "SELECT '" + type + "' AS biz_type, prod_batch_or_sn AS biz_key,"
                        + " product_name AS name, material_code AS code, prod_batch_or_sn AS batch_no, prod_batch_or_sn AS barcode,"
                        + " production_order_no AS production_order,"
                        + " inspection_result AS inspect_result, production_date AS node_date,"
                        + " inspected_qty AS qty, unit AS unit, plant_code AS plant_code, plant_name AS plant_name";
            default:
                return "SELECT *";
        }
    }

    /**
     * 单张大表全量: UNION ALL 两源表三块(material / 半成品 / 成品), 统一精简列投影, 服务端分页。
     * 不含 critical_material_binding(建边表, 仅用于追溯关系, 非源表数据)。
     * 每行携带 bizType(bizKey 供详情/追溯) + 公共展示列(name/code/batch_no/barcode/qty/unit/node_date/plant_*)。
     */
    private PageResult<Map<String, Object>> unionPage(String keyword, String plantCode, String bizType, int page, int size) {
        String materialPart =
                "SELECT 'material' AS biz_type, COALESCE(material_barcode, material_batch_no) AS biz_key," +
                " 'material_inspection' AS source_table, material_name AS name, material_code AS code," +
                " material_batch_no AS batch_no, material_barcode AS barcode, submitted_qty AS qty, unit AS unit," +
                " inspection_date AS node_date, plant_code AS plant_code, plant_name AS plant_name" +
                " FROM qms.material_inspection";
        String semiPart =
                "SELECT 'semi' AS biz_type, prod_batch_or_sn AS biz_key, 'finished_goods_inspection' AS source_table," +
                " product_name AS name, material_code AS code, prod_batch_or_sn AS batch_no, prod_batch_or_sn AS barcode," +
                " inspected_qty AS qty, unit AS unit, production_date AS node_date, plant_code AS plant_code, plant_name AS plant_name" +
                " FROM qms.finished_goods_inspection WHERE category='半成品'";
        String finishedPart =
                "SELECT 'finished' AS biz_type, prod_batch_or_sn AS biz_key, 'finished_goods_inspection' AS source_table," +
                " product_name AS name, material_code AS code, prod_batch_or_sn AS batch_no, prod_batch_or_sn AS barcode," +
                " inspected_qty AS qty, unit AS unit, production_date AS node_date, plant_code AS plant_code, plant_name AS plant_name" +
                " FROM qms.finished_goods_inspection WHERE category='成品'";
        String unionSql = materialPart + " UNION ALL " + semiPart + " UNION ALL " + finishedPart;

        // 外层过滤: 来源类型 + 关键字(统一列名)
        List<String> conds = new ArrayList<>();
        if (bizType != null && !bizType.isBlank() && !"all".equals(bizType)) {
            conds.add(" biz_type = " + quote(bizType));
        }
        if (plantCode != null && !plantCode.isBlank()) {
            conds.add(" plant_code = " + quote(plantCode));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            String kwq = quote("%" + kw + "%");
            StringBuilder kwCond = new StringBuilder(" (code ILIKE " + kwq + " OR name ILIKE " + kwq
                    + " OR batch_no ILIKE " + kwq + " OR barcode ILIKE " + kwq);
            // 桥接反查: 成品/被装件关联条码 → 关联到的半成品/成品 prod_batch_or_sn(仅成品/半成品块有该列)
            Set<String> bridge = bridgeBarcodes(kw);
            if (!bridge.isEmpty()) {
                String inList = bridge.stream().map(this::quote).collect(Collectors.joining(","));
                kwCond.append(" OR (source_table = 'finished_goods_inspection' AND batch_no IN (").append(inList).append("))");
            }
            kwCond.append(")");
            conds.add(kwCond.toString());
        }
        String where = "";
        if (!conds.isEmpty()) {
            StringBuilder sb = new StringBuilder(" WHERE");
            for (int i = 0; i < conds.size(); i++) sb.append(i == 0 ? "" : " AND").append(conds.get(i));
            where = sb.toString();
        }
        String wrapped = "SELECT * FROM (" + unionSql + ") _u" + where;
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM (" + unionSql + ") _c" + where, Long.class);
        if (total == null) total = 0L;
        int offset = (page - 1) * size;
        String dataSql = wrapped + " ORDER BY biz_type, batch_no LIMIT " + size + " OFFSET " + offset;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql);
        List<Map<String, Object>> camelRows = new ArrayList<>(rows.size());
        for (Map<String, Object> r : rows) {
            Map<String, Object> camel = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : r.entrySet()) {
                camel.put(toCamel(e.getKey()), e.getValue());
            }
            camelRows.add(camel);
        }
        return new PageResult<>(camelRows, total, page, size);
    }

    private long countSource(String type, String keyword, String plantCode) {
        String baseSql = sourceBaseSql(type);
        String where = sourceWhere(type, keyword, plantCode);
        Integer c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + baseSql + " " + where, Integer.class);
        return c == null ? 0L : c;
    }

    private String sourceBaseSql(String type) {
        switch (type) {
            case "material":
                return "qms.material_inspection";
            case "semi":
                return "qms.finished_goods_inspection WHERE category='半成品'";
            case "finished":
                return "qms.finished_goods_inspection WHERE category='成品'";
            default:
                return "qms.material_inspection";
        }
    }

    /**
     * 桥接反查: 按成品/关键件关联条码(product_barcode / material_barcode)在 critical_material_binding
     * 中找到关联到的源表业务条码(prod_batch_or_sn), 供 semi/finished 列表按"成品/被装件条码"反查出对应源表行。
     * 例: 搜 WBA0125040014(仅存在于绑定表) → 反查出被装件 material_barcode → 命中对应半成品 prod_batch_or_sn。
     * 列表行仍是干净的源表记录, 仅扩展了检索维度; 绑定表自身行不进入列表(避免污染台账语义)。
     */
    private Set<String> bridgeBarcodes(String keyword) {
        Set<String> out = new LinkedHashSet<>();
        if (keyword == null || keyword.trim().isEmpty()) return out;
        String kwq = "%" + keyword.trim() + "%";
        jdbcTemplate.query(
                "SELECT DISTINCT material_barcode FROM qms.critical_material_binding"
                        + " WHERE product_barcode ILIKE ? OR material_barcode ILIKE ?",
                rs -> {
                    String v = rs.getString(1);
                    if (v != null && !v.isBlank()) out.add(v);
                }, kwq, kwq);
        return out;
    }

    private String sourceWhere(String type, String keyword, String plantCode) {
        List<String> conds = new ArrayList<>();
        if (plantCode != null && !plantCode.isBlank()) {
            conds.add(" plant_code = " + quote(plantCode));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            String kwq = quote("%" + kw + "%");
            if (type.equals("material")) {
                conds.add(" (material_code ILIKE " + kwq + " OR material_name ILIKE " + kwq
                        + " OR material_barcode ILIKE " + kwq + " OR material_batch_no ILIKE " + kwq + ")");
            } else {
                // 源表自身三列
                StringBuilder kwCond = new StringBuilder(" (prod_batch_or_sn ILIKE " + kwq
                        + " OR material_code ILIKE " + kwq + " OR product_name ILIKE " + kwq);
                // 桥接反查: 成品/被装件条码 → 关联到的半成品/成品 prod_batch_or_sn
                Set<String> bridge = bridgeBarcodes(kw);
                if (!bridge.isEmpty()) {
                    String inList = bridge.stream().map(this::quote).collect(Collectors.joining(","));
                    kwCond.append(" OR prod_batch_or_sn IN (").append(inList).append(")");
                }
                kwCond.append(")");
                conds.add(kwCond.toString());
            }
        }
        if (conds.isEmpty()) {
            return "";
        }
        boolean hasBaseWhere = type.equals("semi") || type.equals("finished");
        StringBuilder sb = new StringBuilder();
        sb.append(hasBaseWhere ? " AND" : " WHERE");
        for (int i = 0; i < conds.size(); i++) {
            sb.append(i == 0 ? "" : " AND").append(conds.get(i));
        }
        return sb.toString();
    }

    private String quote(String s) {
        return "'" + s.replace("'", "''") + "'";
    }

    private String bizKeyOf(String type, Map<String, Object> camel) {
        switch (type) {
            case "material":
                // 物料条码偶为空时回退物料批次, 保证详情/追溯 key 非空
                return str(camel.get("materialBarcode")).isBlank()
                        ? str(camel.get("materialBatchNo")) : str(camel.get("materialBarcode"));
            case "semi":
            case "finished":
                return str(camel.get("prodBatchOrSn"));
            default:
                return "";
        }
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    @Override
    public SqmTraceNode getIncomingNode(String rootLotId) {
        if (rootLotId == null || rootLotId.isBlank()) return null;
        return sqmTraceNodeMapper.selectOne(new LambdaQueryWrapper<SqmTraceNode>()
                .eq(SqmTraceNode::getRootLotId, rootLotId)
                .eq(SqmTraceNode::getNodeType, "incoming"));
    }

    @Override
    public SqmIncomingLot findExistingLot(String orgId, String partNo, String partName, String supplierId) {
        if (orgId == null || orgId.isBlank()) {
            return null;
        }
        LambdaQueryWrapper<SqmIncomingLot> qw = new LambdaQueryWrapper<SqmIncomingLot>()
                .eq(SqmIncomingLot::getOrgId, orgId);
        if (partNo != null && !partNo.isBlank()) {
            qw.eq(SqmIncomingLot::getPartNo, partNo);
        } else if (partName != null && !partName.isBlank()) {
            qw.eq(SqmIncomingLot::getPartName, partName);
        } else {
            return null;
        }
        if (supplierId != null && !supplierId.isBlank()) {
            qw.eq(SqmIncomingLot::getSupplierId, supplierId);
        }
        return sqmIncomingLotMapper.selectOne(qw);
    }

    @Override
    @Transactional
    public SqmIncomingLot createLot(SqmIncomingLot lot) {
        // 同一公司下批次号唯一:重复入库直接返回已存在批次,避免产生重复来料节点
        if (lot.getOrgId() != null && lot.getLotNo() != null) {
            SqmIncomingLot exist = sqmIncomingLotMapper.selectOne(new LambdaQueryWrapper<SqmIncomingLot>()
                    .eq(SqmIncomingLot::getOrgId, lot.getOrgId())
                    .eq(SqmIncomingLot::getLotNo, lot.getLotNo()));
            if (exist != null) {
                return exist;
            }
        }
        if (lot.getLotNo() == null) {
            lot.setLotNo("LOT-" + System.currentTimeMillis());
        }
        if (lot.getInspectResult() == null) {
            lot.setInspectResult("待检");
        }
        if (lot.getInspectType() == null) {
            lot.setInspectType("正常");
        }
        if (lot.getIncomingDate() == null) {
            lot.setIncomingDate(LocalDate.now());
        }
        sqmIncomingLotMapper.insert(lot);
        // 来料入库自动建 incoming 追溯节点(追溯链起点)
        SqmTraceNode node = new SqmTraceNode();
        node.setOrgId(lot.getOrgId());
        node.setRootLotId(lot.getId());
        node.setNodeType("incoming");
        node.setNodeName(lot.getPartName() != null ? lot.getPartName() : lot.getLotNo());
        node.setBatchNo(lot.getLotNo());
        node.setQty(lot.getQty());
        node.setUnit(lot.getUnit());
        node.setNodeDate(lot.getIncomingDate() != null ? lot.getIncomingDate() : LocalDate.now());
        node.setSupplierId(lot.getSupplierId());
        node.setTreeLevel(0);
        node.setIsValid("是");
        sqmTraceNodeMapper.insert(node);
        return lot;
    }

    @Override
    public List<SqmTraceNode> listNodes(String rootLotId) {
        return sqmTraceNodeMapper.selectList(
                new LambdaQueryWrapper<SqmTraceNode>()
                        .eq(SqmTraceNode::getRootLotId, rootLotId));
    }

    @Override
    @Transactional
    public SqmTraceNode createNode(SqmTraceNode node) {
        if (node.getTreeLevel() == null) {
            node.setTreeLevel(0);
        }
        if (node.getIsValid() == null) {
            node.setIsValid("是");
        }
        sqmTraceNodeMapper.insert(node);
        if (node.getParentNodeId() != null && !node.getParentNodeId().isBlank()) {
            insertLink(node.getParentNodeId(), node.getId(), node.getOrgId(), "compose");
        }
        return node;
    }

    @Override
    public PageResult<TraceNodeSearchVO> searchNodes(String nodeType, String keyword, String orgId, int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 20;
        }

        String selectCols = "SELECT n.id, n.root_lot_id, l.lot_no AS root_lot_no, n.node_type, n.node_name, " +
                "n.batch_no, n.qty, n.unit, n.node_date, n.supplier_id, s.name AS supplier_name, " +
                "n.remark, n.tree_level, n.is_valid ";
        StringBuilder fromWhere = new StringBuilder(
                "FROM ops.sqm_trace_node n " +
                "LEFT JOIN ops.sqm_incoming_lot l ON n.root_lot_id = l.id " +
                "LEFT JOIN ops.sqm_supplier s ON n.supplier_id = s.id " +
                "WHERE n.is_deleted = false");
        List<Object> args = new ArrayList<>();
        if (orgId != null && !orgId.isBlank()) {
            fromWhere.append(" AND n.org_id = CAST(? AS uuid)");
            args.add(orgId);
        }

        if (nodeType != null && !nodeType.isBlank()) {
            List<String> types = new ArrayList<>();
            for (String t : nodeType.split(",")) {
                if (t != null && !t.trim().isBlank()) {
                    types.add(t.trim());
                }
            }
            if (!types.isEmpty()) {
                if (types.size() == 1) {
                    fromWhere.append(" AND n.node_type = ?");
                    args.add(types.get(0));
                } else {
                    fromWhere.append(" AND n.node_type IN (");
                    for (int i = 0; i < types.size(); i++) {
                        if (i > 0) {
                            fromWhere.append(",");
                        }
                        fromWhere.append("?");
                        args.add(types.get(i));
                    }
                    fromWhere.append(")");
                }
            }
        }
        if (keyword != null && !keyword.isBlank()) {
            fromWhere.append(" AND (n.node_name ILIKE ? OR n.batch_no ILIKE ? OR COALESCE(s.name,'') ILIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        String orderBy = " ORDER BY n.node_date DESC NULLS LAST, n.node_type, n.node_name";

        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + fromWhere, args.toArray(), Long.class);
        long totalCount = total == null ? 0L : total;

        int offset = (page - 1) * size;
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(size);
        pageArgs.add(offset);
        List<TraceNodeSearchVO> records = jdbcTemplate.query(
                selectCols + fromWhere + orderBy + " LIMIT ? OFFSET ?",
                (rs, i) -> {
                    TraceNodeSearchVO vo = new TraceNodeSearchVO();
                    vo.setId(rs.getString("id"));
                    vo.setRootLotId(rs.getString("root_lot_id"));
                    vo.setRootLotNo(rs.getString("root_lot_no"));
                    vo.setNodeType(rs.getString("node_type"));
                    vo.setNodeName(rs.getString("node_name"));
                    vo.setBatchNo(rs.getString("batch_no"));
                    vo.setQty(rs.getBigDecimal("qty"));
                    vo.setUnit(rs.getString("unit"));
                    java.sql.Date d = rs.getDate("node_date");
                    vo.setNodeDate(d == null ? null : d.toLocalDate().toString());
                    vo.setSupplierId(rs.getString("supplier_id"));
                    vo.setSupplierName(rs.getString("supplier_name"));
                    vo.setRemark(rs.getString("remark"));
                    vo.setTreeLevel(rs.getObject("tree_level") == null ? null : rs.getInt("tree_level"));
                    vo.setIsValid(rs.getString("is_valid"));
                    return vo;
                },
                pageArgs.toArray());

        return new PageResult<>(records, totalCount, page, size);
    }

    @Override
    public List<SqmTraceNode> traceTree(String rootLotId) {
        // 按 treeLevel ASC 返回扁平列表,前端构建树
        return sqmTraceNodeMapper.selectList(
                new LambdaQueryWrapper<SqmTraceNode>()
                        .eq(SqmTraceNode::getRootLotId, rootLotId)
                        .orderByAsc(SqmTraceNode::getTreeLevel));
    }

    // ==================== 追溯明细 ====================

    @Override
    public SqmTraceRawDetail getRawDetail(String nodeId) {
        return sqmTraceRawDetailMapper.selectOne(
                new LambdaQueryWrapper<SqmTraceRawDetail>()
                        .eq(SqmTraceRawDetail::getNodeId, nodeId));
    }

    @Override
    @Transactional
    public void saveRawDetail(SqmTraceRawDetail detail) {
        SqmTraceRawDetail existing = detail.getNodeId() == null ? null
                : sqmTraceRawDetailMapper.selectOne(
                        new LambdaQueryWrapper<SqmTraceRawDetail>()
                                .eq(SqmTraceRawDetail::getNodeId, detail.getNodeId()));
        if (existing != null) {
            detail.setId(existing.getId());
            sqmTraceRawDetailMapper.updateById(detail);
        } else {
            sqmTraceRawDetailMapper.insert(detail);
        }
    }

    @Override
    public SqmTraceProductDetail getProductDetail(String nodeId) {
        return sqmTraceProductDetailMapper.selectOne(
                new LambdaQueryWrapper<SqmTraceProductDetail>()
                        .eq(SqmTraceProductDetail::getNodeId, nodeId));
    }

    @Override
    @Transactional
    public void saveProductDetail(SqmTraceProductDetail detail) {
        SqmTraceProductDetail existing = detail.getNodeId() == null ? null
                : sqmTraceProductDetailMapper.selectOne(
                        new LambdaQueryWrapper<SqmTraceProductDetail>()
                                .eq(SqmTraceProductDetail::getNodeId, detail.getNodeId()));
        if (existing != null) {
            detail.setId(existing.getId());
            sqmTraceProductDetailMapper.updateById(detail);
        } else {
            sqmTraceProductDetailMapper.insert(detail);
        }
    }

    // ==================== 关键件 SN ====================

    @Override
    public List<SqmKeyPartSn> listKeyPartSn(String lotId) {
        return sqmKeyPartSnMapper.selectList(
                new LambdaQueryWrapper<SqmKeyPartSn>()
                        .eq(SqmKeyPartSn::getLotId, lotId)
                        .orderByAsc(SqmKeyPartSn::getScanTime));
    }

    @Override
    @Transactional
    public void createKeyPartSn(SqmKeyPartSn sn) {
        sqmKeyPartSnMapper.insert(sn);
    }

    // ==================== 递归树 ====================

    /**
     * WITH RECURSIVE 沿 parent_node_id 递归查整棵追溯树。
     * 使用 JdbcTemplate(MyBatis-Plus BaseMapper 不便表达递归 CTE),
     * BeanPropertyRowMapper 自动将 snake_case 列名映射为 camelCase 属性。
     */
    @Override
    public List<SqmTraceNode> traceTreeRecursive(String rootLotId) {
        // 锚点仅取根节点(parent_node_id IS NULL),否则所有同 root_lot_id 的节点会被锚点
        // 与递归各命中一次,导致返回重复行。
        String sql = "WITH RECURSIVE trace_tree AS (" +
                "  SELECT * FROM ops.sqm_trace_node WHERE root_lot_id = ?::uuid AND is_deleted = false" +
                "    AND id NOT IN (SELECT child_node_id FROM ops.sqm_trace_link WHERE is_deleted = false)" +
                "  UNION ALL" +
                "  SELECT n.* FROM ops.sqm_trace_node n" +
                "  JOIN ops.sqm_trace_link l ON l.child_node_id = n.id" +
                "  JOIN trace_tree t ON t.id = l.parent_node_id" +
                "  WHERE n.is_deleted = false" +
                ") SELECT * FROM trace_tree ORDER BY tree_level, node_date";
        return jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(SqmTraceNode.class),
                rootLotId);
    }

    // ==================== 完整追溯树(嵌套 + 明细) ====================

    @Override
    public TraceFullTreeVO getFullTraceTree(String rootLotId) {
        String sql = "WITH RECURSIVE trace_tree AS (" +
                "  SELECT * FROM ops.sqm_trace_node WHERE root_lot_id = ?::uuid AND is_deleted = false" +
                "    AND id NOT IN (SELECT child_node_id FROM ops.sqm_trace_link WHERE is_deleted = false)" +
                "  UNION ALL" +
                "  SELECT n.* FROM ops.sqm_trace_node n" +
                "  JOIN ops.sqm_trace_link l ON l.child_node_id = n.id" +
                "  JOIN trace_tree t ON t.id = l.parent_node_id" +
                "  WHERE n.is_deleted = false" +
                ") SELECT * FROM trace_tree ORDER BY tree_level, node_date";
        List<SqmTraceNode> nodes = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(SqmTraceNode.class), rootLotId);

        // 供应商名映射,供节点展示供应商
        Map<String, String> supplierNameMap = new HashMap<>();
        try {
            List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                    "SELECT id, name FROM ops.sqm_supplier WHERE is_deleted = false");
            for (Map<String, Object> s : suppliers) {
                supplierNameMap.put(String.valueOf(s.get("id")), String.valueOf(s.get("name")));
            }
        } catch (EmptyResultDataAccessException ignored) {
            // 无供应商时忽略
        }

        Map<String, TraceNodeTreeVO> voMap = new LinkedHashMap<>();
        for (SqmTraceNode n : nodes) {
            TraceNodeTreeVO vo = new TraceNodeTreeVO();
            vo.setId(n.getId());
            vo.setRootLotId(n.getRootLotId());
            vo.setParentNodeId(n.getParentNodeId());
            vo.setNodeType(n.getNodeType());
            vo.setNodeName(n.getNodeName());
            vo.setBatchNo(n.getBatchNo());
            vo.setQty(n.getQty());
            vo.setUnit(n.getUnit());
            vo.setNodeDate(n.getNodeDate() == null ? null : n.getNodeDate().toString());
            vo.setSupplierId(n.getSupplierId());
            vo.setSupplierName(n.getSupplierId() == null ? null : supplierNameMap.get(n.getSupplierId()));
            vo.setRemark(n.getRemark());
            vo.setTreeLevel(n.getTreeLevel());
            vo.setIsValid(n.getIsValid());
            vo.setDetail(loadDetail(n));
            vo.setChildren(new ArrayList<>());
            voMap.put(n.getId(), vo);
        }

        TraceNodeTreeVO root = null;
        for (SqmTraceNode n : nodes) {
            TraceNodeTreeVO vo = voMap.get(n.getId());
            if (n.getParentNodeId() == null) {
                root = vo;
            } else {
                TraceNodeTreeVO parent = voMap.get(n.getParentNodeId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }

        TraceFullTreeVO result = new TraceFullTreeVO();
        result.setRootLotId(rootLotId);
        result.setTree(root);
        SqmIncomingLot lot = sqmIncomingLotMapper.selectById(rootLotId);
        if (lot != null) {
            result.setRootLotNo(lot.getLotNo());
            result.setIsKeyPart(lot.getIsKeyPart());
        }
        return result;
    }

    /** 按 nodeType 取对应明细表行(snake_case 列转 camelCase),无明细返回空 Map。 */
    private Map<String, Object> loadDetail(SqmTraceNode n) {
        if (n.getNodeType() == null) {
            return new HashMap<>();
        }
        String table;
        switch (n.getNodeType()) {
            case "raw":
                table = "sqm_trace_raw_detail";
                break;
            case "customer":
                table = "sqm_trace_customer_detail";
                break;
            default: // semi / ship / incoming
                table = "sqm_trace_product_detail";
        }
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT * FROM ops." + table + " WHERE node_id = ?::uuid", n.getId());
            Map<String, Object> camel = new HashMap<>();
            for (Map.Entry<String, Object> e : row.entrySet()) {
                camel.put(toCamel(e.getKey()), e.getValue());
            }
            return camel;
        } catch (EmptyResultDataAccessException e) {
            return new HashMap<>();
        }
    }

    private static String toCamel(String s) {
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char c : s.toCharArray()) {
            if (c == '_') {
                upperNext = true;
                continue;
            }
            sb.append(upperNext ? Character.toUpperCase(c) : c);
            upperNext = false;
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public void batchCreate(SqmIncomingLot lot, List<SqmTraceNode> nodes,
                            List<SqmTraceRawDetail> rawDetails,
                            List<SqmTraceProductDetail> productDetails,
                            List<SqmKeyPartSn> sns) {
        // 1. 验证上游批次:存在性 + 不超量
        for (SqmTraceRawDetail rd : rawDetails) {
            SqmIncomingLot srcLot = sqmIncomingLotMapper.selectOne(
                    new LambdaQueryWrapper<SqmIncomingLot>().eq(SqmIncomingLot::getLotNo, rd.getMaterialCode()));
            if (srcLot == null) {
                throw new BusinessException(400, "批次 " + rd.getMaterialCode() + " 未入库,无法投料");
            }
            BigDecimal remaining = (srcLot.getQty() != null ? srcLot.getQty() : BigDecimal.ZERO)
                    .subtract(srcLot.getUsedQty() != null ? srcLot.getUsedQty() : BigDecimal.ZERO);
            if (rd.getWoQty() != null && rd.getWoQty().compareTo(remaining) > 0) {
                throw new BusinessException(400, "批次 " + rd.getMaterialCode() + " 剩余 " + remaining + ",投料 " + rd.getWoQty() + " 超量");
            }
            // 更新已投数量
            SqmIncomingLot upd = new SqmIncomingLot();
            upd.setId(srcLot.getId());
            upd.setUsedQty((srcLot.getUsedQty() != null ? srcLot.getUsedQty() : BigDecimal.ZERO).add(rd.getWoQty() != null ? rd.getWoQty() : BigDecimal.ZERO));
            sqmIncomingLotMapper.updateById(upd);
        }

        // 2. 建追溯节点
        for (SqmTraceNode node : nodes) {
            node.setIsValid("是");
            sqmTraceNodeMapper.insert(node);
        }

        // 3. 写投料详情(混批:每个上游一行)
        for (SqmTraceRawDetail rd : rawDetails) {
            sqmTraceRawDetailMapper.insert(rd);
        }

        // 4. 写产出详情
        for (SqmTraceProductDetail pd : productDetails) {
            sqmTraceProductDetailMapper.insert(pd);
        }

        // 5. 关键件SN
        for (SqmKeyPartSn sn : sns) {
            sqmKeyPartSnMapper.insert(sn);
        }
    }

    // ==================== 新录入模型(无来料批次依赖) ====================

    private static String genId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** 建一条组成关系(link); 已存在则忽略(唯一约束兜底)。 */
    private void insertLink(String parentId, String childId, String orgId, String linkType) {
        if (parentId == null || childId == null || orgId == null) {
            return;
        }
        jdbcTemplate.update(
                "INSERT INTO ops.sqm_trace_link (org_id, parent_node_id, child_node_id, link_type, created_at) "
                        + "VALUES (?::uuid, ?::uuid, ?::uuid, ?, now()) "
                        + "ON CONFLICT (parent_node_id, child_node_id) DO NOTHING",
                orgId, parentId, childId, linkType);
    }

    /** 删一条组成关系(link)。 */
    private void deleteLink(String parentId, String childId) {
        if (parentId == null || childId == null) {
            return;
        }
        jdbcTemplate.update(
                "UPDATE ops.sqm_trace_link SET is_deleted = true WHERE parent_node_id = ?::uuid AND child_node_id = ?::uuid",
                parentId, childId);
    }

    /** 在 parentToChildren 邻接表中, 判断 from 是否能到达 to(用于多根 DAG 选根)。 */
    private boolean canReach(String from, String to, Map<String, List<String>> parentToChildren) {
        if (from.equals(to)) {
            return true;
        }
        Set<String> visited = new HashSet<>();
        Queue<String> q = new LinkedList<>();
        q.add(from);
        visited.add(from);
        while (!q.isEmpty()) {
            String cur = q.poll();
            for (String c : parentToChildren.getOrDefault(cur, Collections.emptyList())) {
                if (c.equals(to)) {
                    return true;
                }
                if (visited.add(c)) {
                    q.add(c);
                }
            }
        }
        return false;
    }

    /** 取包含 seed 的连通分量(沿 link 上溯到根再下溯); 返回节点 id 集合。 */
    private Set<String> componentIds(String seed) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "WITH RECURSIVE up AS ("
                        + " SELECT id FROM ops.sqm_trace_node WHERE id = ?::uuid AND is_deleted = false"
                        + " UNION ALL"
                        + " SELECT l.parent_node_id FROM up JOIN ops.sqm_trace_link l ON l.child_node_id = up.id WHERE l.is_deleted = false),"
                        + " root_ids AS (SELECT id FROM up WHERE id NOT IN (SELECT child_node_id FROM ops.sqm_trace_link WHERE is_deleted = false)),"
                        + " down AS (SELECT id FROM root_ids UNION ALL SELECT l.child_node_id FROM down JOIN ops.sqm_trace_link l ON l.parent_node_id = down.id WHERE l.is_deleted = false)"
                        + " SELECT id FROM down",
                seed);
        Set<String> ids = new HashSet<>();
        for (Map<String, Object> r : rows) {
            ids.add(String.valueOf(r.get("id")));
        }
        return ids;
    }

    /** 将 uuid 集合拼成 SQL IN 子句(仅用于本系统生成的节点 id, 非外部输入, 安全)。 */
    private String inClause(Set<String> ids) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String id : ids) {
            if (!first) {
                sb.append(",");
            }
            sb.append("'").append(id).append("'");
            first = false;
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public SqmTraceNode saveNode(TraceNodeSaveRequest req) {
        if (req.getNodeType() == null || req.getNodeType().isBlank()) {
            throw new BusinessException(400, "nodeType 必填(semi/ship/customer)");
        }
        if (!List.of("semi", "ship", "customer").contains(req.getNodeType())) {
            throw new BusinessException(400, "nodeType 仅支持 semi/ship/customer");
        }
        if (req.getOrgId() == null || req.getOrgId().isBlank()) {
            throw new BusinessException(400, "orgId 必填");
        }

        String id = genId();
        SqmTraceNode node = new SqmTraceNode();
        node.setId(id);
        node.setOrgId(req.getOrgId());
        node.setNodeType(req.getNodeType());
        node.setNodeName(req.getNodeName());
        node.setBatchNo(req.getBatchNo());
        node.setQty(req.getQty());
        node.setUnit(req.getUnit());
        node.setNodeDate(req.getNodeDate() != null ? req.getNodeDate() : LocalDate.now());
        node.setSupplierId(req.getSupplierId());
        node.setRemark(req.getRemark());
        node.setQualificationType(req.getQualificationType());
        node.setIsValid("是");

        String parentIdForLink = null;
        if (req.getParentNodeId() != null && !req.getParentNodeId().isBlank()) {
            SqmTraceNode parent = sqmTraceNodeMapper.selectById(req.getParentNodeId());
            if (parent == null) {
                throw new BusinessException(400, "父节点不存在: " + req.getParentNodeId());
            }
            node.setParentNodeId(parent.getId());
            node.setRootLotId(parent.getRootLotId());      // 可空(无来料批次时为 null)
            node.setRootNodeId(parent.getRootNodeId());
            node.setTreeLevel((parent.getTreeLevel() != null ? parent.getTreeLevel() : 0) + 1);
            parentIdForLink = parent.getId();
        } else {
            node.setRootLotId(null);
            node.setRootNodeId(id);                         // 自己即为树根
            node.setTreeLevel(0);
        }
        sqmTraceNodeMapper.insert(node);
        insertLink(parentIdForLink, id, node.getOrgId(), "compose");

        writeDetail(node, req);

        if (req.getComponents() != null) {
            for (TraceNodeSaveRequest.ComponentItem c : req.getComponents()) {
                attachComponent(id, c);
            }
        }
        return node;
    }

    private void writeDetail(SqmTraceNode node, TraceNodeSaveRequest req) {
        switch (req.getNodeType()) {
            case "customer":
                saveCustomerDetail(node, req);
                break;
            case "raw":
                SqmTraceRawDetail rd = new SqmTraceRawDetail();
                rd.setNodeId(node.getId());
                rd.setOrgId(node.getOrgId());
                rd.setMaterialCode(req.getMaterialCode());
                rd.setMaterialName(req.getNodeName());
                rd.setSpecModel(req.getModelSpec());
                rd.setWoQty(node.getQty());
                sqmTraceRawDetailMapper.insert(rd);
                break;
            default: // semi / ship
                SqmTraceProductDetail pd = new SqmTraceProductDetail();
                pd.setNodeId(node.getId());
                pd.setOrgId(node.getOrgId());
                pd.setProductName(req.getProductName());
                pd.setMaterialCode(req.getMaterialCode());
                pd.setModelSpec(req.getModelSpec());
                pd.setBatchNo(node.getBatchNo());
                pd.setInspectQty(req.getInspectQty() != null ? req.getInspectQty() : node.getQty());
                pd.setUnit(node.getUnit());
                pd.setProductionOrderNo(req.getProductionOrderNo());
                pd.setProductionDate(req.getProductionDate());
                pd.setInspector(req.getInspector());
                pd.setDrugRegNo(req.getDrugRegNo());
                pd.setPerfInspectMethod(req.getPerfInspectMethod());
                pd.setPerfBatchNo(req.getPerfBatchNo());
                sqmTraceProductDetailMapper.insert(pd);
        }
    }

    private void saveCustomerDetail(SqmTraceNode node, TraceNodeSaveRequest req) {
        String sql = "INSERT INTO ops.sqm_trace_customer_detail " +
                "(id, org_id, node_id, customer_name, customer_code, customer_order_no, " +
                " ship_date, tracking_no, ship_address, contact_person, contact_phone, qty, unit) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,
                genId(),
                node.getOrgId(),
                node.getId(),
                req.getCustomerName(),
                req.getCustomerCode(),
                req.getCustomerOrderNo(),
                req.getShipDate(),
                req.getTrackingNo(),
                req.getShipAddress(),
                req.getContactPerson(),
                req.getContactPhone(),
                req.getQty(),
                req.getUnit());
    }

    @Override
    @Transactional
    public SqmTraceNode attachComponent(String parentId, TraceNodeSaveRequest.ComponentItem c) {
        SqmTraceNode parent = sqmTraceNodeMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException(400, "父节点不存在: " + parentId);
        }
        String componentType = c.getComponentType();
        if (componentType == null || componentType.isBlank()) {
            componentType = "raw";
        }

        // 引用已有节点: 不新建, 直接建组成关系(link), 实现多对多(被多个父复用)
        if (c.getRefNodeId() != null && !c.getRefNodeId().isBlank()) {
            SqmTraceNode ref = sqmTraceNodeMapper.selectById(c.getRefNodeId());
            if (ref == null) {
                throw new BusinessException(400, "引用的节点不存在: " + c.getRefNodeId());
            }
            insertLink(parent.getId(), ref.getId(), parent.getOrgId(), "compose");
            return ref;
        }

        String id = genId();
        SqmTraceNode child = new SqmTraceNode();
        child.setId(id);
        child.setOrgId(parent.getOrgId());
        child.setNodeType(componentType);
        child.setParentNodeId(parent.getId());
        child.setRootLotId(parent.getRootLotId());
        child.setRootNodeId(parent.getRootNodeId());
        child.setTreeLevel((parent.getTreeLevel() != null ? parent.getTreeLevel() : 0) + 1);
        child.setIsValid("是");
        child.setNodeDate(LocalDate.now());

        if ("semi".equals(componentType)) {
            if (c.getSourceNodeId() == null || c.getSourceNodeId().isBlank()) {
                throw new BusinessException(400, "semi 组成必须提供 sourceNodeId");
            }
            SqmTraceNode src = sqmTraceNodeMapper.selectById(c.getSourceNodeId());
            if (src == null) {
                throw new BusinessException(400, "引用的半成品节点不存在: " + c.getSourceNodeId());
            }
            child.setNodeName(src.getNodeName());
            child.setBatchNo(src.getBatchNo());
            child.setQty(c.getUsageQty());
            child.setUnit(c.getUnit());
            child.setSupplierId(src.getSupplierId());
            sqmTraceNodeMapper.insert(child);
            SqmTraceProductDetail pd = new SqmTraceProductDetail();
            pd.setNodeId(child.getId());
            pd.setOrgId(parent.getOrgId());
            pd.setProductName(src.getNodeName());
            pd.setMaterialCode(src.getBatchNo());
            pd.setBatchNo(src.getBatchNo());
            pd.setInspectQty(c.getUsageQty());
            pd.setUnit(c.getUnit());
            sqmTraceProductDetailMapper.insert(pd);
        } else {
            child.setNodeName(c.getMaterialName());
            child.setBatchNo(c.getMaterialCode());
            child.setQty(c.getUsageQty());
            child.setUnit(c.getUnit());
            sqmTraceNodeMapper.insert(child);
            SqmTraceRawDetail rd = new SqmTraceRawDetail();
            rd.setNodeId(child.getId());
            rd.setOrgId(parent.getOrgId());
            rd.setMaterialCode(c.getMaterialCode());
            rd.setMaterialName(c.getMaterialName());
            rd.setSpecModel(c.getSpecModel());
            rd.setWoQty(c.getUsageQty());
            rd.setProcessName(c.getProcessName());
            sqmTraceRawDetailMapper.insert(rd);
            // 防超卖: 实时校验并扣减物料批次已用量, 用量不得超过批次剩余库存
            deductLotUsage(parent.getOrgId(), c.getMaterialCode(), c.getUsageQty());
        }
        insertLink(parent.getId(), child.getId(), parent.getOrgId(), "compose");
        return child;
    }

    /**
     * 来料用量防超卖: 校验所选物料批次剩余库存, 并实时累加 usedQty 记账, 防止超卖。
     * 仅对来自批次(lotNo)的来料生效; 非批次来源(外部/手工批次)放行由业务层把关。
     */
    private void deductLotUsage(String orgId, String lotNo, BigDecimal usage) {
        if (usage == null || usage.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (lotNo == null || lotNo.isBlank()) {
            return;
        }
        SqmIncomingLot lot = sqmIncomingLotMapper.selectOne(
                new LambdaQueryWrapper<SqmIncomingLot>()
                        .eq(SqmIncomingLot::getOrgId, orgId)
                        .eq(SqmIncomingLot::getLotNo, lotNo.trim()));
        if (lot == null) {
            return;
        }
        BigDecimal used = lot.getUsedQty() == null ? BigDecimal.ZERO : lot.getUsedQty();
        BigDecimal total = lot.getQty() == null ? BigDecimal.ZERO : lot.getQty();
        BigDecimal remain = total.subtract(used);
        if (usage.compareTo(remain) > 0) {
            throw new BusinessException(400,
                    String.format("来料批次 %s 库存不足, 存在超卖风险: 剩余 %s, 本次用量 %s", lotNo, remain, usage));
        }
        lot.setUsedQty(used.add(usage));
        sqmIncomingLotMapper.updateById(lot);
    }

    @Override
    public TraceFullTreeVO getFullTraceTreeByRootNode(String rootNodeId) {
        // 不依赖 root_node_id / parent_node_id 字段, 改为沿 sqm_trace_link 上溯到根再下溯整棵连通分量(支持多父 DAG)
        Set<String> comp = componentIds(rootNodeId);
        if (comp.isEmpty()) {
            TraceFullTreeVO empty = new TraceFullTreeVO();
            empty.setRootNodeId(rootNodeId);
            return empty;
        }
        String in = inClause(comp);
        List<SqmTraceNode> nodes = jdbcTemplate.query(
                "SELECT n.* FROM ops.sqm_trace_node n WHERE n.id IN (" + in + ") AND n.is_deleted = false ORDER BY n.tree_level, n.node_date",
                new BeanPropertyRowMapper<>(SqmTraceNode.class));
        List<Map<String, Object>> edges = jdbcTemplate.queryForList(
                "SELECT l.parent_node_id AS parent_id, l.child_node_id AS child_id FROM ops.sqm_trace_link l"
                        + " WHERE l.is_deleted = false AND l.parent_node_id IN (" + in + ") AND l.child_node_id IN (" + in + ")");

        Map<String, String> supplierNameMap = new HashMap<>();
        try {
            List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                    "SELECT id, name FROM ops.sqm_supplier WHERE is_deleted = false");
            for (Map<String, Object> s : suppliers) {
                supplierNameMap.put(String.valueOf(s.get("id")), String.valueOf(s.get("name")));
            }
        } catch (EmptyResultDataAccessException ignored) {
            // 无供应商时忽略
        }
        Map<String, TraceNodeTreeVO> voMap = new LinkedHashMap<>();
        for (SqmTraceNode n : nodes) {
            TraceNodeTreeVO vo = new TraceNodeTreeVO();
            vo.setId(n.getId());
            vo.setRootLotId(n.getRootLotId());
            vo.setParentNodeId(n.getParentNodeId());
            vo.setNodeType(n.getNodeType());
            vo.setNodeName(n.getNodeName());
            vo.setBatchNo(n.getBatchNo());
            vo.setQty(n.getQty());
            vo.setUnit(n.getUnit());
            vo.setNodeDate(n.getNodeDate() == null ? null : n.getNodeDate().toString());
            vo.setSupplierId(n.getSupplierId());
            vo.setSupplierName(n.getSupplierId() == null ? null : supplierNameMap.get(n.getSupplierId()));
            vo.setRemark(n.getRemark());
            vo.setTreeLevel(n.getTreeLevel());
            vo.setIsValid(n.getIsValid());
            vo.setDetail(loadDetail(n));
            vo.setChildren(new ArrayList<>());
            voMap.put(n.getId(), vo);
        }
        Set<String> childIds = new HashSet<>();
        Map<String, List<String>> parentToChildren = new HashMap<>();
        for (Map<String, Object> e : edges) {
            String pid = String.valueOf(e.get("parent_id"));
            String cid = String.valueOf(e.get("child_id"));
            childIds.add(cid);
            parentToChildren.computeIfAbsent(pid, k -> new ArrayList<>()).add(cid);
            TraceNodeTreeVO p = voMap.get(pid);
            TraceNodeTreeVO c = voMap.get(cid);
            if (p != null && c != null && !p.getChildren().contains(c)) {
                p.getChildren().add(c);
                c.setParentNodeId(pid);
            }
        }
        // 选根: 无 incoming 边且能到达 rootNodeId 的节点; 兜底 rootNodeId
        String chosen = null;
        for (String id : voMap.keySet()) {
            if (!childIds.contains(id) && canReach(id, rootNodeId, parentToChildren)) {
                chosen = id;
                break;
            }
        }
        if (chosen == null) {
            chosen = voMap.containsKey(rootNodeId) ? rootNodeId
                    : (voMap.isEmpty() ? null : voMap.keySet().iterator().next());
        }
        TraceFullTreeVO result = new TraceFullTreeVO();
        result.setRootNodeId(rootNodeId);
        result.setTree(voMap.get(chosen));
        return result;
    }

    @Override
    public List<SqmTraceNode> traceTreeByRootNode(String rootNodeId) {
        // 不依赖 root_node_id 字段, 改为沿 sqm_trace_link 取包含 rootNodeId 的连通分量(扁平返回)
        Set<String> comp = componentIds(rootNodeId);
        if (comp.isEmpty()) {
            return new ArrayList<>();
        }
        String in = inClause(comp);
        return jdbcTemplate.query(
                "SELECT n.* FROM ops.sqm_trace_node n WHERE n.id IN (" + in + ") AND n.is_deleted = false ORDER BY n.tree_level, n.node_date",
                new BeanPropertyRowMapper<>(SqmTraceNode.class));
    }

    @Override
    public List<SqmTraceNode> listRoots(String orgId) {
        // 根 = 没有任何 incoming link 的节点
        return jdbcTemplate.query(
                "SELECT * FROM ops.sqm_trace_node WHERE org_id = ? AND is_deleted = false"
                        + " AND id NOT IN (SELECT child_node_id FROM ops.sqm_trace_link WHERE is_deleted = false)"
                        + " ORDER BY node_date DESC, created_at DESC",
                new BeanPropertyRowMapper<>(SqmTraceNode.class), orgId);
    }

    @Override
    public TraceFullTreeVO traceMesTree(String barcode, String orgId) {
        return traceMesTree(barcode, orgId, TraceDirection.ALL);
    }

    @Override
    public TraceFullTreeVO traceMesTree(String barcode, String orgId, TraceDirection direction) {
        if (barcode == null || barcode.isBlank()) {
            return new TraceFullTreeVO();
        }
        TraceFullTreeVO vo = new TraceFullTreeVO();
        vo.setRootNodeId(barcode);
        vo.setRootLotId(barcode);
        vo.setRootLotNo(barcode);
        TraceNodeTreeVO root = buildNode(barcode);
        if (root == null) {
            root = new TraceNodeTreeVO();
            root.setId(barcode);
            root.setNodeName(barcode);
            root.setBatchNo(barcode);
            root.setNodeType("unknown");
        }
        vo.setTree(root);
        // 共享 visited: 纯 barcode 去重, 跨方向也生效, 彻底切断上下游互绕导致的环。
        Set<String> visited = new HashSet<>();
        // nodeCache: 同一条码只回查一次三源表, 被多个父节点引用时直接复用, 极大减少重复 SQL。
        Map<String, TraceNodeTreeVO> nodeCache = new HashMap<>();
        nodeCache.put(barcode, root);
        // 下游树(构成): 产品料号→成品实例→关键物料→物料批次
        if (direction == TraceDirection.FORWARD || direction == TraceDirection.ALL) {
            expand(root, true, visited, nodeCache);
            // 成品节点下游挂固定“终端客户(待补)”占位: 成品表无客户字段, 仅展示用。
            attachCustomerPlaceholder(root);
        }
        // 上游树(来源): 用独立的根节点对象(深拷贝本节点), 避免与下游树共享同一对象引用
        // 而把下游 children 也带进上游、造成上下游镜像重复。
        if (direction == TraceDirection.BACKWARD || direction == TraceDirection.ALL) {
            TraceNodeTreeVO upRoot = cloneNode(root);
            visited.add(barcode); // 根自身在上游不再重复展开
            expand(upRoot, false, visited, nodeCache);
            vo.setUpTree(upRoot);
        }
        return vo;
    }

    /** 单节点下游/上游扇出硬上限, 防止枢纽条码把子树拉爆。 */
    private static final int MAX_FANOUT = 500;
    /** 全局节点数硬上限(含 buildNode 计数), 兜底防失控。 */
    private static final int MAX_NODES = 800;
    /** 追溯树最大展开深度。 */
    private static final int MAX_DEPTH = 6;

    /** 浅克隆节点(复制基础字段, 清空 children), 用于上下游树各自持有独立根对象。 */
    private TraceNodeTreeVO cloneNode(TraceNodeTreeVO src) {
        if (src == null) return null;
        TraceNodeTreeVO n = new TraceNodeTreeVO();
        n.setId(src.getId());
        n.setBatchNo(src.getBatchNo());
        n.setNodeName(src.getNodeName());
        n.setNodeType(src.getNodeType());
        n.setMaterialCode(src.getMaterialCode());
        n.setQty(src.getQty());
        n.setUnit(src.getUnit());
        n.setSupplierName(src.getSupplierName());
        n.setNodeDate(src.getNodeDate());
        n.setRemark(src.getRemark());
        n.setDetail(src.getDetail());
        n.setChildren(new ArrayList<>());
        return n;
    }

    /** 遍历下游树, 给每个成品(finished)节点追加一个固定的“终端客户(待补)”占位子节点(成品表无客户字段)。 */
    private void attachCustomerPlaceholder(TraceNodeTreeVO node) {
        if (node == null || node.getChildren() == null) return;
        if ("finished".equals(node.getNodeType())) {
            TraceNodeTreeVO customer = new TraceNodeTreeVO();
            customer.setId(node.getId() + "::customer");
            customer.setBatchNo(node.getId() + "::customer");
            customer.setNodeName("终端客户(待补)");
            customer.setNodeType("virtualCustomer");
            customer.setChildren(new ArrayList<>());
            node.getChildren().add(customer);
        }
        for (TraceNodeTreeVO c : node.getChildren()) {
            attachCustomerPlaceholder(c);
        }
    }

    /**
     * 按层 BFS 批量展开: 每层收集待展开条码 → 批量查邻居边(downstreamBatch/upstreamBatch)
     * → 批量回查三源表(buildNodesBatch) → 组装 children 进入下一层。
     * 边只来自绑定表(product_material_no/material_barcode → product_barcode,
     * product_barcode → material_barcode/product_material_no), son_lot_no 仅用于产品详情不作为树边。
     * forward=true 为构成(向下): 码作为料号/物料查其下产品实例(物料→成品/半成品→更上层产品);
     * forward=false 为来源(向上): 产品实例查其用料/所属料号。
     * visited 为整树共享的纯 barcode 去重(跨方向), nodeCache 复用已回查节点。
     */
    private void expand(TraceNodeTreeVO root, boolean forward, Set<String> visited,
                         Map<String, TraceNodeTreeVO> nodeCache) {
        List<TraceNodeTreeVO> layer = new ArrayList<>();
        layer.add(root);
        String rootBarcode = root.getBatchNo();
        if (rootBarcode != null && !rootBarcode.isBlank()) {
            visited.add(rootBarcode);
        }
        int totalNodes = 1;
        for (int depth = 0; depth < MAX_DEPTH; depth++) {
            if (layer.isEmpty()) break;
            // 1) 本层待展开条码(去重)
            Set<String> layerBarcodes = new LinkedHashSet<>();
            for (TraceNodeTreeVO n : layer) {
                String b = n.getBatchNo();
                if (b != null && !b.isBlank()) layerBarcodes.add(b);
            }
            if (layerBarcodes.isEmpty()) break;
            // 2) 批量查邻居
            Map<String, List<String>> neighbors = forward ? downstreamBatch(layerBarcodes) : upstreamBatch(layerBarcodes);
            // 3) 批量回查三源表建节点: 本层 + 邻居层(下一层)一起建, 保证子节点带完整详情
            Set<String> toBuild = new LinkedHashSet<>(layerBarcodes);
            for (List<String> nbs : neighbors.values()) {
                for (String nb : nbs) {
                    if (nb != null && !nb.isBlank()) toBuild.add(nb);
                }
            }
            Map<String, TraceNodeTreeVO> built = buildNodesBatch(toBuild);
            nodeCache.putAll(built);
            // 4) 组装 children, 准备下一层
            List<TraceNodeTreeVO> nextLayer = new ArrayList<>();
            for (TraceNodeTreeVO n : layer) {
                String b = n.getBatchNo();
                List<String> nbs = neighbors.getOrDefault(b, Collections.emptyList());
                int added = 0;
                for (String nb : nbs) {
                    if (nb == null || nb.isBlank() || nb.equals(b)) continue;
                    if (added >= MAX_FANOUT) break;
                    if (visited.contains(nb)) continue;
                    visited.add(nb);
                    TraceNodeTreeVO child = nodeCache.get(nb);
                    if (child == null) {
                        child = new TraceNodeTreeVO();
                        child.setId(nb);
                        child.setBatchNo(nb);
                        child.setNodeName(nb);
                        child.setNodeType("unknown");
                    }
                    n.getChildren().add(child);
                    added++;
                    // 产品料号聚合节点(productNo, 如 80.02.010100)只作"上游来源归属"展示, 不再向下展开,
                    // 否则会拉出同料号下所有其他批次产品(如 209 台血气电解质分析仪), 污染"按批号查该批次产品"的结果。
                    boolean isProductNo = "productNo".equals(child.getNodeType());
                    if (!isProductNo && totalNodes < MAX_NODES) {
                        nextLayer.add(child);
                        totalNodes++;
                    }
                }
            }
            layer = nextLayer;
        }
    }

    /**
     * 批量下游(构成)邻居: 一次 SQL 查整层条码。边只来自绑定表两类关系:
     *  - 码作为产品料号 product_material_no → 该料号所有成品实例 product_barcode(查料号再查下去)
     *  - 码作为产品/半成品实例 product_barcode → 它使用的物料 material_barcode(半成品则继续展开)
     *  (注意: son_lot_no 仅用于产品详情查询, 不作为树边, 避免批次号造成的环状爆炸)
     */
    private Map<String, List<String>> downstreamBatch(Set<String> barcodes) {
        Map<String, List<String>> map = new HashMap<>();
        if (barcodes.isEmpty()) return map;
        queryInto(map, "product_material_no", "product_barcode", barcodes);
        queryInto(map, "product_barcode", "material_barcode", barcodes, true);
        return map;
    }

    /**
     * 批量上游(来源)邻居:
     *  - 码作为物料/半成品 material_barcode → 使用它的产品实例 product_barcode(谁用了它, 半成品链继续向上)
     *  - 码作为产品实例 product_barcode → 它所属的产品料号 product_material_no(来源归属)
     * 注意: 绝不在上游查 product_barcode→material_barcode(那是构成/下游边), 否则上下游镜像重复。
     */
    private Map<String, List<String>> upstreamBatch(Set<String> barcodes) {
        Map<String, List<String>> map = new HashMap<>();
        if (barcodes.isEmpty()) return map;
        queryInto(map, "material_barcode", "product_barcode", barcodes);
        queryInto(map, "product_barcode", "product_material_no", barcodes);
        return map;
    }

    /** 批量查 binding 表 colK→colV 邻居并合并入 map(每 key 去重并截断 MAX_FANOUT)。
     *  useNameFallback=true 时(仅用于 product_barcode→material_barcode 构成边): 当 material_barcode 为空,
     *  改用 material_name 作为合成 key 保留该子件(源表缺失 material_barcode 字段, 前端标注"无 material_barcode"),
     *  避免"有数据但条码字段缺失"的来料/半成品子件从追溯树消失。 */
    private void queryInto(Map<String, List<String>> map, String colK, String colV, Set<String> keys,
                           boolean useNameFallback) {
        if (keys.isEmpty()) return;
        String in = inPlaceholders(keys.size());
        String vSql = useNameFallback
                ? "(CASE WHEN " + colV + " IS NULL OR " + colV + " = '' THEN material_name ELSE " + colV + " END) AS v"
                : colV + " AS v";
        String sql = "SELECT DISTINCT " + colK + " AS k, " + vSql + " FROM qms.critical_material_binding"
                + " WHERE " + colK + " IN (" + in + ")";
        if (!useNameFallback) {
            sql += " AND " + colV + " IS NOT NULL AND " + colV + " <> ''";
        }
        jdbcTemplate.query(sql, rs -> {
            String k = rs.getString("k");
            String v = rs.getString("v");
            if (k != null && v != null && !v.isBlank()) {
                List<String> l = map.computeIfAbsent(k, x -> new ArrayList<>());
                if (l.size() < MAX_FANOUT && !l.contains(v)) l.add(v);
            }
        }, keys.toArray());
    }

    /** queryInto 便捷重载(无 name 兜底)。 */
    private void queryInto(Map<String, List<String>> map, String colK, String colV, Set<String> keys) {
        queryInto(map, colK, colV, keys, false);
    }

    /** 生成 n 个 ? 占位符(IN 子句)。 */
    private String inPlaceholders(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        return sb.toString();
    }

    /**
     * 批量回查三源表组装节点信息(一次 SQL 查整层)。优先级(只决定"从哪张表取行",
     * 节点类型一律由行内的 category 字段决定, 不再按表名猜):
     *  1) finished_goods_inspection.prod_batch_or_sn(成品/半成品实例, 带 category)
     *  2) critical_material_binding.product_material_no(产品料号聚合, category 取 product_name)
     *  3) critical_material_binding.product_barcode(成品实例, 用绑定行 category)
     *  4) material_inspection.material_barcode(来料物料, 无 category → 来料)
     *  5) critical_material_binding.material_barcode(关键物料, 用绑定行 category)
     *  6) material_inspection.material_batch_no(物料批次 → lot)
     * 高优先级命中后不再查低优先级。
     */
    private Map<String, TraceNodeTreeVO> buildNodesBatch(Set<String> barcodes) {
        Map<String, TraceNodeTreeVO> map = new LinkedHashMap<>();
        if (barcodes.isEmpty()) return map;
        // 成品/半成品表: category 直接决定 成品/半成品
        fillDistinct(map, "qms.finished_goods_inspection", "prod_batch_or_sn", barcodes, false, false, false);
        Set<String> rest = new LinkedHashSet<>(barcodes);
        rest.removeAll(map.keySet());
        // 绑定表 product_material_no 聚合节点: 产品料号(用 product_name 作名称, 此列在绑定表即自身名)
        fillDistinct(map, "qms.critical_material_binding", "product_material_no", rest, true, false, false);
        rest.removeAll(map.keySet());
        // 绑定表 material_barcode: 优先于 product_barcode! 一个条码在绑定表里可能既是"被装件"(material_barcode)
        // 又是"装配者"(product_barcode)。被装件身份(category=半成品/来料)才是它在父产品构成里的真实角色,
        // 必须先取, 否则会被 product_barcode 步取到它装配的某个子件行(可能 category=来料)而误标类型/错名。
        fillDistinct(map, "qms.critical_material_binding", "material_barcode", rest, false, false, true);
        rest.removeAll(map.keySet());
        // 绑定表 product_barcode: 兜底(主表无此条码、且未作为被装件出现时). 该行列的 product_name 是"父产品名",
        // 非自身名, bindingFallback=true 让名称取 material_name/自身。
        fillDistinct(map, "qms.critical_material_binding", "product_barcode", rest, false, false, true);
        rest.removeAll(map.keySet());
        // 物料表 material_barcode: 兜底(绑定表也无此条码时), 来料(无 category 列, 默认 material)
        fillDistinct(map, "qms.material_inspection", "material_barcode", rest, false, true, false);
        rest.removeAll(map.keySet());
        // 物料批次
        fillDistinct(map, "qms.material_inspection", "material_batch_no", rest, false, true, false);
        rest.removeAll(map.keySet());
        // 源表缺失 material_barcode 的子件: 上游/下游邻居以 material_name 作合成 key 兜底进入 rest,
        // 此处按 material_name 从绑定表查 material_barcode 为空的行, 生成节点并标注 noBarcode=true(不编造条码)。
        fillByNameFallback(map, rest);
        return map;
    }

    /** 按 material_name 从 binding 表取 material_barcode 为空的行(源表缺字段), 生成节点并标注 noBarcode。
     *  这些 key 由 downstreamBatch/upstreamBatch 的 name fallback 产生, 真实 barcode 已被前面步骤命中, 不会误伤。 */
    private void fillByNameFallback(Map<String, TraceNodeTreeVO> map, Set<String> names) {
        if (names.isEmpty()) return;
        String in = inPlaceholders(names.size());
        String sql = "SELECT DISTINCT ON (material_name) * FROM qms.critical_material_binding"
                + " WHERE material_name IN (" + in + ") AND (material_barcode IS NULL OR material_barcode = '')";
        jdbcTemplate.query(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            java.sql.ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            Object raw = row.get("material_name");
            if (raw == null) return;
            String name = String.valueOf(raw);
            if (map.containsKey(name)) return;
            String nodeType = categoryToNodeType(String.valueOf(row.get("category")));
            TraceNodeTreeVO n = toTreeNode(row, nodeType, name, true);
            n.setDetailSource("binding");
            n.setNoBarcode(true);
            n.setBatchNo(null); // 缺失字段, 不编造
            if (n.getNodeName() == null || n.getNodeName().equals(name)) n.setNodeName(name);
            map.put(name, n);
        }, names.toArray());
    }

    /** 单个条码回查(根节点用, 语义与批量版完全一致)。 */
    private TraceNodeTreeVO buildNode(String barcode) {
        if (barcode == null || barcode.isBlank()) return null;
        Set<String> one = new LinkedHashSet<>();
        one.add(barcode);
        return buildNodesBatch(one).get(barcode);
    }

    /**
     * 从 table 按 keyCol 批量取行(DISTINCT ON 保证每 key 一行), 转 TraceNodeTreeVO 写入 map(putIfAbsent)。
     * nodeType 由行内 category 字段决定(categoryToNodeType); 仅当 forceMaterial=true(物料表无 category 列)
     * 或行无 category 时回退到 material。isProductNo=true 时节点为产品料号聚合节点: 名称显示「产品名称 (料号)」。
     * bindingFallback=true 表示此行来自绑定表兜底: 该表 product_name 是"父产品名"而非自身名, 名称须取自身名(material_name)。
     */
    private void fillDistinct(Map<String, TraceNodeTreeVO> map, String table, String keyCol,
                              Set<String> keys, boolean isProductNo, boolean forceMaterial,
                              boolean bindingFallback) {
        if (keys.isEmpty()) return;
        String in = inPlaceholders(keys.size());
        String sql = "SELECT DISTINCT ON (" + keyCol + ") * FROM " + table + " WHERE " + keyCol + " IN (" + in + ")";
        jdbcTemplate.query(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            java.sql.ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                row.put(md.getColumnLabel(i), rs.getObject(i));
            }
            Object raw = row.get(keyCol);
            if (raw == null) return;
            String bc = String.valueOf(raw);
            String nodeType = forceMaterial ? "material" : categoryToNodeType(String.valueOf(row.get("category")));
            TraceNodeTreeVO n = toTreeNode(row, nodeType, bc, bindingFallback);
            // 标注详情来源表(前端如实告知用户, 不编造): 绑定表兜底节点来源为 binding
            if (isProductNo) {
                n.setDetailSource("product_no");
            } else if (bindingFallback) {
                n.setDetailSource("binding");
            } else if (table.contains("finished_goods_inspection")) {
                n.setDetailSource("finished_goods_inspection");
            } else if (table.contains("material_inspection")) {
                n.setDetailSource("material_inspection");
            } else {
                n.setDetailSource("binding");
            }
            if (isProductNo) {
                Object pn = n.getDetail() == null ? null : n.getDetail().get("productName");
                n.setNodeName((pn == null ? bc : String.valueOf(pn)) + " (" + bc + ")");
            }
            map.putIfAbsent(bc, n);
        }, keys.toArray());
    }

    /** 源表 category 文本 → 节点类型。成品→finished / 半成品→semi / 来料→material / 空或其他→material。 */
    private String categoryToNodeType(String category) {
        if (category == null) return "material";
        switch (category.trim()) {
            case "成品": return "finished";
            case "半成品": return "semi";
            case "来料": return "material";
            default: return "material";
        }
    }

    /** 通用: 把源表行转成 TraceNodeTreeVO(节点基础信息 + 全字段 detail)。
     *  bindingFallback=true 时(来源为绑定表兜底行): 该表 product_name 是父产品名而非自身名,
     *  名称优先取 material_name(自身物料名)/nodeName, 避免把父名当节点名。 */
    private TraceNodeTreeVO toTreeNode(Map<String, Object> row, String nodeType, String barcode,
                                       boolean bindingFallback) {
        TraceNodeTreeVO n = new TraceNodeTreeVO();
        n.setId(barcode);
        n.setBatchNo(barcode);
        n.setNodeType(nodeType);
        Map<String, Object> camel = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : row.entrySet()) {
            camel.put(toCamel(e.getKey()), e.getValue());
        }
        n.setDetail(camel);
        // 名称/编码/数量/单位/日期/供应商 抽提
        Object name;
        if (bindingFallback) {
            // 绑定表兜底: 自身名优先(materialName / nodeName), product_name 是父名不用
            name = camel.get("materialName");
            if (name == null) name = camel.get("nodeName");
            if (name == null) name = camel.get("productName");
        } else {
            name = camel.get("productName");
            if (name == null) name = camel.get("materialName");
            if (name == null) name = camel.get("nodeName");
        }
        n.setNodeName(name == null ? barcode : String.valueOf(name));
        Object code = camel.get("materialCode");
        n.setMaterialCode(code == null ? null : String.valueOf(code));
        Object qty = camel.get("qualifiedQty");
        if (qty == null) qty = camel.get("inspectedQty");
        if (qty == null) qty = camel.get("submittedQty");
        if (qty != null) {
            try { n.setQty(new BigDecimal(String.valueOf(qty))); } catch (Exception ignored) {}
        }
        Object unit = camel.get("unit");
        n.setUnit(unit == null ? null : String.valueOf(unit));
        Object sup = camel.get("supplierName");
        n.setSupplierName(sup == null ? null : String.valueOf(sup));
        Object date = camel.get("productionDate");
        if (date == null) date = camel.get("inspectionDate");
        if (date == null) date = camel.get("arrivalDate");
        n.setNodeDate(date == null ? null : String.valueOf(date));
        return n;
    }

    @Override
    public List<TraceFullTreeVO> traceByBatchNo(String batchNo, String orgId, TraceDirection direction) {
        List<TraceFullTreeVO> forest = new ArrayList<>();
        if (batchNo == null || batchNo.isBlank()) return forest;
        // 批号可能为: 成品/半成品 prod_batch_or_sn, 或来料 material_batch_no / material_barcode
        Set<String> seeds = new LinkedHashSet<>();
        try {
            seeds.addAll(jdbcTemplate.queryForList(
                    "SELECT prod_batch_or_sn FROM qms.finished_goods_inspection WHERE prod_batch_or_sn = ? OR production_order_no = ?",
                    String.class, batchNo, batchNo));
        } catch (EmptyResultDataAccessException ignored) {}
        try {
            seeds.addAll(jdbcTemplate.queryForList(
                    "SELECT material_barcode FROM qms.material_inspection WHERE material_batch_no = ? OR material_barcode = ?",
                    String.class, batchNo, batchNo));
        } catch (EmptyResultDataAccessException ignored) {}
        if (seeds.isEmpty()) {
            // 当作业务条码直接追溯
            seeds.add(batchNo);
        }
        for (String seed : seeds) {
            forest.add(traceMesTree(seed, orgId, direction));
        }
        return forest;
    }

    @Override
    public List<String> listProductionOrders(String orgId, String keyword, Integer limit) {
        // 工装派工 / 不良登记等场景的"工单号"下拉数据源:
        // 真实生产工单号来自 MES 落地宽表(analyze2026.finished_goods_inspection.production_order_no
        // 与 analyze2026.critical_material_binding.work_order_no),二者并集去重。
        // ops.sqm_trace_node 为废表(无数据),不可作为数据源。MES 宽表无 org_id,故不做组织隔离。
        // keyword 可选(前缀/包含匹配,防前端一次性渲染 2.8 万条 DOM 卡死);limit 默认 200 上限保护。
        int top = (limit == null || limit <= 0) ? 200 : Math.min(limit, 500);
        StringBuilder sql = new StringBuilder("SELECT DISTINCT wo FROM (")
                .append("SELECT production_order_no AS wo FROM analyze2026.finished_goods_inspection ")
                .append("WHERE production_order_no IS NOT NULL AND production_order_no <> '' ");
        List<Object> args = new java.util.ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND production_order_no ILIKE ? ");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append("UNION SELECT work_order_no AS wo FROM analyze2026.critical_material_binding ")
                .append("WHERE work_order_no IS NOT NULL AND work_order_no <> '' ");
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND work_order_no ILIKE ? ");
            args.add("%" + keyword.trim() + "%");
        }
        sql.append(") t ORDER BY wo ASC LIMIT ?");
        args.add(top);
        try {
            return jdbcTemplate.queryForList(sql.toString(), String.class, args.toArray());
        } catch (EmptyResultDataAccessException e) {
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public List<TraceFullTreeVO> traceByLotNo(String lotNo, String orgId, TraceDirection direction) {
        List<TraceFullTreeVO> forest = new ArrayList<>();
        if (lotNo == null || lotNo.isBlank()) return forest;
        // 批号兼容两类: 来料批次号(material_inspection.material_batch_no / record_no → material_barcode)
        // 与成品/半成品批号(finished_goods_inspection.prod_batch_or_sn, 直接作为根条码)。
        // 无论输哪种批号, 都能定位到源表业务条码并展开追溯树(查该批次对应的产品)。
        Set<String> seeds = new LinkedHashSet<>();
        try {
            seeds.addAll(jdbcTemplate.queryForList(
                    "SELECT material_barcode FROM qms.material_inspection WHERE material_batch_no = ? OR record_no = ?",
                    String.class, lotNo, lotNo));
        } catch (EmptyResultDataAccessException ignored) {}
        try {
            seeds.addAll(jdbcTemplate.queryForList(
                    "SELECT prod_batch_or_sn FROM qms.finished_goods_inspection WHERE prod_batch_or_sn = ? OR production_order_no = ?",
                    String.class, lotNo, lotNo));
        } catch (EmptyResultDataAccessException ignored) {}
        if (seeds.isEmpty()) {
            // 当作业务条码直接追溯(兼容用户直接粘贴 material_barcode 等)
            seeds.add(lotNo);
        }
        for (String seed : seeds) {
            forest.add(traceMesTree(seed, orgId, direction));
        }
        return forest;
    }

    @Override
    public List<TraceDirectionNode> traceDirection(String nodeId, String direction) {
        List<TraceDirectionNode> result = new ArrayList<>();
        if (nodeId == null || nodeId.isBlank()) {
            return result;
        }
        Set<String> comp = componentIds(nodeId);
        if (comp.isEmpty()) {
            comp = new HashSet<>();
            comp.add(nodeId);
        }
        String in = inClause(comp);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT n.id, n.node_type, n.node_name, n.batch_no, n.unit, n.node_date, n.supplier_name, n.is_valid"
                        + " FROM ops.sqm_trace_node n WHERE n.id IN (" + in + ") AND n.is_deleted = false");
        for (Map<String, Object> r : rows) {
            TraceDirectionNode n = new TraceDirectionNode();
            n.setId(String.valueOf(r.get("id")));
            n.setNodeType(String.valueOf(r.get("node_type")));
            n.setNodeName(String.valueOf(r.get("node_name")));
            n.setBatchNo(r.get("batch_no") == null ? null : String.valueOf(r.get("batch_no")));
            n.setUnit(r.get("unit") == null ? null : String.valueOf(r.get("unit")));
            n.setNodeDate(r.get("node_date") == null ? null : String.valueOf(r.get("node_date")));
            n.setSupplierName(r.get("supplier_name") == null ? null : String.valueOf(r.get("supplier_name")));
            n.setIsValid(r.get("is_valid") == null ? null : String.valueOf(r.get("is_valid")));
            result.add(n);
        }
        return result;
    }

    @Override
    public TraceNodeFullVO getNodeDetail(String nodeId) {
        TraceNodeFullVO vo = new TraceNodeFullVO();
        if (nodeId == null || nodeId.isBlank()) {
            return vo;
        }
        SqmTraceNode node = sqmTraceNodeMapper.selectById(nodeId);
        vo.setNode(node);
        if (node != null) {
            vo.setDetail(loadDetail(node));
            // 上游组成(parent)
            List<TraceLinkRef> parents = jdbcTemplate.query(
                    "SELECT c.id, c.node_type, c.node_name, c.batch_no FROM ops.sqm_trace_link l"
                            + " JOIN ops.sqm_trace_node c ON c.id = l.parent_node_id"
                            + " WHERE l.child_node_id = ?::uuid AND l.is_deleted = false",
                    (rs, i) -> {
                        TraceLinkRef r = new TraceLinkRef();
                        r.setId(rs.getString("id"));
                        r.setNodeType(rs.getString("node_type"));
                        r.setNodeName(rs.getString("node_name"));
                        r.setBatchNo(rs.getString("batch_no"));
                        return r;
                    }, nodeId);
            // 下游组成(child)
            List<TraceLinkRef> children = jdbcTemplate.query(
                    "SELECT p.id, p.node_type, p.node_name, p.batch_no FROM ops.sqm_trace_link l"
                            + " JOIN ops.sqm_trace_node p ON p.id = l.child_node_id"
                            + " WHERE l.parent_node_id = ?::uuid AND l.is_deleted = false",
                    (rs, i) -> {
                        TraceLinkRef r = new TraceLinkRef();
                        r.setId(rs.getString("id"));
                        r.setNodeType(rs.getString("node_type"));
                        r.setNodeName(rs.getString("node_name"));
                        r.setBatchNo(rs.getString("batch_no"));
                        return r;
                    }, nodeId);
            vo.setParents(parents);
            vo.setChildren(children);
        }
        return vo;
    }

    @Override
    public TraceFullTreeVO getTraceTreeFromNode(String nodeId) {
        // 以任意节点为根,复用 getFullTraceTreeByRootNode 的连通分量组装逻辑
        return getFullTraceTreeByRootNode(nodeId);
    }
}
