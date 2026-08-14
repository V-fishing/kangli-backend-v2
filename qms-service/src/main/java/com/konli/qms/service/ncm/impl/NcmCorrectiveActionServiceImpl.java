package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.domain.ncm.mapper.NcmCorrectiveActionMapper;
import com.konli.qms.domain.ncm.mapper.NcmDefectRecordMapper;
import com.konli.qms.service.ncm.NcmCorrectiveActionService;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NcmCorrectiveActionServiceImpl implements NcmCorrectiveActionService {

    private final NcmCorrectiveActionMapper ncmCorrectiveActionMapper;
    private final NcmDefectRecordMapper ncmDefectRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final NotificationService notificationService;
    private final com.konli.qms.service.assign.AssignReassignService assignReassignService;

    @Override
    public List<NcmCorrectiveAction> list() {
        List<NcmCorrectiveAction> list = ncmCorrectiveActionMapper.selectList(null);
        fillOwnerNames(list);
        return list;
    }

    @Override
    public PageResult<NcmCorrectiveAction> listPage(String defectNo, String status, int page, int size) {
        LambdaQueryWrapper<NcmCorrectiveAction> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(defectNo)) {
            w.like(NcmCorrectiveAction::getDefectNo, defectNo.trim());
        }
        if (StringUtils.hasText(status)) {
            w.eq(NcmCorrectiveAction::getStatus, status);
        }
        w.orderByDesc(NcmCorrectiveAction::getCreatedAt);
        IPage<NcmCorrectiveAction> ip = ncmCorrectiveActionMapper.selectPage(new Page<>(page, size), w);
        fillOwnerNames(ip.getRecords());
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    /** 批量解析责任人姓名(owner 存的是 sys_user.id):仅对合法 UUID 查询,避免历史自由文本触发的类型转换错误。 */
    private void fillOwnerNames(List<NcmCorrectiveAction> list) {
        Set<String> ownerIds = list.stream()
                .map(NcmCorrectiveAction::getOwner)
                .filter(this::isUuid)
                .collect(Collectors.toSet());
        if (ownerIds.isEmpty()) return;
        List<SysUser> users = sysUserMapper.selectBatchIds(ownerIds);
        Map<String, String> nameMap = users.stream().collect(Collectors.toMap(
                SysUser::getId,
                u -> (u.getRealName() != null && !u.getRealName().isBlank())
                        ? u.getRealName() : u.getUsername(),
                (a, b) -> a));
        list.forEach(ca -> {
            String name = nameMap.get(ca.getOwner());
            if (name != null) ca.setOwnerName(name);
        });
    }

    private boolean isUuid(String s) {
        if (s == null || s.length() != 36) return false;
        try {
            java.util.UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public List<NcmCorrectiveAction> listByDefectNo(String defectNo) {
        return ncmCorrectiveActionMapper.selectList(
                new LambdaQueryWrapper<NcmCorrectiveAction>()
                        .eq(defectNo != null && !defectNo.isBlank(), NcmCorrectiveAction::getDefectNo, defectNo));
    }

    @Override
    public NcmCorrectiveAction get(String id) {
        return ncmCorrectiveActionMapper.selectById(id);
    }

    @Override
    @Transactional
    public NcmCorrectiveAction create(NcmCorrectiveAction action) {
        action.setCaNo("CA-" + System.currentTimeMillis());
        action.setStatus("待启动");
        if (action.getProgress() == null) {
            action.setProgress((short) 0);
        }
        ncmCorrectiveActionMapper.insert(action);
        // 通知负责人
        try {
            notificationService.notify("ncm", "ncm_capa_created",
                    "纠正措施已创建",
                    "纠正措施" + action.getCaNo() + "(" + (action.getIssue() != null ? action.getIssue() : "") + ") 已创建,负责人:" + (action.getOwner() != null ? action.getOwner() : "-"),
                    "capa_action", action.getId(), "/ncm/capa");
        } catch (Exception e) {
            log.warn("[CAPA] 创建通知失败: {}", e.getMessage());
        }
        return action;
    }

    @Override
    @Transactional
    public void updateProgress(String id, Short progress) {
        NcmCorrectiveAction action = ncmCorrectiveActionMapper.selectById(id);
        if (action == null) {
            throw new BusinessException(404, "纠正措施不存在");
        }
        NcmCorrectiveAction upd = new NcmCorrectiveAction();
        upd.setId(id);
        upd.setProgress(progress);
        if (progress != null) {
            if (progress == 100) {
                upd.setStatus("已完成");
            } else if (progress > 0 && progress < 100) {
                upd.setStatus("进行中");
            }
        }
        ncmCorrectiveActionMapper.updateById(upd);
        // 进度100%完成通知
        if (progress != null && progress == 100) {
            try {
                notificationService.notify("ncm", "ncm_capa_completed",
                        "纠正措施已完成",
                        "纠正措施" + action.getCaNo() + " 进度已达100%,请核查闭环。",
                        "capa_action", id, "/ncm/capa");
            } catch (Exception e) {
                log.warn("[CAPA] 完成通知失败: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void close(String id) {
        NcmCorrectiveAction action = ncmCorrectiveActionMapper.selectById(id);
        if (action == null) {
            throw new BusinessException(404, "纠正措施不存在");
        }
        NcmCorrectiveAction upd = new NcmCorrectiveAction();
        upd.setId(id);
        upd.setStatus("已关闭");
        ncmCorrectiveActionMapper.updateById(upd);

        // 关闭通知
        try {
                notificationService.notify("ncm", "ncm_capa_closed",
                    "纠正措施已关闭",
                    "纠正措施" + action.getCaNo() + "(" + (action.getIssue() != null ? action.getIssue() : "") + ") 已关闭。",
                    "capa_action", id, "/ncm/capa");
        } catch (Exception e) {
            log.warn("[CAPA] 关闭通知失败: {}", e.getMessage());
        }

        // 闭环:回写关联不良记录的disposition
        if (action.getDefectNo() != null && !action.getDefectNo().isBlank()) {
            NcmDefectRecord defect = ncmDefectRecordMapper.selectOne(
                    new LambdaQueryWrapper<NcmDefectRecord>()
                            .eq(NcmDefectRecord::getDefectNo, action.getDefectNo()));
            if (defect != null) {
                NcmDefectRecord defUpd = new NcmDefectRecord();
                defUpd.setId(defect.getId());
                defUpd.setDisposition("已纠正");
                ncmDefectRecordMapper.updateById(defUpd);
            }
        }
    }

    /** 列表级改派责任人(更新 owner_user_id/owner + 推送被指派人任务中心)。 */
    @Override
    @Transactional
    public void reassign(String id, DefectLaunchRequest req) {
        NcmCorrectiveAction action = ncmCorrectiveActionMapper.selectById(id);
        if (action == null) throw new BusinessException(404, "纠正措施不存在");
        String ownerName = assignReassignService.execute(new com.konli.qms.service.assign.AssignReassignService.ReassignContext(
                "CA", id, action.getCaNo(), action.getOrgId(),
                "/ncm/corrective-actions/" + id, null, action.getDefectNo(), req, true));
        NcmCorrectiveAction upd = new NcmCorrectiveAction();
        upd.setId(id);
        upd.setOwnerUserId(req.getOwnerUserId());
        upd.setOwner(req.getOwnerUserId());   // 纠正措施 owner 字段存的是 sys_user.id(UUID)
        ncmCorrectiveActionMapper.updateById(upd);
        action.setOwnerName(ownerName);
    }
}
