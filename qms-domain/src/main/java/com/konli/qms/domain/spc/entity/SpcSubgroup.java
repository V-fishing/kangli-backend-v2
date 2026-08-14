package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SPC 子组(分区表,仅 insert + select,不做 update/delete)。
 * 一个子组聚合 n 个测量值,产出 xbar / rangeR。
 */
@Data
@TableName("ops.spc_subgroup")
public class SpcSubgroup {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("param_id")
    private String paramId;

    @TableField("subgroup_no")
    private Integer subgroupNo;

    @TableField("subgroup_time")
    private LocalDateTime subgroupTime;

    /** @deprecated 班次字段已废弃,保留映射兼容历史数据 */
    @Deprecated
    private String shift;

    private Integer n;

    private BigDecimal xbar;

    @TableField("range_r")
    private BigDecimal rangeR;

    /** 子组标准差(支持 Xbar-S 图的 S 图);计量型采集时由测量值计算写入。 */
    @TableField("std_dev")
    private BigDecimal stdDev;

    /** 子组不合格数(计数型 P/NP 图);非计数型为 null。 */
    @TableField("nonconforming")
    private Integer nonconforming;

    /** 子组检验总数(计数型 P/NP 图样本量 n);非计数型为 null。 */
    @TableField("inspect_n")
    private Integer inspectN;

    /** 子组缺陷数(计数型 C/U 图);非计数型为 null。 */
    @TableField("defect_count")
    private Integer defectCount;

    private String judge;

    @TableField("is_outlier")
    private Boolean isOutlier;

    @TableField("outlier_rule")
    private String outlierRule;     // 命中判异规则编号(①-⑧)

    @TableField("data_source")
    private String dataSource;

    /** SPC 数据阶段: FIRST=首件能力验证(点状/一次性); ROUTINE=量产过程监控(线状/持续)。默认 ROUTINE。 */
    @TableField("stage")
    private String stage;

    @TableField("operator_id")
    private String operatorId;

    @TableField("wo_no")
    private String woNo;

    @TableField("batch_no")
    private String batchNo;

    @TableField("task_id")
    private String taskId;

    /** 关联抽样批次任务(ROUTINE 量产采集载体);首件仍为 task_id。 */
    @TableField("sample_task_id")
    private String sampleTaskId;

    @TableField("product_code")
    private String productCode;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("created_by")
    private String createdBy;

    /** 录入人姓名(由 operator_id 解析,虚拟字段不落库)。 */
    @TableField(exist = false)
    private String operatorName;

    /** 创建人姓名(由 created_by 解析,虚拟字段不落库)。 */
    @TableField(exist = false)
    private String createdByName;
}
