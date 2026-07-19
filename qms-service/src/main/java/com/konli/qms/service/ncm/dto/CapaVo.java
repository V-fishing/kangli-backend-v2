package com.konli.qms.service.ncm.dto;

import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.domain.ncm.entity.QmsCapaAction;
import lombok.Data;

import java.util.List;

/** CAPA + 措施明细(详情返回) */
@Data
public class CapaVo {
    private QmsCapa capa;
    private List<QmsCapaAction> actions;
}
