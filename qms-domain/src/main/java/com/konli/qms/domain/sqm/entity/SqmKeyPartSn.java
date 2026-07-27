package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关键件 SN(逐件追溯,非关键件走树状)。
 * sn_code 唯一;lot_id 关联 sqm_incoming_lot。
 * 无审计字段。
 */
@Data
@TableName("ops.sqm_key_part_sn")
public class SqmKeyPartSn {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("org_id")
    private String orgId;

    @TableField("sn_code")
    private String snCode;

    @TableField("lot_id")
    private String lotId;

    @TableField("part_no")
    private String partNo;

    @TableField("part_name")
    private String partName;

    @TableField("supplier_id")
    private String supplierId;

    private String status;                // VARCHAR(16) 在线/入库/出货

    private String line;

    @TableField("wo_no")
    private String woNo;

    @TableField("scan_time")
    private LocalDateTime scanTime;
}
