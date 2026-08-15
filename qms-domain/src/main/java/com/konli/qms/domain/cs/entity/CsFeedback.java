package com.konli.qms.domain.cs.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 客户反馈(投诉/建议/表扬/咨询),手动录入并跟踪处理。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.cs_feedback")
public class CsFeedback extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("customer_name")
    private String customerName;

    @TableField("customer_contact")
    private String customerContact;

    @TableField("fb_type")
    private String fbType;          // COMPLAINT / SUGGESTION / PRAISE / INQUIRY

    @TableField("content")
    private String content;

    @TableField("related_wo_no")
    private String relatedWoNo;

    @TableField("status")
    private String status;          // OPEN / HANDLING / DONE

    @TableField("handle_detail")
    private String handleDetail;

    @TableField("handle_at")
    private LocalDateTime handleAt;

    @TableField("owner_name")
    private String ownerName;

    @TableField("satisfaction")
    private Integer satisfaction;

    /** 低分诱因维度(需求 2.4.2.2): RESPONSE_SLOW / REPAIR_INCOMPLETE / ATTITUDE / OTHER。 */
    @TableField("cause")
    private String cause;

    /** 联动 NCM 8D/CAPA 纠正措施 ID(需求 2.4.2.5 质量改进闭环)。 */
    @TableField("related_ncm_id")
    private String relatedNcmId;

    /** 触发 8D 报告 ID(需求 2.4.2.5 从反馈直接发起 8D)。 */
    @TableField("related_8d_id")
    private String related8dId;

    /** 触发 CAPA ID(需求 2.4.2.5 从反馈直接发起 CAPA)。 */
    @TableField("related_capa_id")
    private String relatedCapaId;

    /** 触发 CA 纠正措施 ID(需求 2.4.2.5 从反馈直接发起 CA)。 */
    @TableField("related_ca_id")
    private String relatedCaId;
}
