package com.konli.qms.service.sqm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.domain.sqm.entity.SqmAbnormalMeasure;
import com.konli.qms.domain.sqm.entity.SqmAbnormalBatchVerify;
import com.konli.qms.domain.sqm.dto.AbnormalRectificationRequest;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;

import java.util.List;
import java.util.Map;

/** 来料异常整改单。sqm.abnormal.* */
public interface SqmAbnormalService {

    List<SqmIncomingAbnormal> listAbnormals();

    PageResult<SqmIncomingAbnormal> listAbnormalsPage(String keyword, String level, String status, String supplierId, int page, int size);

    SqmIncomingAbnormal create(SqmIncomingAbnormal abnormal);

    /** 关闭异常整改单(填写处置方式)。 */
    void close(String id, String disposal, String disposalRemark);

    /**
     * 重复问题升级审核:查近 30 天 sqm_incoming_abnormal,若同一 supplierId+partNo
     * 出现 >=2 次异常,则创建 sqm_supplier_escalation 记录(观察中)。
     * 可手动调用,也可后续接定时任务。异常不阻断主流程。
     */
    void checkRepeatEscalation();

    /** 保存整改进度持久化:通知/措施/验证/批验/关闭 (V21)。 */
    void saveRectification(String id, AbnormalRectificationRequest req);

    /** 加载整改记录(措施+批验) */
    Map<String, List<?>> loadRectificationDetail(String id);

    /** 列表级改派责任人(更新 handler_id + 推送被指派人任务中心)。 */
    void reassign(String id, DefectLaunchRequest req);
}
