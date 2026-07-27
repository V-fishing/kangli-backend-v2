package com.konli.qms.domain.ncm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.konli.qms.domain.ncm.entity.NcmDefectRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface NcmDefectRecordMapper extends BaseMapper<NcmDefectRecord> {

    /** 按 defect_dict_code 分组统计各字典被不良记录引用的次数。 */
    @Select("SELECT defect_dict_code AS code, COUNT(*) AS cnt FROM ops.ncm_defect_record " +
            "WHERE defect_dict_code IS NOT NULL AND defect_dict_code <> '' " +
            "GROUP BY defect_dict_code")
    List<Map<String, Object>> countByDictCode();
}
