package com.konli.qms.service.fia.dto;

import lombok.Data;

/**
 * 完工检验 completion-gate 门禁校验结果。
 * 建单与放行前各调用一次,保证首件已放行且无待确认 SPC 报警才可继续。
 */
@Data
public class CompletionGateResult {

    /** 门禁是否通过(首件已放行 且 无待确认 SPC 报警) */
    private boolean passed;

    /** 该工单最近一条普通首件是否已合格放行 */
    private boolean firstArticleReleased;

    /** 最近一条普通首件任务 ID(无则 null) */
    private String firstArticleTaskId;

    /** 最近一条普通首件综合判定(合格/警告/不合格/无) */
    private String firstArticleJudge;

    /** 该工单是否存在待确认 SPC 报警 */
    private boolean spcAlarmPending;

    /** 待确认 SPC 报警数量 */
    private int spcAlarmCount;

    /** 提示信息(门禁不通过时的原因说明) */
    private String message;
}
