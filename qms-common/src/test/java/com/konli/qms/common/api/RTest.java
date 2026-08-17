package com.konli.qms.common.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 统一响应 R&lt;T&gt; 单元测试（M15 common 公共组件）。
 * 验证 code/msg/data 约定：code=0 成功，非 0 失败。
 */
class RTest {

    @Test
    @DisplayName("ok(data) 返回 code=0、msg=success、data 透传")
    void ok_withData_setsSuccess() {
        R<String> r = R.ok("hello");
        assertThat(r.getCode()).isZero();
        assertThat(r.getMsg()).isEqualTo("success");
        assertThat(r.getData()).isEqualTo("hello");
    }

    @Test
    @DisplayName("ok() 无参返回 code=0 且 data 为 null")
    void ok_noArg_setsSuccessNullData() {
        R<Object> r = R.ok();
        assertThat(r.getCode()).isZero();
        assertThat(r.getMsg()).isEqualTo("success");
        assertThat(r.getData()).isNull();
    }

    @Test
    @DisplayName("fail(code,msg) 保留自定义错误码与消息")
    void fail_withCode_keepsCodeAndMsg() {
        R<Object> r = R.fail(400, "参数错误");
        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMsg()).isEqualTo("参数错误");
        assertThat(r.getData()).isNull();
    }

    @Test
    @DisplayName("fail(msg) 默认 code=500")
    void fail_onlyMsg_defaults500() {
        R<Object> r = R.fail("服务异常");
        assertThat(r.getCode()).isEqualTo(500);
        assertThat(r.getMsg()).isEqualTo("服务异常");
    }
}
