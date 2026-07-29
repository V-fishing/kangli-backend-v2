package com.konli.qms.service.fia.dto;

import lombok.Data;

@Data
public class FiaWoLockActiveDTO {
    private String woNo;
    private String lockReason;
    private String lockedAt;
    private String productName;
    private String lineName;
    private String taskCode;
}