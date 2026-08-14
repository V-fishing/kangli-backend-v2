package com.konli.qms.domain.sqm.mapper;

import com.konli.qms.domain.sqm.entity.SqmSupplierPerformance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PerfAnalysisMapper {

    /** 按周期对供应商综合分排名(分页),带名次。列别名用驼峰以匹配前端 PerfRankRow。 */
    @Select("<script>" +
        "SELECT s.id AS \"supplierId\", s.name AS \"supplierName\", s.category AS category, " +
        "       p.score AS score, p.level AS level, " +
        "       ROW_NUMBER() OVER (ORDER BY p.score DESC) AS rank " +
        "FROM ops.sqm_supplier_performance p " +
        "JOIN ops.sqm_supplier s ON s.id = p.supplier_id AND s.is_deleted = false " +
        "WHERE p.period = #{period} " +
        "<if test=\"category != null and category != ''\">AND s.category = #{category}</if> " +
        "ORDER BY p.score DESC " +
        "LIMIT #{size} OFFSET #{offset}" +
        "</script>")
    List<Map<String, Object>> rank(@Param("period") String period, @Param("category") String category,
                                   @Param("size") int size, @Param("offset") int offset);

    /** 按周期+品类统计排名总数(分页用)。 */
    @Select("<script>" +
        "SELECT COUNT(*) FROM ops.sqm_supplier_performance p " +
        "JOIN ops.sqm_supplier s ON s.id = p.supplier_id AND s.is_deleted = false " +
        "WHERE p.period = #{period} " +
        "<if test=\"category != null and category != ''\">AND s.category = #{category}</if> " +
        "</script>")
    long rankCount(@Param("period") String period, @Param("category") String category);

    /** 多供应商近 N 周期绩效序列(按供应商、周期排序)。 */
    @Select("<script>" +
        "SELECT p.* FROM ops.sqm_supplier_performance p " +
        "WHERE p.supplier_id IN " +
        "<foreach collection='supplierIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
        " AND p.period &gt;= #{periodStart} AND p.period &lt;= #{periodEnd} " +
        "ORDER BY p.supplier_id, p.period" +
        "</script>")
    List<SqmSupplierPerformance> trend(@Param("supplierIds") List<String> supplierIds,
                                       @Param("periodStart") String periodStart,
                                       @Param("periodEnd") String periodEnd);

    /** 按缺陷字典码聚合缺陷数量 TOP N(柏拉图,用 defect_count 加权)。 */
    @Select("""
        SELECT d.defect_dict_code AS "defectCode", SUM(d.defect_count) AS cnt
        FROM ops.ncm_defect_record d
        WHERE d.is_deleted = false
          AND d.occurred_at >= CAST(#{periodStart} || '-01' AS DATE)
          AND d.occurred_at <  (CAST(#{periodEnd} || '-01' AS DATE) + INTERVAL '1 month')
        GROUP BY d.defect_dict_code
        ORDER BY cnt DESC
        LIMIT #{topN}
        """)
    List<Map<String, Object>> pareto(@Param("periodStart") String periodStart,
                                     @Param("periodEnd") String periodEnd,
                                     @Param("topN") int topN);
}
