package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.SqmAuditApproval;
import com.konli.qms.domain.sqm.entity.SqmAuditChecklistItem;
import com.konli.qms.domain.sqm.entity.SqmAuditNc;
import com.konli.qms.domain.sqm.entity.SqmAuditPhoto;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmAuditRecord;
import com.konli.qms.domain.sqm.entity.SqmAuditWorkflowLog;

import java.util.List;

/**
 * 供应商审核执行环节:检查项打分、现场照片、不符合项录入、流程轨迹、执行结果复核。
 * 首次录入时惰性创建审核记录(record_id 回填计划),最后通过 review 会签节点放行归档。
 */
public interface SqmAuditExecuteService {

    /** 执行页初始化:返回计划、当前记录 id(无则惰性建)、已有复核节点。 */
    ExecuteInit init(String planId);

    /** 批量保存检查项(全量 upsert;首次保存前若计划无 record_id 则惰性建记录)。 */
    void saveChecklist(String recordId, List<SqmAuditChecklistItem> items);

    List<SqmAuditChecklistItem> listChecklist(String recordId);

    SqmAuditPhoto addPhoto(SqmAuditPhoto photo);

    List<SqmAuditPhoto> listPhotos(String recordId);

    void removePhoto(String photoId);

    SqmAuditNc createNc(String recordId, SqmAuditNc nc);

    List<SqmAuditNc> listNcs(String recordId);

    List<SqmAuditWorkflowLog> workflowLog(String planId);

    /** 提交执行结果复核:向会签链追加 review 节点(幂等)。 */
    SqmAuditApproval submitReview(String planId);

    /** 复核通过后闭环:计划置已完成 + 自动判定/人工兜底结论 + 自动归档(前端在 review 节点 approve 成功后调用)。 */
    void completeReview(String planId, String conclusion);

    /** 内部 DTO:执行页初始化聚合结果。 */
    class ExecuteInit {
        public SqmAuditPlan plan;
        public String recordId;
        public SqmAuditApproval review;
    }
}
