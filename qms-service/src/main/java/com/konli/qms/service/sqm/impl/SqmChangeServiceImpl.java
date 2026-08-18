package com.konli.qms.service.sqm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import org.springframework.util.StringUtils;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.DataScopeGuard;
import com.konli.qms.domain.sqm.entity.SqmAuditPlan;
import com.konli.qms.domain.sqm.entity.SqmChangeApproval;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import com.konli.qms.domain.sqm.mapper.SqmChangeApprovalMapper;
import com.konli.qms.domain.sqm.mapper.SqmChangeOrderMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.sqm.SqmAuditService;
import com.konli.qms.service.sqm.SqmChangeStrictInspectService;
import com.konli.qms.domain.sqm.entity.SqmChangeStrictInspect;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.service.fia.impl.FiaStdVersionService;
import com.konli.qms.domain.sqm.entity.SqmSupplier;
import com.konli.qms.domain.sqm.mapper.SqmSupplierMapper;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.sqm.SqmChangeService;
import com.konli.qms.service.fia.FiaTaskService;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcSubgroupMapper;
import com.konli.qms.service.sqm.dto.SqmChangeOrderListVo;
import com.konli.qms.service.sqm.dto.SqmChangeOrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqmChangeServiceImpl implements SqmChangeService {

    private final SqmChangeOrderMapper sqmChangeOrderMapper;
    private final SqmChangeApprovalMapper sqmChangeApprovalMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final SqmAuditService sqmAuditService;
    private final JdbcTemplate jdbcTemplate;
    private final SqmChangeStrictInspectService sqmChangeStrictInspectService;
    private final FiaStdVersionService fiaStdVersionService;
    private final SqmSupplierMapper sqmSupplierMapper;
    private final NotificationService notificationService;
    private final FiaTaskService fiaTaskService;
    private final SpcSubgroupMapper spcSubgroupMapper;

    /**
     * 解析当前用户所属组织;当为空/超管哨兵(ROOT)/非法时,回退取默认组织。
     * 与 FMEA/供应商模块对齐,避免 admin(orgId=null) 触发 NOT NULL 约束 -> 500。
     */
    private String currentOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        String orgId = (u != null) ? u.orgId() : null;
        if (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) {
            return resolveDefaultOrgId();
        }
        return orgId;
    }

    private String resolveDefaultOrgId() {
        try {
            String id = jdbcTemplate.queryForObject(
                    "SELECT id::text FROM ops.sys_org WHERE org_code='MZ' LIMIT 1", String.class);
            if (id != null) {
                return id;
            }
        } catch (Exception ignored) {
            // 忽略
        }
        try {
            return jdbcTemplate.queryForObject("SELECT id::text FROM ops.sys_org LIMIT 1", String.class);
        } catch (Exception e) {
            log.warn("resolveDefaultOrgId failed: {}", e.getMessage());
            return null;
        }
    }


    @Override
    public List<SqmChangeOrderListVo> list() {
        List<SqmChangeOrder> orders = sqmChangeOrderMapper.selectList(
                new LambdaQueryWrapper<SqmChangeOrder>().orderByDesc(SqmChangeOrder::getApplyDate));
        return orders.stream().map(this::toListVo).collect(Collectors.toList());
    }

    @Override
    public PageResult<SqmChangeOrderListVo> listPage(String keyword, String status, String supplierId, int page, int size) {
        LambdaQueryWrapper<SqmChangeOrder> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(SqmChangeOrder::getChangeNo, keyword)
                    .or().like(SqmChangeOrder::getTitle, keyword)
                    .or().like(SqmChangeOrder::getPartNo, keyword));
        }
        if (StringUtils.hasText(status)) {
            qw.eq(SqmChangeOrder::getStatus, status);
        }
        if (StringUtils.hasText(supplierId)) {
            qw.eq(SqmChangeOrder::getSupplierId, supplierId);
        }
        qw.orderByDesc(SqmChangeOrder::getApplyDate);
        IPage<SqmChangeOrder> ip = sqmChangeOrderMapper.selectPage(new Page<>(page, size), qw);
        List<SqmChangeOrderListVo> vos = ip.getRecords().stream().map(this::toListVo).collect(Collectors.toList());
        return new PageResult<>(vos, ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    private SqmChangeOrderListVo toListVo(SqmChangeOrder o) {
        SqmChangeOrderListVo v = new SqmChangeOrderListVo();
        v.setId(o.getId());
        v.setChangeNo(o.getChangeNo());
        v.setTitle(o.getTitle());
        v.setSupplierId(o.getSupplierId());
        v.setSupplierName(supplierName(o.getSupplierId()));
        v.setPartNo(o.getPartNo());
        v.setChangeType(o.getChangeType());
        v.setUrgency(o.getUrgency());
        v.setStatus(o.getStatus());
        v.setApplicant(o.getApplicant());
        v.setApplyDate(o.getApplyDate() == null ? null : o.getApplyDate().toString());
        v.setReason(o.getReason());
        v.setVerifyReport(o.getVerifyReport());
        v.setRiskFile(o.getRiskFile());
        v.setRiskPreMark(o.getRiskPreMark());
        v.setOldPartNo(o.getOldPartNo());
        v.setNewPartNo(o.getNewPartNo());
        v.setEffDate(o.getEffDate() == null ? null : o.getEffDate().toString());
        v.setSwitchDate(o.getSwitchDate() == null ? null : o.getSwitchDate().toString());
        v.setImpactDesc(o.getImpactDesc());
        v.setCustomerNotify(o.getCustomerNotify());
        v.setCustomerApproved(o.getCustomerApproved());
        return v;
    }

    private String supplierName(String supplierId) {
        if (supplierId == null) return "—";
        SqmSupplier s = sqmSupplierMapper.selectById(supplierId);
        return s != null ? s.getName() : "—";
    }

    @Override
    public SqmChangeOrderVo get(String id) {
        SqmChangeOrder order = sqmChangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "变更单不存在");
        }
        SqmChangeOrderVo vo = new SqmChangeOrderVo();
        vo.setOrder(order);
        List<SqmChangeApproval> approvals = sqmChangeApprovalMapper.selectList(
                new LambdaQueryWrapper<SqmChangeApproval>()
                        .eq(SqmChangeApproval::getChangeId, id)
                        .orderByAsc(SqmChangeApproval::getSeqOrder));
        resolveOperators(approvals);
        vo.setApprovals(approvals);
        try {
            vo.setStrictInspects(sqmChangeStrictInspectService.list(id));
        } catch (Exception e) {
            log.warn("加载加严检验记录失败, changeId={}: {}", id, e.getMessage());
            vo.setStrictInspects(List.of());
        }
        return vo;
    }

    @Override
    public Map<String, SqmChangeOrderVo> batchDetail(List<String> ids) {
        Map<String, SqmChangeOrderVo> map = new LinkedHashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }
        List<SqmChangeOrder> orders = sqmChangeOrderMapper.selectBatchIds(ids);
        List<SqmChangeApproval> allApprovals = sqmChangeApprovalMapper.selectList(
                new LambdaQueryWrapper<SqmChangeApproval>()
                        .in(SqmChangeApproval::getChangeId, ids)
                        .orderByAsc(SqmChangeApproval::getSeqOrder));
        Map<String, List<SqmChangeApproval>> approvalMap = allApprovals.stream()
                .collect(Collectors.groupingBy(SqmChangeApproval::getChangeId));
        for (SqmChangeOrder order : orders) {
            SqmChangeOrderVo vo = new SqmChangeOrderVo();
            vo.setOrder(order);
            List<SqmChangeApproval> apps = approvalMap.getOrDefault(order.getId(), Collections.emptyList());
            resolveOperators(apps);
            vo.setApprovals(apps);
            map.put(order.getId(), vo);
        }
        return map;
    }

    @Override
    @Transactional
    public SqmChangeOrder create(SqmChangeOrder order) {
        // 注入租户上下文与必填默认值(表中 org_id/apply_date/source/urgency 均为 NOT NULL,前端不传)
        order.setOrgId(currentOrgId());
        order.setChangeNo("ECN-" + System.currentTimeMillis());
        order.setStatus("待申请");
        // 供应商为物料变更单必要主体,DB 列 NOT NULL;缺 supplierId 给出明确 400 而非 DB 约束 500
        if (order.getSupplierId() == null || order.getSupplierId().isBlank()) {
            throw new BusinessException(400, "供应商不能为空");
        }
        // 物料编码也为 NOT NULL 列;若未指定置为占位值(后续可由用户补填)
        if (order.getPartNo() == null || order.getPartNo().isBlank()) {
            order.setPartNo("TBD-" + order.getChangeNo());
        }
        if (order.getApplyDate() == null) {
            order.setApplyDate(LocalDate.now());
        }
        if (order.getSource() == null || order.getSource().isBlank()) {
            order.setSource("门户提报");
        }
        if (order.getUrgency() == null || order.getUrgency().isBlank()) {
            order.setUrgency("中");
        }
        if (order.getStrictFlag() == null) {
            order.setStrictFlag(false);
        }
        if (order.getReceiveFrozen() == null) {
            order.setReceiveFrozen(false);
        }
        // SR-SCM:高风险(riskPreMark=高)强制加严检验+小批试产
        if ("高".equals(order.getRiskPreMark())) {
            order.setStrictFlag(true);
        }
        sqmChangeOrderMapper.insert(order);
        // 预建三方依次签字会签记录:采购(seq=1) -> 研发(seq=2) -> 质量(seq=3,一票否决)
        String[][] roles = {
                {"purchase", "采购", "1"},
                {"rd", "研发", "2"},
                {"quality", "质量", "3"}};
        for (String[] role : roles) {
            SqmChangeApproval ap = new SqmChangeApproval();
            ap.setOrgId(order.getOrgId());
            ap.setChangeId(order.getId());
            ap.setApprovalRole(role[0]);
            ap.setRoleLabel(role[1]);
            ap.setStatus("pending");
            ap.setHasVeto("quality".equals(role[0]));
            ap.setSeqOrder(Integer.parseInt(role[2]));
            sqmChangeApprovalMapper.insert(ap);
        }
        return order;
    }

    @Override
    @Transactional
    public void submit(String id) {
        SqmChangeOrder order = sqmChangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "变更单不存在");
        }
        if (!"待申请".equals(order.getStatus())) {
            throw new BusinessException(400, "变更单当前状态为 " + order.getStatus() + ",无法提交");
        }
        DataScopeGuard.ensureOwner(order.getOrgId());
        // 用已加载实体更新(带 @Version,乐观锁生效);返回 0 行表示并发冲突
        order.setStatus("审批中");
        order.setReceiveFrozen(true); // SR-SCM:提交即冻结收货
        if (sqmChangeOrderMapper.updateById(order) == 0) {
            throw new BusinessException(409, "变更单已被他人修改,请刷新后重试");
        }
        // 物料变更申请提交后联动: 自动生成"物料变更审核"审核计划(由供应商发起,进入供应商物料变更管理流程)
        createMaterialChangeAudit(order);
        // 提交后通知三方(采购/研发/质量)待审批
        notifySubmitted(order);
    }

    /**
     * 物料变更联动: 供应商物料变更申请提交后,自动生成一条「物料变更审核」审核计划。
     * 该计划进入审核模块由质量/采购/研发执行现场审核,完成后流入供应商物料变更管理流程(试产验证等)。
     * 异常(如审核服务不可用)不阻断变更主流程。
     */
    private void createMaterialChangeAudit(SqmChangeOrder order) {
        try {
            SqmAuditPlan plan = new SqmAuditPlan();
            plan.setOrgId(order.getOrgId());
            plan.setSupplierId(order.getSupplierId());
            plan.setAuditType("物料变更审核");
            plan.setPlanDate(LocalDate.now());
            plan.setAuditLead("质量");
            plan.setAuditorTeam("质量,采购,研发");
            plan.setScope("物料变更[" + order.getChangeNo() + "] " + (order.getTitle() == null ? "" : order.getTitle()));
            plan.setRiskLevel(order.getRiskPreMark() != null ? order.getRiskPreMark()
                    : (order.getUrgency() != null ? order.getUrgency() : "中"));
            plan.setStatus("待执行");
            // 双向追溯: 关联来源变更单(保留标准 UUID 格式, 与变更单列表 API 返回的 id 一致)
            plan.setChangeId(order.getId() == null ? null : String.valueOf(order.getId()));
            sqmAuditService.createPlanInNewTx(plan);
            log.info("物料变更提交联动生成审核计划成功, changeId={}, auditType=物料变更审核, planNo={}",
                    order.getId(), plan.getPlanNo());
        } catch (Exception e) {
            log.warn("物料变更提交联动生成审核计划失败, changeId={}: {}", order.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void approve(String id, String approvalRole, boolean approved, String opinion) {
        SqmChangeOrder order = sqmChangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "变更单不存在");
        }
        if (!"审批中".equals(order.getStatus())) {
            throw new BusinessException(400, "变更单当前状态为 " + order.getStatus() + ",无法审批");
        }
        DataScopeGuard.ensureOwner(order.getOrgId());

        // 强制串行:当前必须由 seqOrder 最小且未审批的节点处理(采购 -> 研发 -> 质量)
        List<SqmChangeApproval> all = sqmChangeApprovalMapper.selectList(
                new LambdaQueryWrapper<SqmChangeApproval>()
                        .eq(SqmChangeApproval::getChangeId, id)
                        .orderByAsc(SqmChangeApproval::getSeqOrder));
        SqmChangeApproval expected = all.stream()
                .filter(a -> "pending".equals(a.getStatus()))
                .min(Comparator.comparingInt(a -> a.getSeqOrder() == null ? 99 : a.getSeqOrder()))
                .orElse(null);
        if (expected == null) {
            throw new BusinessException(400, "无待审批节点");
        }
        if (!expected.getApprovalRole().equals(approvalRole)) {
            throw new BusinessException(400,
                    "请按 采购→研发→质量 顺序审批:当前应由【" + expected.getRoleLabel() + "】审批");
        }

        // 更新当前会签记录
        expected.setStatus(approved ? "done" : "rejected");
        expected.setOperator(currentOperator());
        expected.setOperateDate(LocalDateTime.now());
        expected.setOpinion(opinion);
        sqmChangeApprovalMapper.updateById(expected);

        // 任一驳回 -> 立即终止(质量一票否决在末位自然生效)
        if (!approved) {
            order.setStatus("已驳回");
            if (sqmChangeOrderMapper.updateById(order) == 0) {
                throw new BusinessException(409, "变更单已被他人修改,请刷新后重试");
            }
            notifyRejected(order, expected.getRoleLabel());
            return;
        }

        // 全部通过 -> 已批准
        List<SqmChangeApproval> fresh = sqmChangeApprovalMapper.selectList(
                new LambdaQueryWrapper<SqmChangeApproval>()
                        .eq(SqmChangeApproval::getChangeId, id));
        boolean allDone = fresh.stream().allMatch(a -> "done".equals(a.getStatus()));
        if (allDone) {
            order.setStatus("已批准");
            if (sqmChangeOrderMapper.updateById(order) == 0) {
                throw new BusinessException(409, "变更单已被他人修改,请刷新后重试");
            }
            // 变更批准后联动 FIA 检验标准(旧标准停用 + 新版本草稿,待质量审核后生效)
            try {
                fiaStdVersionService.syncVersion(order);
            } catch (Exception e) {
                log.warn("变更联动FIA标准失败, changeId={}: {}", order.getId(), e.getMessage(), e);
            }
            // 变更→来料加严检验联动:批准后自动创建3批加严检验(独立事务,失败不影响审批)
            try {
                SqmChangeStrictInspect si = new SqmChangeStrictInspect();
                si.setOrgId(order.getOrgId());
                si.setChangeId(order.getId());
                si.setStrictNo("ST-" + System.currentTimeMillis());
                si.setInspectType("加严");
                si.setAqlLevel("II");
                si.setSeq(1);
                si.setTotalSeq(3);
                sqmChangeStrictInspectService.createInNewTx(si);
            } catch (Exception ignored) {}
            notifyApproved(order);
        } else {
            // 通知下一位审批人
            SqmChangeApproval next = fresh.stream()
                    .filter(a -> "pending".equals(a.getStatus()))
                    .min(Comparator.comparingInt(a -> a.getSeqOrder() == null ? 99 : a.getSeqOrder()))
                    .orElse(null);
            if (next != null) {
                notifyNext(order, next.getApprovalRole());
            }
        }
    }

    @Override
    public void verifySign(String changeId, String approvalRole, String username, String password) {
        if (username == null || username.isBlank() || password == null) {
            throw new BusinessException(400, "签名用户名与口令不能为空");
        }
        // 1) 校验签名用户存在且口令正确(与首件检验录入一致:PasswordEncoder 比对)
        SysUser u = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username.trim()));
        if (u == null) {
            throw new BusinessException(400, "签名用户不存在: " + username);
        }
        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            throw new BusinessException(400, "签名口令错误,电子签名校验未通过");
        }
        // 2) 校验该会签角色属于本变更单(防止越权签名)
        SqmChangeApproval ap = sqmChangeApprovalMapper.selectOne(
                new LambdaQueryWrapper<SqmChangeApproval>()
                        .eq(SqmChangeApproval::getChangeId, changeId)
                        .eq(SqmChangeApproval::getApprovalRole, approvalRole));
        if (ap == null) {
            throw new BusinessException(404, "未找到该角色的会签记录: " + approvalRole);
        }
    }

    @Transactional
    public void close(String id) {
        SqmChangeOrder order = sqmChangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "变更单不存在");
        }
        DataScopeGuard.ensureOwner(order.getOrgId());

        // 闭环门禁:变更归档前必须完成「首件 FIA 验证 + SPC 连续稳定 + 绑定一致性」校验
        // (范式A:三方会签已在审批中完成,此处只校验验证结果,不重复人工审核)
        assertVerificationGate(order);

        order.setStatus("已关闭");
        order.setReceiveFrozen(false); // SR-SCM:关闭即解冻收货,恢复正常
        if (sqmChangeOrderMapper.updateById(order) == 0) {
            throw new BusinessException(409, "变更单已被他人修改,请刷新后重试");
        }
    }

    /**
     * 变更归档门禁:校验该变更单关联的首件 FIA 已合格放行、对应 SPC 连续稳定,且绑定一致。
     * 四者绑定锚点 = supplier_id:变更单.supplier_id == 首件.supplier_id。
     * SPC 经 change_id -> fia_task -> task_id 间接绑定,禁止凭工单/料号模糊匹配。
     * 任一不满足抛 BusinessException 拒绝归档。
     */
    private void assertVerificationGate(SqmChangeOrder order) {
        String changeId = order.getId();
        FiaTask fia = fiaTaskService.findByChangeId(changeId);
        if (fia == null) {
            throw new BusinessException(400, "该物料变更尚未创建关联首件检验任务(FIA),不可归档。"
                    + "请通过「创建首件任务」生成首件并完成验证");
        }
        // 断言①:首件已合格放行(已完成 + 合格/警告)
        boolean passed = "已完成".equals(fia.getStatus())
                && fia.getOverallJudge() != null
                && ("合格".equals(fia.getOverallJudge()) || "警告".equals(fia.getOverallJudge()));
        if (!passed) {
            throw new BusinessException(400, "关联首件检验任务" + fia.getCode() + "尚未合格放行(状态="
                    + fia.getStatus() + ",判定=" + fia.getOverallJudge() + "),不可归档");
        }
        // 断言②:绑定一致性(供应商锚点)
        if (order.getSupplierId() != null && fia.getSupplierId() != null
                && !order.getSupplierId().equals(fia.getSupplierId())) {
            throw new BusinessException(400, "首件供应商与变更单不一致(变更单供应商="
                    + order.getSupplierId() + ",首件供应商=" + fia.getSupplierId() + "),绑定关系异常,不可归档");
        }
        // 断言③:SPC 经 change_id -> fia_task -> task_id 反查,ROUTINE 阶段最近 N 批连续合格且无异常点
        List<SpcSubgroup> subgroups = spcSubgroupMapper.selectList(new LambdaQueryWrapper<SpcSubgroup>()
                .eq(SpcSubgroup::getTaskId, fia.getId())
                .eq(SpcSubgroup::getStage, "ROUTINE")
                .orderByDesc(SpcSubgroup::getSubgroupTime)
                .last("LIMIT 25"));
        if (subgroups.isEmpty()) {
            throw new BusinessException(400, "关联首件" + fia.getCode() + "尚未采集 SPC 量产子组(ROUTINE),不可归档");
        }
        boolean stable = subgroups.stream().allMatch(s ->
                "合格".equals(s.getJudge()) && Boolean.FALSE.equals(s.getIsOutlier()));
        if (!stable) {
            throw new BusinessException(400, "SPC 量产子组存在不合格或异常点(最近" + subgroups.size()
                    + "批未全部连续合格),不可归档");
        }
    }

    /** SR-SCM:加严检验不合格->回滚变更(恢复冻结+标记退货) */
    @Override
    @Transactional
    public void rollback(String id, String reason) {
        SqmChangeOrder order = sqmChangeOrderMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "变更单不存在");
        DataScopeGuard.ensureOwner(order.getOrgId());
        order.setReceiveFrozen(true);
        order.setStatus("已回滚");
        if (sqmChangeOrderMapper.updateById(order) == 0) {
            throw new BusinessException(409, "变更单已被他人修改,请刷新后重试");
        }
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.username();
    }

    /**
     * 历史签字人字段曾存为 userId(无连字符 UUID),详情展示时解析为真实姓名;
     * 非 UUID 的值(姓名/"系统")原样返回。
     */
    private void resolveOperators(List<SqmChangeApproval> approvals) {
        if (approvals == null) return;
        for (SqmChangeApproval a : approvals) {
            a.setOperator(resolveOperator(a.getOperator()));
        }
    }

    private String resolveOperator(String op) {
        if (op == null || op.isBlank()) return op;
        if (!op.matches("[0-9a-fA-F-]{32,36}")) return op; // 已是姓名/"系统"等
        // sys_user.id 为 uuid 类型,需先转 text 再做 REPLACE 比较
        SysUser u = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().apply("REPLACE(id::text,'-','') = REPLACE({0},'-','')", op));
        if (u != null) {
            return (u.getRealName() != null && !u.getRealName().isBlank()) ? u.getRealName() : u.getUsername();
        }
        return op;
    }

    // ===== 审批流程站内通知 =====

    /** 供应商提交变更后通知三方(采购/研发/质量)。 */
    private void notifySubmitted(SqmChangeOrder order) {
        String title = "物料变更待审批";
        String content = String.format(
                "供应商【%s】发起物料变更《%s》(单号 %s,料号 %s)。请按 采购→研发→质量 顺序审批。",
                supplierName(order.getSupplierId()), order.getTitle(),
                order.getChangeNo(), order.getPartNo());
        notificationService.notify("sqm", "sqm_change_submitted",
                title, content, "sqm_change", order.getId(), order.getChangeNo(), "/sqm/change", order.getOrgId());
    }

    /** 通知下一位审批人。 */
    private void notifyNext(SqmChangeOrder order, String role) {
        String title = "物料变更待您审批";
        String content = String.format("《%s》(单号 %s) 前序审批已通过,现轮到【%s】审批。",
                order.getTitle(), order.getChangeNo(), roleLabel(role));
        notificationService.notifyRoles(List.of(roleCode(role)),
                title, content, "sqm_change", order.getId(), order.getChangeNo(), "/sqm/change", null, order.getOrgId());
    }

    private void notifyApproved(SqmChangeOrder order) {
        String title = "物料变更已批准";
        String content = String.format("《%s》(单号 %s) 已通过 采购→研发→质量 三方审批,正式生效。",
                order.getTitle(), order.getChangeNo());
        notifyPartiesAndApplicant(order, title, content);
    }

    private void notifyRejected(SqmChangeOrder order, String roleLabel) {
        String title = "物料变更被驳回";
        String content = String.format("《%s》(单号 %s) 被【%s】驳回。",
                order.getTitle(), order.getChangeNo(), roleLabel);
        notifyPartiesAndApplicant(order, title, content);
    }

    private void notifyPartiesAndApplicant(SqmChangeOrder order, String title, String content) {
        notificationService.notify("sqm", "sqm_change_result",
                title, content, "sqm_change", order.getId(), order.getChangeNo(), "/sqm/change", order.getOrgId());
        String applicantId = applicantUserId(order.getApplicant());
        notificationService.notifyUser(applicantId, title, content, "sqm_change", order.getId(), order.getChangeNo(), "/sqm/change");
    }

    private String roleLabel(String role) {
        return switch (role) {
            case "purchase" -> "采购";
            case "rd" -> "研发";
            case "quality" -> "质量";
            default -> role;
        };
    }

    private String roleCode(String role) {
        return switch (role) {
            case "purchase" -> "purchaser";
            case "rd" -> "rd";
            case "quality" -> "sqe";
            default -> role;
        };
    }

    private String applicantUserId(String username) {
        if (username == null) return null;
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id::text FROM ops.sys_user WHERE username = ?", String.class, username);
        } catch (Exception e) {
            return null;
        }
    }
}
