package com.konli.qms.service.fia.dto;

import java.util.List;

/**
 * 首件任务列表「产品→工序」二级树节点(去重汇总)。
 * 一级为产品(名称/料号/品类),二级为该产品的各工序。
 */
public class ProductTreeNode {
    /** 产品名称(去重键) */
    private String productName;
    /** 代表料号(该名称下取其一) */
    private String partNo;
    /** 品类 material/semi/product */
    private String category;
    /** 该产品的去重工序列表 */
    private List<String> procNames;

    public ProductTreeNode(String productName, String partNo, String category, List<String> procNames) {
        this.productName = productName;
        this.partNo = partNo;
        this.category = category;
        this.procNames = procNames;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getProcNames() {
        return procNames;
    }

    public void setProcNames(List<String> procNames) {
        this.procNames = procNames;
    }
}
