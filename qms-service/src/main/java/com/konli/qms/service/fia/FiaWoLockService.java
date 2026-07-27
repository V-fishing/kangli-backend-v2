package com.konli.qms.service.fia;

import com.konli.qms.domain.fia.entity.FiaWoLock;

/**
 * 首件工单锁定服务(SR-FIA-022~026)。
 *
 * <p>锁定语义:首件任务创建即锁定工单(首件未完成);首件合格完成自动解锁;
 * 质量主管紧急放行审批通过后解锁并留痕。同一工单仅保留一条当前锁定记录。</p>
 */
public interface FiaWoLockService {

    /** 首件任务创建时锁定工单(若已有当前锁定记录则更新,不重复新建)。 */
    void lockOnCreate(String orgId, String woNo, String taskCode);

    /** 首件不合格时强化锁定原因(由"未完成"转为"不合格")。 */
    void lockOnFail(String orgId, String woNo, String taskCode);

    /** 不合格强化锁定(独立事务):锁定失败不影响调用方主事务。 */
    void lockOnFailInNewTx(String orgId, String woNo, String taskCode);

    /** 重新校验通过 / 首件合格完成 -> 自动解锁。 */
    void unlockAuto(String orgId, String woNo, String taskCode);

    /** 自动解锁(独立事务):解锁失败不影响调用方主事务。 */
    void unlockAutoInNewTx(String orgId, String woNo, String taskCode);

    /** 质量主管紧急放行 -> 审批解锁并留痕(追溯标签)。 */
    void unlockByApproval(String orgId, String woNo, String approverId, String releaseReason, String traceTag, String taskCode);

    /** 查询工单当前锁定记录(无则返回 null)。 */
    FiaWoLock getByWoNo(String orgId, String woNo);
}
