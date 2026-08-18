package com.konli.qms.service.archive;

import com.konli.qms.common.api.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 统一归档查询(跨模块)。复用 {@code sqm.audit.list} / {@code fia.task.list} 权限(不新增)。
 *
 * <p>跨表 UNION 查询用 JdbcTemplate(参考 {@code SqmAnalysisServiceImpl} 的聚合写法)。
 * 目前覆盖:
 * <ul>
 *   <li>{@code ops.fia_archived_report}(首件检验归档)</li>
 *   <li>{@code ops.sqm_audit_report_archive}(审核报告归档)</li>
 * </ul>
 * 8D 归档表暂未建,type=8d 返回空列表。</p>
 */
public interface ArchiveService {

    /**
     * 统一归档查询。
     *
     * @param type    归档类型:fia / audit / 8d;null 或空 = 全部(UNION)
     * @param keyword 模糊匹配 reportNo/woNo/archiveNo;null/空 = 不限
     * @param page    页码(从 1 开始),null = 1
     * @param size    每页条数,null = 20
     * @return 分页结果:{records:[{archiveType, archiveNo, refId, refNo, archiveDate, retentionUntil, reportHash}], total, page, size}
     */
    PageResult<Map<String, Object>> list(String type, String keyword, Integer page, Integer size);

    /**
     * 留存到期提醒:查所有归档表中 retentionUntil <= now()+days 的记录。
     *
     * @param days 提前天数(默认 30)
     * @return [{archiveType, archiveNo, refId, retentionUntil, daysRemaining}]
     */
    List<Map<String, Object>> expiring(Integer days);

    /**
     * 档案详情:按 type + refId 查对应归档表全量字段及关联业务信息。
     * @return 档案元数据 Map;查不到返回 null
     */
    Map<String, Object> detail(String type, String refId);

    /**
     * 返回档案 PDF 的本地路径(pdf_ref);占位/不存在返回 null。
     */
    String pdfRef(String type, String refId);
}
