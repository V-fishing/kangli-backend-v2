package com.konli.qms.service.fia;

import com.konli.qms.domain.fia.dto.PreviewJudgeRequest;
import com.konli.qms.domain.fia.dto.PreviewJudgeResult;
import com.konli.qms.domain.fia.dto.StdTraceResult;
import com.konli.qms.service.fia.dto.FiaTaskVo;
import com.konli.qms.domain.fia.entity.FiaArchivedReport;
import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaTask;

import java.util.List;
import java.util.Map;

public interface FiaTaskService {

    List<FiaTask> list(String orgId, String status, String woNo);

    /** 按来源过滤: FACTORY(产线首件) / SUPPLIER(供应商来料首件) */
    List<FiaTask> listBySource(String source);

    FiaTaskVo get(String id);

    FiaTask create(FiaTask task);

    /**
     * 来料批次驱动:按 物料编码 + 供应商 + 工序 从标准库匹配检验标准。
     * 匹配优先级(逐级回退):
     * 1) 物料编码 + 供应商 + 工序
     * 2) 物料编码 + 供应商(忽略工序)
     * 3) 仅物料编码
     * 全部未命中返回 null。
     */
    FiaInspStd matchStd(String orgId, String partNo, String supplierId, String procName);

    void enterResults(String taskId, List<FiaInspItem> items);

    /**
     * 检验结果试算:依据检验项的标准规则(std_value/tolerance 或 enum_values/pass_values)预判合格/不合格。
     * 可匹配 → 系统判定(覆盖人工);不可匹配 → 返回 null,由前端走人工兜底。
     */
    List<PreviewJudgeResult> previewJudge(String taskId, PreviewJudgeRequest req);

    /**
     * 标准引用追溯:列出引用该标准(stdId)的首件任务;itemId 非空时精确到引用该标准项的任务。
     * 组织隔离在实体内处理。
     */
    StdTraceResult traceStd(String stdId, String itemId);

    /**
     * 检验人签名(密码校验 + 锁定)-> 待复核。
     * <p>itemId 非空(逐项签名模式):仅记"检验人签名-项X"日志,不改 task 状态;
     * itemId 为空(整单签名 / 逐项签完后的整单确认):设 inspectorId + 状态流转。</p>
     */
    void signInspector(String taskId, String password, String itemId);

    /**
     * 复核人签名 -> 两级:已完成+归档;三级:待批准。
     * <p>itemId 语义同 {@link #signInspector}。</p>
     */
    void signReviewer(String taskId, String password, String itemId);

    /** 批准人签名(三级第三签)-> 已完成+归档 */
    void signApprover(String taskId, String password);

    /**
     * 处置/放行路径:退货 / 返工 / 让步接收 / 紧急放行 / 豁免开工。
     * 退货/返工 -> 直接归档(不释放不写SPC);让步接收/紧急放行/豁免开工 -> 需质量主管审批,
     * 自动发起对应类型审批单(待审批),签名后任务置"审批中"挂起,审批通过才放行。
     */
    void setDisposition(String taskId, String disposition, String remark);

    /**
     * 审批通过后的放行:归档 + 首件CTQ数据写入SPC基准(已获批准,无论判定是否合格)。
     * 仅当任务处于"审批中"才执行,幂等安全。
     */
    void releaseAfterApproval(String taskId);

    /**
     * 审批驳回:任务置"已驳回",不放行不归档。仅"审批中"可驳回,幂等安全。
     */
    void rejectTask(String taskId);

    FiaArchivedReport getArchive(String taskId);

    /** 当前组织已归档报告列表(追溯归档页) */
    List<Map<String, Object>> listArchives();

    /** 某任务的全流程日志(首件检验时间线) */
    List<Map<String, Object>> getTaskLog(String taskId);
}
