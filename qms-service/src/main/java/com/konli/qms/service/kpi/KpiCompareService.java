package com.konli.qms.service.kpi;

import java.util.Map;

/**
 * 分公司 KPI 对比:按 A/B 组织分别聚合核心质量指标(8D/CAPA/不良/SPC/FIA/巡检),并列返回。
 */
public interface KpiCompareService {

    /**
     * @return { orgs:[{orgId,orgCode,orgName}...], items:[{key,name,type,values:{orgCode:val}...}] }
     */
    Map<String, Object> compare();
}
