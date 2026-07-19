package com.konli.qms.service.fia;

import com.konli.qms.domain.fia.entity.FiaArchivedReport;
import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.service.fia.dto.FiaTaskVo;

import java.util.List;

public interface FiaTaskService {

    List<FiaTask> list();

    FiaTaskVo get(String id);

    FiaTask create(FiaTask task);

    void enterResults(String taskId, List<FiaInspItem> items);

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

    FiaArchivedReport getArchive(String taskId);
}
