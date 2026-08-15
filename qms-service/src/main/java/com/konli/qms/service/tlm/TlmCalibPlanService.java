package com.konli.qms.service.tlm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.tlm.entity.TlmCalibPlan;

import java.time.LocalDate;

/** 计量校准计划单服务(P1): 列表、到期自动生成、校准结果录入。 */
public interface TlmCalibPlanService {

    /** 校准计划单分页(GAUGE 器具, keyword 匹配器具编号/名称, status 过滤)。 */
    PageResult<TlmCalibPlan> page(String keyword, String status, int page, int size);

    /**
     * 到期自动生成校准计划单: 对 calib_due_date 落在 [today, today+leadDays] 的 GAUGE,
     * 若尚无可覆盖该周期的待执行计划则生成 PENDING 计划单, 并推送计量管理员。
     * @param leadDays 提前生成天数(默认 30)
     */
    void autoGenerate(int leadDays);

    /** 校准结果录入: 计划单置 DONE, 回写器具校准日期/到期(按 plan_cycle 或传入 cycle 推算), 并解锁。 */
    void recordResult(String planId, LocalDate calibDate, LocalDate calibDueDate, Integer calibCycle,
                      String upperLimit, String result, String remark, String certNo);

    /** 手动新建校准计划单(来源 MANUAL): 指定器具 + 计划周期 + 计划到期日。 */
    TlmCalibPlan createManual(String toolId, Integer planCycle, LocalDate planDueDate);
}
