package com.konli.qms.api.patrol.dto;

import lombok.Data;

import java.util.List;

/** 创建巡检路线请求(路线 + 点位 + 检查项) */
@Data
public class CreateRouteRequest {

    private String orgId;
    private String routeCode;
    private String routeName;
    private String procName;
    private String freq;
    private String status;

    private List<CheckpointInput> checkpoints;

    @Data
    public static class CheckpointInput {
        private Short seq;
        private String pointName;
        private String location;
        private Boolean needPhoto;
        private List<ItemInput> items;
    }

    @Data
    public static class ItemInput {
        private Short seq;
        private String itemName;
        private String checkType;
        private String stdValue;
        private String enumValues;
        private Boolean isRequired;
    }
}
