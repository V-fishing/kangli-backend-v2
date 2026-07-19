package com.konli.qms.api.fia.dto;

import lombok.Data;

import java.util.List;

/** 检验结果录入(每项 measured_value + judge) */
@Data
public class InspItemResultRequest {

    private List<Item> items;

    @Data
    public static class Item {
        private String id;          // fia_insp_item.id
        private String measuredValue;
        private String judge;       // 合格/不合格/-
    }
}
