package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.entity.SqmKeyPartSn;
import com.konli.qms.domain.sqm.entity.SqmTraceNode;
import com.konli.qms.domain.sqm.entity.SqmTraceProductDetail;
import com.konli.qms.domain.sqm.entity.SqmTraceRawDetail;
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
import com.konli.qms.service.sqm.SqmTraceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import com.konli.qms.domain.sqm.dto.TraceNodeSaveRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqmTraceServiceImpl implements SqmTraceService {

    private final SqmIncomingLotMapper sqmIncomingLotMapper;
    private final SqmTraceNodeMapper sqmTraceNodeMapper;
    private final SqmTraceRawDetailMapper sqmTraceRawDetailMapper;
    private final SqmTraceProductDetailMapper sqmTraceProductDetailMapper;
    private final SqmKeyPartSnMapper sqmKeyPartSnMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SqmIncomingLot> listLots(String keyword) {
        LambdaQueryWrapper<SqmIncomingLot> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            qw.and(w -> w.like(SqmIncomingLot::getLotNo, like)
                         .or().like(SqmIncomingLot::getPartNo, like)
                         .or().like(SqmIncomingLot::getPartName, like));
        }
        List<SqmIncomingLot> lots = sqmIncomingLotMapper.selectList(qw);
        if (!lots.isEmpty()) {
            Map<String, String> supplierNameMap = buildSupplierNameMap();
            for (SqmIncomingLot lot : lots) {
                lot.setSupplierName(lot.getSupplierId() == null ? null : supplierNameMap.get(lot.getSupplierId()));
            }
        }
        return lots;
    }

    @Override
    public SqmIncomingLot getLotByLotNo(String lotNo) {
        if (lotNo == null || lotNo.isBlank()) return null;
        return sqmIncomingLotMapper.selectOne(
                new LambdaQueryWrapper<SqmIncomingLot>().eq(SqmIncomingLot::getLotNo, lotNo.trim()));
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SqmIncomingLot createLot(SqmIncomingLot lot) {
        // 将哨兵值 "ROOT" 解析为真实组织 UUID
        lot.setOrgId(resolveOrgId(lot.getOrgId()));
        // 同一公司下批次号唯一:重复入库直接返回已存在批次,避免产生重复来料节点
        if (lot.getOrgId() != null && lot.getLotNo() != null) {
            SqmIncomingLot exist = sqmIncomingLotMapper.selectOne(new LambdaQueryWrapper<SqmIncomingLot>()
                    .eq(SqmIncomingLot::getOrgId, lot.getOrgId())
                    .eq(SqmIncomingLot::getLotNo, lot.getLotNo()));
            if (exist != null) {
                throw new BusinessException(409, "来料批次 " + lot.getLotNo() + " 已入库, 请勿重复创建");
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
        node.setMaterialCode(lot.getPartNo());
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
        // 将哨兵值 "ROOT" 解析为真实组织 UUID
        node.setOrgId(resolveOrgId(node.getOrgId()));
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

        String resolvedOrgId = resolveOrgId(orgId);

        String selectCols = "SELECT n.id, n.root_lot_id, l.lot_no AS root_lot_no, n.node_type, n.node_name, " +
                "n.batch_no, n.material_code, n.qty, n.unit, n.node_date, n.supplier_id, s.name AS supplier_name, " +
                "n.remark, n.tree_level, n.is_valid ";
        StringBuilder fromWhere = new StringBuilder(
                "FROM ops.sqm_trace_node n " +
                "LEFT JOIN ops.sqm_incoming_lot l ON n.root_lot_id = l.id " +
                "LEFT JOIN ops.sqm_supplier s ON n.supplier_id = s.id " +
                "LEFT JOIN ops.sqm_trace_product_detail pd ON pd.node_id = n.id " +
                "WHERE n.is_deleted = false");
        List<Object> args = new ArrayList<>();
        if (resolvedOrgId != null && !resolvedOrgId.isBlank()) {
            fromWhere.append(" AND n.org_id = CAST(? AS uuid)");
            args.add(resolvedOrgId);
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
            fromWhere.append(" AND (n.node_name ILIKE ? OR n.batch_no ILIKE ? OR n.material_code ILIKE ? OR COALESCE(s.name,'') ILIKE ? OR COALESCE(pd.product_name,'') ILIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
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
        vo.setMaterialCode(rs.getString("material_code"));
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
        // 1) 收集连通节点 id: 沿 link 上溯到根再下溯, 用路径数组防环(避免 link 成环导致 CTE 死循环)
        String idSql = "WITH RECURSIVE up AS (" +
                "  SELECT id, ARRAY[id] AS path FROM ops.sqm_trace_node" +
                "    WHERE root_lot_id = ?::uuid AND is_deleted = false" +
                "  UNION ALL" +
                "  SELECT l.parent_node_id, up.path || l.parent_node_id" +
                "    FROM up JOIN ops.sqm_trace_link l ON l.child_node_id = up.id" +
                "    WHERE l.is_deleted = false AND NOT (l.parent_node_id = ANY(up.path)))," +
                " root_ids AS (SELECT id FROM up WHERE id NOT IN (SELECT child_node_id FROM ops.sqm_trace_link WHERE is_deleted = false))," +
                " down AS (" +
                "  SELECT id, ARRAY[id] AS path FROM root_ids" +
                "  UNION ALL" +
                "  SELECT l.child_node_id, down.path || l.child_node_id" +
                "    FROM down JOIN ops.sqm_trace_link l ON l.parent_node_id = down.id" +
                "    WHERE l.is_deleted = false AND NOT (l.child_node_id = ANY(down.path)))" +
                " SELECT id FROM down";
        List<Map<String, Object>> idRows = jdbcTemplate.queryForList(idSql, rootLotId);
        if (idRows.isEmpty()) {
            // 回退: rootLotId 实际可能是 incoming_lot 表主键(id), 而该批次的追溯数据挂在同 batch_no 的 trace node 上。
            // 通过 lot_no = batch_no 反查对应节点, 再按节点追溯整棵连通树, 避免列表"追溯"落空只显示单节点。
            try {
                Map<String, Object> nodeRow = jdbcTemplate.queryForMap(
                        "SELECT n.id FROM ops.sqm_trace_node n JOIN ops.sqm_incoming_lot l ON n.batch_no = l.lot_no"
                                + " WHERE l.id = ?::uuid AND n.is_deleted = false LIMIT 1", rootLotId);
                return getFullTraceTreeByRootNode(String.valueOf(nodeRow.get("id")));
            } catch (EmptyResultDataAccessException noNode) {
                // 无关联追溯节点, 走下方空树兜底
            }
            TraceFullTreeVO empty = new TraceFullTreeVO();
            empty.setRootLotId(rootLotId);
            SqmIncomingLot lot0 = sqmIncomingLotMapper.selectById(rootLotId);
            if (lot0 != null) {
                empty.setRootLotNo(lot0.getLotNo());
                empty.setIsKeyPart(lot0.getIsKeyPart());
            }
            return empty;
        }
        Set<String> ids = new HashSet<>();
        for (Map<String, Object> r : idRows) {
            ids.add(String.valueOf(r.get("id")));
        }
        String in = inClause(ids);
        List<SqmTraceNode> nodes = jdbcTemplate.query(
                "SELECT n.* FROM ops.sqm_trace_node n WHERE n.id IN (" + in + ") AND n.is_deleted = false ORDER BY n.tree_level, n.node_date",
                new BeanPropertyRowMapper<>(SqmTraceNode.class));

        // 供应商名映射,供节点展示供应商
        Map<String, String> supplierNameMap = new HashMap<>();
        try {
            List<Map<String, Object>> suppliers = querySupplierRows();
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
            vo.setMaterialCode(n.getMaterialCode());
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

        // 2) 纯 link 建边: 将 child 挂到其所有 link 父的 children(支持多父 DAG, 不再吞边)
        List<Map<String, Object>> edges = jdbcTemplate.queryForList(
                "SELECT l.parent_node_id AS parent_id, l.child_node_id AS child_id FROM ops.sqm_trace_link l"
                        + " WHERE l.is_deleted = false AND l.parent_node_id IN (" + in + ") AND l.child_node_id IN (" + in + ")");
        Set<String> childIds = new HashSet<>();
        for (Map<String, Object> e : edges) {
            String pid = String.valueOf(e.get("parent_id"));
            String cid = String.valueOf(e.get("child_id"));
            childIds.add(cid);
            TraceNodeTreeVO p = voMap.get(pid);
            TraceNodeTreeVO c = voMap.get(cid);
            if (p != null && c != null && !p.getChildren().contains(c)) {
                p.getChildren().add(c);
                c.setParentNodeId(pid);
            }
        }
        // 3) 选根: 连通集内无入边(不在 childIds)者; 兜底取第一个
        String chosen = null;
        for (String id : voMap.keySet()) {
            if (!childIds.contains(id)) {
                chosen = id;
                break;
            }
        }
        if (chosen == null) {
            chosen = voMap.keySet().iterator().next();
        }
        TraceNodeTreeVO root = voMap.get(chosen);

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

    private static boolean isValidUuid(String s) {
        if (s == null) return false;
        try {
            java.util.UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
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

    /**
     * 判断 candidateId 是否为 ancestorId 的下游节点(沿 sqm_trace_link 向下递归)。
     * 用于挂载前防御: 若被挂节点(parent)已是引用节点(ref)的下游, 新增 link 会形成环。
     */
    private boolean isDescendantOf(String ancestorId, String candidateId) {
        Integer cnt = jdbcTemplate.queryForObject(
                "WITH RECURSIVE d AS (" +
                "  SELECT child_node_id FROM ops.sqm_trace_link WHERE parent_node_id = ?::uuid AND is_deleted = false" +
                "  UNION ALL" +
                "  SELECT l.child_node_id FROM ops.sqm_trace_link l JOIN d ON d.child_node_id = l.parent_node_id WHERE l.is_deleted = false" +
                ") SELECT COUNT(*) FROM d WHERE child_node_id = ?::uuid",
                Integer.class, ancestorId, candidateId);
        return cnt != null && cnt > 0;
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
                        + " SELECT id, ARRAY[id] AS path FROM ops.sqm_trace_node WHERE id = ?::uuid AND is_deleted = false"
                        + " UNION ALL"
                        + " SELECT l.parent_node_id, up.path || l.parent_node_id FROM up JOIN ops.sqm_trace_link l ON l.child_node_id = up.id"
                        + "   WHERE l.is_deleted = false AND NOT (l.parent_node_id = ANY(up.path))),"
                        + " root_ids AS (SELECT id FROM up WHERE id NOT IN (SELECT child_node_id FROM ops.sqm_trace_link WHERE is_deleted = false)),"
                        + " down AS (SELECT id, ARRAY[id] AS path FROM root_ids"
                        + "   UNION ALL SELECT l.child_node_id, down.path || l.child_node_id FROM down JOIN ops.sqm_trace_link l ON l.parent_node_id = down.id"
                        + "   WHERE l.is_deleted = false AND NOT (l.child_node_id = ANY(down.path)))"
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
            throw new BusinessException(400, "nodeType 必填(incoming/raw/semi/ship/customer)");
        }
        if (!List.of("incoming", "raw", "semi", "ship", "customer").contains(req.getNodeType())) {
            throw new BusinessException(400, "nodeType 仅支持 incoming/raw/semi/ship/customer");
        }
        // 将哨兵值 "ROOT" 解析为真实组织 UUID
        String resolvedOrgId = resolveOrgId(req.getOrgId());
        if (resolvedOrgId == null || resolvedOrgId.isBlank()) {
            throw new BusinessException(400, "orgId 必填");
        }
        req.setOrgId(resolvedOrgId);

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
        node.setMaterialCode(req.getMaterialCode());
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
            // 根节点: 允许挂载到来料批次下
            node.setRootLotId(req.getRootLotId());
            node.setRootNodeId(id);                         // 自己即为树根
            node.setTreeLevel(0);
        }
        sqmTraceNodeMapper.insert(node);
        // 环防御(防御性, 对齐 attachComponent): 若所选父已是本节点下游, 会形成循环
        if (parentIdForLink != null && isDescendantOf(id, parentIdForLink)) {
            throw new BusinessException(400, "目标父节点已是该节点的下游, 挂载会形成循环, 请先解除原有关系");
        }
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
            default: // incoming / semi / ship (都写入产品明细)
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

    /**
     * 为 parent 挂载/创建子节点(组成关系)。
     * DAG 模式: sqm_trace_link 是父子关系的唯一真相源(支持多父); parent_node_id 仅作去规范化"主父指针"。
     * 跨聚合根挂载时, 会把 ref 整棵下游子树 reroot 到新父(加父级 = 移动整个子树)。
     */
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
            if (!parent.getOrgId().equals(ref.getOrgId())) {
                throw new BusinessException(400, "不能跨组织引用节点");
            }
            // 防御: 防止节点挂载到自己
            if (parent.getId().equals(ref.getId())) {
                throw new BusinessException(400, "不能将节点挂载到自身");
            }
            // 防御(深层环): 若 parent 本就是 ref 的下游, 新增 link(parent→ref) 会形成环(A→..→B→A)
            if (isDescendantOf(ref.getId(), parent.getId())) {
                throw new BusinessException(400, "目标节点已是该节点的下游, 挂载会形成循环, 请先解除原有关系");
            }
            // 防御: 检测直接反向链接(A→B 且 B→A)
            Integer reverseCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM ops.sqm_trace_link" +
                    " WHERE parent_node_id = ?::uuid AND child_node_id = ?::uuid AND is_deleted = false",
                    Integer.class, ref.getId(), parent.getId());
            if (reverseCount != null && reverseCount > 0) {
                throw new BusinessException(400, "节点已存在相反的组成关系（逆向链接），无法重复挂载，请先解除原有关系");
            }
            // 不再 reroot 迁移整棵子树: 按被引用节点类型分别处理, 原树始终保留(避免"添加已有节点却搬走整棵树")
            if (isMaterialNode(ref.getNodeType())) {
                // 物料(来料/物料行): 仅建立多父 link 共享, 不复制、不搬移原树
                BigDecimal refUsage = c.getUsageQty();
                if (refUsage != null && refUsage.signum() > 0) {
                    if (isMaterialNode(parent.getNodeType()) && parent.getBatchNo() != null) {
                        deductLotUsage(parent.getOrgId(), parent.getBatchNo(), refUsage);
                        if (parent.getQty() != null) {
                            BigDecimal left = parent.getQty().subtract(refUsage);
                            jdbcTemplate.update(
                                    "UPDATE ops.sqm_trace_node SET qty = ?::numeric, updated_at = NOW() WHERE id = ?::uuid AND is_deleted = false",
                                    left.signum() < 0 ? BigDecimal.ZERO : left, parent.getId());
                        }
                    }
                    if (isMaterialNode(ref.getNodeType()) && ref.getBatchNo() != null) {
                        deductLotUsage(parent.getOrgId(), ref.getBatchNo(), refUsage);
                        if (ref.getQty() != null) {
                            BigDecimal left = ref.getQty().subtract(refUsage);
                            jdbcTemplate.update(
                                    "UPDATE ops.sqm_trace_node SET qty = ?::numeric, updated_at = NOW() WHERE id = ?::uuid AND is_deleted = false",
                                    left.signum() < 0 ? BigDecimal.ZERO : left, ref.getId());
                        }
                    }
                }
                insertLink(parent.getId(), ref.getId(), parent.getOrgId(), "compose");
                // 同步去规范化主父指针: 同根内给原本无父的独立节点加父时, 仅插 link 会导致 parent_node_id 与 link 表漂移
                String parentRoot = parent.getRootNodeId() != null ? parent.getRootNodeId() : parent.getId();
                String refRoot = ref.getRootNodeId() != null ? ref.getRootNodeId() : ref.getId();
                if (parentRoot.equals(refRoot) && ref.getParentNodeId() == null) {
                    jdbcTemplate.update(
                            "UPDATE ops.sqm_trace_node SET parent_node_id = ?::uuid, updated_at = NOW() WHERE id = ?::uuid AND is_deleted = false",
                            parent.getId(), ref.getId());
                }
                return ref;
            } else {
                // 去重: 父节点下已存在同 batch_no + 同 node_type 的直接子节点时复用, 不再克隆(避免重复 link)
                if (ref.getBatchNo() != null) {
                    List<String> existIds = jdbcTemplate.query(
                            "SELECT c.id FROM ops.sqm_trace_link l"
                            + " JOIN ops.sqm_trace_node c ON c.id = l.child_node_id"
                            + " WHERE l.parent_node_id = ?::uuid AND l.is_deleted = false AND c.is_deleted = false"
                            + " AND c.node_type = ? AND c.batch_no = ? LIMIT 1",
                            (rs, i) -> rs.getString("id"),
                            parent.getId(), ref.getNodeType(), ref.getBatchNo());
                    if (existIds != null && !existIds.isEmpty()) {
                        return sqmTraceNodeMapper.selectById(existIds.get(0));
                    }
                }
                // 半成品/成品等: 复制为新子节点(新 UUID), 原节点及其子树保留不搬移
                String newId = genId();
                SqmTraceNode clone = new SqmTraceNode();
                clone.setId(newId);
                clone.setOrgId(parent.getOrgId());
                clone.setNodeType(ref.getNodeType());
                clone.setParentNodeId(parent.getId());
                clone.setRootLotId(parent.getRootLotId());
                clone.setRootNodeId(parent.getRootNodeId());
                clone.setTreeLevel((parent.getTreeLevel() != null ? parent.getTreeLevel() : 0) + 1);
                clone.setIsValid(ref.getIsValid() != null ? ref.getIsValid() : "是");
                clone.setNodeName(ref.getNodeName());
                clone.setBatchNo(ref.getBatchNo());
                clone.setMaterialCode(ref.getMaterialCode());
                clone.setQty(c.getUsageQty());
                clone.setUnit(c.getUnit());
                clone.setNodeDate(LocalDate.now());
                clone.setSupplierId(ref.getSupplierId());
                sqmTraceNodeMapper.insert(clone);
                // 复制被引半成品/成品的明细(若有)
                if ("semi".equals(ref.getNodeType()) || "ship".equals(ref.getNodeType())) {
                    SqmTraceProductDetail pd = sqmTraceProductDetailMapper.selectOne(
                            new LambdaQueryWrapper<SqmTraceProductDetail>().eq(SqmTraceProductDetail::getNodeId, ref.getId()));
                    if (pd != null) {
                        SqmTraceProductDetail npd = new SqmTraceProductDetail();
                        npd.setNodeId(newId);
                        npd.setOrgId(parent.getOrgId());
                        npd.setProductName(pd.getProductName());
                        npd.setMaterialCode(pd.getMaterialCode());
                        npd.setBatchNo(pd.getBatchNo());
                        npd.setInspectQty(c.getUsageQty());
                        npd.setUnit(c.getUnit());
                        sqmTraceProductDetailMapper.insert(npd);
                    }
                }
                insertLink(parent.getId(), newId, parent.getOrgId(), "compose");
                return clone;
            }
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
            child.setMaterialCode(src.getMaterialCode());
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
            // 物料号: 由所耗来料批次(lotNo)反查零件号; 找不到则退回批次号, 保证节点行有物料号
            SqmIncomingLot srcLot = sqmIncomingLotMapper.selectOne(
                    new LambdaQueryWrapper<SqmIncomingLot>()
                            .eq(SqmIncomingLot::getOrgId, parent.getOrgId())
                            .eq(SqmIncomingLot::getLotNo, c.getMaterialCode()));
            child.setMaterialCode(srcLot != null ? srcLot.getPartNo() : c.getMaterialCode());
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

    /** 是否为来料/物料类节点(挂接时会消耗其来源批次库存)。 */
    private boolean isMaterialNode(String nodeType) {
        return "raw".equals(nodeType) || "incoming".equals(nodeType);
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
            List<Map<String, Object>> suppliers = querySupplierRows();
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
            vo.setMaterialCode(n.getMaterialCode());
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

    /** 从 nodeId 沿 sqm_trace_link(parent->child) 递归取全部下游(含自身),用于"以选中节点为根向下延伸"。 */
    private Set<String> descendantIds(String seed) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "WITH RECURSIVE down AS ("
                        + " SELECT id, ARRAY[id] AS path FROM ops.sqm_trace_node WHERE id = ?::uuid AND is_deleted = false"
                        + " UNION ALL"
                        + " SELECT l.child_node_id, down.path || l.child_node_id FROM down JOIN ops.sqm_trace_link l ON l.parent_node_id = down.id"
                        + "   WHERE l.is_deleted = false AND NOT (l.child_node_id = ANY(down.path)))"
                        + " SELECT id FROM down",
                seed);
        Set<String> ids = new HashSet<>();
        for (Map<String, Object> r : rows) {
            ids.add(String.valueOf(r.get("id")));
        }
        return ids;
    }

    /** 从 nodeId 沿 sqm_trace_link(child->parent) 递归取全部上游(含自身),用于"上游组成"树。 */
    private Set<String> ancestorIds(String seed) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "WITH RECURSIVE up AS ("
                        + " SELECT id, ARRAY[id] AS path FROM ops.sqm_trace_node WHERE id = ?::uuid AND is_deleted = false"
                        + " UNION ALL"
                        + " SELECT l.parent_node_id, up.path || l.parent_node_id FROM up JOIN ops.sqm_trace_link l ON l.child_node_id = up.id"
                        + "   WHERE l.is_deleted = false AND NOT (l.parent_node_id = ANY(up.path)))"
                        + " SELECT id FROM up",
                seed);
        Set<String> ids = new HashSet<>();
        for (Map<String, Object> r : rows) {
            ids.add(String.valueOf(r.get("id")));
        }
        return ids;
    }

    /** 按 id 集合加载节点并转 VO(children 置空,由调用方挂边)。 */
    private Map<String, TraceNodeTreeVO> loadVoMap(Set<String> ids, Map<String, String> supplierNameMap) {
        Map<String, TraceNodeTreeVO> voMap = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return voMap;
        }
        String in = inClause(ids);
        List<SqmTraceNode> nodes = jdbcTemplate.query(
                "SELECT n.* FROM ops.sqm_trace_node n WHERE n.id IN (" + in + ") AND n.is_deleted = false ORDER BY n.tree_level, n.node_date",
                new BeanPropertyRowMapper<>(SqmTraceNode.class));
        for (SqmTraceNode n : nodes) {
            TraceNodeTreeVO vo = new TraceNodeTreeVO();
            vo.setId(n.getId());
            vo.setRootLotId(n.getRootLotId());
            vo.setParentNodeId(n.getParentNodeId());
            vo.setNodeType(n.getNodeType());
            vo.setNodeName(n.getNodeName());
            vo.setBatchNo(n.getBatchNo());
            vo.setMaterialCode(n.getMaterialCode());
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
        return voMap;
    }

    /** 查询 id 集合内部的链路边(parent_id -> child_id)。 */
    private List<Map<String, Object>> edgesWithin(Set<String> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        String in = inClause(ids);
        return jdbcTemplate.queryForList(
                "SELECT l.parent_node_id AS parent_id, l.child_node_id AS child_id FROM ops.sqm_trace_link l"
                        + " WHERE l.is_deleted = false AND l.parent_node_id IN (" + in + ") AND l.child_node_id IN (" + in + ")");
    }

    /**
     * 以 nodeId 为根,同时返回上下游两棵树:
     *  - tree:下游去向树(选中节点 → 半成品 → 成品 → 客户…),children = 流向的下游;
     *  - upTree:上游组成树(选中节点 ← 半成品 ← 来料…),children = 组成它的上游来源(BOM 方向)。
     * 支持以来料/半成品/成品等任意带批次号的节点作为查询根。
     */
    @Override
    public TraceFullTreeVO getTraceTreeFromNode(String nodeId) {
        TraceFullTreeVO result = new TraceFullTreeVO();
        result.setRootNodeId(nodeId);

        Set<String> desc = descendantIds(nodeId);
        Set<String> anc = ancestorIds(nodeId);
        if (desc.isEmpty() && anc.isEmpty()) {
            return result;
        }

        Map<String, String> supplierNameMap = new HashMap<>();
        try {
            List<Map<String, Object>> suppliers = querySupplierRows();
            for (Map<String, Object> s : suppliers) {
                supplierNameMap.put(String.valueOf(s.get("id")), String.valueOf(s.get("name")));
            }
        } catch (EmptyResultDataAccessException ignored) {
            // 无供应商时忽略
        }

        // 下游去向树:边方向 parent -> child(与物流方向一致)
        Map<String, TraceNodeTreeVO> downMap = loadVoMap(desc, supplierNameMap);
        for (Map<String, Object> e : edgesWithin(desc)) {
            String pid = String.valueOf(e.get("parent_id"));
            String cid = String.valueOf(e.get("child_id"));
            TraceNodeTreeVO p = downMap.get(pid);
            TraceNodeTreeVO c = downMap.get(cid);
            if (p != null && c != null && !p.getChildren().contains(c)) {
                p.getChildren().add(c);
                c.setParentNodeId(pid);
            }
        }
        result.setTree(downMap.get(nodeId));

        // 上游组成树:边方向反向挂(child 的 children = 它的 parent 们,即"由谁构成")
        Map<String, TraceNodeTreeVO> upMap = loadVoMap(anc, supplierNameMap);
        for (Map<String, Object> e : edgesWithin(anc)) {
            String pid = String.valueOf(e.get("parent_id"));
            String cid = String.valueOf(e.get("child_id"));
            TraceNodeTreeVO p = upMap.get(pid);
            TraceNodeTreeVO c = upMap.get(cid);
            if (p != null && c != null && !c.getChildren().contains(p)) {
                c.getChildren().add(p);
            }
        }
        result.setUpTree(upMap.get(nodeId));
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

    /** 将 "ROOT" 哨兵值解析为默认组织 UUID。 */
    private String resolveOrgId(String orgId) {
        if (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) {
            try {
                String id = jdbcTemplate.queryForObject(
                        "SELECT id::text FROM ops.sys_org WHERE org_code='MZ' LIMIT 1", String.class);
                if (id != null) return id;
            } catch (Exception ignored) { /* ignore */ }
            try {
                return jdbcTemplate.queryForObject("SELECT id::text FROM ops.sys_org LIMIT 1", String.class);
            } catch (Exception e) {
                log.warn("resolveOrgId failed: {}", e.getMessage());
                return null;
            }
        }
        return orgId;
    }

    @Override
    public List<SqmTraceNode> listRoots(String orgId) {
        String resolved = resolveOrgId(orgId);
        if (resolved == null) return new ArrayList<>();
        // 根 = 没有任何 incoming link 的节点
        List<SqmTraceNode> roots = jdbcTemplate.query(
                "SELECT * FROM ops.sqm_trace_node WHERE org_id = ?::uuid AND is_deleted = false"
                        + " AND id NOT IN (SELECT child_node_id FROM ops.sqm_trace_link WHERE is_deleted = false)"
                        + " ORDER BY node_date DESC, created_at DESC",
                new BeanPropertyRowMapper<>(SqmTraceNode.class), resolved);
        if (!roots.isEmpty()) {
            Map<String, String> supplierNameMap = buildSupplierNameMap();
            for (SqmTraceNode node : roots) {
                node.setSupplierName(node.getSupplierId() == null ? null : supplierNameMap.get(node.getSupplierId()));
            }
        }
        return roots;
    }

    // ---- 供应商名称映射缓存 ----

    /** 当前用户分公司 org_id；跨公司管理员(dataScope=all, JWT orgId="ROOT")返回 null。 */
    private String currentBranchOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || CompanyContext.isAdmin()) {
            return null;
        }
        String orgId = u.orgId();
        return (orgId == null || "ROOT".equals(orgId)) ? null : orgId;
    }

    /**
     * 供应商 (id, name) 查询：JdbcTemplate 不经过 MyBatis 数据权限拦截器，
     * 须手工按当前用户 org_id 过滤（sysadmin/跨公司不过滤）。
     */
    private List<Map<String, Object>> querySupplierRows() {
        String orgId = currentBranchOrgId();
        if (orgId == null) {
            return jdbcTemplate.queryForList(
                    "SELECT id, name FROM ops.sqm_supplier WHERE is_deleted = false");
        }
        return jdbcTemplate.queryForList(
                "SELECT id, name FROM ops.sqm_supplier WHERE is_deleted = false AND org_id = ?::uuid", orgId);
    }

    private Map<String, String> buildSupplierNameMap() {
        Map<String, String> map = new HashMap<>();
        try {
            for (Map<String, Object> row : querySupplierRows()) {
                map.put(String.valueOf(row.get("id")), String.valueOf(row.get("name")));
            }
        } catch (Exception ignored) {
            log.warn("buildSupplierNameMap failed: {}", ignored.getMessage());
        }
        return map;
    }

    // ---- 节点完整详情(表 + 树共用) ----

    @Override
    public TraceNodeFullVO getNodeDetail(String nodeId) {
        if (!isValidUuid(nodeId)) {
            // 非法 uuid(如前端 seed 占位节点 "seed-...")直接空返回, 避免 ?::uuid 解析抛 500
            return null;
        }
        SqmTraceNode node = sqmTraceNodeMapper.selectById(nodeId);
        if (node == null) {
            return null;
        }
        TraceNodeFullVO vo = new TraceNodeFullVO();
        vo.setNode(node);
        vo.setDetail(loadDetail(node));

        String supplierName = null;
        if (node.getSupplierId() != null && !node.getSupplierId().isBlank()) {
            String curOrgId = currentBranchOrgId();
            try {
                Map<String, Object> s = curOrgId == null
                        ? jdbcTemplate.queryForMap(
                                "SELECT name FROM ops.sqm_supplier WHERE id = ?::uuid AND is_deleted = false",
                                node.getSupplierId())
                        : jdbcTemplate.queryForMap(
                                "SELECT name FROM ops.sqm_supplier WHERE id = ?::uuid AND is_deleted = false AND org_id = ?::uuid",
                                node.getSupplierId(), curOrgId);
                supplierName = String.valueOf(s.get("name"));
            } catch (EmptyResultDataAccessException ignored) {
                // 供应商不存在或不属于本分公司
            }
        }
        vo.setSupplierName(supplierName);

        // 正向(用于):其他节点把本节点作为组成 → 本节点是 child, 查 parent
        List<TraceLinkRef> parents = jdbcTemplate.query(
                "SELECT n.id AS id, n.node_type AS node_type, n.node_name AS node_name, n.batch_no AS batch_no "
                        + "FROM ops.sqm_trace_link l JOIN ops.sqm_trace_node n ON n.id = l.parent_node_id "
                        + "WHERE l.child_node_id = ?::uuid AND l.is_deleted = false AND n.is_deleted = false "
                        + "ORDER BY n.node_type, n.node_name",
                (rs, i) -> {
                    TraceLinkRef r = new TraceLinkRef();
                    r.setId(rs.getString("id"));
                    r.setNodeType(rs.getString("node_type"));
                    r.setNodeName(rs.getString("node_name"));
                    r.setBatchNo(rs.getString("batch_no"));
                    return r;
                }, nodeId);
        vo.setParents(parents);

        // 反向(组成):本节点包含的下层节点 → 本节点是 parent, 查 child
        List<TraceLinkRef> children = jdbcTemplate.query(
                "SELECT n.id AS id, n.node_type AS node_type, n.node_name AS node_name, n.batch_no AS batch_no "
                        + "FROM ops.sqm_trace_link l JOIN ops.sqm_trace_node n ON n.id = l.child_node_id "
                        + "WHERE l.parent_node_id = ?::uuid AND l.is_deleted = false AND n.is_deleted = false "
                        + "ORDER BY n.node_type, n.node_name",
                (rs, i) -> {
                    TraceLinkRef r = new TraceLinkRef();
                    r.setId(rs.getString("id"));
                    r.setNodeType(rs.getString("node_type"));
                    r.setNodeName(rs.getString("node_name"));
                    r.setBatchNo(rs.getString("batch_no"));
                    return r;
                }, nodeId);
        vo.setChildren(children);

        return vo;
    }

    // ---- 按方向追溯 ----

    @Override
    public List<TraceDirectionNode> traceDirection(String nodeId, String direction) {
        if (!"forward".equalsIgnoreCase(direction) && !"backward".equalsIgnoreCase(direction)) {
            // 全部:连通分量(无序 depth,按层级展示)
            Set<String> comp = componentIds(nodeId);
            if (comp.isEmpty()) {
                return new ArrayList<>();
            }
            String in = inClause(comp);
            return jdbcTemplate.query(
                    "SELECT n.id AS id, n.node_type AS node_type, n.node_name AS node_name, n.batch_no AS batch_no, "
                            + "n.qty AS qty, n.unit AS unit, n.node_date AS node_date, n.supplier_id AS supplier_id, "
                            + "n.is_valid AS is_valid, n.tree_level AS tree_level, s.name AS supplier_name, 0 AS depth "
                            + "FROM ops.sqm_trace_node n LEFT JOIN ops.sqm_supplier s ON n.supplier_id = s.id "
                            + "WHERE n.id IN (" + in + ") AND n.is_deleted = false "
                            + "ORDER BY n.tree_level, n.node_date",
                    (rs, i) -> mapDirectionNode(rs));
        }

        boolean forward = "forward".equalsIgnoreCase(direction);
        // forward: 本节点被哪些上层节点使用(parent where child = 当前)
        // backward: 本节点由哪些下层节点组成(child where parent = 当前)
        String recurseOn = forward ? "l.child_node_id = dir.id" : "l.parent_node_id = dir.id";
        String seedCol = forward ? "l.parent_node_id" : "l.child_node_id";

        String sql = "WITH RECURSIVE dir AS ("
                + " SELECT n.id AS id, 0 AS depth FROM ops.sqm_trace_node n"
                + " WHERE n.id = ?::uuid AND n.is_deleted = false"
                + " UNION ALL"
                + " SELECT " + seedCol + ", dir.depth + 1 FROM dir"
                + " JOIN ops.sqm_trace_link l ON " + recurseOn
                + " WHERE l.is_deleted = false"
                + ") SELECT DISTINCT n.id AS id, n.node_type AS node_type, n.node_name AS node_name,"
                + " n.batch_no AS batch_no, n.qty AS qty, n.unit AS unit, n.node_date AS node_date,"
                + " n.supplier_id AS supplier_id, n.is_valid AS is_valid, n.tree_level AS tree_level,"
                + " s.name AS supplier_name, dir.depth AS depth"
                + " FROM dir JOIN ops.sqm_trace_node n ON n.id = dir.id"
                + " LEFT JOIN ops.sqm_supplier s ON n.supplier_id = s.id"
                + " WHERE n.is_deleted = false"
                + " ORDER BY dir.depth, n.tree_level, n.node_name";
        return jdbcTemplate.query(sql, (rs, i) -> mapDirectionNode(rs), nodeId);
    }

    private TraceDirectionNode mapDirectionNode(ResultSet rs) throws SQLException {
        TraceDirectionNode d = new TraceDirectionNode();
        d.setId(rs.getString("id"));
        d.setNodeType(rs.getString("node_type"));
        d.setNodeName(rs.getString("node_name"));
        d.setBatchNo(rs.getString("batch_no"));
        d.setQty(rs.getBigDecimal("qty"));
        d.setUnit(rs.getString("unit"));
        java.sql.Date nd = rs.getDate("node_date");
        d.setNodeDate(nd == null ? null : nd.toString());
        d.setSupplierName(rs.getString("supplier_name"));
        d.setIsValid(rs.getString("is_valid"));
        int tl = rs.getInt("tree_level");
        d.setTreeLevel(rs.wasNull() ? null : tl);
        int dp = rs.getInt("depth");
        d.setDepth(rs.wasNull() ? null : dp);
        return d;
    }
}
