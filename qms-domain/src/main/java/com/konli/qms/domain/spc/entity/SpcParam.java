package com.konli.qms.domain.spc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/** SPC 参数主数据(控制对象定义:规格限/子组大小/采集频率/图表类型) */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.spc_param")
public class SpcParam extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("param_name")
    private String paramName;

    @TableField("proc_name")
    private String procName;

    /** 所属工序ID(父级分组,可空=未分类);原 proc_name 保留为工位/说明。 */
    @TableField("process_id")
    private String processId;

    /** 是否可制图:规格不完整(无上下限且无目标值)则 false。 */
    @TableField("chartable")
    private Boolean chartable;

    private String unit;

    @TableField("spec_lower")
    private BigDecimal specLower;

    @TableField("spec_upper")
    private BigDecimal specUpper;

    @TableField("spec_text")
    private String specText;

    @TableField("target_value")
    private BigDecimal targetValue;

    @TableField("subgroup_size")
    private Integer subgroupSize;

    @TableField("collect_freq")
    private String collectFreq;

    @TableField("chart_type")
    private String chartType;

    /** 控制图全集(基础图码逗号分隔,如 'Xbar,R,S');由首件任务从标准项 chart_types 带入并经 normalizeToBasic 归一。 */
    @TableField("chart_candidates")
    private String chartCandidates;

    /** CPK 标准差算法:within(组内,默认) / overall(整体)。 */
    @TableField("sigma_method")
    private String sigmaMethod;

    /** 控制限/能力指数 σ 倍数(默认 3,可配 2 / 2.5 等)。 */
    @TableField("sigma_k")
    private BigDecimal sigmaK;

    @TableField("cpk_period")
    private String cpkPeriod;

    /** 数据形态: VARIABLE(计量型,对应 Xbar/R/S/I/MR) / ATTRIBUTE(计数型,对应 P/NP/C/U)。由 chartCandidates 推断,用于约束图类型不可混选。 */
    @TableField("data_type")
    private String dataType;

    @TableField("is_active")
    private Boolean isActive;

    /** 参数来源: SAMPLE(抽样任务流程派生/复制) / FIA_FIRST(产线/来料首件任务生成) / TOOLING(工装首件任务生成) / MANUAL(手动新建)。FIA_FIRST 与 TOOLING 均归属首件 SPC 视图,前端按来源筛选区分工装 SPC。 */
    @TableField("param_source")
    private String paramSource;

    /** 关联供应商:该工序参数归属的供应商,用于 CPK→供应商质量分联动(可空)。 */
    @TableField("supplier_id")
    private String supplierId;

    /** 关联的 FIA 检验标准项(可空);关联后 specLower/specUpper/targetValue/unit 从标准项派生。 */
    @TableField("fia_std_item_id")
    private String fiaStdItemId;

    /** 来源 FIA 任务检验项ID(可空);由 FIA 任务一键生成 SPC 参数时写入,用于去重。 */
    @TableField("src_item_id")
    private String srcItemId;

    /** 来源 FIA 任务单号(可空);首件派生时带入,用于采集页自动填充工单号。 */
    @TableField("src_wo_no")
    private String srcWoNo;

    /** 来源 FIA 任务批号(可空);首件派生时带入,用于采集页自动填充批次号。 */
    @TableField("src_batch_no")
    private String srcBatchNo;

    /** 关联的 SPC 标准线(可空);选择标准线后 specLower/specUpper/targetValue/unit 从标准线自动填充。 */
    @TableField("spec_standard_id")
    private String specStandardId;

    /** 关联工序显示名(非数据库字段,仅用于前端传输/展示)。 */
    @TableField(exist = false)
    private String processName;

    /** 关联的标准项显示名(非数据库字段,仅用于前端传输/展示)。 */
    @TableField(exist = false)
    private String fiaStdItemName;

    /** 关联的标准线显示名(非数据库字段,仅用于前端传输/展示)。 */
    @TableField(exist = false)
    private String specStandardName;

    /** 关联的产品列表(非数据库字段):参数↔产品多对多,由 ops.spc_param_product 回填。 */
    @TableField(exist = false)
    private List<SpcParamProduct> products;
}
