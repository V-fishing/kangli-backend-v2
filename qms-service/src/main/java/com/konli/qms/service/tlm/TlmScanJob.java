package com.konli.qms.service.tlm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.tlm.entity.TlmTooling;
import com.konli.qms.domain.tlm.mapper.TlmToolingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 工装计量/保养/寿命预警扫描。
 * 照搬 SqmSupplierCertServiceImpl.scanCertExpiry() 范式: 每天 8:00 扫描,
 * 对校准到期/保养到期/寿命超限的工装写 ops.notification_log(org_id 默认 MZ)。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TlmScanJob {

    private static final String MZ_ORG = "019f701f-0411-71ed-9eac-ab9440335832";

    private final TlmToolingMapper toolingMapper;
    private final JdbcTemplate jdbcTemplate;

    private UUID orgUuid(String orgId) {
        try {
            return orgId == null ? UUID.fromString(MZ_ORG) : UUID.fromString(orgId);
        } catch (Exception e) {
            return UUID.fromString(MZ_ORG);
        }
    }

    /** 每天 8:00 扫描工装预警。 */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scanToolingMaintenance() {
        try {
            LocalDate today = LocalDate.now();
            // 1) 校准到期(30/60/90 天前预警)
            for (int days : new int[]{30, 60, 90}) {
                LocalDate target = today.plusDays(days);
                List<TlmTooling> list = toolingMapper.selectList(
                        new LambdaQueryWrapper<TlmTooling>()
                                .eq(TlmTooling::getToolCategory, "GAUGE")
                                .eq(TlmTooling::getCalibDueDate, target)
                                .ne(TlmTooling::getStatus, "SCRAPPED"));
                for (TlmTooling t : list) {
                    insertLog(t, "TLM_CALIB_EXPIRY",
                            "工装(测量设备) " + t.getToolName() + "(" + t.getToolNo() + ") 将于" + days + "天后校准到期");
                }
            }
            // 2) 保养到期(7/30 天前预警)
            for (int days : new int[]{7, 30}) {
                LocalDate target = today.plusDays(days);
                List<TlmTooling> list = toolingMapper.selectList(
                        new LambdaQueryWrapper<TlmTooling>()
                                .eq(TlmTooling::getNextMaintDate, target)
                                .ne(TlmTooling::getStatus, "SCRAPPED"));
                for (TlmTooling t : list) {
                    insertLog(t, "TLM_MAINT_DUE",
                            "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 将于" + days + "天后保养到期");
                }
            }
            // 3) 寿命超限(已锁定的工装)
            List<TlmTooling> overLife = toolingMapper.selectList(
                    new LambdaQueryWrapper<TlmTooling>()
                            .isNotNull(TlmTooling::getDesignLife)
                            .apply("bind_count >= design_life")
                            .eq(TlmTooling::getLocked, true));
            for (TlmTooling t : overLife) {
                insertLog(t, "TLM_LIFE_OVER",
                        "工装 " + t.getToolName() + "(" + t.getToolNo() + ") 已达寿命上限,已锁定");
            }
        } catch (Exception e) {
            log.warn("工装预警扫描异常: {}", e.getMessage());
        }
    }

    private void insertLog(TlmTooling t, String bizType, String content) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) VALUES (?::uuid, ?, ?, '站内', '工装管理员', ?, '提醒', '已发送', now())",
                    orgUuid(t.getOrgId()), t.getId(), content);
        } catch (Exception e) {
            log.warn("工装预警写入失败: {}", e.getMessage());
        }
    }
}
