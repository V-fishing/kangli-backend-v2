package com.konli.qms.service.sqm.dto;

import com.konli.qms.domain.sqm.entity.SqmChangeApproval;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import lombok.Data;

import java.util.List;

/** 变更单 + 会签明细(详情返回) */
@Data
public class SqmChangeOrderVo {
    private SqmChangeOrder order;
    private List<SqmChangeApproval> approvals;
}
