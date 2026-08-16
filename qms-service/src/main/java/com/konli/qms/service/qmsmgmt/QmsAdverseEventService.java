package com.konli.qms.service.qmsmgmt;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.qmsmgmt.entity.QmsAdverseEvent;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/** 不良事件管理。qms-mgmt.adverse.* */
public interface QmsAdverseEventService {

    PageResult<QmsAdverseEvent> page(String keyword, String eventType, String status, int page, int size);

    QmsAdverseEvent get(String id);

    QmsAdverseEvent create(QmsAdverseEvent event);

    QmsAdverseEvent update(QmsAdverseEvent event);

    void delete(String id);

    /** 处理流转: PENDING->HANDLING->DONE。 */
    void handle(String id, String status, String handleDesc, String owner);

    Map<String, Object> stats();

    /** 导出不良事件为 CSV(GBK, 带 BOM)。 */
    void exportCsv(HttpServletResponse response, String keyword, String eventType, String status);
}
