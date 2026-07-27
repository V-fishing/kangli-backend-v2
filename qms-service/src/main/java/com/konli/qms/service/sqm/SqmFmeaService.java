package com.konli.qms.service.sqm;

import com.konli.qms.domain.sqm.entity.QmsFmeaRisk;
import com.konli.qms.domain.sqm.entity.QmsFmeaRiskTrack;

import java.util.List;

/**
 * FMEA 风险跟踪：FMEA 分析与高风险闭环流程。
 * 闭环阶段：识别 → 措施分配 → 措施验证 → 闭环；每次流转写入 {@link QmsFmeaRiskTrack} 轨迹。
 */
public interface SqmFmeaService {

    /** FMEA 类型枚举(PFMEA/DFMEA/SFMEA)。 */
    List<String> listTypes();

    /** 依据 S/O/D 预测 RPN、风险等级与是否高风险。 */
    java.util.Map<String, Object> predict(int severity, int occurrence, int detection);

    /** 列表(按当前组织过滤；管理员全量)。可选 status 过滤。 */
    List<QmsFmeaRisk> list(String status);

    /** 新建风险项：自动计算 RPN/风险等级/高风险标识/风险编号，并记录"识别"轨迹。 */
    QmsFmeaRisk create(QmsFmeaRisk risk);

    /** 更新风险项(措施分配 / 评分更新 / 状态流转)，必要时记录轨迹。 */
    QmsFmeaRisk update(String id, QmsFmeaRisk risk);

    /**
     * 高风险闭环：须满足 SR-PTL-024 —— 已提交证据(evidence 非空)且
     * 已确认"3 个月无复发"(recurrenceVerified=true)，方可将状态置为"已闭环"。
     */
    QmsFmeaRisk close(String id, String evidence, String actionNote, boolean recurrenceVerified, String operator);

    /** 某风险项的闭环轨迹(按时间升序)。 */
    List<QmsFmeaRiskTrack> tracks(String id);

    /** SR-PTL-027:验证期内再发生->重新打开FMEA。status已闭环->进行中,允许更新评分。 */
    QmsFmeaRisk reopen(String id, String reason);

    /** SR-PTL-025:扫描超期措施(targetDate已过且status≠已闭环),超7天通知责任人,超14天通知质量经理。返回处理条数。 */
    int scanOverdue();
}
