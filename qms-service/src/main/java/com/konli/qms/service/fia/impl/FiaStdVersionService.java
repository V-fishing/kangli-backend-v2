package com.konli.qms.service.fia.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.mapper.FiaInspStdMapper;
import com.konli.qms.domain.sqm.entity.SqmChangeOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物料变更批准后联动 FIA 检验标准:
 * 匹配 material = partNo 且 status='生效' 的标准,旧标准置为'停用',同时新建一条草稿版本
 * (std_version 递增 v2->v3,prev_version_id 指向旧 id),待质量审核后生效。
 *
 * 关键设计:
 * 1) 使用 REQUIRES_NEW 独立事务。否则本步若因 code 唯一约束等异常中断,
 *    PostgreSQL 会中止整个事务,污染外层的 approve 事务,导致三方会签在提交时返回
 *    "系统异常,请联系管理员"(事务中止后后续语句/提交均失败)。
 * 2) code 为全局 UNIQUE,新版本必须在原 code 基础上追加版本后缀,不能直接复用旧 code。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiaStdVersionService {

    private final FiaInspStdMapper fiaInspStdMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncVersion(SqmChangeOrder order) {
        if (order.getPartNo() == null || order.getPartNo().isBlank()) {
            return;
        }
        List<FiaInspStd> stds = fiaInspStdMapper.selectList(
                new LambdaQueryWrapper<FiaInspStd>()
                        .eq(FiaInspStd::getMaterial, order.getPartNo())
                        .eq(FiaInspStd::getStatus, "生效"));
        for (FiaInspStd old : stds) {
            // 旧标准停用(软更新)
            FiaInspStd upd = new FiaInspStd();
            upd.setId(old.getId());
            upd.setStatus("停用");
            fiaInspStdMapper.updateById(upd);

            // 计算业务版本号
            String oldVer = old.getStdVersion() == null ? "v0" : old.getStdVersion();
            int verNum = 0;
            String digits = oldVer.replaceAll("\\D", "");
            if (!digits.isEmpty()) {
                verNum = Integer.parseInt(digits);
            }
            // code 全局 UNIQUE,必须追加版本后缀,否则与旧标准 code 重复 -> 唯一约束异常
            FiaInspStd next = new FiaInspStd();
            next.setOrgId(order.getOrgId());
            next.setCode(old.getCode() + "-v" + (verNum + 1));
            next.setMaterial(old.getMaterial());
            next.setProcName(old.getProcName());
            next.setStdVersion("v" + (verNum + 1));
            next.setStatus("草稿");
            next.setPrevVersionId(old.getId());
            fiaInspStdMapper.insert(next);
        }
    }
}
