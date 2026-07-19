package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.NcmBiReport;

import java.util.List;

/** BI 报表:查询/创建。ncm.record.list */
public interface NcmBiReportService {

    List<NcmBiReport> list();

    NcmBiReport get(String id);

    NcmBiReport create(NcmBiReport report);
}
