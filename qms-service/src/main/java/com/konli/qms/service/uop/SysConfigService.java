package com.konli.qms.service.uop;

/**
 * 系统开关/配置项服务(key-value 型全局配置,如 org.switch.affectsWrite)。
 */
public interface SysConfigService {

    /** 读取值,缺省返回 null。 */
    String getValue(String key);

    /** 读取值,缺省返回 defaultVal。 */
    String getValue(String key, String defaultVal);

    /** 读取布尔值("1"/"true"/"Y"/"yes" 视为 true),缺省返回 defaultVal。 */
    boolean getBool(String key, boolean defaultVal);

    /** 写入值(不存在则插入,存在则更新)。 */
    void set(String key, String value);

    /** 写入值并附带说明。 */
    void set(String key, String value, String remark);
}
