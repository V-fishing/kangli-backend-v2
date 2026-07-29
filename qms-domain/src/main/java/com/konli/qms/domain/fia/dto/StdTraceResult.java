package com.konli.qms.domain.fia.dto;

import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaTask;
import lombok.Data;

import java.util.List;

/** 标准引用追溯结果:列出引用该标准的首件任务,及命中检验项(实测/判定)。 */
@Data
public class StdTraceResult {
    private String stdId;
    private List<StdTraceTask> tasks;

    /** 单个引用任务及其命中的检验项(按 std_item_id 过滤) */
    @Data
    public static class StdTraceTask {
        private FiaTask task;
        private List<FiaInspItem> items;
    }
}
