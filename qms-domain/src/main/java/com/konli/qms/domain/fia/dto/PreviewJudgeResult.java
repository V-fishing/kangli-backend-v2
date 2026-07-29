package com.konli.qms.domain.fia.dto;

import lombok.Data;

/** 检验结果试算结果:系统判定合格/不合格,或不可匹配(人工兜底)。 */
@Data
public class PreviewJudgeResult {
    private String id;        // fia_insp_item.id
    private String judge;     // 合格/不合格;不可匹配时为 null
    private boolean matchable;   // 系统能否依据规则判定
    private boolean autoJudged;  // 是否为系统自动判定(可匹配即 true)
}
