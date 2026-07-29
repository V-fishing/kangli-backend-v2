package com.konli.qms.domain.fia.dto;

import lombok.Data;

import java.util.List;

/** 检验结果试算请求:传入各检验项实测值,返回系统判定。 */
@Data
public class PreviewJudgeRequest {
    private List<PreviewJudgeItem> items;

    @Data
    public static class PreviewJudgeItem {
        private String id;            // fia_insp_item.id
        private String measuredValue; // 实测值
    }
}
