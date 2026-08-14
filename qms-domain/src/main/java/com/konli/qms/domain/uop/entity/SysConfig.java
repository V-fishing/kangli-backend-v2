package com.konli.qms.domain.uop.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.konli.qms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统开关/配置项(对应 ops.sys_config)。
 *
 * <p>键值型全局配置,当前用于「组织切换」相关可配置开关,如:
 * <ul>
 *   <li>{@code org.switch.affectsWrite}：切换分公司时,新建/修改数据是否归属所选组织(true/false)。</li>
 * </ul>
 * 取值统一为字符串,布尔类用 "true"/"false" 表示。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ops.sys_config")
public class SysConfig extends BaseEntity {

    @TableField("config_key")
    private String configKey;

    @TableField("config_value")
    private String configValue;

    private String remark;
}
