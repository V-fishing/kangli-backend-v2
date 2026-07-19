package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.NcmAlertEscalation;

import java.util.List;

/** 告警升级配置 CRUD。ncm.record.create */
public interface NcmAlertEscalationService {

    List<NcmAlertEscalation> list();

    NcmAlertEscalation create(NcmAlertEscalation escalation);

    void update(NcmAlertEscalation escalation);

    void delete(String id);
}
