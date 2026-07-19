package com.konli.qms.domain.sqm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 供应商资质(证书版本管理,临期预警)。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sqm_supplier_cert")
public class SqmSupplierCert extends BaseEntity {

    @TableField("org_id")
    private String orgId;

    @TableField("supplier_id")
    private String supplierId;

    @TableField("cert_type")
    private String certType;

    @TableField("cert_name")
    private String certName;

    @TableField("cert_no")
    private String certNo;

    @TableField("issue_date")
    private LocalDate issueDate;

    @TableField("expiry_date")
    private LocalDate expiryDate;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_hash")
    private String fileHash;

    @TableField("cert_version")
    private Integer certVersion;          // 证书版本(避让乐观锁 version)

    private String status;                // 生效/失效

    @TableField("warn_level")
    private String warnLevel;             // 预警级别
}
