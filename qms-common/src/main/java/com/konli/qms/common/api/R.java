package com.konli.qms.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应包装(代码规范文档§3.6)。
 *
 * <p>{@code {code, msg, data}},code=0 成功,非 0 为错误码。
 * 前端 {@code request.ts} 已对齐读取 {@code msg}。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> implements Serializable {

    private int code;       // 0=成功,非 0=错误码
    private String msg;
    private T data;

    public static <T> R<T> ok(T data) {
        return new R<>(0, "success", data);
    }

    public static <T> R<T> ok() {
        return new R<>(0, "success", null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }
}
