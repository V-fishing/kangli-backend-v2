package com.konli.qms.domain.tlm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.konli.qms.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 工装-产品关联(来源: MES 真实产品, 按 material_code 聚合)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.tlm_tool_product")
public class TlmToolProduct extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("tool_id")
    private String toolId;

    /** MES 产品编码(material_inspection.material_code / finished_goods_inspection.material_code)。 */
    @TableField("product_code")
    private String productCode;

    /** 产品名称(取自 MES 源表 material_name/product_name)。 */
    @TableField("product_name")
    private String productName;

    /** 产品类型: MATERIAL / SEMI / FINISHED。 */
    @TableField("kind")
    private String kind;

    /** 规格型号(取自 MES 源表 spec_model/model_spec)。 */
    @TableField("spec_model")
    private String specModel;
}
