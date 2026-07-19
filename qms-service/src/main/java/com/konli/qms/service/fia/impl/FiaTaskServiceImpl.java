package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.fia.entity.FiaArchivedReport;
import com.konli.qms.domain.fia.entity.FiaInspItem;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.domain.fia.entity.FiaSignConfig;
import com.konli.qms.domain.fia.entity.FiaTask;
import com.konli.qms.domain.fia.entity.FiaTaskLog;
import com.konli.qms.domain.fia.mapper.FiaArchivedReportMapper;
import com.konli.qms.domain.fia.mapper.FiaInspItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdItemMapper;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.domain.fia.mapper.FiaTaskLogMapper;
import com.konli.qms.domain.fia.mapper.FiaTaskMapper;
import com.konli.qms.domain.spc.entity.SpcParam;
import com.konli.qms.domain.spc.entity.SpcSubgroup;
import com.konli.qms.domain.spc.mapper.SpcParamMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.fia.FiaTaskService;
import com.konli.qms.service.fia.SignConfigService;
import com.konli.qms.service.fia.dto.FiaTaskVo;
import com.konli.qms.service.spc.SpcSubgroupService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FiaTaskServiceImpl implements FiaTaskService {

    private final FiaTaskMapper fiaTaskMapper;
    private final FiaInspItemMapper fiaInspItemMapper;
    private final FiaInspStdMapper fiaInspStdMapper;
    private final FiaInspStdItemMapper fiaInspStdItemMapper;
    private final FiaArchivedReportMapper fiaArchivedReportMapper;
    private final FiaTaskLogMapper fiaTaskLogMapper;
    private final SignConfigService signConfigService;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final SpcParamMapper spcParamMapper;
    private final SpcSubgroupService spcSubgroupService;

    @Override
    public List<FiaTask> list() {
        return fiaTaskMapper.selectList(null);
    }

    @Override
    public FiaTaskVo get(String id) {
        FiaTaskVo vo = new FiaTaskVo();
        vo.setTask(fiaTaskMapper.selectById(id));
        vo.setItems(fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, id).orderByAsc(FiaInspItem::getSeq)));
        return vo;
    }

    @Override
    @Transactional
    public FiaTask create(FiaTask task) {
        FiaInspStd std = fiaInspStdMapper.selectById(task.getStdId());
        if (std == null) {
            throw new BusinessException(400, "检验标准不存在");
        }
        task.setCode("FA-" + System.currentTimeMillis());
        task.setStdVersion(std.getStdVersion());
        task.setAql(std.getAql());
        if (task.getStatus() == null) {
            task.setStatus("待检");
        }
        task.setIsOverdue(false);
        task.setSlaDueAt(LocalDateTime.now().plusHours(2));
        fiaTaskMapper.insert(task);

        List<FiaInspStdItem> stdItems = fiaInspStdItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspStdItem>().eq(FiaInspStdItem::getStdId, task.getStdId()).orderByAsc(FiaInspStdItem::getSeq));
        for (FiaInspStdItem si : stdItems) {
            FiaInspItem it = new FiaInspItem();
            it.setOrgId(task.getOrgId());
            it.setTaskId(task.getId());
            it.setSeq(si.getSeq());
            it.setItemName(si.getItemName());
            it.setIsCtq(si.getIsCtq());
            it.setStdValue(si.getStdValue());
            it.setTolerance(si.getTolerance());
            it.setUnit(si.getUnit());
            it.setStdItemId(si.getId());
            it.setJudge("-");
            fiaInspItemMapper.insert(it);
        }
        log(task, 1, "创建任务", "系统");
        log(task, 2, "标准调取(" + std.getCode() + ")", "系统");
        return task;
    }

    @Override
    @Transactional
    public void enterResults(String taskId, List<FiaInspItem> items) {
        for (FiaInspItem it : items) {
            FiaInspItem upd = new FiaInspItem();
            upd.setId(it.getId());
            upd.setMeasuredValue(it.getMeasuredValue());
            upd.setJudge(it.getJudge());
            fiaInspItemMapper.updateById(upd);
        }
        FiaTask task = new FiaTask();
        task.setId(taskId);
        task.setStatus("进行中");
        fiaTaskMapper.updateById(task);
        log(fiaTaskMapper.selectById(taskId), 3, "检验录入", currentOperator());
    }

    @Override
    public void signInspector(String taskId, String password, String itemId) {
        FiaTask task = fiaTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(400, "任务不存在");
        }
        verifyPassword(task.getOrgId(), password);
        // 逐项签名(sign_granularity=逐项签名):itemId 非空时仅记日志,不改 task 状态;
        // 所有项签完后由调用方再发起一次整单签名(itemId 为空)触发状态流转。
        if (itemId != null && !itemId.isEmpty()) {
            log(task, 4, "检验人签名-项" + itemId, currentOperator());
            return;
        }
        // 整单签名:原逻辑(设 inspectorId + 状态 -> 待复核)
        if (!"进行中".equals(task.getStatus())) {
            throw new BusinessException(400, "任务状态不允许检验签名(需为进行中)");
        }
        task.setInspectorId(currentOperator());
        task.setStatus("待复核");
        fiaTaskMapper.updateById(task);
        log(task, 4, "检验人签名", currentOperator());
    }

    @Override
    @Transactional
    public void signReviewer(String taskId, String password, String itemId) {
        FiaTask task = fiaTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(400, "任务不存在");
        }
        verifyPassword(task.getOrgId(), password);
        // 逐项签名:itemId 非空时仅记日志,不改 task 状态
        if (itemId != null && !itemId.isEmpty()) {
            log(task, 5, "复核人签名-项" + itemId, currentOperator());
            return;
        }
        // 整单签名:原逻辑
        if (!"待复核".equals(task.getStatus())) {
            throw new BusinessException(400, "任务状态不允许复核签名(需为待复核)");
        }
        task.setReviewerId(currentOperator());
        task.setReviewedAt(LocalDateTime.now());
        FiaSignConfig config = signConfigService.get(task.getOrgId());
        if (config != null && "三级".equals(config.getSignNodes())) {
            task.setStatus("待批准");
            fiaTaskMapper.updateById(task);
            log(task, 5, "复核人签名(待批准)", currentOperator());
        } else {
            completeAndArchive(task, 5, "复核人签名");
        }
    }

    @Override
    @Transactional
    public void signApprover(String taskId, String password) {
        FiaTask task = fiaTaskMapper.selectById(taskId);
        if (task == null || !"待批准".equals(task.getStatus())) {
            throw new BusinessException(400, "任务状态不允许批准签名(需为待批准)");
        }
        verifyPassword(task.getOrgId(), password);
        task.setApproverId(currentOperator());
        task.setApprovedAt(LocalDateTime.now());
        completeAndArchive(task, 6, "批准人签名");
    }

    @Override
    public FiaArchivedReport getArchive(String taskId) {
        return fiaArchivedReportMapper.selectOne(
                new LambdaQueryWrapper<FiaArchivedReport>().eq(FiaArchivedReport::getTaskId, taskId));
    }

    // ---- 可配置签名:密码校验 + 锁定 ----
    private void verifyPassword(String orgId, String password) {
        FiaSignConfig config = signConfigService.get(orgId);
        if (config == null || config.getSignMethods() == null) {
            return;
        }
        if (!Arrays.asList(config.getSignMethods()).contains("password")) {
            return; // password 不在配置方式内(手写/CA),免密
        }
        String userId = currentOperator();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException(401, "账号已锁定,请稍后再试");
        }
        if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            int fail = (user.getFailCount() == null ? 0 : user.getFailCount()) + 1;
            int lockAfter = config.getLockAfterFail() == null ? 3 : config.getLockAfterFail();
            SysUser upd = new SysUser();
            upd.setId(userId);
            upd.setFailCount(fail);
            if (fail >= lockAfter) {
                int lockMin = config.getLockMinutes() == null ? 5 : config.getLockMinutes();
                upd.setLockUntil(LocalDateTime.now().plusMinutes(lockMin));
                sysUserMapper.updateById(upd);
                throw new BusinessException(401, "密码错误次数过多,已锁定" + lockMin + "分钟");
            }
            sysUserMapper.updateById(upd);
            throw new BusinessException(401, "密码错误(剩余" + (lockAfter - fail) + "次)");
        }
        if (user.getFailCount() != null && user.getFailCount() > 0) {
            SysUser upd = new SysUser();
            upd.setId(userId);
            upd.setFailCount(0);
            upd.setLockUntil(null);
            sysUserMapper.updateById(upd);
        }
    }

    private void completeAndArchive(FiaTask task, int logSeq, String logName) {
        task.setStatus("已完成");
        task.setSubmittedAt(LocalDateTime.now());
        task.setOverallJudge(computeJudge(task.getId()));
        // 拦截生产:首件不合格时设 disposition=拦截(记录级,不阻断 MES;一期不接 MES)
        if (!"合格".equals(task.getOverallJudge())) {
            if (task.getDisposition() == null || task.getDisposition().isEmpty()) {
                task.setDisposition("拦截");
            }
        }
        fiaTaskMapper.updateById(task);
        archive(task);
        log(task, logSeq, logName, currentOperator());
        log(task, logSeq + 1, "归档报告", "系统");
        // 拦截生产日志 + 告警(归档日志之后)
        if (!"合格".equals(task.getOverallJudge())) {
            log(task, logSeq + 2, "拦截生产-首件不合格", "系统");
            log.warn("FIA拦截生产: code={}, woNo={}, judge={}", task.getCode(), task.getWoNo(), task.getOverallJudge());
        }
        // FIA->SPC 联动:仅合格时同步 CTQ 数值到 SPC,异常只 log 不阻断主流程
        if ("合格".equals(task.getOverallJudge())) {
            try {
                syncToSpc(task);
            } catch (Exception e) {
                log.warn("FIA->SPC 联动失败, taskId={}: {}", task.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * FIA->SPC 联动:FIA 任务合格时,将 CTQ 检验项的数值型测量值同步到
     * procName 匹配的激活 SPC 参数,触发 SPC 子组录入 + WECO 判异。
     * 异常由调用方 try-catch,不阻断 FIA 主流程。
     */
    private void syncToSpc(FiaTask task) {
        // 查 FIA 任务的 CTQ 项
        List<FiaInspItem> ctqItems = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>()
                        .eq(FiaInspItem::getTaskId, task.getId())
                        .eq(FiaInspItem::getIsCtq, true));
        if (ctqItems.isEmpty()) {
            return;
        }
        // 查 FIA 标准(by task.stdId)获取 procName(SPC param 无 material 字段,按 procName 匹配)
        FiaInspStd std = fiaInspStdMapper.selectById(task.getStdId());
        if (std == null || std.getProcName() == null || std.getProcName().isBlank()) {
            return;
        }
        // 查激活的 SPC 参数(by procName)
        List<SpcParam> params = spcParamMapper.selectList(
                new LambdaQueryWrapper<SpcParam>()
                        .eq(SpcParam::getProcName, std.getProcName())
                        .eq(SpcParam::getIsActive, true));
        if (params.isEmpty()) {
            return;
        }
        // CTQ 项的数值型测量值(非数值/null 跳过)
        List<BigDecimal> values = ctqItems.stream()
                .map(i -> {
                    try {
                        return i.getMeasuredValue() == null ? null : new BigDecimal(i.getMeasuredValue().trim());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        if (values.isEmpty()) {
            return;
        }
        // 对每个匹配的 SPC 参数创建子组(spcSubgroupService.create 会算 xbar/rangeR + WECO)
        for (SpcParam param : params) {
            try {
                SpcSubgroup sg = new SpcSubgroup();
                sg.setOrgId(task.getOrgId());
                sg.setParamId(param.getId());
                sg.setSubgroupTime(LocalDateTime.now());
                sg.setShift("FIA联动");
                sg.setWoNo(task.getWoNo());
                sg.setBatchNo(task.getBatchNo());
                sg.setDataSource("fia");
                spcSubgroupService.create(sg, values);
            } catch (Exception e) {
                // 单个参数失败不影响其他参数,整体异常由上层 try-catch 兜底
                log.warn("FIA->SPC 联动:SPC 参数 {} 创建子组失败: {}", param.getId(), e.getMessage(), e);
            }
        }
    }

    private String computeJudge(String taskId) {
        List<FiaInspItem> items = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, taskId));
        boolean anyCtqFail = items.stream().anyMatch(i -> Boolean.TRUE.equals(i.getIsCtq()) && "不合格".equals(i.getJudge()));
        boolean anyFail = items.stream().anyMatch(i -> "不合格".equals(i.getJudge()));
        if (anyCtqFail) {
            return "不合格";
        }
        return anyFail ? "警告" : "合格";
    }

    private void archive(FiaTask task) {
        List<FiaInspItem> items = fiaInspItemMapper.selectList(
                new LambdaQueryWrapper<FiaInspItem>().eq(FiaInspItem::getTaskId, task.getId()));
        StringBuilder sb = new StringBuilder(task.getCode()).append('|').append(task.getWoNo());
        for (FiaInspItem i : items) {
            sb.append('|').append(i.getItemName()).append(':').append(i.getMeasuredValue()).append('(').append(i.getJudge()).append(')');
        }
        FiaArchivedReport r = new FiaArchivedReport();
        r.setOrgId(task.getOrgId());
        r.setReportNo("AR-" + task.getCode());
        r.setTaskId(task.getId());
        r.setWoNo(task.getWoNo());
        r.setArchiveDate(LocalDate.now());
        r.setStatus("已归档");
        r.setReportHash(sha256(sb.toString()));
        r.setRetentionUntil(LocalDate.now().plusYears(15));
        // 生成归档 PDF(openhtmltopdf),失败回退占位
        r.setPdfRef(generatePdf(task, items, r));
        fiaArchivedReportMapper.insert(r);
    }

    /**
     * 用 openhtmltopdf 从 HTML 模板生成归档 PDF,存本地 logs/reports/ 目录(一期不接 MinIO)。
     * 生成失败则回退 placeholder://,不阻断归档主流程。
     */
    private String generatePdf(FiaTask task, List<FiaInspItem> items, FiaArchivedReport r) {
        try {
            String html = buildReportHtml(task, items, r);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            builder.toStream(os);
            builder.run();
            byte[] pdfBytes = os.toByteArray();
            String fileName = "fia-report-" + task.getCode() + ".pdf";
            Path pdfPath = Paths.get("logs", "reports", fileName);
            Files.createDirectories(pdfPath.getParent());
            Files.write(pdfPath, pdfBytes);
            return pdfPath.toString();
        } catch (Exception e) {
            log.warn("FIA 归档 PDF 生成失败,回退占位, taskId={}: {}", task.getId(), e.getMessage(), e);
            return "placeholder://" + task.getId();
        }
    }

    /** 构建归档报告 HTML(内联样式,不依赖外部 CSS;openhtmltopdf 渲染)。 */
    private String buildReportHtml(FiaTask task, List<FiaInspItem> items, FiaArchivedReport r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/><style>")
          .append("body{font-family:'SimSun',serif;font-size:12px;color:#000;margin:24px;}")
          .append("h1{text-align:center;font-size:20px;margin:8px 0 16px;}")
          .append("h2{font-size:14px;margin:12px 0 4px;border-left:4px solid #333;padding-left:6px;}")
          .append("table{border-collapse:collapse;width:100%;margin:4px 0;}")
          .append("th,td{border:1px solid #333;padding:4px 6px;text-align:left;font-size:11px;}")
          .append("th{background:#eee;}")
          .append(".info{margin:2px 0;}")
          .append(".label{display:inline-block;width:110px;font-weight:bold;}")
          .append("</style></head><body>");
        // 标题
        sb.append("<h1>首件检验归档报告</h1>");
        // 报告元信息
        sb.append("<div class=\"info\"><span class=\"label\">报告编号:</span>").append(str(r.getReportNo())).append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">归档日期:</span>").append(str(r.getArchiveDate())).append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">留存至:</span>").append(str(r.getRetentionUntil())).append("</div>");
        sb.append("<div class=\"info\"><span class=\"label\">报告哈希:</span>").append(str(r.getReportHash())).append("</div>");
        // 任务信息
        sb.append("<h2>任务信息</h2>");
        sb.append("<table>");
        appendRow(sb, "任务编号", str(task.getCode()), "工单号", str(task.getWoNo()));
        appendRow(sb, "产品名称", str(task.getProductName()), "工序名称", str(task.getProcName()));
        appendRow(sb, "产线", str(task.getLineName()), "批次号", str(task.getBatchNo()));
        appendRow(sb, "触发类型", str(task.getTriggerType()), "是否紧急", Boolean.TRUE.equals(task.getIsUrgent()) ? "是" : "否");
        appendRow(sb, "标准版本", str(task.getStdVersion()), "AQL", str(task.getAql()));
        appendRow(sb, "任务状态", str(task.getStatus()), "综合判定", str(task.getOverallJudge()));
        appendRow(sb, "处置", str(task.getDisposition()), "提交时间", str(task.getSubmittedAt()));
        sb.append("</table>");
        // 检验项目
        sb.append("<h2>检验项目</h2>");
        sb.append("<table><tr><th>序号</th><th>检验项</th><th>CTQ</th><th>标准值</th><th>公差</th><th>单位</th><th>测量值</th><th>判定</th></tr>");
        for (FiaInspItem i : items) {
            sb.append("<tr>")
              .append("<td>").append(str(i.getSeq())).append("</td>")
              .append("<td>").append(str(i.getItemName())).append("</td>")
              .append("<td>").append(Boolean.TRUE.equals(i.getIsCtq()) ? "是" : "").append("</td>")
              .append("<td>").append(str(i.getStdValue())).append("</td>")
              .append("<td>").append(str(i.getTolerance())).append("</td>")
              .append("<td>").append(str(i.getUnit())).append("</td>")
              .append("<td>").append(str(i.getMeasuredValue())).append("</td>")
              .append("<td>").append(str(i.getJudge())).append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");
        // 签名
        sb.append("<h2>签名</h2>");
        sb.append("<table><tr><th>角色</th><th>签名人</th><th>时间</th></tr>");
        sb.append("<tr><td>检验人</td><td>").append(str(task.getInspectorId())).append("</td><td></td></tr>");
        sb.append("<tr><td>复核人</td><td>").append(str(task.getReviewerId())).append("</td><td>").append(str(task.getReviewedAt())).append("</td></tr>");
        sb.append("<tr><td>批准人</td><td>").append(str(task.getApproverId())).append("</td><td>").append(str(task.getApprovedAt())).append("</td></tr>");
        sb.append("</table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, String l1, String v1, String l2, String v2) {
        sb.append("<tr><td><b>").append(l1).append("</b></td><td>").append(v1).append("</td>")
          .append("<td><b>").append(l2).append("</b></td><td>").append(v2).append("</td></tr>");
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder h = new StringBuilder();
            for (byte x : b) {
                h.append(String.format("%02x", x));
            }
            return h.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void log(FiaTask task, int seq, String nodeName, String operator) {
        FiaTaskLog l = new FiaTaskLog();
        l.setOrgId(task.getOrgId());
        l.setTaskId(task.getId());
        l.setNodeSeq(seq);
        l.setNodeName(nodeName);
        l.setOpTime(LocalDateTime.now());
        l.setOperator(operator);
        l.setOpType("系统".equals(operator) ? "系统" : "人工");
        l.setIsDone(true);
        fiaTaskLogMapper.insert(l);
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }
}
