package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.common.security.DataScopeGuard;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.Qms8dStageDetail;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.domain.ncm.entity.QmsAssignRecord;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.ncm.mapper.Qms8dStageDetailMapper;
import com.konli.qms.domain.ncm.mapper.QmsAssignRecordMapper;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.domain.sqm.mapper.SqmIncomingAbnormalMapper;
import com.konli.qms.service.ncm.Ncm8dArchiveService;
import com.konli.qms.service.ncm.Ncm8dService;
import com.konli.qms.service.ncm.Ncm8dApprovalConfigService;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.ncm.NcmDefectRecordService;
import com.konli.qms.service.ncm.dto.Abnormal8dLaunchRequest;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.konli.qms.service.ncm.dto.EightDVo;
import com.konli.qms.service.notify.NotificationService;
import com.konli.qms.service.ncm.Qms8dFishboneService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Set;

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
    // @Lazy 打破与 NcmDefectRecordServiceImpl(其构造器注入 Ncm8dService)的循环依赖
    @Autowired @Lazy
    private NcmDefectRecordService ncmDefectRecordService;
    private final JdbcTemplate jdbcTemplate;
    private final NcmCapaService ncmCapaService;
    private final Ncm8dApprovalConfigService approvalConfigService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final Qms8dFishboneService fishboneService;
    private final Ncm8dArchiveService ncm8dArchiveService;
    private final QmsAssignRecordMapper assignRecordMapper;
    private final com.konli.qms.service.assign.AssignReassignService assignReassignService;

    /** 8D 阶段顺序:D1->D2->D3->D4->D5->D6->D7->D8 */
    private static final String[] STAGES = {"D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8"};

    /** 用于解析 D4 阶段明细 content(JSON)中的 5Why 层级。 */
    private static final ObjectMapper FIVE_WHY_OM = new ObjectMapper();

    @Override
    public List<Qms8dReport> list() {
        return qms8dReportMapper.selectList(null);
    }

    @Override
    public PageResult<Qms8dReport> listPage(String keyword, String status, String source, int page, int size) {
        LambdaQueryWrapper<Qms8dReport> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(k -> k.like(Qms8dReport::getD8No, keyword)
                    .or().like(Qms8dReport::getIssue, keyword)
                    .or().like(Qms8dReport::getSourceRefId, keyword));
        }
        if (StringUtils.hasText(status)) {
            w.eq(Qms8dReport::getStatus, status);
        }
        if (StringUtils.hasText(source)) {
            w.eq(Qms8dReport::getSource, source);
        }
        w.orderByDesc(Qms8dReport::getCreatedAt);
        IPage<Qms8dReport> ip = qms8dReportMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
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
            // 简易流程也先落缺陷记录(统一整改源头),再建 8D 报告并直接闭环
            if (report.getOrgId() == null || report.getOrgId().isBlank() || "ROOT".equals(report.getOrgId())) {
                report.setOrgId(resolveOrgId());
            }
            NcmDefectRecord def = buildManualDefect(report);
            def = ncmDefectRecordService.create(def);

            report.setD8No("8D-S-" + System.currentTimeMillis());
            report.setCurrentStage("D8");
            report.setStatus("已闭环");
            report.setCloseDate(LocalDate.now());
            // 简易流程为手动快速闭环,无上游触发事件,来源指向刚登记的缺陷记录
            report.setSource("不良记录");
            report.setSourceRefId(def.getId());
            if (report.getCapaTriggered() == null) report.setCapaTriggered(false);
            // severity 列为 NOT NULL,兜底为"一般",避免插入报 500
            if (report.getSeverity() == null || report.getSeverity().isBlank()) {
                report.setSeverity("一般");
            }
            // issue 优先保留用户填写的主题
            if (report.getIssue() == null || report.getIssue().isBlank()) {
                report.setIssue(def.getIssue() != null ? def.getIssue() : "不良:" + def.getDefectNo());
            }
            qms8dReportMapper.insert(report);
            // 回写缺陷记录:记录关联 8D 单号
            ncmDefectRecordService.linkD8(def.getId(), report.getD8No());
            // 简易流程直接闭环 -> 触发 8D 归档
            ncm8dArchiveService.archive(report);
            notify8d(report, "新建 8D 报告(简易闭环)", String.format(
                "新建并闭环 8D 报告《%s》(单号 %s,严重度 %s)。",
                report.getIssue(), report.getD8No(), report.getSeverity()));
            return report;
        }
        // flowType 兜底:从缺陷记录/异常单等入口发起 8D 时未显式传类型,统一归为标准流程"8D",避免 flow_type 落库为 NULL
        if (report.getFlowType() == null || report.getFlowType().isBlank()) {
            report.setFlowType("8D");
        }
        report.setD8No("8D-" + System.currentTimeMillis());
        report.setStatus("进行中");
        // 发起时仅指定负责人;团队由负责人在 D1 阶段自行组建并提交审核,故一律从 D1 开始
        report.setCurrentStage("D1");
        if (report.getCapaTriggered() == null) {
            report.setCapaTriggered(false);
        }
        // severity 列为 NOT NULL,人工来源前端可能不传严重度,兜底为"一般",避免插入报 500
        if (report.getSeverity() == null || report.getSeverity().isBlank()) {
            report.setSeverity("一般");
        }
        // 表单可能把 ROOT 超级管理员的组织(字面量 "ROOT")带进来,需归一化为真实组织,否则写入 UUID 列报 500
        if (report.getOrgId() == null || report.getOrgId().isBlank() || "ROOT".equals(report.getOrgId())) {
            report.setOrgId(resolveOrgId());
        }
        if (report.getSource() == null || report.getSource().isBlank()) report.setSource("人工");
        // 来源编码归一化(前端短码 SQM/SPC → 存储值 SQM异常/SPC报警)
        String src = normalizeSource(report.getSource());
        report.setSource(src);
        // 统一整改源头:人工来源(无上游事件)先登记缺陷记录,再从缺陷记录发起 8D,
        // 使 8D 的 sourceRefId 指向缺陷记录,缺陷记录成为全平台 8D/CAPA/CA 的唯一源头,不破例。
        if ("人工".equals(src)) {
            NcmDefectRecord def = buildManualDefect(report);
            def = ncmDefectRecordService.create(def);
            // 改为统一来源"不良记录",并关联刚登记的缺陷记录
            report.setSource("不良记录");
            report.setSourceRefId(def.getId());
            // 从缺陷记录发起 8D(标准范式:launch8dFromDefect 内部回写缺陷记录 d8No、指派通知、保留 issue)
            return (Qms8dReport) ncmDefectRecordService.launch8dFromDefect(def.getId(), new DefectLaunchRequest());
        }
        // 事件类来源必须回填来源单号,否则会产生"有来源无单号"的脏数据
        if (requiresSourceRef(src)
                && (report.getSourceRefId() == null || report.getSourceRefId().isBlank())) {
            throw new BusinessException(400, "来源类型为「" + src + "」时必须填写来源单号(sourceRefId)");
        }
        qms8dReportMapper.insert(report);
        // D1 团队组建不再预填:由负责人在 D1 阶段自行添加团队成员并提交审核
        notify8d(report, "新建 8D 报告", String.format(
            "新建 8D 报告《%s》(单号 %s,严重度 %s),请跟进处理。",
            report.getIssue(), report.getD8No(), report.getSeverity()));
        return report;
    }

    /** 由人工发起的 8D 报告构造对应的缺陷记录(统一整改源头)。 */
    private NcmDefectRecord buildManualDefect(Qms8dReport report) {
        NcmDefectRecord def = new NcmDefectRecord();
        def.setOrgId(report.getOrgId());
        def.setSource("人工");
        // 人工发起不良字典项(全局 code=NCM,由 V236 幂等 seed),满足 defect_dict_code NOT NULL 约束
        def.setDefectDictCode("NCM");
        def.setIssue(report.getIssue());
        def.setSeverity(report.getSeverity() != null ? report.getSeverity() : "一般");
        // 不良数量/批量为 NOT NULL:人工发起未提供,兜底 defectCount=1、batchTotal=1(单条发起)
        def.setDefectCount(1);
        def.setBatchTotal(1);
        // 人工建 8D 不传工单号/工序,缺陷记录 wo_no/process_code 列 NOT NULL,兜底空串
        def.setWoNo("");
        def.setProcessCode("");
        def.setRemark("人工发起 8D:" + (report.getIssue() != null ? report.getIssue() : ""));
        return def;
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

    /** 来源类型短码 → 存储值 归一化映射 */
    private static final Map<String, String> SOURCE_ALIAS = Map.of(
            "SQM", "SQM异常",
            "SPC", "SPC报警");
    /** 无需关联上游事件的来源(手动发起的 8D,允许无来源单号) */
    private static final Set<String> MANUAL_SOURCES = Set.of("人工");

    /** 前端下拉短码(SQM/SPC)归一化为存储值(SQM异常/SPC报警) */
    private String normalizeSource(String source) {
        if (source == null) return null;
        String mapped = SOURCE_ALIAS.get(source);
        return mapped != null ? mapped : source;
    }

    /** 该来源类型是否必须关联上游事件单号(人工来源无需) */
    private boolean requiresSourceRef(String source) {
        return source != null && !source.isBlank() && !MANUAL_SOURCES.contains(source);
    }

    /** 站内信通知(8D 报告相关):推送给质量经理/SQE,失败不回滚主流程。 */
    private void notify8d(Qms8dReport r, String title, String content) {
        try {
            notificationService.notify("ncm", "ncm_8d_status",
                title, content, "ncm_8d", r.getId(), r.getD8No(), "/ncm/8d-reports", r.getOrgId());
        } catch (Exception ignored) {
            log.warn("[8D通知] 站内信推送失败: {}", ignored.getMessage());
        }
    }

    /**
     * D1 团队组建经质量部门签批通过后,逐人通知团队成员已被加入该 8D 团队。
     * teamMembers 为逗号分隔的 user_id 列表(新前端 D1 提交时写入);为空则不通知。
     * 仅向形如 UUID 的 token 推送,跳过旧数据以姓名顿号串存储的遗留格式(避免写入无效 user_id)。
     * 单个成员推送失败不影响其他成员与主流程。
     */
    private static final java.util.regex.Pattern UUID_RE =
            java.util.regex.Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private void notifyTeamMembers(Qms8dReport r, String teamMembers) {
        if (!StringUtils.hasText(teamMembers)) return;
        String[] ids = teamMembers.split(",");
        String link = "/ncm/8d-reports/" + r.getId();
        for (String uid : ids) {
            String id = uid.trim();
            if (!UUID_RE.matcher(id).matches()) {
                log.debug("[8D通知] 跳过非 UUID 团队成员 token: {}", id);
                continue;
            }
            try {
                notificationService.notifyUser(id, "8D 团队组建通知",
                    String.format("您已被加入 8D 报告《%s》(单号 %s)的团队,请登录系统跟进处理。",
                        r.getIssue(), r.getD8No()),
                    "ncm_8d", r.getId(), r.getD8No(), link);
            } catch (Exception ex) {
                log.warn("[8D通知] 团队成员({})站内信推送失败: {}", id, ex.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public Qms8dReport launchFromAbnormal(Abnormal8dLaunchRequest req) {
        Qms8dReport report = req.getReport();
        DefectLaunchRequest launch = req.getLaunch();
        if (report == null) {
            throw new BusinessException(400, "缺少 8D 报告主体");
        }
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
        // 统一整改源头:先登记一条 source=SQM异常 的缺陷记录,再从该缺陷记录发起 8D,
        // 使 8D 的 sourceRefId 指向缺陷记录(而非异常单),缺陷记录成为全平台 8D/CAPA/CA 的唯一源头
        String orgId = ab.getOrgId();
        if (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) {
            orgId = resolveOrgId();
        }
        NcmDefectRecord def = new NcmDefectRecord();
        def.setOrgId(orgId);
        def.setSource("SQM异常");
        def.setDefectDictCode("SQM"); // 来料异常不良字典项(全局,由 V235 幂等 seed)
        def.setSeverity("严重".equals(ab.getLevel()) ? "严重" : ("一般".equals(ab.getLevel()) ? "一般" : "中"));
        def.setDefectCount(ab.getQty() != null ? ab.getQty() : 1);
        def.setBatchTotal(ab.getIncomingQty() != null && ab.getIncomingQty() > 0 ? ab.getIncomingQty() : 1);
        def.setBatchNo(ab.getBatchNo());
        def.setProductModel(ab.getPartName());
        def.setRemark("来料异常单 " + ab.getAbnormalNo() + ":" + (ab.getDescription() != null ? ab.getDescription() : ""));
        def = ncmDefectRecordService.create(def);

        // 从缺陷记录发起 8D(标准范式:launch8dFromDefect 内部回写缺陷记录 d8No 并指派通知)
        Qms8dReport created = (Qms8dReport) ncmDefectRecordService.launch8dFromDefect(def.getId(), launch != null ? launch : new DefectLaunchRequest());

        // 保留异常单反向链接:回写 d8Id / rectifyType / 状态(异常单→8D 可追溯)
        SqmIncomingAbnormal upd = new SqmIncomingAbnormal();
        upd.setId(abnormalId);
        upd.setD8Id(created.getId());
        upd.setRectifyType("8D");
        upd.setStatus("整改中");
        abnormalMapper.updateById(upd);
        return created;
    }

    /** 负责人指派:写 qms_assign_record 并站内信通知负责人本人(8D 新流程:仅指定负责人)。 */
    private void saveAbnormalOwnerAssign(SqmIncomingAbnormal ab, String bizId, String bizNo,
                                         String bizType, DefectLaunchRequest launch) {
        List<String> channels = (launch.getNotifyChannels() == null || launch.getNotifyChannels().isEmpty())
                ? List.of("站内弹窗") : launch.getNotifyChannels();
        String channelStr = String.join(",", channels);
        boolean inbox = channels.contains("站内弹窗");
        String assignerId = currentOperator();
        String ownerName = queryUserName(launch.getOwnerUserId());
        QmsAssignRecord rec = new QmsAssignRecord();
        rec.setOrgId(ab.getOrgId());
        rec.setDefectId(ab.getId());
        rec.setDefectNo(ab.getAbnormalNo());
        rec.setBizType(bizType);
        rec.setBizId(bizId);
        rec.setBizNo(bizNo);
        rec.setAssigneeUserId(launch.getOwnerUserId());
        rec.setAssigneeUserName(ownerName != null ? ownerName : launch.getOwnerUserId());
        rec.setNotifyChannels(channelStr);
        rec.setAssignerId(assignerId);
        rec.setRemark(launch.getRemark());
        assignRecordMapper.insert(rec);
        if (inbox) {
            String title = "[" + bizType + "指派] 来料异常 " + ab.getAbnormalNo() + " 指定您为负责人";
            String content = "来料异常 " + ab.getAbnormalNo() + " 已发起" + bizType + "报告(" + bizNo + "),"
                    + "您被指定为负责人,请登录系统在 D1 阶段组建团队并提交审核。"
                    + (launch.getRemark() != null && !launch.getRemark().isBlank() ? "\n指派备注: " + launch.getRemark() : "");
            String link = "/sqm/abnormals";
            notificationService.notifyUser(launch.getOwnerUserId(), title, content, "NCM_ASSIGN", bizId, bizNo, link);
        }
    }

    /** 8D 新流程:发起时仅指定负责人(ownerUserId 单选),返回其真实姓名用于 team/owner。 */
    private String resolveOwnerName(DefectLaunchRequest req) {
        if (req == null || req.getOwnerUserId() == null || req.getOwnerUserId().isBlank()) {
            return null;
        }
        return queryUserName(req.getOwnerUserId());
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }

    private String queryUserName(String userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT real_name FROM ops.sys_user WHERE id = ?::uuid", String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void advanceStage(String d8Id, String stageCode, String content, String owner, String teamMembers) {
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
            // D1 团队组建阶段:写入负责人自建团队成员名单
            if (StringUtils.hasText(teamMembers)) {
                detail.setTeamMembers(teamMembers);
            }
            // 需审核:进入待审批并停留当前阶段等待签批;否则标记无需审批
            detail.setApprovalStatus(need ? "待审批" : "无需审批");
            qms8dStageDetailMapper.insert(detail);
        } else {
            existing.setContent(content);
            existing.setOwner(owner);
            if (StringUtils.hasText(teamMembers)) {
                existing.setTeamMembers(teamMembers);
            }
            // 重新提交(被驳回后)回到待审批;已通过的阶段不回退
            if (need && !"已通过".equals(existing.getApprovalStatus())) {
                existing.setApprovalStatus("待审批");
            } else if (!need) {
                existing.setApprovalStatus("无需审批");
            }
            qms8dStageDetailMapper.updateById(existing);
        }

        // 兜底回填主表负责人(便于列表页展示责任人):以本阶段提交的 owner 为准
        if (owner != null && !owner.isBlank()
                && (report.getOwnerUserName() == null || report.getOwnerUserName().isBlank())) {
            report.setOwnerUserName(owner);
            qms8dReportMapper.updateById(report);
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
            // D8 闭环 -> 触发 8D 归档
            ncm8dArchiveService.archive(report);
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
        // 若审核配置指定了签批人(支持多人,逗号分隔 userId/用户名,OR 语义:任一命中即可签),
        // 仅被指定的用户可签(其余人即使口令正确也无权限)。
        String signer = approvalConfigService.signerOf(report.getOrgId(), stageCode);
        if (signer != null && !signer.isBlank()) {
            boolean hit = isAmongSigners(signer, cur.userId(), cur.username());
            if (!hit) {
                throw new BusinessException(400, "本阶段需由指定签批人签批,当前用户无签名权限");
            }
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
        // D1 团队组建签批通过:通知各团队成员已被加入团队(避免驳回重改时反复打扰,故仅通过时通知)
        if ("D1".equals(stageCode) && StringUtils.hasText(detail.getTeamMembers())) {
            notifyTeamMembers(report, detail.getTeamMembers());
        }
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

    /**
     * 判断当前用户是否在被指定的签批人列表中(OR 语义:任一命中即可签)。
     * signer 字段支持逗号分隔的多人(userId 或 username 均可,兼容历史单 username 数据)。
     */
    private boolean isAmongSigners(String signer, String userId, String username) {
        if (signer == null || signer.isBlank()) return false;
        String uId = userId == null ? "" : userId.trim();
        String uName = username == null ? "" : username.trim();
        for (String s : signer.split(",")) {
            String t = s.trim();
            if (t.isEmpty()) continue;
            if (!uId.isEmpty() && t.equalsIgnoreCase(uId)) return true;
            if (!uName.isEmpty() && t.equalsIgnoreCase(uName)) return true;
        }
        return false;
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
        // 报告已退回进行中,原归档不再有效:软作废旧归档(保留记录与 PDF 留痕,不物理删除)
        ncm8dArchiveService.invalidate(d8Id, reason);
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

    /** 列表级改派责任人(更新 owner_user_id/owner_user_name + 推送被指派人任务中心 + 追加指派记录)。 */
    @Override
    @Transactional
    public void reassign(String d8Id, DefectLaunchRequest req) {
        Qms8dReport report = qms8dReportMapper.selectById(d8Id);
        if (report == null) throw new BusinessException(404, "8D 报告不存在");
        String ownerName = assignReassignService.execute(new com.konli.qms.service.assign.AssignReassignService.ReassignContext(
                "8D", d8Id, report.getD8No(), report.getOrgId(),
                "/ncm/8d-reports/" + d8Id, null, null, req, true));
        Qms8dReport upd = new Qms8dReport();
        upd.setId(d8Id);
        upd.setOwnerUserId(req.getOwnerUserId());
        upd.setOwnerUserName(ownerName);
        qms8dReportMapper.updateById(upd);
    }
}
