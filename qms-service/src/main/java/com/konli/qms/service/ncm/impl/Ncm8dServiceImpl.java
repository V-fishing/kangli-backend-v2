package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.entity.Qms8dStageDetail;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.ncm.mapper.Qms8dStageDetailMapper;
import com.konli.qms.service.ncm.Ncm8dService;
import com.konli.qms.service.ncm.dto.EightDVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Ncm8dServiceImpl implements Ncm8dService {

    private final Qms8dReportMapper qms8dReportMapper;
    private final Qms8dStageDetailMapper qms8dStageDetailMapper;

    /** 8D 阶段顺序:D1->D2->D3->D4->D5->D6->D7->D8 */
    private static final String[] STAGES = {"D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8"};

    /** 需审批的阶段(对应 qms_8d_stage_config.need_approval=true) */
    private static final List<String> APPROVAL_STAGES = Arrays.asList("D3", "D5", "D7");

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
        report.setD8No("8D-" + System.currentTimeMillis());
        report.setCurrentStage("D1");
        report.setStatus("进行中");
        if (report.getCapaTriggered() == null) {
            report.setCapaTriggered(false);
        }
        qms8dReportMapper.insert(report);
        return report;
    }

    @Override
    @Transactional
    public void advanceStage(String d8Id, String stageCode, String content, String owner) {
        Qms8dReport report = qms8dReportMapper.selectById(d8Id);
        if (report == null) {
            throw new BusinessException(404, "8D 报告不存在");
        }
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

        // 创建/更新阶段明细(UNIQUE(d8_id, stage_code))
        Qms8dStageDetail existing = qms8dStageDetailMapper.selectOne(
                new LambdaQueryWrapper<Qms8dStageDetail>()
                        .eq(Qms8dStageDetail::getD8Id, d8Id)
                        .eq(Qms8dStageDetail::getStageCode, stageCode));
        if (existing == null) {
            Qms8dStageDetail detail = new Qms8dStageDetail();
            detail.setOrgId(report.getOrgId());
            detail.setD8Id(d8Id);
            detail.setStageCode(stageCode);
            detail.setContent(content);
            detail.setOwner(owner);
            if (APPROVAL_STAGES.contains(stageCode)) {
                detail.setApprovalStatus("待审批");
            }
            qms8dStageDetailMapper.insert(detail);
        } else {
            existing.setContent(content);
            existing.setOwner(owner);
            if (APPROVAL_STAGES.contains(stageCode) && existing.getApprovalStatus() == null) {
                existing.setApprovalStatus("待审批");
            }
            qms8dStageDetailMapper.updateById(existing);
        }

        // 更新 8D 报告 currentStage -> 下一阶段;若已是 D8 则闭环
        Qms8dReport upd = new Qms8dReport();
        upd.setId(d8Id);
        if (idx == STAGES.length - 1) {
            // 推进到 D8 完成最后一阶段 -> 闭环
            upd.setStatus("已闭环");
            upd.setCloseDate(LocalDate.now());
            upd.setCurrentStage(stageCode);
        } else {
            upd.setCurrentStage(STAGES[idx + 1]);
        }
        qms8dReportMapper.updateById(upd);
    }
}
