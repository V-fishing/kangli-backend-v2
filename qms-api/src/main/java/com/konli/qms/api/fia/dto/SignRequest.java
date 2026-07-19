package com.konli.qms.api.fia.dto;

import lombok.Data;

/** 签名请求(sign_methods 含 password 时必填密码,经 PasswordEncoder 校验) */
@Data
public class SignRequest {

    private String password;

    /**
     * 逐项签名时传入的检验项 ID(可选)。
     * <p>sign_granularity=逐项签名 时,前端按项调用并传 itemId:
     * 仅记录"检验人签名-项X"日志,不改 task 状态;
     * 不传(整单签名 / 逐项签完后的整单确认)则走原逻辑(设签名人与状态流转)。</p>
     */
    private String itemId;
}
