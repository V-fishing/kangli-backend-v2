package com.konli.qms.common.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置。属性来源:application*.yml 的 minio.* 段
 * (dev: http://localhost:9000 / minioadmin / qms)。
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {

    /** S3 端点,如 http://localhost:9000 */
    private String endpoint = "http://localhost:9000";
    /** access key */
    private String accessKey = "minioadmin";
    /** secret key */
    private String secretKey = "minioadmin";
    /** 默认桶名 */
    private String bucket = "qms";

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
