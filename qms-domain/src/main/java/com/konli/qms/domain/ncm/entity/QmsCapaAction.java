package com.konli.qms.domain.ncm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/** CAPA 措施明细(子表,无审计字段;按 seq 排序)。 */
@Data
@TableName("ops.qms_capa_action")
public class QmsCapaAction {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("capa_id")
    private String capaId;

    private Short seq;

    @TableField("action_text")
    private String actionText;

    private Boolean done;

    @TableField("complete_date")
    private LocalDate completeDate;
}
