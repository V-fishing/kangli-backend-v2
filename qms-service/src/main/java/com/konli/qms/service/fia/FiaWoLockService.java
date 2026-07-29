package com.konli.qms.service.fia;

import com.konli.qms.service.fia.dto.FiaWoLockActiveDTO;
import com.konli.qms.domain.fia.entity.FiaWoLock;

import java.util.List;

/**
 * 首件工单锁定服务(SR-FIA-022~026)。
 */
public interface FiaWoLockService {

    void lockOnCreate(String orgId, String woNo, String taskCode);

    void lockOnFail(String orgId, String woNo, String taskCode);

    void lockOnFailInNewTx(String orgId, String woNo, String taskCode);

    void unlockAuto(String orgId, String woNo, String taskCode);

    void unlockAutoInNewTx(String orgId, String woNo, String taskCode);

    void unlockByApproval(String orgId, String woNo, String approverId, String releaseReason, String traceTag, String taskCode);

    FiaWoLock getByWoNo(String orgId, String woNo);

    List<FiaWoLockActiveDTO> listActive(String orgId);

    List<FiaWoLock> listAll(String orgId, String status, String woNo);

    void release(String orgId, String woNo, String approverId, String releaseReason, String traceTag);

    void emergencyRelease(String orgId, String woNo, String approverId, String releaseReason, String traceTag);
}