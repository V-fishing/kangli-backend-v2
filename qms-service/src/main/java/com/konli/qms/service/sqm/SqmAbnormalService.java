package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.QmsFmeaRisk;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;

import java.util.List;

/** 来料异常整改单 + FMEA 高风险项。sqm.abnormal.* */
public interface SqmAbnormalService {

    List<SqmIncomingAbnormal> listAbnormals();

    SqmIncomingAbnormal create(SqmIncomingAbnormal abnormal);

    /** 关闭异常整改单(填写处置方式)。 */
    void close(String id, String disposal, String disposalRemark);

    /**
     * 重复问题升级审核:查近 30 天 sqm_incoming_abnormal,若同一 supplierId+partNo
     * 出现 >=2 次异常,则创建 sqm_supplier_escalation 记录(观察中)。
     * 可手动调用,也可后续接定时任务。异常不阻断主流程。
     */
    void checkRepeatEscalation();

    // ---- FMEA ----

    List<QmsFmeaRisk> listFmea();

    QmsFmeaRisk createFmea(QmsFmeaRisk risk);

    void closeFmea(String id);
}
