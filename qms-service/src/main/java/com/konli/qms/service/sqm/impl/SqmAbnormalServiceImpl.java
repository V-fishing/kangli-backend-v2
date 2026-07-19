package com.konli.qms.service.sqm.impl;

import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.sqm.entity.QmsFmeaRisk;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.domain.sqm.entity.SqmSupplierEscalation;
import com.konli.qms.domain.sqm.mapper.QmsFmeaRiskMapper;
import com.konli.qms.domain.sqm.mapper.SqmIncomingAbnormalMapper;
import com.konli.qms.domain.sqm.mapper.SqmSupplierEscalationMapper;
import com.konli.qms.service.sqm.SqmAbnormalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqmAbnormalServiceImpl implements SqmAbnormalService {

    private final SqmIncomingAbnormalMapper sqmIncomingAbnormalMapper;
    private final QmsFmeaRiskMapper qmsFmeaRiskMapper;
    private final SqmSupplierEscalationMapper sqmSupplierEscalationMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SqmIncomingAbnormal> listAbnormals() {
        return sqmIncomingAbnormalMapper.selectList(null);
    }

    @Override
    @Transactional
    public SqmIncomingAbnormal create(SqmIncomingAbnormal abnormal) {
        abnormal.setAbnormalNo("ABN-" + System.currentTimeMillis());
        abnormal.setStatus("待处理");
        abnormal.setOverdueDays(0);
        sqmIncomingAbnormalMapper.insert(abnormal);
        return abnormal;
    }

    @Override
    @Transactional
    public void close(String id, String disposal, String disposalRemark) {
        SqmIncomingAbnormal abnormal = sqmIncomingAbnormalMapper.selectById(id);
        if (abnormal == null) {
            throw new BusinessException(404, "来料异常整改单不存在");
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
        QmsFmeaRisk upd = new QmsFmeaRisk();
        upd.setId(id);
        upd.setStatus("已闭环");
        upd.setCloseDate(LocalDate.now());
        qmsFmeaRiskMapper.updateById(upd);
    }
}
