package com.konli.qms.service.ncm;

import com.konli.qms.domain.ncm.entity.Qms8dApprovalConfig;

import java.util.List;

/** 8D 阶段审核配置:按公司配置哪些阶段需审核人签名及指定签批人。 */
public interface Ncm8dApprovalConfigService {

    /** 读取当前公司的完整配置(固定返回 D1-D8 共 8 条,缺省阶段按默认规则填充)。 */
    List<Qms8dApprovalConfig> getConfig();

    /** 保存当前公司的配置(全量覆盖,传入 D1-D8 共 8 条)。 */
    void saveConfig(List<Qms8dApprovalConfig> items);

    /** 某阶段是否需要审核人签名(供 8D 推进/审批流程判定)。 */
    boolean needApproval(String orgId, String stageCode);

    /** 某阶段配置的指定签批人(为空表示任意审核人即可)。 */
    String signerOf(String orgId, String stageCode);
}
