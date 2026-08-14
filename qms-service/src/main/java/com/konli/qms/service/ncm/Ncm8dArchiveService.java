package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.Qms8dArchivedReport;
import com.konli.qms.domain.ncm.entity.Qms8dReport;

/**
 * 8D 整改报告归档(15 年留存)。
 *
 * <p>归档逻辑参考 {@code FiaTaskServiceImpl} 的 generatePdf + buildReportHtml,
 * 在 8D 报告 D8 闭环或简易流程闭环时触发:生成 PDF(本地 logs/reports),计算 SHA-256,
 * 落 ops.qms_8d_archived_report。幂等:同一 reportId 重复归档时覆盖更新。</p>
 */
public interface Ncm8dArchiveService {

    /** 归档指定 8D 报告(按 reportId 幂等)。 */
    Qms8dArchivedReport archive(Qms8dReport report);

    /** 按 8D 报告主键查询最新一条归档记录(按归档日期倒序)。 */
    Qms8dArchivedReport getByReportId(String reportId);

    /**
     * 软作废:将指定报告的"已归档"记录置为"已作废"(保留记录与 PDF 留痕,不物理删除)。
     * 用于 8D 重开场景——报告退回进行中时,原归档不再有效。幂等:无已归档记录则直接返回。
     *
     * @param reportId 8D 报告主键
     * @param reason   作废原因(如重开原因),仅记系统日志
     */
    void invalidate(String reportId, String reason);

    /**
     * 历史数据补归档:将数据库中所有已闭环但未归档的 8D 报告补生成归档记录。
     * 复用 {@link #archive(Qms8dReport)} 的幂等逻辑(已归档则覆盖更新),可重复安全执行。
     *
     * @return 成功补归档的条数
     */
    int backfill();
}
