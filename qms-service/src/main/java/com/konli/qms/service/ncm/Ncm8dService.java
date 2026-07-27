package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.service.ncm.dto.EightDVo;

import java.util.List;

/** 8D 报告:创建/查询/阶段推进(D1->D8)。ncm.8d.* */
public interface Ncm8dService {

    List<Qms8dReport> list();

    EightDVo get(String id);

    Qms8dReport create(Qms8dReport report);

    /** 由来料异常单发起 8D(写入 source=SQM异常 / sourceRefId,并回写异常单 d8Id/rectifyType/整改中)。 */
    Qms8dReport launchFromAbnormal(Qms8dReport report);

    /** 推进到指定阶段(必须按 D1->D2->...->D8 顺序);D4 完成自动触发 CAPA,D8 完成回写异常单闭环。 */
    void advanceStage(String d8Id, String stageCode, String content, String owner);

    /** 审批某阶段(通过/驳回,驳回时currentStage退回该阶段允许重新提交)。 */
    void approveStage(String d8Id, String stageCode, boolean approved, String comment, String password);

    /** 效果验证问题复发->重新打开8D(SR-PTL),currentStage退回D6重新验证。 */
    void reopen(String d8Id, String reason);

    /** 按来源(source)+来源引用(sourceRefId)查找关联的 8D 报告(如 SPC 告警关联 8D);未找到返回 null。 */
    Qms8dReport findBySourceRef(String source, String sourceRefId);
}
