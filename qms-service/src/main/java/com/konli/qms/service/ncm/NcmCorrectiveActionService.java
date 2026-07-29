package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;

import java.util.List;

/** 纠正措施:创建/查询/进度更新/关闭。ncm.record.* */
public interface NcmCorrectiveActionService {

    List<NcmCorrectiveAction> list();

    /** 按关联不良单号查询纠正措施。 */
    List<NcmCorrectiveAction> listByDefectNo(String defectNo);

    NcmCorrectiveAction get(String id);

    NcmCorrectiveAction create(NcmCorrectiveAction action);

    /** 更新进度,progress=100 时自动置为已完成。 */
    void updateProgress(String id, Short progress);

    /** 关闭纠正措施。 */
    void close(String id);
}
