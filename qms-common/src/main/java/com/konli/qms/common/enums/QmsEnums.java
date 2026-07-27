package com.konli.qms.common.enums;

/**
 * QMS 全局枚举常量 — 替代散落在各 Service 中的硬编码中文字符串。
 * 所有状态/结果/类型取值以此为准，不新增零散的字面量比较。
 */
public final class QmsEnums {

    private QmsEnums() {}

    /** 检验结果 */
    public static final class InspResult {
        public static final String PASS = "合格";
        public static final String FAIL = "不合格";
    }

    /** 审核状态 */
    public static final class ReviewStatus {
        public static final String PENDING  = "待审核";
        public static final String APPROVED = "已审核";
        public static final String REJECTED = "驳回";
    }

    /** 首件任务状态 */
    public static final class FiaTaskStatus {
        public static final String PENDING        = "待检";
        public static final String IN_PROGRESS    = "进行中";
        public static final String WAIT_REVIEW    = "待复核";
        public static final String IN_APPROVAL    = "审批中";
        public static final String COMPLETED      = "已完成";
        public static final String OVERDUE        = "超时";
        public static final String CANCELLED      = "已作废";
        public static final String REJECTED       = "已驳回";
    }

    /** 首件来源 */
    public static final class FiaSource {
        public static final String FACTORY  = "FACTORY";
        public static final String SUPPLIER = "SUPPLIER";
    }

    /** 处置路径 — 产线首件 */
    public static final class FactoryDisposition {
        public static final String RELEASE       = "合格放行";
        public static final String RETURN        = "退货";
        public static final String REWORK        = "返工";
        public static final String CONCESSION    = "让步接收";
        public static final String EMERGENCY     = "紧急放行";
        public static final String EXEMPTION     = "豁免开工";
    }

    /** 处置路径 — 供应商来料首件 */
    public static final class SupplierDisposition {
        public static final String ACCEPT     = "合格入库";
        public static final String RETURN     = "退货";
        public static final String CONCESSION = "让步接收";
        public static final String SORT       = "挑选";
    }

    /** 8D 状态 */
    public static final class EightDStatus {
        public static final String IN_PROGRESS = "进行中";
        public static final String CLOSED      = "已闭环";
    }

    /** 8D 流程类型 */
    public static final class EightDFlow {
        public static final String SIMPLE = "简易";
        public static final String FULL   = "8D";
    }

    /** CAPA 状态 */
    public static final class CapaStatus {
        public static final String PENDING_START  = "待启动";
        public static final String ANALYZING      = "分析中";
        public static final String WAIT_APPROVAL  = "待审批";
        public static final String IMPLEMENTING   = "实施中";
        public static final String VERIFIED       = "已验证";
        public static final String CLOSED         = "已关闭";
    }

    /** 异常单状态 */
    public static final class AbnormalStatus {
        public static final String PENDING    = "待整改";
        public static final String RECTIFYING = "整改中";
        public static final String VERIFYING  = "待验证";
        public static final String CLOSED     = "已闭环";
        public static final String CLOSED_ALT = "已关闭";
    }

    /** SPC 告警级别 */
    public static final class SpcAlarmLevel {
        public static final String WARNING = "预警";
        public static final String ALARM   = "报警";
    }

    /** SPC 通知渠道 */
    public static final class NotifyChannel {
        public static final String POPUP       = "站内弹窗";
        public static final String WECOM       = "企业微信";
        public static final String DINGTALK    = "钉钉";
        public static final String CUSTOM_WEBHOOK = "自定义Webhook";
    }

    /** 组织类型 */
    public static final class OrgType {
        public static final String COMPANY  = "公司";
        public static final String FACTORY  = "工厂";
        public static final String WORKSHOP = "车间";
        public static final String LINE     = "产线";
        public static final String STATION  = "工位";
    }

    /** 通用启用/停用 */
    public static final String ENABLED  = "启用";
    public static final String DISABLED = "停用";

    /** 通用是否 */
    public static final String YES = "是";
    public static final String NO  = "否";
}
