package com.konli.qms.service.cs;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.cs.entity.CsFeedback;

import java.util.Map;

/** 客户反馈(投诉/建议/表扬/咨询)管理。cs.feedback.* */
public interface CsFeedbackService {

    PageResult<CsFeedback> page(String keyword, String fbType, String status, int page, int size);

    CsFeedback get(String id);

    CsFeedback create(CsFeedback fb);

    CsFeedback update(CsFeedback fb);

    void delete(String id);

    /** 处理反馈: OPEN/HANDLING -> DONE, 写 handleDetail/handleAt/ownerName。 */
    void handle(String id, String handleDetail, String ownerName);

    /** 标记反馈为处理中: OPEN -> HANDLING。 */
    void markHandling(String id, String ownerName);

    /** 反馈联动质量改进: 绑定 NCM 8D/CAPA 纠正措施 ID(需求 2.4.2.5)。 */
    void linkNcm(String id, String ncmId);
}
