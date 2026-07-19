package com.konli.qms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * 康立 QMS 后端启动类。
 *
 * <p>按代码规范文档 V1.0 §2.1:启动类位于 qms-bootstrap 模块,根包 {@code com.konli.qms}。
 * 组件扫描覆盖 com.konli.qms.**(common/domain/service/api 各层子包均在其下)。
 * Mapper 扫描 qms-domain 各模块的 mapper 包。</p>
 *
 * <p>排除 {@link UserDetailsServiceAutoConfiguration}:避免 Spring Security 生成默认 in-memory 用户
 * (我们用自研 JWT,无 UserDetailsService)。</p>
 */
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@MapperScan("com.konli.qms.domain.**.mapper")
public class QmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(QmsApplication.class, args);
    }
}
