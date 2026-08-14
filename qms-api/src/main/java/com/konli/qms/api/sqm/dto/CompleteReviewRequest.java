package com.konli.qms.api.sqm.dto;

import lombok.Data;

/**
 * 执行结果复核通过后闭环请求:conclusion 为人工兜底结论,
 * 留空时由后端按不符合项数自动判定(有不符合项→有条件通过,否则→通过)。
 */
@Data
public class CompleteReviewRequest {
    private String conclusion;
}
