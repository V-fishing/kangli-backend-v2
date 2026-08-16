package com.konli.qms.service.tlm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.tlm.entity.TlmMetroRecord;

import java.time.LocalDateTime;

/** 计量数据采集记录服务: 录入计量器具(GAUGE)实测值并绑定工单/批次(P2)。 */
public interface TlmMetroRecordService {

    /** 采集记录分页(支持 器具编号/名称、工单号、判定 过滤)。 */
    PageResult<TlmMetroRecord> page(String keyword, String woNo, String judged, int page, int size);

    /** 录入一条计量采集记录。 */
    TlmMetroRecord create(TlmMetroRecord record);
}
