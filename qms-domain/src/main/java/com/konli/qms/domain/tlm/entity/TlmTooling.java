package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 工装管理主表: 工装夹具(TOOL) 与 监视测量设备(GAUGE) 合一资产台账。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_tooling")
public class TlmTooling extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("tool_no")
    private String toolNo;

    @TableField("tool_name")
    private String toolName;

    @TableField("tool_category")
    private String toolCategory;   // TOOL / GAUGE

    @TableField("tool_type")
    private String toolType;

    @TableField("risk_class")
    private String riskClass;      // I/II/III/IV (GAUGE)

    @TableField("verify_cycle")
    private String verifyCycle;    // 验证周期(工装夹具台账,如 一年)

    @TableField("quantity")
    private Integer quantity;      // 数量

    @TableField("material")
    private String material;       // 材质

    @TableField("remark")
    private String remark;         // 备注(工装夹具台账/设备总表)

    @TableField("owner_name")
    private String ownerName;      // 领用人(设备总表,纯文本)

    @TableField("admin_name")
    private String adminName;      // 设备管理员(设备总表,纯文本)

    @TableField("supplier_name")
    private String supplierName;   // 供应商/生产厂家(设备总表,纯文本)

    @TableField("process_id")
    private String processId;

    @TableField("proc_name")
    private String procName;       // 工序名称(与 process_id 同源，文本匹配防 ID 漂移)

    @TableField("product_code")
    private String productCode;

    @TableField("spec")
    private String spec;

    @TableField("supplier_id")
    private String supplierId;

    private String status;         // IN_USE/DISABLED/REPAIRING/SCRAPPED

    @TableField("location")
    private String location;

    @TableField("owner_id")
    private String ownerId;

    @TableField("admin_id")
    private String adminId;        // 设备管理员(GAUGE)

    @TableField("software_ver")
    private String softwareVer;    // 软件版本(GAUGE)

    @TableField("precision_val")
    private String precisionVal;   // 精度(GAUGE)

    @TableField("measure_point")
    private String measurePoint;   // 计量点位(GAUGE)

    @TableField("calib_date")
    private LocalDate calibDate;

    @TableField("calib_due_date")
    private LocalDate calibDueDate;

    @TableField("calib_cycle")
    private Integer calibCycle;

    @TableField("bind_count")
    private Integer bindCount;     // 已绑定工单次数

    @TableField("design_life")
    private Integer designLife;    // 寿命上限次数

    @TableField("next_maint_date")
    private LocalDate nextMaintDate;

    @TableField("maint_cycle")
    private Integer maintCycle;

    @TableField("cost")
    private java.math.BigDecimal cost;

    @TableField("inbound_date")
    private LocalDate inboundDate;

    @TableField("purchase_date")
    private LocalDate purchaseDate;

    @TableField("locked")
    private Boolean locked;
}
