package com.konli.qms.service.sqm;

import com.konli.qms.common.api.PageResult;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import com.konli.qms.service.sqm.dto.SqmChangeOrderListVo;
import com.konli.qms.service.sqm.dto.SqmChangeOrderVo;

import java.util.List;
import java.util.Map;

/** 物料变更单:创建/查询/提交/会签审批/关闭。sqm.change.* */
public interface SqmChangeService {

    List<SqmChangeOrderListVo> list();

    PageResult<SqmChangeOrderListVo> listPage(String keyword, String status, String supplierId, int page, int size);

    SqmChangeOrderVo get(String id);

    /** 按变更单 ID 列表批量取详情(单次 SQL,避免 N+1)。 */
    Map<String, SqmChangeOrderVo> batchDetail(List<String> ids);

    SqmChangeOrder create(SqmChangeOrder order);

    /** 提交申请(待申请 -> 审批中)。 */
    void submit(String id);

    /**
     * 会签审批(质量/采购/研发并行 + 质量一票否决 + 试产串行)。
     *
     * @param id            变更单 ID
     * @param approvalRole  审批角色 quality/purchase/rd/trial
     * @param approved      是否通过
     * @param opinion       审批意见
     */
    void approve(String id, String approvalRole, boolean approved, String opinion);

    /** 关闭变更单(解冻收货)。 */
    void close(String id);

    /** SR-SCM:加严检验不合格->回滚变更(恢复冻结,标记已回滚)。 */
    void rollback(String id, String reason);

    /**
     * 并行会签电子签名校验(采用与首件检验录入相同的「用户名+密码」验证)。
     * 校验签名用户的口令(复用 PasswordEncoder),并确认该角色属于本变更单的会签记录。
     *
     * @param changeId      变更单 ID
     * @param approvalRole  会签角色 quality/purchase/rd
     * @param username      签名用户名
     * @param password      签名口令(明文,由后端比对)
     */
    void verifySign(String changeId, String approvalRole, String username, String password);
}
