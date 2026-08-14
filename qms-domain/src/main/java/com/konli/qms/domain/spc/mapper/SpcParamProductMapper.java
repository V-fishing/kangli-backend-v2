package com.konli.qms.domain.spc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.konli.qms.domain.spc.entity.SpcParamProduct;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SpcParamProductMapper extends BaseMapper<SpcParamProduct> {

    /** 批量查某批参数绑定的产品关联(仅未删除)。 */
    @Select("<script>SELECT * FROM ops.spc_param_product " +
            "WHERE param_id IN " +
            "<foreach collection='paramIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND is_deleted = false</script>")
    List<SpcParamProduct> selectByParamIds(@Param("paramIds") List<String> paramIds);

    /** 物理删除某参数下的全部产品关联(upsert 前清理)。 */
    @Delete("DELETE FROM ops.spc_param_product WHERE param_id = #{paramId}")
    int deleteByParamId(@Param("paramId") String paramId);
}
