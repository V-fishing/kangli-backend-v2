package com.konli.qms.service.assign;

import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.QmsAssignRecord;
import com.konli.qms.domain.ncm.mapper.QmsAssignRecordMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用「指派 / 改派」服务：把"写 qms_assign_record + 推送被指派人任务中心"集中实现，
 * 供 8D / CAPA / 纠正措施 / SQM 来料异常 / 审核 / FMEA 列表级指派与改派复用。
 *
 * 行为：
 *  - 优先按 ownerUserId(单人) 指派；否则按 assignRoleCodes(部门/角色团队) 指派。
 *  - 追加一条 qms_assign_record(action='reassign' 或 'assign')。
 *  - 通过 NotificationService 推送到被指派人站内信(任务中心数据源)。
 *  - 返回被指派人姓名，供调用方同步实体 owner_user_name 展示字段。
 */
@Service
@RequiredArgsConstructor
public class AssignReassignService {

    private final QmsAssignRecordMapper assignRecordMapper;
    private final NotificationService notificationService;
    private final SysUserMapper sysUserMapper;
    private final JdbcTemplate jdbcTemplate;

    /** 改派/指派上下文(由各业务模块在调用前组装)。 */
    public record ReassignContext(
            String bizType,            // 8D / CAPA / CA / SQM异常 / 审核 / FMEA
            String bizId,              // 业务单据 ID
            String bizNo,              // 业务单据编号(d8_no/capa_no/...)
            String orgId,              // 组织(数据权限)
            String link,               // 任务中心跳转链接
            String defectId,           // 关联不良/异常单 ID(可空)
            String defectNo,           // 关联不良/异常单编号(可空)
            DefectLaunchRequest req,   // 复用现有指派请求体
            boolean reassign           // true=改派(追加 action=reassign)，false=首次指派
    ) {}

    /**
     * 执行指派/改派：写记录 + 推送通知，返回被指派人姓名(单人时)或角色名(团队时)。
     */
    @Transactional
    public String execute(ReassignContext ctx) {
        DefectLaunchRequest req = ctx.req();
        List<String> channels = (req.getNotifyChannels() == null || req.getNotifyChannels().isEmpty())
                ? List.of("站内弹窗") : req.getNotifyChannels();
        String channelStr = String.join(",", channels);
        boolean inbox = channels.contains("站内弹窗");
        String assignerId = currentOperator();

        QmsAssignRecord rec = new QmsAssignRecord();
        rec.setOrgId(ctx.orgId());
        rec.setDefectId(ctx.defectId());
        rec.setDefectNo(ctx.defectNo());
        rec.setBizType(ctx.bizType());
        rec.setBizId(ctx.bizId());
        rec.setBizNo(ctx.bizNo());
        rec.setNotifyChannels(channelStr);
        rec.setAssignerId(assignerId);
        rec.setRemark(req.getRemark());
        rec.setAction(ctx.reassign() ? "reassign" : "assign");

        String displayName;
        if (StringUtils.hasText(req.getOwnerUserId())) {
            // 单人指派
            String userName = queryUserName(req.getOwnerUserId());
            displayName = (userName != null && !userName.isBlank()) ? userName : req.getOwnerUserId();
            rec.setAssigneeUserId(req.getOwnerUserId());
            rec.setAssigneeUserName(displayName);
            assignRecordMapper.insert(rec);
            if (inbox) {
                notificationService.notifyUser(req.getOwnerUserId(),
                        buildTitle(ctx), buildContent(ctx, displayName), "NCM_ASSIGN", ctx.bizId(), ctx.link());
            }
        } else if (req.getAssignRoleCodes() != null && !req.getAssignRoleCodes().isEmpty()) {
            // 部门/角色团队指派
            List<String> roleNames = resolveRoleNames(req.getAssignRoleCodes());
            displayName = String.join("、", roleNames);
            rec.setAssigneeRoleCode(String.join(",", req.getAssignRoleCodes()));
            rec.setAssigneeRoleName(displayName);
            assignRecordMapper.insert(rec);
            if (inbox) {
                notificationService.notifyRoles(req.getAssignRoleCodes(),
                        buildTitle(ctx), buildContent(ctx, displayName), "NCM_ASSIGN", ctx.bizId(), ctx.link(), assignerId);
            }
        } else {
            throw new IllegalArgumentException("指派必须指定 ownerUserId 或 assignRoleCodes 之一");
        }
        return displayName;
    }

    private String buildTitle(ReassignContext ctx) {
        return "[" + ctx.bizType() + (ctx.reassign() ? "改派" : "指派") + "] " + ctx.bizNo();
    }

    private String buildContent(ReassignContext ctx, String who) {
        return ctx.bizType() + " " + ctx.bizNo() + " 已将您" + (ctx.reassign() ? "改派" : "指派") + "为责任人(" + who + ")。"
                + (ctx.req().getRemark() != null && !ctx.req().getRemark().isBlank()
                ? "\n指派备注: " + ctx.req().getRemark() : "");
    }

    private String queryUserName(String userId) {
        try {
            SysUser u = sysUserMapper.selectById(userId);
            if (u != null) {
                return (u.getRealName() != null && !u.getRealName().isBlank()) ? u.getRealName() : u.getUsername();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<String> resolveRoleNames(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) return List.of();
        String placeholders = roleCodes.stream().map(c -> "?").collect(Collectors.joining(","));
        String sql = "SELECT DISTINCT r.role_name FROM ops.sys_role r WHERE r.role_code IN (" + placeholders + ")";
        List<String> names = jdbcTemplate.query(sql, (rs, row) -> rs.getString(1), roleCodes.toArray());
        return names == null ? List.of() : names;
    }

    private String currentOperator() {
        CompanyContext.CurrentUser u = CompanyContext.get();
        return u == null ? "系统" : u.userId();
    }
}
