package com.konli.qms.domain.tlm.vo;

import lombok.Data;

/** MES 候选产品(按 material_code 去重)。 */
@Data
public class TlmProductCandidate {
    private String materialCode;
    private String productName;
    private String specModel;
    private String kind; // MATERIAL / SEMI / FINISHED
}
