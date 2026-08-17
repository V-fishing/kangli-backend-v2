package com.konli.qms.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 业务异常 BusinessException 单元测试（M15 common 公共组件）。
 * 验证错误码默认值与构造重载。
 */
class BusinessExceptionTest {

    @Test
    @DisplayName("单参构造默认 code=500，msg 透传")
    void singleArg_defaultsCode500() {
        BusinessException ex = new BusinessException("数据库不可用");
        assertThat(ex.getCode()).isEqualTo(500);
        assertThat(ex.getMessage()).isEqualTo("数据库不可用");
    }

    @Test
    @DisplayName("双参构造保留自定义 code 与 msg")
    void twoArg_keepsCodeAndMsg() {
        BusinessException ex = new BusinessException(409, "版本冲突");
        assertThat(ex.getCode()).isEqualTo(409);
        assertThat(ex.getMessage()).isEqualTo("版本冲突");
    }

    @Test
    @DisplayName("是 RuntimeException 子类，可被统一异常处理器捕获")
    void isRuntimeException() {
        BusinessException ex = new BusinessException("x");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
