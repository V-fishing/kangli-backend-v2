package com.konli.qms.service.ncm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;

import java.util.List;

/** 纠正措施:创建/查询/进度更新/关闭。ncm.record.* */
public interface NcmCorrectiveActionService {

    List<NcmCorrectiveAction> list();

    PageResult<NcmCorrectiveAction> listPage(String defectNo, String status, int page, int size);

    /** 按关联不良单号查询纠正措施。 */
    List<NcmCorrectiveAction> listByDefectNo(String defectNo);

    NcmCorrectiveAction get(String id);

    NcmCorrectiveAction create(NcmCorrectiveAction action);

    /** 更新进度,progress=100 时自动置为已完成。 */
    void updateProgress(String id, Short progress);

    /** 关闭纠正措施。 */
    void close(String id);

    /** 列表级改派责任人(更新 owner_user_id/owner + 推送被指派人任务中心)。 */
    void reassign(String id, DefectLaunchRequest req);
}
