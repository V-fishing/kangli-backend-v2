package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.ncm.entity.NcmCorrectiveAction;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.domain.ncm.mapper.NcmCorrectiveActionMapper;
import com.konli.qms.domain.ncm.mapper.NcmDefectRecordMapper;
import com.konli.qms.service.ncm.NcmCorrectiveActionService;
import com.konli.qms.service.notify.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NcmCorrectiveActionServiceImpl implements NcmCorrectiveActionService {

    private final NcmCorrectiveActionMapper ncmCorrectiveActionMapper;
    private final NcmDefectRecordMapper ncmDefectRecordMapper;
    private final NotificationService notificationService;

    @Override
    public List<NcmCorrectiveAction> list() {
        return ncmCorrectiveActionMapper.selectList(null);
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
            notificationService.notifyRoles(List.of("qmanager", "sqe"),
                    "纠正措施已创建",
                    "纠正措施" + action.getCaNo() + "(" + (action.getIssue() != null ? action.getIssue() : "") + ") 已创建,负责人:" + (action.getOwner() != null ? action.getOwner() : "-"),
                    "capa_action", action.getId(), "/ncm/capa", null);
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
                notificationService.notifyRoles(List.of("qmanager", "sqe"),
                        "纠正措施已完成",
                        "纠正措施" + action.getCaNo() + " 进度已达100%,请核查闭环。",
                        "capa_action", id, "/ncm/capa", null);
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
            notificationService.notifyRoles(List.of("qmanager", "sqe"),
                    "纠正措施已关闭",
                    "纠正措施" + action.getCaNo() + "(" + (action.getIssue() != null ? action.getIssue() : "") + ") 已关闭。",
                    "capa_action", id, "/ncm/capa", null);
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
}
