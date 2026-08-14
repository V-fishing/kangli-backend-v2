package com.konli.qms.service.patrol;

import com.konli.qms.domain.patrol.entity.PatlArchivedReport;

/**
 * 巡检任务归档(15 年留存)。
 *
 * <p>归档逻辑参考 {@code Ncm8dArchiveServiceImpl},在巡检任务全部点位完成或手动关闭时触发:
 * 生成 PDF(本地 logs/reports),计算 SHA-256,落 ops.patl_archived_report。幂等:
 * 同一 taskId 重复归档时覆盖更新。</p>
 */
public interface PatlArchiveService {

    /** 归档指定巡检任务(按 taskId 幂等)。 */
    PatlArchivedReport archive(String taskId);

    /** 按巡检任务主键查询归档记录。 */
    PatlArchivedReport getByTaskId(String taskId);

    /**
     * 历史数据补归档:将数据库中所有已完成但未归档的巡检任务补生成归档记录。
     * 复用 {@link #archive(String)} 的幂等逻辑(已归档则覆盖更新),可重复安全执行。
     *
     * @return 成功补归档的条数
     */
    int backfill();
}
