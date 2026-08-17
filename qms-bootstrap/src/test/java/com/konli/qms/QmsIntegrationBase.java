package com.konli.qms;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * 集成测试基座（T2 阶段）。
 *
 * <p>本机已运行 dev 容器 qms-postgres(5432)/qms-redis(6379)/qms-minio(9000)，但 Docker Desktop
 * 守护进程（Testcontainers 所需的 npipe 控制面）未启动，故此处直连本地容器，使用<b>独立测试库
 * qms_test</b>（不污染 dev 的 qms 业务库）。Flyway 在 qms_test 上全量迁移，保留
 * validate-on-migrate=false 规避 V90 checksum 历史问题。</p>
 *
 * <p>若该环境具备 Docker 守护进程，可改回 Testcontainers 方案（见 git 历史）：用
 * PostgreSQLContainer/GenericContainer 拉起独立容器并通过 @DynamicPropertySource 注入。</p>
 *
 * <p>本类所在模块 qms-bootstrap 含唯一主类 QmsApplication，故 @SpringBootTest 可正常启动。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class QmsIntegrationBase {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // 独立测试库，避免污染 dev 的 qms 库
        r.add("spring.datasource.url",
                () -> "jdbc:postgresql://localhost:5432/qms_test?stringtype=unspecified&characterEncoding=UTF-8");
        r.add("spring.datasource.username", () -> "qms");
        r.add("spring.datasource.password", () -> "qms");
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.flyway.validate-on-migrate", () -> "false");
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.data.redis.host", () -> "localhost");
        r.add("spring.data.redis.port", () -> 6379);
        r.add("minio.endpoint", () -> "http://localhost:9000");
        r.add("minio.access-key", () -> "minioadmin");
        r.add("minio.secret-key", () -> "minioadmin");
        r.add("minio.bucket", () -> "qms");
        r.add("logging.level.com.konli.qms", () -> "warn");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }
}
