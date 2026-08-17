package com.konli.qms.service.ncm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.konli.qms.common.api.PageResult;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.security.CompanyContext;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import com.konli.qms.domain.ncm.entity.QmsCapa;
import com.konli.qms.domain.ncm.entity.QmsCapaAction;
import com.konli.qms.domain.ncm.entity.Qms8dReport;
import com.konli.qms.domain.ncm.mapper.Qms8dReportMapper;
import com.konli.qms.domain.ncm.mapper.QmsCapaActionMapper;
import com.konli.qms.domain.ncm.mapper.QmsCapaMapper;
import com.konli.qms.domain.sqm.entity.SqmIncomingAbnormal;
import com.konli.qms.domain.sqm.mapper.SqmIncomingAbnormalMapper;
import com.konli.qms.service.ncm.NcmCapaService;
import com.konli.qms.service.ncm.NcmDefectRecordService;
import com.konli.qms.service.ncm.dto.AbnormalCapaLaunchRequest;
import com.konli.qms.service.ncm.dto.CapaVo;
import com.konli.qms.service.ncm.dto.DefectLaunchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NcmCapaServiceImpl implements NcmCapaService {

    private final QmsCapaMapper qmsCapaMapper;
    private final QmsCapaActionMapper qmsCapaActionMapper;
    private final Qms8dReportMapper qms8dReportMapper;
    private final SqmIncomingAbnormalMapper abnormalMapper;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final com.konli.qms.service.assign.AssignReassignService assignReassignService;
    // @Lazy 打破与 NcmDefectRecordServiceImpl(其构造器注入 NcmCapaService)的循环依赖
    @Autowired @Lazy
    private NcmDefectRecordService ncmDefectRecordService;

    private String resolveDefaultOrgId() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id::text FROM ops.sys_org ORDER BY created_at LIMIT 1", String.class);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public List<QmsCapa> list() {
        return qmsCapaMapper.selectList(null);
    }

    @Override
    public PageResult<QmsCapa> listPage(String keyword, int page, int size) {
        LambdaQueryWrapper<QmsCapa> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(k -> k.like(QmsCapa::getCapaNo, keyword)
                    .or().like(QmsCapa::getIssue, keyword)
                    .or().like(QmsCapa::getSourceRefId, keyword));
        }
        w.orderByDesc(QmsCapa::getCreatedAt);
        IPage<QmsCapa> ip = qmsCapaMapper.selectPage(new Page<>(page, size), w);
        return new PageResult<>(ip.getRecords(), ip.getTotal(), (int) ip.getCurrent(), (int) ip.getSize());
    }

    @Override
    public CapaVo get(String id) {
        CapaVo vo = new CapaVo();
        vo.setCapa(qmsCapaMapper.selectById(id));
        vo.setActions(qmsCapaActionMapper.selectList(
                new LambdaQueryWrapper<QmsCapaAction>()
                        .eq(QmsCapaAction::getCapaId, id)
                        .orderByAsc(QmsCapaAction::getSeq)));
        return vo;
    }

    @Override
    @Transactional
    public QmsCapa create(QmsCapa capa) {
        capa.setCapaNo("CAPA-" + System.currentTimeMillis());
        capa.setStatus("待启动");
        if (capa.getProgress() == null) {
            capa.setProgress((short) 0);
        }
        if (capa.getOrgId() == null) {
            CompanyContext.CurrentUser u = CompanyContext.get();
            if (u != null && u.orgId() != null && !"ROOT".equals(u.orgId())) {
                capa.setOrgId(u.orgId());
            }
        }
        // 兜底 orgId：集团管理员(dataScope=all)无归属 org 时，使用关联异常单/8D 所属组织，
        // 避免 qms_capa.org_id 非空约束导致插入 500（前端集团总览视图 orgId 为空）。
        if (capa.getOrgId() == null && capa.getAbnormalId() != null && !capa.getAbnormalId().isBlank()) {
            SqmIncomingAbnormal ab = abnormalMapper.selectById(capa.getAbnormalId());
            if (ab != null && ab.getOrgId() != null && !"ROOT".equals(ab.getOrgId())) {
                capa.setOrgId(ab.getOrgId());
            }
        }
        if (capa.getOrgId() == null && capa.getD8Id() != null && !capa.getD8Id().isBlank()) {
            Qms8dReport d8 = qms8dReportMapper.selectById(capa.getD8Id());
            if (d8 != null && d8.getOrgId() != null && !"ROOT".equals(d8.getOrgId())) {
                capa.setOrgId(d8.getOrgId());
            }
        }
        if (capa.getOrgId() == null) {
            capa.setOrgId(resolveDefaultOrgId());
        }
        // due_date NOT NULL,未指定时兜底为 30 天后(与 SQM 严重 NC→CAPA 链路一致)
        if (capa.getDueDate() == null) {
            capa.setDueDate(LocalDate.now().plusDays(30));
        }
        // SR-CAR分支4:来料异常触发时,自动检测同供应商同物料30天内>=2次重复问题
        if ((capa.getTriggerType() == null || capa.getTriggerType().isBlank())
                && capa.getAbnormalId() != null && !capa.getAbnormalId().isBlank()) {
            SqmIncomingAbnormal ab = abnormalMapper.selectById(capa.getAbnormalId());
            if (ab != null && ab.getSupplierId() != null) {
                LocalDateTime since = LocalDateTime.now().minusDays(30);
                Long cnt = abnormalMapper.selectCount(new LambdaQueryWrapper<SqmIncomingAbnormal>()
                        .eq(SqmIncomingAbnormal::getSupplierId, ab.getSupplierId())
                        .eq(SqmIncomingAbnormal::getPartNo, ab.getPartNo())
                        .ge(SqmIncomingAbnormal::getOccurDate, since));
                if (cnt != null && cnt >= 2) {
                    capa.setTriggerType("重复问题");
                    capa.setTriggerCondition("同供应商同物料30天内>=" + cnt + "次异常");
                }
            }
        }
        qmsCapaMapper.insert(capa);
        return capa;
    }

    @Override
    @Transactional
    public QmsCapa launchFromAbnormal(AbnormalCapaLaunchRequest req) {
        QmsCapa capa = req.getCapa();
        if (capa == null) {
            throw new BusinessException(400, "缺少 CAPA 主体(capa)");
        }
        String abnormalId = capa.getAbnormalId();
        if (abnormalId == null || abnormalId.isBlank()) {
            throw new BusinessException(400, "缺少来源异常单(abnormalId)");
        }
        SqmIncomingAbnormal ab = abnormalMapper.selectById(abnormalId);
        if (ab == null) {
            throw new BusinessException(404, "来料异常单不存在");
        }
        // 统一整改源头:先登记一条 source=SQM异常 的缺陷记录,再从该缺陷记录发起 CAPA,
        // 使 CAPA 的 sourceRefId 指向缺陷记录(而非异常单),缺陷记录成为全平台 8D/CAPA/CA 的唯一源头
        String orgId = ab.getOrgId();
        if (orgId == null || orgId.isBlank() || "ROOT".equals(orgId)) {
            orgId = resolveDefaultOrgId();
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

        // 从缺陷记录发起 CAPA(标准范式:launchCapaFromDefect 内部回写缺陷记录 capaNo 并指派通知)
        QmsCapa created = (QmsCapa) ncmDefectRecordService.launchCapaFromDefect(def.getId(), req.getLaunch() != null ? req.getLaunch() : new DefectLaunchRequest());

        // 保留异常单反向链接:回写 capaId / rectifyType / 状态(异常单→CAPA 可追溯)
        SqmIncomingAbnormal upd = new SqmIncomingAbnormal();
        upd.setId(abnormalId);
        upd.setCapaId(created.getId());
        upd.setRectifyType("CAPA");
        upd.setStatus("整改中");
        abnormalMapper.updateById(upd);
        return created;
    }

    /** 独立事务创建 CAPA:联动场景(如严重审核NC/8D-D4 触发CAPA)失败时不回滚调用方主事务。 */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public QmsCapa createInNewTx(QmsCapa capa) {
        return create(capa);
    }

    @Override
    @Transactional
    public void updateProgress(String capaId, Short progress) {
        QmsCapa capa = qmsCapaMapper.selectById(capaId);
        if (capa == null) {
            throw new BusinessException(404, "CAPA 不存在");
        }
        QmsCapa upd = new QmsCapa();
        upd.setId(capaId);
        upd.setProgress(progress);
        // SR-CAR:progress>=60且<100且当前非"实施中"时触发审批关卡(status=待审批);100=已验证
        if (progress != null && progress == 100) {
            upd.setStatus("已验证");
        } else if (progress != null && progress >= 60 && !"实施中".equals(capa.getStatus())) {
            upd.setStatus("待审批");
        }
        qmsCapaMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void approve(String capaId, boolean approved, String comment) {
        QmsCapa capa = qmsCapaMapper.selectById(capaId);
        if (capa == null) throw new BusinessException(404, "CAPA 不存在");
        if (!"待审批".equals(capa.getStatus())) throw new BusinessException(400, "CAPA 当前状态不允许审批(需为待审批)");
        QmsCapa upd = new QmsCapa();
        upd.setId(capaId);
        if (approved) {
            upd.setStatus("实施中");
        } else {
            // SR-CAR分支1:审批不通过->退回,progress回退至50,status=分析中
            upd.setProgress((short) 50);
            upd.setStatus("分析中");
        }
        qmsCapaMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void reset(String capaId, String reason) {
        QmsCapa capa = qmsCapaMapper.selectById(capaId);
        if (capa == null) throw new BusinessException(404, "CAPA 不存在");
        QmsCapa upd = new QmsCapa();
        upd.setId(capaId);
        upd.setProgress((short) 0);
        upd.setStatus("分析中");
        qmsCapaMapper.updateById(upd);
    }

    @Override
    @Transactional
    public void close(String capaId) {
        QmsCapa capa = qmsCapaMapper.selectById(capaId);
        if (capa == null) {
            throw new BusinessException(404, "CAPA 不存在");
        }
        if ("已关闭".equals(capa.getStatus())) {
            throw new BusinessException(400, "CAPA 已关闭，无需重复操作");
        }
        if (capa.getProgress() == null || capa.getProgress() < 100) {
            throw new BusinessException(400, "CAPA 闭环失败：进度须达到 100%（已验证）");
        }
        if (!"已验证".equals(capa.getStatus())) {
            throw new BusinessException(400, "CAPA 闭环失败：当前状态为 " + capa.getStatus() + "，须为\"已验证\"");
        }
        QmsCapa upd = new QmsCapa();
        upd.setId(capaId);
        upd.setStatus("已关闭");
        qmsCapaMapper.updateById(upd);

        // 闭环链路:CAPA 闭环 -> 关联 8D 到 D8 闭环 -> 回写来源异常单
        if (capa.getD8Id() != null && !capa.getD8Id().isBlank()) {
            close8dById(capa.getD8Id());
            Qms8dReport d8 = qms8dReportMapper.selectById(capa.getD8Id());
            if (d8 != null) {
                closeAbnormalById(d8.getSourceRefId());
            }
        } else if (capa.getAbnormalId() != null && !capa.getAbnormalId().isBlank()) {
            closeAbnormalById(capa.getAbnormalId());
        }
    }

    /** 8D 闭环(幂等):CAPA 闭环时将其关联的 8D 置为 D8 已闭环。 */
    private void close8dById(String d8Id) {
        Qms8dReport d8 = qms8dReportMapper.selectById(d8Id);
        if (d8 == null || "已闭环".equals(d8.getStatus())) {
            return;
        }
        Qms8dReport upd = new Qms8dReport();
        upd.setId(d8Id);
        upd.setStatus("已闭环");
        upd.setCurrentStage("D8");
        upd.setCloseDate(LocalDate.now());
        qms8dReportMapper.updateById(upd);
    }

    /** 异常单闭环(幂等):仅当未关闭时置 已关闭 并写入闭环日期。 */
    private void closeAbnormalById(String abnormalId) {
        if (abnormalId == null || abnormalId.isBlank()) {
            return;
        }
        SqmIncomingAbnormal ab = abnormalMapper.selectById(abnormalId);
        if (ab == null || "已关闭".equals(ab.getStatus())) {
            return;
        }
        SqmIncomingAbnormal upd = new SqmIncomingAbnormal();
        upd.setId(abnormalId);
        upd.setStatus("已关闭");
        upd.setCloseDate(LocalDate.now());
        abnormalMapper.updateById(upd);
    }

    /** 列表级改派责任人(更新 owner_user_id/owner + 推送被指派人任务中心)。 */
    @Override
    @Transactional
    public void reassign(String capaId, DefectLaunchRequest req) {
        QmsCapa capa = qmsCapaMapper.selectById(capaId);
        if (capa == null) throw new BusinessException(404, "CAPA 不存在");
        String ownerName = assignReassignService.execute(new com.konli.qms.service.assign.AssignReassignService.ReassignContext(
                "CAPA", capaId, capa.getCapaNo(), capa.getOrgId(),
                "/ncm/capas/" + capaId, capa.getAbnormalId(), null, req, true));
        QmsCapa upd = new QmsCapa();
        upd.setId(capaId);
        upd.setOwnerUserId(req.getOwnerUserId());
        upd.setOwner(ownerName);
        qmsCapaMapper.updateById(upd);
    }
}
