package com.konli.qms.service.fia.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 某工单「最近一次合格首件」视图(完工检验建单时 1:1 选择并带出生产字段)。
 */
@Data
public class EligibleFirstArticleVO {

    /** 首件任务主键 */
    private String taskId;

    /** 校验单号 */
    private String code;

    /** 工单号 */
    private String woNo;

    /** 产品名称 */
    private String productName;

    /** 物料编码 */
    private String partNo;

    /** 工序 */
    private String procName;

    /** 综合判定(合格/警告/不合格) */
    private String overallJudge;

    /** 任务状态(已完成=已放行可绑定; 其余为未放行,仅用于带出生产字段) */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 型号规格(从物料主数据按 part_no 带出,定死不可手填) */
    private String modelSpec;

    /** 生产日期(YYYY-MM-DD,按工单从 MES 成品表带出,无真实工单留空) */
    private String productionDate;

    /** 提交数量(按工单从 MES 成品表 submitted_qty 带出,无真实工单留空) */
    private BigDecimal submittedQty;

    /** 单位(从物料主数据按 part_no 带出,定死不可手填) */
    private String unit;
}
