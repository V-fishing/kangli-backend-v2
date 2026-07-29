package com.konli.qms.domain.fia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.konli.qms.domain.fia.entity.FiaTriggerType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface FiaTriggerTypeMapper extends BaseMapper<FiaTriggerType> {

    /**
     * 按 trigger_type 分组统计 ops.fia_task 聚合数据。
     * 单表查询,DataScopeInterceptor 会自动追加 org_id 过滤(对齐 list() 的组织隔离)。
     * 仅返回有任务的触发类型;无任务的类型不出现,由服务层补 0。
     */
    @Select({
        "SELECT trigger_type AS \"triggerType\",",
        "       COUNT(*) AS \"taskCount\",",
        "       COALESCE(SUM(CASE WHEN overall_judge = '合格' THEN 1 ELSE 0 END), 0) AS \"qualifiedCount\",",
        "       COALESCE(SUM(CASE WHEN overall_judge = '不合格' THEN 1 ELSE 0 END), 0) AS \"unqualifiedCount\",",
        "       COALESCE(SUM(CASE WHEN status NOT IN ('已完成','已作废') THEN 1 ELSE 0 END), 0) AS \"pendingCount\",",
        "       COALESCE(SUM(CASE WHEN is_overdue = true THEN 1 ELSE 0 END), 0) AS \"overdueCount\"",
        "FROM ops.fia_task",
        "WHERE is_deleted = false",
        "GROUP BY trigger_type"
    })
    List<Map<String, Object>> countByType();
}
