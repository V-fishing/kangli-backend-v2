package com.konli.qms.service.sqm.impl;

import com.konli.qms.domain.sqm.dto.AbnormalRectificationRequest;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.DataScopeGuard;
import com.konli.qms.domain.sqm.entity.QmsFmeaRisk;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.domain.sqm.entity.SqmAbnormalMeasure;
import com.konli.qms.domain.sqm.entity.SqmAbnormalBatchVerify;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.domain.sqm.entity.SqmSupplierEscalation;
import com.konli.qms.domain.sqm.entity.SqmIncomingLot;
import com.konli.qms.domain.sqm.mapper.QmsFmeaRiskMapper;
import com.konli.qms.domain.sqm.mapper.SqmIncomingAbnormalMapper;
import com.konli.qms.domain.sqm.mapper.SqmIncomingLotMapper;
import com.konli.qms.domain.sqm.mapper.SqmAbnormalMeasureMapper;
import com.konli.qms.domain.sqm.mapper.SqmAbnormalBatchVerifyMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierEscalationMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMapper;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.sqm.SqmAbnormalService;
import com.konli.qms.service.sqm.SqmAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqmAbnormalServiceImpl implements SqmAbnormalService {

    private final SqmIncomingAbnormalMapper sqmIncomingAbnormalMapper;
    private final QmsFmeaRiskMapper qmsFmeaRiskMapper;
    private final SqmSupplierEscalationMapper sqmSupplierEscalationMapper;
    private final SqmSupplierMapper sqmSupplierMapper;
    private final SqmIncomingLotMapper sqmIncomingLotMapper;
    private final JdbcTemplate jdbcTemplate;
    private final SqmAuditService sqmAuditService;
    private final NcmCapaService ncmCapaService;
    private final SqmAbnormalMeasureMapper measureMapper;
    private final SqmAbnormalBatchVerifyMapper batchVerifyMapper;
    private final Qms8dReportMapper qms8dReportMapper;

    @Override
    public List<SqmIncomingAbnormal> listAbnormals() {
        List<SqmIncomingAbnormal> list = sqmIncomingAbnormalMapper.selectList(null);
        // 填充供应商名称(关联 sqm_supplier)
        List<String> supplierIds = list.stream()
                .map(SqmIncomingAbnormal::getSupplierId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!supplierIds.isEmpty()) {
            List<SqmSupplier> suppliers = sqmSupplierMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SqmSupplier>()
                            .in(SqmSupplier::getId, supplierIds));
            Map<String, String> nameMap = suppliers.stream()
                    .collect(Collectors.toMap(SqmSupplier::getId, SqmSupplier::getName, (a, b) -> a));
            list.forEach(a -> a.setSupplierName(nameMap.get(a.getSupplierId())));
        }
        // 兜底填充可读批次号:batchNo 为空时,尝试将 lotId(去横杠 UUID)反查来料批次 lotNo,否则沿用 lotId
        List<SqmIncomingAbnormal> needFill = list.stream()
                .filter(a -> a.getBatchNo() == null || a.getBatchNo().isBlank())
                .collect(Collectors.toList());
        if (!needFill.isEmpty()) {
            Map<String, String> lotNoMap = buildLotNoMap(needFill);
            needFill.forEach(a -> a.setBatchNo(resolveBatchNo(a.getLotId(), lotNoMap)));
        }
        return list;
    }

    /** 以 lotId(去横杠 UUID)为 key 反查来料批次 lot_no。 */
    private Map<String, String> buildLotNoMap(List<SqmIncomingAbnormal> abnormals) {
        List<String> lotIds = abnormals.stream()
                .map(SqmIncomingAbnormal::getLotId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (lotIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<SqmIncomingLot> lots = sqmIncomingLotMapper.selectList(null);
        return lots.stream().collect(Collectors.toMap(
                l -> l.getId() == null ? "" : l.getId().replace("-", ""),
                SqmIncomingLot::getLotNo,
                (a, b) -> a));
    }

    /** lotId 命中来料批次 UUID 则返回 lotNo,否则原样返回 lotId。 */
    private String resolveBatchNo(String lotId, Map<String, String> lotNoMap) {
        if (lotId == null) {
            return null;
        }
        String key = lotId.replace("-", "");
        String lotNo = lotNoMap.get(key);
        return (lotNo != null && !lotNo.isBlank()) ? lotNo : lotId;
    }

    @Override
    @Transactional
    public SqmIncomingAbnormal create(SqmIncomingAbnormal abnormal) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = (u != null) ? u.orgId() : null;
        if (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) {
            orgId = resolveDefaultOrgId();
        }
        abnormal.setOrgId(orgId);
        abnormal.setAbnormalNo("ABN-" + System.currentTimeMillis());
        abnormal.setStatus("待处理");
        abnormal.setOverdueDays(0);
        if (abnormal.getLotId() == null || abnormal.getLotId().isBlank()) {
            abnormal.setLotId("LOT-" + System.currentTimeMillis());
        }
        // 填充可读批次号:优先反查来料批次 lotNo,反查不到则沿用 lotId
        if (abnormal.getBatchNo() == null || abnormal.getBatchNo().isBlank()) {
            String key = abnormal.getLotId().replace("-", "");
            String lotNo = null;
            try {
                lotNo = jdbcTemplate.queryForObject(
                        "SELECT lot_no FROM ops.sqm_incoming_lot WHERE REPLACE(id::text,'-','') = ? LIMIT 1",
                        String.class, key);
            } catch (Exception ignored) {}
            abnormal.setBatchNo((lotNo != null && !lotNo.isBlank()) ? lotNo : abnormal.getLotId());
        }
        // SR-CAR:严重不良1件触发;一般不良累计>=3件触发
        if ("严重".equals(abnormal.getLevel())) {
            abnormal.setRectifyType("8D");
        } else {
            // 检查同供应商同物料30天内一般不良累计>=3件
            try {
                Long cnt = sqmIncomingAbnormalMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SqmIncomingAbnormal>()
                        .eq(SqmIncomingAbnormal::getSupplierId, abnormal.getSupplierId())
                        .eq(SqmIncomingAbnormal::getPartNo, abnormal.getPartNo())
                        .eq(SqmIncomingAbnormal::getLevel, "一般")
                        .ge(SqmIncomingAbnormal::getOccurDate, LocalDate.now().minusDays(30)));
                if (cnt != null && cnt + 1 >= 3) {
                    abnormal.setRectifyType("8D");
                }
            } catch (Exception ignored) {}
        }
        sqmIncomingAbnormalMapper.insert(abnormal);
        // SR-SQA:重大来料异常(level=严重)->自动触发专项审核计划
        if ("严重".equals(abnormal.getLevel())) {
            try { createAbnormalAudit(abnormal); } catch (Exception ignored) {}
        }
        // SR-CAR:涉及安全的严重异常->自动创建CAPA
        if ("严重".equals(abnormal.getLevel()) && abnormal.getDescription() != null
                && (abnormal.getDescription().contains("安全") || abnormal.getDescription().contains("召回"))) {
            try { createCapaFromAbnormal(abnormal); } catch (Exception ignored) {}
        }
        return abnormal;
    }

    private String resolveDefaultOrgId() {
        try {
            String id = jdbcTemplate.queryForObject(
                    "SELECT id::text FROM ops.sys_org WHERE org_code='MZ' LIMIT 1", String.class);
            if (id != null) {
                return id;
            }
        } catch (Exception ignored) {
            // 忽略,继续尝试取任意有效组织
        }
        try {
            return jdbcTemplate.queryForObject("SELECT id::text FROM ops.sys_org LIMIT 1", String.class);
        } catch (Exception e) {
            log.warn("resolveDefaultOrgId failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public void close(String id, String disposal, String disposalRemark) {
        SqmIncomingAbnormal abnormal = sqmIncomingAbnormalMapper.selectById(id);
        if (abnormal == null) {
            throw new BusinessException(404, "来料异常整改单不存在");
        }
        if ("已关闭".equals(abnormal.getStatus())) {
            throw new BusinessException(400, "该异常单已关闭，无需重复操作");
        }
        if (abnormal.getD8Id() != null && !abnormal.getD8Id().isBlank()) {
            Qms8dReport d8 = qms8dReportMapper.selectById(abnormal.getD8Id());
            if (d8 != null && !"已闭环".equals(d8.getStatus())) {
                throw new BusinessException(400, "关联的 8D 报告（" + d8.getD8No() + "）尚未闭环，当前阶段 " + d8.getCurrentStage());
            }
        }
        SqmIncomingAbnormal upd = new SqmIncomingAbnormal();
        upd.setId(id);
        upd.setStatus("已关闭");
        upd.setDisposal(disposal);
        upd.setDisposalRemark(disposalRemark);
        upd.setCloseDate(LocalDate.now());
        sqmIncomingAbnormalMapper.updateById(upd);
    }

    /**
     * 重复问题升级审核:查近 30 天 sqm_incoming_abnormal,若同一 supplierId+partNo
     * 出现 >=2 次异常,则创建 sqm_supplier_escalation 记录(suggestedAction=增加审核频次,
     * escalationStatus=观察中)。JdbcTemplate 聚合查询,异常 try-catch 不阻断主流程。
     */
    @Override
    @Transactional
    public void checkRepeatEscalation() {
        try {
            // 聚合:近 30 天同一 supplierId+partNo 异常 >=2 次,带 supplier level + org_id
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT a.supplier_id AS supplier_id, a.part_no AS part_no, ")
              .append("MAX(a.part_name) AS part_name, MAX(a.org_id) AS org_id, ")
              .append("MAX(s.level) AS current_level, COUNT(*) AS cnt ")
              .append("FROM ops.sqm_incoming_abnormal a ")
              .append("LEFT JOIN ops.sqm_supplier s ON s.id = a.supplier_id AND s.is_deleted = false ")
              .append("WHERE a.is_deleted = false ")
              .append("AND a.occur_date >= CURRENT_DATE - INTERVAL '30 days' ")
              .append(orgFilter("a"))
              .append("GROUP BY a.supplier_id, a.part_no ")
              .append("HAVING COUNT(*) >= 2");
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString());

            for (Map<String, Object> row : rows) {
                String supplierId = str(row.get("supplier_id"));
                String partNo = str(row.get("part_no"));
                String partName = str(row.get("part_name"));
                String orgId = str(row.get("org_id"));
                String currentLevel = str(row.get("current_level"));
                int repeatCount = toInt(row.get("cnt"));
                if (currentLevel.isBlank()) {
                    currentLevel = "C"; // 默认观察级
                }
                int issueCount6m = countIssues6m(supplierId);

                SqmSupplierEscalation esc = new SqmSupplierEscalation();
                esc.setOrgId(orgId);
                esc.setSupplierId(supplierId);
                esc.setCurrentLevel(currentLevel);
                esc.setQualityIssueCount6m(issueCount6m);
                esc.setRepeatProblemCount(repeatCount);
                esc.setSuggestedAction("增加审核频次");
                esc.setEscalationStatus("观察中");
                esc.setNoticeSentFlag(false);
                sqmSupplierEscalationMapper.insert(esc);

                // 自动降份额:观察中且≥3次异常则降低 5%(最低 5%)
                if (repeatCount >= 3) {
                    try {
                        jdbcTemplate.update(
                            "UPDATE ops.sqm_supplier_share SET share_ratio = GREATEST(share_ratio - 5, 5), " +
                            "change_reason = '重复异常自动降份额(≥3次)', linked_level = ? " +
                            "WHERE supplier_id = ? AND is_deleted = false",
                            currentLevel, supplierId);
                    } catch (Exception shareEx) {
                        log.warn("自动降份额失败(不阻断): supplier={}, {}", supplierId, shareEx.getMessage());
                    }
                }

                // 自动触发审核频次联动:写入 supplier next_audit_date 为 today+30
                try {
                    jdbcTemplate.update(
                        "UPDATE ops.sqm_supplier SET next_audit_date = CURRENT_DATE + INTERVAL '30 days' " +
                        "WHERE id = ?", supplierId);
                } catch (Exception auditEx) {
                    log.warn("审核频次联动失败(不阻断): supplier={}, {}", supplierId, auditEx.getMessage());
                }

                log.warn("重复问题升级: supplier={}, partNo={}, partName={}, count={}",
                        supplierId, partNo, partName, repeatCount);
            }
            if (!rows.isEmpty()) {
                log.info("重复问题升级审核完成,共生成 {} 条升级记录", rows.size());
            }
        } catch (Exception e) {
            // 不阻断主流程:升级审核失败仅记录日志
            log.warn("重复问题升级审核失败(不阻断主流程): {}", e.getMessage(), e);
        }
    }

    /** 统计供应商近 6 个月(180 天)异常总数,用于 quality_issue_count_6m。 */
    private int countIssues6m(String supplierId) {
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM ops.sqm_incoming_abnormal " +
                    "WHERE is_deleted = false AND supplier_id = ? " +
                    "AND occur_date >= CURRENT_DATE - INTERVAL '180 days'",
                    Integer.class, supplierId);
            return cnt == null ? 0 : cnt;
        } catch (Exception e) {
            log.warn("统计供应商近6个月异常数失败, supplier={}: {}", supplierId, e.getMessage());
            return 0;
        }
    }

    /**
     * 应用级 org_id 过滤(对齐 DataScopeInterceptor 语义,JdbcTemplate 不走 MyBatis 拦截器需手工拼接)。
     * 定时任务无用户上下文时返回 ""(不过滤,等同 admin 看全部)。
     */
    private String orgFilter(String alias) {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u == null || CompanyContext.isAdmin()) {
            return "";
        }
        String orgId = u.orgId();
        if (orgId == null || orgId.isBlank()) {
            return "";
        }
        String safe = orgId.replace("'", "''");
        String col = (alias == null || alias.isBlank()) ? "org_id" : alias + ".org_id";
        return " AND " + col + " = '" + safe + "' ";
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static int toInt(Object o) {
        if (o == null) {
            return 0;
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    // ---- FMEA ----

    @Override
    public List<QmsFmeaRisk> listFmea() {
        return qmsFmeaRiskMapper.selectList(null);
    }

    @Override
    @Transactional
    public QmsFmeaRisk createFmea(QmsFmeaRisk risk) {
        risk.setRiskNo("FMEA-" + System.currentTimeMillis());
        // RPN = S × O × D
        if (risk.getSeverityS() != null && risk.getOccurrenceO() != null && risk.getDetectionD() != null) {
            short rpn = (short) (risk.getSeverityS() * risk.getOccurrenceO() * risk.getDetectionD());
            risk.setRpn(rpn);
            if (rpn >= 100) {
                risk.setHighRiskFlag(true);
                if (risk.getRiskLevel() == null || risk.getRiskLevel().isEmpty()) {
                    risk.setRiskLevel(rpn >= 150 ? "高" : "中高");
                }
            } else {
                risk.setHighRiskFlag(false);
                if (risk.getRiskLevel() == null || risk.getRiskLevel().isEmpty()) {
                    risk.setRiskLevel(rpn >= 50 ? "中" : "低");
                }
            }
        }
        if (risk.getStatus() == null) {
            risk.setStatus("待闭环");
        }
        qmsFmeaRiskMapper.insert(risk);
        return risk;
    }

    @Override
    @Transactional
    public void closeFmea(String id) {
        QmsFmeaRisk risk = qmsFmeaRiskMapper.selectById(id);
        if (risk == null) {
            throw new BusinessException(404, "FMEA 风险项不存在");
        }
        DataScopeGuard.ensureOwner(risk.getOrgId());
        // 用已加载 risk 更新(带 @Version 乐观锁)
        risk.setStatus("已闭环");
        risk.setCloseDate(LocalDate.now());
        if (qmsFmeaRiskMapper.updateById(risk) == 0) {
            throw new BusinessException(409, "FMEA 风险项已被他人修改,请刷新后重试");
        }
    }

    // ==================== 自动触发辅助 + 超期扫描 ====================

    private void createAbnormalAudit(SqmIncomingAbnormal ab) {
        SqmAuditPlan plan = new SqmAuditPlan();
        plan.setOrgId(ab.getOrgId());
        plan.setSupplierId(ab.getSupplierId());
        plan.setAuditType("专项审核");
        plan.setPlanDate(LocalDate.now());
        plan.setAuditLead("SQE");
        plan.setAuditorTeam("SQE,质量");
        plan.setScope("来料异常触发[" + ab.getAbnormalNo() + "] " + (ab.getPartName() != null ? ab.getPartName() : ""));
        plan.setRiskLevel("高");
        plan.setStatus("待执行");
        sqmAuditService.createPlan(plan);
    }

    private void createCapaFromAbnormal(SqmIncomingAbnormal ab) {
        QmsCapa capa = new QmsCapa();
        capa.setOrgId(ab.getOrgId());
        capa.setAbnormalId(ab.getId());
        capa.setIssue("安全相关异常:" + ab.getAbnormalNo() + " " + (ab.getDescription() != null ? ab.getDescription() : ""));
        capa.setTriggerType("来料异常");
        capa.setCapaType("纠正措施");
        capa.setOwner("质量经理");
        capa.setDueDate(LocalDate.now().plusDays(30));
        ncmCapaService.create(capa);
    }

    @Override
    @Transactional
    public void saveRectification(String id, AbnormalRectificationRequest req) {
        SqmIncomingAbnormal exist = sqmIncomingAbnormalMapper.selectById(id);
        if (exist == null) throw new BusinessException(404, "异常单不存在");
        DataScopeGuard.ensureOwner(exist.getOrgId());
        SqmIncomingAbnormal rect = req.getAbnormal();
        // 更新主体字段(用已加载 exist,带 @Version 乐观锁)
        if (rect.getStatus() != null) exist.setStatus(rect.getStatus());
        exist.setDisposal(rect.getDisposal());
        exist.setDisposalRemark(rect.getDisposalRemark());
        exist.setNoticeDate(rect.getNoticeDate());
        exist.setNoticeContent(rect.getNoticeContent());
        exist.setPlanDate(rect.getPlanDate());
        exist.setExtensionApproved(rect.getExtensionApproved());
        exist.setExtensionDate(rect.getExtensionDate());
        exist.setVerifyResult(rect.getVerifyResult());
        exist.setVerifyComment(rect.getVerifyComment());
        exist.setVerifyDate(rect.getVerifyDate());
        exist.setVerifyBy(rect.getVerifyBy());
        exist.setReturnReason(rect.getReturnReason());
        exist.setCloseDate(rect.getCloseDate());
        exist.setCloseAuditor(rect.getCloseAuditor());
        if (sqmIncomingAbnormalMapper.updateById(exist) == 0) {
            throw new BusinessException(409, "异常单已被他人修改,请刷新后重试");
        }

        // 整改措施：删除旧→插入新
        if (req.getMeasures() != null) {
            measureMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SqmAbnormalMeasure>()
                .eq(SqmAbnormalMeasure::getAbnormalId, id));
            for (SqmAbnormalMeasure m : req.getMeasures()) {
                m.setId(null);
                m.setAbnormalId(id);
                m.setOrgId(exist.getOrgId());
                if (m.getStatus() == null) m.setStatus("待完成");
                measureMapper.insert(m);
            }
        }

        // 三批验证：删除旧→插入新
        if (req.getBatchVerifies() != null) {
            batchVerifyMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SqmAbnormalBatchVerify>()
                .eq(SqmAbnormalBatchVerify::getAbnormalId, id));
            for (SqmAbnormalBatchVerify bv : req.getBatchVerifies()) {
                bv.setId(null);
                bv.setAbnormalId(id);
                bv.setOrgId(exist.getOrgId());
                batchVerifyMapper.insert(bv);
            }
        }
    }

    @Override
    public Map<String, List<?>> loadRectificationDetail(String id) {
        Map<String, List<?>> result = new HashMap<>();
        List<SqmAbnormalMeasure> measures = measureMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SqmAbnormalMeasure>()
                .eq(SqmAbnormalMeasure::getAbnormalId, id)
                .orderByAsc(SqmAbnormalMeasure::getSeq));
        result.put("measures", measures == null ? new ArrayList<>() : measures);
        List<SqmAbnormalBatchVerify> batches = batchVerifyMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SqmAbnormalBatchVerify>()
                .eq(SqmAbnormalBatchVerify::getAbnormalId, id));
        result.put("batchVerifies", batches == null ? new ArrayList<>() : batches);
        return result;
    }

    /** SR-CAR:每天8:00扫描异常超期(occurDate>7天未闭环),超7天通知SQE,超14天升级质量经理+采购 */
    @Scheduled(cron = "0 5 8 * * ?")
    public void scanOverdue() {
        try {
            List<SqmIncomingAbnormal> list = sqmIncomingAbnormalMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SqmIncomingAbnormal>()
                            .lt(SqmIncomingAbnormal::getOccurDate, LocalDate.now().minusDays(7))
                            .ne(SqmIncomingAbnormal::getStatus, "已关闭"));
            for (SqmIncomingAbnormal a : list) {
                long days = ChronoUnit.DAYS.between(a.getOccurDate(), LocalDate.now());
                SqmIncomingAbnormal upd = new SqmIncomingAbnormal();
                upd.setId(a.getId());
                upd.setOverdueDays((int) days);
                sqmIncomingAbnormalMapper.updateById(upd);
                String receiver = days >= 14 ? "质量经理,采购" : "SQE";
                jdbcTemplate.update(
                        "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) VALUES (?::uuid, 'ABNORMAL_OVERDUE', ?, '站内', ?, ?, '告警', '已发送', now())",
                        java.util.UUID.fromString(a.getOrgId() != null ? a.getOrgId() : "019f701f-0411-71ed-9eac-ab9440335832"),
                        a.getId(), receiver,
                        "来料异常" + a.getAbnormalNo() + " 超期" + days + "天未闭环");
            }
        } catch (Exception e) { log.warn("异常超期扫描失败: {}", e.getMessage()); }
    }
}
