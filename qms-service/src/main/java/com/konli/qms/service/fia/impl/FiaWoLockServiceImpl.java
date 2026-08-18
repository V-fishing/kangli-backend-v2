package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.service.fia.dto.FiaWoLockActiveDTO;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.entity.FiaWoLock;
import com.konli.qms.domain.fia.mapper.FiaTaskMapper;
import com.konli.qms.domain.fia.mapper.FiaWoLockMapper;
import com.konli.qms.service.fia.FiaWoLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FiaWoLockServiceImpl implements FiaWoLockService {

    private final FiaWoLockMapper fiaWoLockMapper;
    private final FiaTaskMapper fiaTaskMapper;

    private static final String STATUS_LOCKED = "\u9501\u5B9A";
    private static final String STATUS_NORMAL = "\u6B63\u5E38";
    private static final String REASON_UNFINISHED = "\u9996\u4EF6\u672A\u5B8C\u6210";
    private static final String REASON_FAIL = "\u9996\u4EF6\u4E0D\u5408\u683C";
    private static final String UNLOCK_AUTO = "\u81EA\u52A8\u89E3\u9501";
    private static final String UNLOCK_APPROVE = "\u5BA1\u6279\u91CA\u653E";
    private static final String UNLOCK_EMERGENCY = "\u7D27\u6025\u653E\u884C";

    @Override
    @Transactional
    public void lockOnCreate(String orgId, String woNo, String taskCode) {
        FiaWoLock exist = getByWoNo(orgId, woNo);
        if (exist != null) {
            if (!STATUS_LOCKED.equals(exist.getLockStatus())) {
                exist.setLockStatus(STATUS_LOCKED);
                exist.setLockReason(REASON_UNFINISHED);
                exist.setLockedAt(LocalDateTime.now());
                exist.setWipHold(true);
                exist.setUnlockType(null);
                exist.setUnlockedAt(null);
                exist.setApproverId(null);
                exist.setReleaseReason(null);
                exist.setTraceTag(null);
                exist.setTaskCode(taskCode);
                fiaWoLockMapper.updateById(exist);
            }
            return;
        }
        FiaWoLock l = new FiaWoLock();
        l.setOrgId(orgId);
        l.setWoNo(woNo);
        l.setLockStatus(STATUS_LOCKED);
        l.setLockReason(REASON_UNFINISHED);
        l.setLockedAt(LocalDateTime.now());
        l.setWipHold(true);
        l.setTaskCode(taskCode);
        fiaWoLockMapper.insert(l);
        log.info("[WO-LOCK] lock on create woNo={} task={}", woNo, taskCode);
    }

    @Override
    @Transactional
    public void lockOnFail(String orgId, String woNo, String taskCode) {
        FiaWoLock exist = getByWoNo(orgId, woNo);
        if (exist == null) {
            FiaWoLock l = new FiaWoLock();
            l.setOrgId(orgId);
            l.setWoNo(woNo);
            l.setLockStatus(STATUS_LOCKED);
            l.setLockReason(REASON_FAIL);
            l.setLockedAt(LocalDateTime.now());
            l.setWipHold(true);
            l.setTaskCode(taskCode);
            fiaWoLockMapper.insert(l);
            log.info("[WO-LOCK] lock on fail woNo={} task={}", woNo, taskCode);
            return;
        }
        exist.setLockStatus(STATUS_LOCKED);
        exist.setLockReason(REASON_FAIL);
        exist.setWipHold(true);
        if (exist.getLockedAt() == null) {
            exist.setLockedAt(LocalDateTime.now());
        }
        exist.setTaskCode(taskCode);
        fiaWoLockMapper.updateById(exist);
        log.info("[WO-LOCK] enforce fail reason woNo={} task={}", woNo, taskCode);
    }

    @Override
    @Transactional
    public void unlockAuto(String orgId, String woNo, String taskCode) {
        FiaWoLock exist = getByWoNo(orgId, woNo);
        if (exist == null) {
            log.warn("[WO-UNLOCK] no lock record woNo={}, skip auto unlock", woNo);
            return;
        }
        if (!STATUS_LOCKED.equals(exist.getLockStatus())) {
            return;
        }
        exist.setLockStatus(STATUS_NORMAL);
        exist.setUnlockType(UNLOCK_AUTO);
        exist.setUnlockedAt(LocalDateTime.now());
        exist.setWipHold(false);
        exist.setTaskCode(taskCode);
        fiaWoLockMapper.updateById(exist);
        log.info("[WO-UNLOCK] auto unlock woNo={} task={}", woNo, taskCode);
    }

    @Override
    @Transactional
    public void unlockByApproval(String orgId, String woNo, String approverId, String releaseReason, String traceTag, String taskCode) {
        FiaWoLock exist = getByWoNo(orgId, woNo);
        if (exist == null) {
            exist = new FiaWoLock();
            exist.setOrgId(orgId);
            exist.setWoNo(woNo);
            exist.setLockStatus(STATUS_NORMAL);
            exist.setLockedAt(LocalDateTime.now());
            exist.setTaskCode(taskCode);
            exist.setApproverId(approverId);
            exist.setReleaseReason(releaseReason);
            exist.setTraceTag(traceTag);
            exist.setUnlockType(UNLOCK_APPROVE);
            exist.setUnlockedAt(LocalDateTime.now());
            exist.setWipHold(false);
            fiaWoLockMapper.insert(exist);
            log.info("[WO-RELEASE] approval release (no prior lock) woNo={} task={}", woNo, taskCode);
            return;
        }
        exist.setLockStatus(STATUS_NORMAL);
        exist.setUnlockType(UNLOCK_APPROVE);
        exist.setUnlockedAt(LocalDateTime.now());
        exist.setWipHold(false);
        exist.setApproverId(approverId);
        exist.setReleaseReason(releaseReason);
        exist.setTraceTag(traceTag);
        exist.setTaskCode(taskCode);
        fiaWoLockMapper.updateById(exist);
        log.info("[WO-RELEASE] approval release woNo={} approver={} tag={} task={}", woNo, approverId, traceTag, taskCode);
    }    @Override
    public FiaWoLock getByWoNo(String orgId, String woNo) {
        if (woNo == null || woNo.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<FiaWoLock> w = new LambdaQueryWrapper<FiaWoLock>()
                .eq(FiaWoLock::getWoNo, woNo)
                .orderByDesc(FiaWoLock::getCreatedAt)
                .last("LIMIT 1");
        if (orgId != null && !orgId.isEmpty() && !"all".equals(orgId)) {
            w.eq(FiaWoLock::getOrgId, orgId);
        }
        return fiaWoLockMapper.selectOne(w);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void unlockAutoInNewTx(String orgId, String woNo, String taskCode) {
        unlockAuto(orgId, woNo, taskCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockOnFailInNewTx(String orgId, String woNo, String taskCode) {
        lockOnFail(orgId, woNo, taskCode);
    }

    public List<FiaWoLock> listAll(String orgId, String status, String woNo) {
        LambdaQueryWrapper<FiaWoLock> w = new LambdaQueryWrapper<FiaWoLock>();
        if (orgId != null && !orgId.isEmpty() && !"all".equals(orgId)) {
            w.eq(FiaWoLock::getOrgId, orgId);
        }
        if (status != null && !status.trim().isEmpty()) {
            w.eq(FiaWoLock::getLockStatus, status);
        }
        if (woNo != null && !woNo.trim().isEmpty()) {
            w.like(FiaWoLock::getWoNo, woNo.trim());
        }
        w.orderByDesc(FiaWoLock::getLockedAt);
        w.orderByDesc(FiaWoLock::getUnlockedAt);
        return fiaWoLockMapper.selectList(w);
    }

    @Override
    public List<FiaWoLockActiveDTO> listActive(String orgId) {
        LambdaQueryWrapper<FiaWoLock> w = new LambdaQueryWrapper<FiaWoLock>();
        if (orgId != null && !orgId.isEmpty() && !"all".equals(orgId)) {
            w.eq(FiaWoLock::getOrgId, orgId);
        }
        w.eq(FiaWoLock::getLockStatus, STATUS_LOCKED);
        w.eq(FiaWoLock::getWipHold, true);
        w.orderByAsc(FiaWoLock::getLockedAt);
        List<FiaWoLock> locks = fiaWoLockMapper.selectList(w);
        List<FiaWoLockActiveDTO> result = new ArrayList<>();
        for (FiaWoLock lk : locks) {
            FiaWoLockActiveDTO dto = new FiaWoLockActiveDTO();
            dto.setWoNo(lk.getWoNo());
            dto.setLockReason(lk.getLockReason());
            dto.setLockedAt(lk.getLockedAt() == null ? null : lk.getLockedAt().toString());
            dto.setTaskCode(lk.getTaskCode());
            if (lk.getTaskCode() != null && !lk.getTaskCode().isEmpty()) {
                FiaTask t = fiaTaskMapper.selectOne(
                        new LambdaQueryWrapper<FiaTask>().eq(FiaTask::getCode, lk.getTaskCode()));
                if (t != null) {
                    dto.setProductName(t.getProductName());
                    dto.setLineName(t.getLineName());
                }
            }
            result.add(dto);
        }
        return result;
    }

    @Override
    public void release(String orgId, String woNo, String approverId, String releaseReason, String traceTag) {
        FiaWoLock exist = getByWoNo(orgId, woNo);
        String taskCode = exist != null ? exist.getTaskCode() : null;
        unlockByApproval(orgId, woNo, approverId, releaseReason, traceTag, taskCode);
    }

    @Override
    @Transactional
    public void emergencyRelease(String orgId, String woNo, String approverId, String releaseReason, String traceTag) {
        FiaWoLock exist = getByWoNo(orgId, woNo);
        String taskCode = exist != null ? exist.getTaskCode() : null;
        if (exist == null) {
            exist = new FiaWoLock();
            exist.setOrgId(orgId);
            exist.setWoNo(woNo);
            exist.setLockStatus(STATUS_NORMAL);
            exist.setLockedAt(LocalDateTime.now());
            exist.setTaskCode(taskCode);
            exist.setApproverId(approverId);
            exist.setReleaseReason(releaseReason);
            exist.setTraceTag(traceTag);
            exist.setUnlockType(UNLOCK_EMERGENCY);
            exist.setUnlockedAt(LocalDateTime.now());
            exist.setWipHold(false);
            fiaWoLockMapper.insert(exist);
            log.info("[WO-RELEASE] emergency release (no prior lock) woNo={} task={}", woNo, taskCode);
            return;
        }
        exist.setLockStatus(STATUS_NORMAL);
        exist.setUnlockType(UNLOCK_EMERGENCY);
        exist.setUnlockedAt(LocalDateTime.now());
        exist.setWipHold(false);
        exist.setApproverId(approverId);
        exist.setReleaseReason(releaseReason);
        exist.setTraceTag(traceTag);
        exist.setTaskCode(taskCode);
        fiaWoLockMapper.updateById(exist);
        log.info("[WO-RELEASE] emergency release woNo={} approver={} tag={} task={}", woNo, approverId, traceTag, taskCode);
    }
}