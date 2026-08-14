package com.konli.qms.service.spc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 检验项「类型」(value_type) → 推荐控制图集合 的集中映射。
 * 前端 StdItemEditor 联动默认值与后端 ensureFromFiaTask 带入主图均依赖此处,
 * 保证两端图类型字符串一致(避免散落硬编码)。
 *
 * 图类型统一为「基础图码」单一体系: Xbar/R/S/I/MR/P/NP/C/U。
 * 不再使用 Xbar-R/Xbar-S/I-MR 组合码(组合码是基础图码的打包, 会形成重复权限码)。
 * 映射规则(与用户确认):
 *  - 数值(numeric,有上下限): 默认 Xbar,R(均值+极差;子组>10 用 Xbar,S、单值用 I,MR 由用户手动加)
 *  - 枚举(enum,计数):         默认 P(不合格品率; NP/C/U 由用户手动加)
 *  - 文本(text,外观二值判定):  P(不良率趋势)
 */
public final class FiaChartTypeResolver {

    /** 全部可选基础控制图码(与前端 chartTypes 常量、字典 spc_chart_type 对齐)。 */
    public static final List<String> ALL_CHART_TYPES = List.of("Xbar", "R", "S", "I", "MR", "P", "NP", "C", "U");

    /** 旧组合码 → 基础图码等价展开(兼容历史标准线/检验项仍存组合码的情况)。 */
    private static final Map<String, List<String>> COMBO_TO_BASIC = Map.of(
            "Xbar-R", List.of("Xbar", "R"),
            "Xbar-S", List.of("Xbar", "S"),
            "I-MR", List.of("I", "MR"));

    private FiaChartTypeResolver() {
    }

    /** 按 value_type 给出默认推荐基础图码集合(用户可手动增减)。 */
    public static List<String> defaultChartTypes(String valueType) {
        if ("enum".equals(valueType) || "text".equals(valueType)) {
            return List.of("P");
        }
        return List.of("Xbar", "R");
    }

    /** 取推荐集合第一个为主图;空集合兜底 Xbar。 */
    public static String primaryChartType(List<String> types) {
        return (types != null && !types.isEmpty()) ? types.get(0) : "Xbar";
    }

    /** 旧组合码兼容: 若输入为组合码(如 Xbar-R)展开为基础图码集合; 否则原样返回基础图码列表。 */
    public static List<String> normalizeToBasic(List<String> types) {
        if (types == null || types.isEmpty()) return List.of();
        List<String> out = new ArrayList<>();
        for (String t : types) {
            List<String> basic = COMBO_TO_BASIC.get(t);
            if (basic != null) out.addAll(basic);
            else if (ALL_CHART_TYPES.contains(t)) out.add(t);
        }
        // 保序去重
        LinkedHashSet<String> dedup = new LinkedHashSet<>(out);
        return new ArrayList<>(dedup);
    }

    /** 逗号分隔字符串 → 列表(容错:去空白、跳过空段)。 */
    public static List<String> parse(String chartTypes) {
        if (chartTypes == null || chartTypes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(chartTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** 列表 → 逗号分隔字符串。 */
    public static String join(List<String> types) {
        return types == null ? "" : String.join(",", types);
    }
}
