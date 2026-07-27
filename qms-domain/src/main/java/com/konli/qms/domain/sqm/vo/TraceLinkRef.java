package com.konli.qms.domain.sqm.vo;

import lombok.Data;

/**
 * 追溯组成关系引用（用于树/详情展示某节点的上级(被使用)或下级(组成)节点）
 */
@Data
public class TraceLinkRef {
    private String id;
    private String nodeType;
    private String nodeName;
    private String batchNo;
}
