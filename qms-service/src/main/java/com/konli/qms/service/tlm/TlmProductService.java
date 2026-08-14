package com.konli.qms.service.tlm;

import com.konli.qms.domain.tlm.vo.TlmProductCandidate;
import com.konli.qms.domain.tlm.vo.TlmProductDetail;

import java.util.List;

/** 工装关联产品: 真实来源为 MES 检验表(qms.material_inspection / qms.finished_goods_inspection)。 */
public interface TlmProductService {

    /** 候选产品清单(按 material_code 去重), 供关联选择。 */
    List<TlmProductCandidate> candidates(String keyword, String kind);

    /** 某产品编码在 MES 的检验记录明细(物料/半成品/成品合并, 按日期倒序)。 */
    List<TlmProductDetail> detail(String materialCode);
}
