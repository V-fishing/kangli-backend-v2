package com.konli.qms.service.cs.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.cs.entity.CsFeedback;
import com.konli.qms.domain.cs.mapper.CsFeedbackMapper;
import com.konli.qms.service.cs.CsFeedbackService;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsFeedbackServiceImpl implements CsFeedbackService {

    private final CsFeedbackMapper mapper;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;

    private String curOrg() {
        try {
            String o = CompanyContext.get().orgId();
            return (o == null || o.isBlank() || "ROOT".equals(o)) ? null : o;
        } catch (Exception e) {
            return null;
        }
    }

    private String defaultOrgId() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM ops.sys_org WHERE is_deleted = false ORDER BY created_at LIMIT 1", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String curUser() {
        try {
            return CompanyContext.get().userId();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public PageResult<CsFeedback> page(String keyword, String fbType, String status, int page, int size) {
        LambdaQueryWrapper<CsFeedback> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(CsFeedback::getCustomerName, keyword)
                    .or().like(CsFeedback::getContent, keyword)
                    .or().like(CsFeedback::getRelatedWoNo, keyword));
        }
        if (fbType != null && !fbType.isBlank()) w.eq(CsFeedback::getFbType, fbType);
        if (status != null && !status.isBlank()) w.eq(CsFeedback::getStatus, status);
        w.orderByDesc(CsFeedback::getCreatedAt);
        IPage<CsFeedback> p = mapper.selectPage(new Page<>(page, size), w);
        return new PageResult<CsFeedback>(p.getRecords(), p.getTotal(), page, size);
    }

    @Override
    public CsFeedback get(String id) {
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public CsFeedback create(CsFeedback fb) {
        if (fb.getOrgId() == null) {
            String o = curOrg();
            fb.setOrgId(o != null ? o : defaultOrgId());
        }
        if (fb.getStatus() == null || fb.getStatus().isBlank()) fb.setStatus("OPEN");
        if (fb.getFbType() == null || fb.getFbType().isBlank()) fb.setFbType("COMPLAINT");
        fb.setCreatedBy(curUser());
        fb.setUpdatedBy(curUser());
        mapper.insert(fb);
        try {
            notificationService.notify("cs", "cs_fb_created", "客户反馈登记",
                    "客户 " + fb.getCustomerName() + " 登记了新反馈(" + fb.getFbType()
                            + "),请及时跟进处理。", "cs_feedback", fb.getId(), "/cs/feedback");
        } catch (Exception e) {
            log.warn("[CS] 客户反馈通知发送失败: {}", e.getMessage());
        }
        return fb;
    }

    @Override
    @Transactional
    public CsFeedback update(CsFeedback fb) {
        CsFeedback exist = mapper.selectById(fb.getId());
        if (exist == null) throw new com.konli.qms.common.exception.BusinessException("反馈不存在");
        exist.setCustomerName(fb.getCustomerName());
        exist.setCustomerContact(fb.getCustomerContact());
        exist.setFbType(fb.getFbType());
        exist.setContent(fb.getContent());
        exist.setRelatedWoNo(fb.getRelatedWoNo());
        exist.setSatisfaction(fb.getSatisfaction());
        exist.setUpdatedBy(curUser());
        mapper.updateById(exist);
        return exist;
    }

    @Override
    @Transactional
    public void delete(String id) {
        mapper.deleteById(id);
    }

    @Override
    @Transactional
    public void handle(String id, String handleDetail, String ownerName) {
        CsFeedback f = mapper.selectById(id);
        if (f == null) throw new com.konli.qms.common.exception.BusinessException("反馈不存在");
        f.setStatus("DONE");
        f.setHandleDetail(handleDetail);
        f.setOwnerName(ownerName);
        f.setHandleAt(LocalDateTime.now());
        f.setUpdatedBy(curUser());
        mapper.updateById(f);
    }

    @Override
    @Transactional
    public void markHandling(String id, String ownerName) {
        CsFeedback f = mapper.selectById(id);
        if (f == null) throw new com.konli.qms.common.exception.BusinessException("反馈不存在");
        if (!"OPEN".equals(f.getStatus())) {
            throw new com.konli.qms.common.exception.BusinessException("仅待处理反馈可标记为处理中");
        }
        f.setStatus("HANDLING");
        if (ownerName != null && !ownerName.isBlank()) f.setOwnerName(ownerName);
        f.setUpdatedBy(curUser());
        mapper.updateById(f);
    }
}
