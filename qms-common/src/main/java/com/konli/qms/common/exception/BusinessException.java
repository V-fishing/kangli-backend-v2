package com.konli.qms.common.exception;

import lombok.Getter;

/**
 * 业务异常(代码规范文档§3.7)。携带业务错误码,由 {@link GlobalExceptionHandler} 统一转 R&lt;T&gt;。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String msg) {
        super(msg);
        this.code = 500;
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
