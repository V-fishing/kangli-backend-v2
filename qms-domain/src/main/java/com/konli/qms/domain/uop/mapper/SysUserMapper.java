package com.konli.qms.domain.uop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.konli.qms.domain.uop.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper(代码规范§2.1,由 qms-bootstrap @MapperScan 扫描)。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
