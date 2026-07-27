package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.sqm.entity.SqmSupplierCert;
import com.konli.qms.domain.sqm.mapper.SqmSupplierCertMapper;
import com.konli.qms.service.sqm.SqmSupplierCertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqmSupplierCertServiceImpl implements SqmSupplierCertService {

    private final SqmSupplierCertMapper sqmSupplierCertMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SqmSupplierCert> list(String supplierId) {
        LambdaQueryWrapper<SqmSupplierCert> w = new LambdaQueryWrapper<>();
        if (supplierId != null && !supplierId.isBlank()) {
            w.eq(SqmSupplierCert::getSupplierId, supplierId);
        }
        w.orderByDesc(SqmSupplierCert::getExpiryDate);
        return sqmSupplierCertMapper.selectList(w);
    }

    @Override
    public SqmSupplierCert get(String id) {
        return sqmSupplierCertMapper.selectById(id);
    }

    @Override
    @Transactional
    public SqmSupplierCert create(SqmSupplierCert cert) {
        if (cert.getStatus() == null) {
            cert.setStatus("生效");
        }
        if (cert.getCertVersion() == null) {
            cert.setCertVersion(1);
        }
        sqmSupplierCertMapper.insert(cert);
        return cert;
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (sqmSupplierCertMapper.selectById(id) == null) {
            throw new BusinessException(404, "供应商资质不存在");
        }
        sqmSupplierCertMapper.deleteById(id);
    }

    @Override
    public List<SqmSupplierCert> expiring(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        return sqmSupplierCertMapper.selectList(
                new LambdaQueryWrapper<SqmSupplierCert>()
                        .le(SqmSupplierCert::getExpiryDate, deadline)
                        .gt(SqmSupplierCert::getExpiryDate, LocalDate.now())
                        .ne(SqmSupplierCert::getStatus, "过期")
                        .orderByAsc(SqmSupplierCert::getExpiryDate));
    }

    /** SR-SBM:每天8:00扫描资质到期预警(30/60/90天前通知) */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scanCertExpiry() {
        try {
            LocalDate today = LocalDate.now();
            for (int days : new int[]{30, 60, 90}) {
                LocalDate target = today.plusDays(days);
                List<SqmSupplierCert> certs = sqmSupplierCertMapper.selectList(
                        new LambdaQueryWrapper<SqmSupplierCert>()
                                .eq(SqmSupplierCert::getExpiryDate, target)
                                .ne(SqmSupplierCert::getStatus, "过期"));
                for (SqmSupplierCert c : certs) {
                    jdbcTemplate.update(
                            "INSERT INTO ops.notification_log (org_id, biz_type, biz_id, channel, receiver, content, level, send_status, sent_at) VALUES (?::uuid, 'CERT_EXPIRING', ?, '站内', ?, ?, '提醒', '已发送', now())",
                            java.util.UUID.fromString("019f701f-0411-71ed-9eac-ab9440335832"),
                            c.getId(), "供应商管理员",
                            "供应商资质证书 " + c.getCertNo() + " 将于" + days + "天后到期,请及时续期");
                }
            }
        } catch (Exception e) { log.warn("资质到期扫描异常: {}", e.getMessage()); }
    }
}
