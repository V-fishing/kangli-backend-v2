package com.konli.qms.service.support;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * openhtmltopdf 中文字体注册工具。openhtmltopdf 不会自动使用系统字体渲染中文,
 * 必须显式注册一个含 CJK 字形(TrueType)的字体,否则中文会显示为方块/乱码。
 * 优先使用 Windows 自带的 SimHei(simhei.ttf,纯 TTF,覆盖常用中文),
 * 并以家族名 "SimHei" 注册,供 CSS font-family:'SimHei' 引用。
 */
@Slf4j
public final class CjkFontUtil {

    private static final List<String> CANDIDATES = Arrays.asList(
            "C:\\Windows\\Fonts\\simhei.ttf",
            "C:\\Windows\\Fonts\\simsunb.ttf",
            "C:\\Windows\\Fonts\\simsun.ttc",
            "C:\\Windows\\Fonts\\msyh.ttc",
            "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
    );

    private CjkFontUtil() {
    }

    /** 注册首个可用的中文字体(家族名 SimHei),供 CSS font-family 引用。 */
    public static void register(PdfRendererBuilder builder) {
        for (String path : CANDIDATES) {
            try {
                File f = new File(path);
                if (!f.exists()) {
                    continue;
                }
                builder.useFont(f, "SimHei");
                log.info("SQM PDF 注册中文字体: {}", path);
                return;
            } catch (Exception e) {
                log.debug("SQM PDF 字体加载失败 {}: {}", path, e.getMessage());
            }
        }
        log.warn("SQM PDF 未找到可用中文字体,中文可能显示为方块");
    }
}
