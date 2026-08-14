package com.konli.qms.common.oss;

import com.konli.qms.common.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * 统一对象存储服务(后端所有文件/图片上传统一走 MinIO)。
 * 对象 key 形如 {prefix}/{uuid}-{原文件名},返回可直接用于下载/访问的 objectKey。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ObjectStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket:qms}")
    private String bucket;

    /** 上传一个 MultipartFile,返回 objectKey(不含桶名)。 */
    public String upload(String prefix, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException(400, "上传文件为空");
        String objectKey = buildKey(prefix, file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .stream(in, file.getSize(), -1)
                    .build());
            return objectKey;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("MinIO 上传失败 objectKey={}: {}", objectKey, e.getMessage(), e);
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }

    /** 上传原始字节(如程序生成的 PDF),返回 objectKey。 */
    public String uploadBytes(String prefix, String fileName, byte[] data, String contentType) {
        String objectKey = buildKey(prefix, fileName);
        try {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .stream(new java.io.ByteArrayInputStream(data), data.length, -1)
                    .build());
            return objectKey;
        } catch (Exception e) {
            log.error("MinIO 字节上传失败 objectKey={}: {}", objectKey, e.getMessage(), e);
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }

    /** 下载对象为字节数组。 */
    public byte[] download(String objectKey) {
        try (InputStream in = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket).object(objectKey).build())) {
            return in.readAllBytes();
        } catch (Exception e) {
            log.error("MinIO 下载失败 objectKey={}: {}", objectKey, e.getMessage(), e);
            throw new BusinessException(404, "文件不存在或读取失败");
        }
    }

    /** 删除对象。 */
    public void remove(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            log.warn("MinIO 删除失败 objectKey={}: {}", objectKey, e.getMessage());
        }
    }

    private String buildKey(String prefix, String original) {
        String safe = (original == null || original.isBlank()) ? "file" : original.replaceAll("[\\\\/]", "_");
        String seg = (prefix == null || prefix.isBlank()) ? "" : (prefix.endsWith("/") ? prefix : prefix + "/");
        return seg + UUID.randomUUID() + "-" + safe;
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        } catch (Exception e) {
            throw new BusinessException(500, "MinIO 桶不可用: " + e.getMessage());
        }
    }
}
