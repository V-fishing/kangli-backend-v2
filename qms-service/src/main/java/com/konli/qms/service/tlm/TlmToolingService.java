package com.konli.qms.service.tlm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.tlm.entity.TlmRepair;
import com.konli.qms.domain.tlm.entity.TlmScrap;
import com.konli.qms.domain.tlm.entity.TlmTooling;

import java.util.List;

/** 工装台账: CRUD + 分页 + 送修/报废/绑定/锁定/异常查询。tlm.tooling.* */
public interface TlmToolingService {

    /** 分页查询(支持 编号/名称/类别 TOOL|GAUGE/状态 过滤, 组织隔离由 DataScopeInterceptor 处理)。 */
    PageResult<TlmTooling> page(String keyword, String category, String status,
                                String ownerId, int page, int size);

    TlmTooling get(String id);

    TlmTooling create(TlmTooling tooling);

    TlmTooling update(TlmTooling tooling);

    void delete(String id);

    /** 送修: 置状态 REPAIRING, 生成维修工单(PENDING)。 */
    void repair(String id, String faultDesc, String approverId);

    /** 维修工单填写措施: PENDING -> REPAIRING, 写 measure。 */
    void repairFill(String id, String measure);

    /** 维修完成(措施已填): REPAIRING -> DONE, 工装仍 REPAIRING 等待验证。 */
    void repairDone(String id);

    /** 发起报废: 生成报废单(PENDING), 指定审批人 approverId(进审批中心)。 */
    void scrap(String id, String scrapMethod, String reason, String approverId);

    /** 审批中心回调: 报废审批通过, 置工装 SCRAPPED + 报废单 DONE。 */
    void onScrapApproved(String scrapId);

    /** 审批中心回调: 报废审批驳回, 报废单 REJECTED, 工装维持原状态。 */
    void onScrapRejected(String scrapId);

    /** 审批中心回调: 维修审批通过, 工装 REPAIRING + 维修单 REPAIRING。 */
    void onRepairApproved(String repairId);

    /** 审批中心回调: 维修审批驳回, 维修单 REJECTED, 工装维持原状态。 */
    void onRepairRejected(String repairId);

    /** 不合格锁定/解锁。 */
    void lock(String id, boolean locked);

    /** 工装-工单绑定: 写入绑定记录 + bind_count +1, 达 design_life 自动 locked。 */
    void bind(String id, String woNo);

    /** 异常列表: 锁定 / 寿命超限(bind_count>=design_life) / 校准逾期(calib_due_date<now)。 */
    List<TlmTooling> abnormalList(String type);

    /** 报废单分页查询(支持 关键词/报废单号/状态 过滤)。 */
    PageResult<TlmScrap> scrapPage(String keyword, String scrapNo, String status, int page, int size);

    /** 维修完成: 状态恢复 IN_USE + 触发 FIA 首件检验任务。 */
    void onRepairCompleted(String id);

    /** 维修工单分页查询(支持 工装编号/名称关键词/状态 过滤)。 */
    PageResult<TlmRepair> repairPage(String keyword, String status, int page, int size);

    /** 计量看板: GAUGE 器具的 总数/合格(在期内)/限用预警(临期30天)/超期 统计。 */
    java.util.Map<String, Object> metroDashboard();

    /** 工装-工单绑定记录(含 GAUGE 校准状态快照), 供计量追溯反查。 */
    java.util.List<com.konli.qms.domain.tlm.entity.TlmToolWoBind> bindRecords(String toolId);
}
