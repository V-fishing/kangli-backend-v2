package com.konli.qms.service.fia;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AQL 抽样方案(GB/T 2828.1 简化版,正常检验 II 级)
 * 单次抽样:输入批量 + AQL值 → 输出 样本量/接收数(Ac)/拒收数(Re)
 */
public final class AqlSamplingUtil {

    private AqlSamplingUtil() {}

    /** 批量分档 → 样本量字码(正常II级) */
    private static final int[][] LOT_SAMPLE = {
        {2, 8, 2}, {9, 15, 3}, {16, 25, 5}, {26, 50, 8},
        {51, 90, 13}, {91, 150, 20}, {151, 280, 32}, {281, 500, 50},
        {501, 1200, 80}, {1201, 3200, 125}, {3201, 10000, 200},
        {10001, 35000, 315}, {35001, 150000, 500}, {150001, 500000, 800},
        {500001, Integer.MAX_VALUE, 1250},
    };

    /** AQL 值 → Ac/Re (单次抽样) */
    private static final Map<BigDecimal, int[]> AQL_AC_RE = new LinkedHashMap<>();
    static {
        AQL_AC_RE.put(new BigDecimal("0.65"), new int[]{0, 1});
        AQL_AC_RE.put(new BigDecimal("1.0"), new int[]{0, 1});
        AQL_AC_RE.put(new BigDecimal("1.5"), new int[]{0, 1});
        AQL_AC_RE.put(new BigDecimal("2.5"), new int[]{1, 2});
        AQL_AC_RE.put(new BigDecimal("4.0"), new int[]{2, 3});
        AQL_AC_RE.put(new BigDecimal("6.5"), new int[]{3, 4});
    }

    public static class SamplePlan {
        public int sampleSize;  // 样本量
        public int ac;          // 接收数
        public int re;          // 拒收数
    }

    /** 计算抽样方案: 批量 + AQL → (样本量, Ac, Re) */
    public static SamplePlan calc(int lotSize, BigDecimal aql) {
        SamplePlan sp = new SamplePlan();
        // 找样本量
        for (int[] row : LOT_SAMPLE) {
            if (lotSize >= row[0] && lotSize <= row[1]) {
                sp.sampleSize = row[2];
                break;
            }
        }
        if (sp.sampleSize == 0) sp.sampleSize = Math.min(lotSize, 5);
        // 找 Ac/Re
        BigDecimal bestAql = BigDecimal.ZERO;
        for (Map.Entry<BigDecimal, int[]> e : AQL_AC_RE.entrySet()) {
            if (aql.compareTo(e.getKey()) >= 0 && e.getKey().compareTo(bestAql) > 0) {
                bestAql = e.getKey();
                sp.ac = e.getValue()[0];
                sp.re = e.getValue()[1];
            }
        }
        if (bestAql.equals(BigDecimal.ZERO)) { sp.ac = 0; sp.re = 1; }
        return sp;
    }
}
