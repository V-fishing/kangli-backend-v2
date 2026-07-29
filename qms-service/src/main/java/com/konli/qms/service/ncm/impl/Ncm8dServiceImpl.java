package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.DataScopeGuard;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.Qms8dStageDetail;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.ncm.mapper.Qms8dStageDetailMapper;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.domain.sqm.mapper.SqmIncomingAbnormalMapper;
import com.konli.qms.service.ncm.Ncm8dService;
import com.konli.qms.service.ncm.Ncm8dApprovalConfigService;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.ncm.dto.EightDVo;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.ncm.Qms8dFishboneService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class Ncm8dServiceImpl implements Ncm8dService {

    private final Qms8dReportMapper qms8dReportMapper;
    private final Qms8dStageDetailMapper qms8dStageDetailMapper;
    private final SqmIncomingAbnormalMapper abnormalMapper;
    private final JdbcTemplate jdbcTemplate;
    private final NcmCapaService ncmCapaService;
    private final Ncm8dApprovalConfigService approvalConfigService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final Qms8dFishboneService fishboneService;

    /** 8D 阶段顺序:D1->D2->D3->D4->D5->D6->D7->D8 */
    private static final String[] STAGES = {"D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8"};

    /** 用于解析 D4 阶段明细 content(JSON)中的 5Why 层级。 */
    private static final ObjectMapper FIVE_WHY_OM = new ObjectMapper();

    @Override
    public List<Qms8dReport> list() {
        return qms8dReportMapper.selectList(null);
    }

    @Override
    public EightDVo get(String id) {
        EightDVo vo = new EightDVo();
        vo.setReport(qms8dReportMapper.selectById(id));
        vo.setStages(qms8dStageDetailMapper.selectList(
                new LambdaQueryWrapper<Qms8dStageDetail>()
                        .eq(Qms8dStageDetail::getD8Id, id)
                        .orderByAsc(Qms8dStageDetail::getStageCode)));
        return vo;
    }

    @Override
    @Transactional
    public Qms8dReport create(Qms8dReport report) {
        // SR-PTL 简易流程:flowType=简易 -> 直接D8闭环,跳过D2-D7
        if ("简易".equals(report.getFlowType())) {
            report.setD8No("8D-S-" + System.currentTimeMillis());
            report.setCurrentStage("D8");
            report.setStatus("已闭环");
            report.setCloseDate(LocalDate.now());
            if (report.getCapaTriggered() == null) report.setCapaTriggered(false);
            if (report.getOrgId() == null) report.setOrgId(resolveOrgId());
            qms8dReportMapper.insert(report);
            notify8d(report, "新建 8D 报告(简易闭环)", String.format(
                "新建并闭环 8D 报告《%s》(单号 %s,严重度 %s)。",
                report.getIssue(), report.getD8No(), report.getSeverity()));
            return report;
        }
        report.setD8No("8D-" + System.currentTimeMillis());
        report.setCurrentStage("D1");
        report.setStatus("进行中");
        if (report.getCapaTriggered() == null) {
            report.setCapaTriggered(false);
        }
        if (report.getOrgId() == null) report.setOrgId(resolveOrgId());
        if (report.getSource() == null || report.getSource().isBlank()) report.setSource("NCM");
        qms8dReportMapper.insert(report);
        notify8d(report, "新建 8D 报告", String.format(
            "新建 8D 报告《%s》(单号 %s,严重度 %s),请跟进处理。",
            report.getIssue(), report.getD8No(), report.getSeverity()));
        return report;
    }

    private String resolveOrgId() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        if (u != null && u.orgId() != null && !"ROOT".equals(u.orgId())) {
            return u.orgId();
        }
        try {
            return jdbcTemplate.queryForObject(
                "SELECT id::text FROM ops.sys_org ORDER BY created_at LIMIT 1", String.class);
        } catch (Exception ex) {
            return null;
        }
    }

    /** 站内信通知(8D 报告相关):推送给质量经理/SQE,失败不回滚主流程。 */
    private void notify8d(Qms8dReport r, String title, String content) {
        try {
            notificationService.notifyRoles(List.of("qmanager", "sqe"),
                title, content, "ncm_8d", r.getId(), "/ncm/8d-reports", null);
        } catch (Exception ignored) {
            log.warn("[8D通知] 站内信推送失败: {}", ignored.getMessage());
        }
    }

    @Override
    @Transactional
    public Qms8dReport launchFromAbnormal(Qms8dReport report) {
        String abnormalId = report.getSourceRefId();
        if (abnormalId == null || abnormalId.isBlank()) {
            throw new BusinessException(400, "缺少来源异常单(sourceRefId)");
        }
        SqmIncomingAbnormal ab = abnormalMapper.selectById(abnormalId);
        if (ab == null) {
            throw new BusinessException(404, "来料异常单不存在");
        }
        if (ab.getD8Id() != null && !ab.getD8Id().isBlank()) {
            throw new BusinessException(400, "该异常单已发起8D");
        }
        report.setId(UUID.randomUUID().toString());
        // 从异常单继承 orgId,对旧数据中 ROOT/null 兜底为 sys_org 首个组织
        String orgId = ab.getOrgId();
        if (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) {
            orgId = resolveOrgId();
        }
        report.setOrgId(orgId);
        report.setSource("SQM异常");
        report.setSourceRefId(abnormalId);
        report.setD8No("8D-" + System.currentTimeMillis());
        report.setCurrentStage("D1");
        report.setStatus("进行中");
        report.setCapaTriggered(false);
        report.setFlowType("8D");
        qms8dReportMapper.insert(report);
        notify8d(report, "从来料异常发起 8D 报告", String.format(
            "已根据来料异常单发起 8D 报告《%s》(单号 %s),请跟进处理。",
            report.getIssue(), report.getD8No()));
        SqmIncomingAbnormal upd = new SqmIncomingAbnormal();
        upd.setId(abnormalId);
        upd.setD8Id(report.getId());
        upd.setRectifyType("8D");
        upd.setStatus("整改中");
        abnormalMapper.updateById(upd);
        return report;
    }

    @Override
    @Transactional
    public void advanceStage(String d8Id, String stageCode, String content, String owner) {
        Qms8dReport report = qms8dReportMapper.selectById(d8Id);
        if (report == null) {
            throw new BusinessException(404, "8D 报告不存在");
        }
        DataScopeGuard.ensureOwner(report.getOrgId());
        if ("已闭环".equals(report.getStatus())) {
            throw new BusinessException(400, "8D 报告已闭环,无法继续推进");
        }
        // 校验:必须按顺序推进(当前阶段 == 传入 stageCode)
        if (!stageCode.equals(report.getCurrentStage())) {
            throw new BusinessException(400, "阶段推进顺序错误,当前阶段为 " + report.getCurrentStage() + ",期望 " + stageCode);
        }
        int idx = Arrays.asList(STAGES).indexOf(stageCode);
        if (idx < 0) {
            throw new BusinessException(400, "非法阶段编号:" + stageCode);
        }

        // 前一阶段若是审批关口(D3/D5/D7), 校验审批已通过才能推进
        int prevIdx = idx - 1;
        if (prevIdx >= 0) {
            String prevStage = STAGES[prevIdx];
            if (approvalConfigService.needApproval(report.getOrgId(), prevStage)) {
                Qms8dStageDetail prevDetail = qms8dStageDetailMapper.selectOne(
                        new LambdaQueryWrapper<Qms8dStageDetail>()
                                .eq(Qms8dStageDetail::getD8Id, d8Id)
                                .eq(Qms8dStageDetail::getStageCode, prevStage));
                if (prevDetail == null || !"已通过".equals(prevDetail.getApprovalStatus())) {
                    throw new BusinessException(400, "阶段 " + prevStage + " 审批未通过，无法推进到 " + stageCode);
                }
            }
        }

        // 该阶段是否需要审核人签名(由审核配置决定)
        boolean need = approvalConfigService.needApproval(report.getOrgId(), stageCode);

        // 创建/更新阶段明细(UNIQUE(d8_id, stage_code))
        Qms8dStageDetail existing = qms8dStageDetailMapper.selectOne(
                new LambdaQueryWrapper<Qms8dStageDetail>()
                        .eq(Qms8dStageDetail::getD8Id, d8Id)
                        .eq(Qms8dStageDetail::getStageCode, stageCode));
        if (existing == null) {
            Qms8dStageDetail detail = new Qms8dStageDetail();
            String org = report.getOrgId();
            CompanyContext.CurrentUser u = CompanyContext.get();
            if (org == null && u != null) org = u.orgId();
            detail.setOrgId(org);
            detail.setD8Id(d8Id);
            detail.setStageCode(stageCode);
            detail.setContent(content);
            detail.setOwner(owner);
            // 需审核:进入待审批并停留当前阶段等待签批;否则标记无需审批
            detail.setApprovalStatus(need ? "待审批" : "无需审批");
            qms8dStageDetailMapper.insert(detail);
        } else {
            existing.setContent(content);
            existing.setOwner(owner);
            // 重新提交(被驳回后)回到待审批;已通过的阶段不回退
            if (need && !"已通过".equals(existing.getApprovalStatus())) {
                existing.setApprovalStatus("待审批");
            } else if (!need) {
                existing.setApprovalStatus("无需审批");
            }
            qms8dStageDetailMapper.updateById(existing);
        }

        // D4 完成 -> 自动触发 CAPA(若尚未触发)
        if ("D4".equals(stageCode) && !Boolean.TRUE.equals(report.getCapaTriggered())) {
            triggerCapaFrom8d(report);
        }

        // 需审核的阶段:停留当前阶段,等待审核人签名通过后才进入下一阶段
        if (need) {
            notify8d(report, "8D 报告待审批", String.format(
                "8D 报告《%s》(单号 %s) 的 %s 阶段已提交,待质量经理签批。",
                report.getIssue(), report.getD8No(), stageCode));
            return;
        }
        // D4 推进至 D5 前,强制校验根因分析已录入(鱼骨图≥1 且 5Why≥1)
        if ("D4".equals(stageCode)) {
            checkD4RootCause(d8Id, content);
        }
        // 无需审核 -> 进入下一阶段(或 D8 闭环)
        advanceCurrent(report, idx);
    }

    /** 进入下一阶段;若已是 D8 则闭环并回写来源异常单(带 @Version 乐观锁)。 */
    private void advanceCurrent(Qms8dReport report, int idx) {
        if (idx == STAGES.length - 1) {
            report.setStatus("已闭环");
            report.setCloseDate(LocalDate.now());
            report.setCurrentStage(STAGES[idx]);
            if (qms8dReportMapper.updateById(report) == 0) {
                throw new BusinessException(409, "8D 报告已被他人修改,请刷新后重试");
            }
            closeAbnormalById(report.getSourceRefId());
        } else {
            report.setCurrentStage(STAGES[idx + 1]);
            if (qms8dReportMapper.updateById(report) == 0) {
                throw new BusinessException(409, "8D 报告已被他人修改,请刷新后重试");
            }
        }
    }

    @Override
    @Transactional
    public void approveStage(String d8Id, String stageCode, boolean approved, String comment, String password) {
        Qms8dReport report = qms8dReportMapper.selectById(d8Id);
        if (report == null) {
            throw new BusinessException(404, "8D 报告不存在");
        }
        DataScopeGuard.ensureOwner(report.getOrgId());
        // 只能审批“当前停留的待审批阶段”
        if (!stageCode.equals(report.getCurrentStage())) {
            throw new BusinessException(400, "当前阶段为 " + report.getCurrentStage() + ",并非待审批阶段 " + stageCode);
        }
        Qms8dStageDetail detail = qms8dStageDetailMapper.selectOne(
                new LambdaQueryWrapper<Qms8dStageDetail>()
                        .eq(Qms8dStageDetail::getD8Id, d8Id)
                        .eq(Qms8dStageDetail::getStageCode, stageCode));
        if (detail == null || !"待审批".equals(detail.getApprovalStatus())) {
            throw new BusinessException(400, "阶段 " + stageCode + " 未处于待审批状态");
        }

        // 电子签名:审核人即当前登录用户,须通过本人口令校验
        CompanyContext.CurrentUser cur = CompanyContext.get();
        if (cur == null || cur.username() == null || cur.username().isBlank()) {
            throw new BusinessException(401, "未获取到当前登录用户,无法签名");
        }
        SysUser signUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, cur.username().trim()));
        if (signUser == null) {
            throw new BusinessException(400, "签名用户不存在: " + cur.username());
        }
        if (password == null || password.isBlank() || !passwordEncoder.matches(password, signUser.getPasswordHash())) {
            throw new BusinessException(400, "签名口令错误,电子签名校验未通过");
        }
        // 若审核配置指定了签批人,仅该用户可签(其余人即使口令正确也无权限)
        String signer = approvalConfigService.signerOf(report.getOrgId(), stageCode);
        if (signer != null && !signer.isBlank() && !signer.trim().equalsIgnoreCase(cur.username().trim())) {
            throw new BusinessException(400, "本阶段需由【" + signer + "】签批,当前用户无签名权限");
        }
        String signerName = cur.username();

        // 驳回:退回当前阶段,允许重新提交
        if (!approved) {
            detail.setApprovalStatus("已驳回");
            detail.setApprovalComment(comment);
            detail.setApprovedBy(signerName);
            detail.setApprovedAt(LocalDateTime.now());
            if (qms8dStageDetailMapper.updateById(detail) == 0) {
                throw new BusinessException(409, "阶段明细已被他人修改,请刷新后重试");
            }
            report.setCurrentStage(stageCode); // 退回至审批阶段
            if (qms8dReportMapper.updateById(report) == 0) {
                throw new BusinessException(409, "8D 报告已被他人修改,请刷新后重试");
            }
            notify8d(report, "8D 报告阶段被驳回", String.format(
                "8D 报告《%s》(单号 %s) 的 %s 阶段被驳回。",
                report.getIssue(), report.getD8No(), stageCode));
            return;
        }

        // 通过:电子签名已校验通过
        detail.setApprovalStatus("已通过");
        detail.setApprovalComment(comment);
        detail.setApprovedBy(signerName);
        detail.setApprovedAt(LocalDateTime.now());
        if (qms8dStageDetailMapper.updateById(detail) == 0) {
            throw new BusinessException(409, "阶段明细已被他人修改,请刷新后重试");
        }
        // D4 审批通过、推进至 D5 前,强制校验根因分析已录入
        if ("D4".equals(stageCode)) {
            checkD4RootCause(d8Id, detail.getContent());
        }
        // 签名通过 -> 进入下一阶段(或 D8 闭环)
        int idx = Arrays.asList(STAGES).indexOf(stageCode);
        advanceCurrent(report, idx);
        notify8d(report, "8D 报告阶段审批通过", String.format(
            "8D 报告《%s》(单号 %s) 的 %s 阶段已审批通过。",
            report.getIssue(), report.getD8No(), stageCode));
    }

    /** 校验 D4 根因分析完整性:鱼骨图≥1 条且 5Why≥1 层,否则拦截推进至 D5。 */
    private void checkD4RootCause(String d8Id, String content) {
        long fishboneCount = fishboneService.count(d8Id);
        int whyCount = parseFiveWhyCount(content);
        if (fishboneCount < 1 || whyCount < 1) {
            throw new BusinessException(400, "D4 根因分析需至少录入1条鱼骨图原因与1层5Why后方可推进至D5");
        }
    }

    /** 解析 D4 阶段明细 content(JSON)中的 5Why 层数;非 JSON 或缺失时返回 0。 */
    private int parseFiveWhyCount(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        try {
            JsonNode node = FIVE_WHY_OM.readTree(content);
            JsonNode why = node.get("fiveWhy");
            if (why != null && why.isArray()) {
                return why.size();
            }
        } catch (Exception ignored) {
            // 非 JSON 内容(如历史纯文本)视为未填写 5Why
        }
        return 0;
    }

    /** D4 阶段完成时,严重度≥7(severity=高)自动发起 CAPA 并回写关联。 */
    private void triggerCapaFrom8d(Qms8dReport report) {
        // SR-PTL:S≥7(severity=高)才自动触发CAPA;低/中不触发
        if (!"高".equals(report.getSeverity())) {
            return;
        }
        QmsCapa capa = new QmsCapa();
        capa.setOrgId(report.getOrgId());
        capa.setD8Id(report.getId());
        capa.setAbnormalId(report.getSourceRefId());
        capa.setIssue(report.getIssue());
        capa.setTriggerType("8D");
        capa.setTriggerStage("D4");
        capa.setTriggerCondition("8D-D4 阶段触发");
        capa.setCapaType("纠正措施");
        capa.setOwner(report.getTeam() != null ? report.getTeam() : "未指定");
        capa.setDueDate(LocalDate.now().plusDays(30));
        ncmCapaService.create(capa);
        Qms8dReport u = new Qms8dReport();
        u.setId(report.getId());
        u.setCapaTriggered(true);
        qms8dReportMapper.updateById(u);
        // 关联来源异常单:按业务单号 abnormal_no 解析真实 UUID 主键;
        // SPC 报警等非来料异常来源无对应来料异常单时,查不到则跳过(不报错)。
        if (report.getSourceRefId() != null && !report.getSourceRefId().isBlank()) {
            SqmIncomingAbnormal au = abnormalMapper.selectOne(
                    new LambdaQueryWrapper<SqmIncomingAbnormal>()
                            .eq(SqmIncomingAbnormal::getAbnormalNo, report.getSourceRefId())
                            .last("LIMIT 1"));
            if (au != null) {
                au.setCapaId(capa.getId());
                au.setRectifyType("8D");
                abnormalMapper.updateById(au);
            }
        }
    }

    /** SR-PTL 效果验证问题复发->重新打开8D:status已闭环->进行中,currentStage退回D6(重新验证)。 */
    @Override
    @Transactional
    public void reopen(String d8Id, String reason) {
        Qms8dReport report = qms8dReportMapper.selectById(d8Id);
        if (report == null) throw new BusinessException(404, "8D 报告不存在");
        if (!"已闭环".equals(report.getStatus())) throw new BusinessException(400, "仅已闭环的8D可以重新打开");
        DataScopeGuard.ensureOwner(report.getOrgId());
        // 用已加载 report 更新(带 @Version 乐观锁)
        report.setStatus("进行中");
        report.setCurrentStage("D6"); // 退回D6重新验证
        report.setCloseDate(null);
        if (qms8dReportMapper.updateById(report) == 0) {
            throw new BusinessException(409, "8D 报告已被他人修改,请刷新后重试");
        }
        notify8d(report, "8D 报告重新打开", String.format(
            "8D 报告《%s》(单号 %s) 已重新打开,退回 D6 重新验证。",
            report.getIssue(), report.getD8No()));
    }

    /** 异常单闭环(幂等):仅当未关闭时置 已关闭 并写入闭环日期。 */
    private void closeAbnormalById(String abnormalNo) {
        if (abnormalNo == null || abnormalNo.isBlank()) {
            return;
        }
        // 按业务单号解析真实 UUID(原逻辑误将业务单号当主键,会触发 UUID 解析错误)
        SqmIncomingAbnormal ab = abnormalMapper.selectOne(
                new LambdaQueryWrapper<SqmIncomingAbnormal>()
                        .eq(SqmIncomingAbnormal::getAbnormalNo, abnormalNo)
                        .last("LIMIT 1"));
        if (ab == null || "已关闭".equals(ab.getStatus())) {
            return;
        }
        ab.setStatus("已关闭");
        ab.setCloseDate(LocalDate.now());
        abnormalMapper.updateById(ab);
    }

    @Override
    public Qms8dReport findBySourceRef(String source, String sourceRefId) {
        if (source == null || source.isBlank() || sourceRefId == null || sourceRefId.isBlank()) return null;
        return qms8dReportMapper.selectOne(
            new LambdaQueryWrapper<Qms8dReport>()
                .eq(Qms8dReport::getSource, source)
                .eq(Qms8dReport::getSourceRefId, sourceRefId)
                .orderByDesc(Qms8dReport::getCreatedAt)
                .last("LIMIT 1"));
    }
}
