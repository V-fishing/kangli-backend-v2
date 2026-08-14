package com.konli.qms.api.common.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.common.exception.BusinessException;
import com.konli.qms.common.oss.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用文件上传/下载(统一写入 MinIO,objectKey 形如 files/{uuid}-{原文件名})。
 * 返回 path=objectKey,供业务表存储;下载按 objectKey 从 MinIO 取回。
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final ObjectStorageService objectStorageService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }
        String objectKey = objectStorageService.upload("files", file);
        Map<String, String> res = new HashMap<>();
        res.put("path", objectKey);
        res.put("fileName", file.getOriginalFilename() != null ? file.getOriginalFilename() : objectKey);
        return R.ok(res);
    }

    @GetMapping("/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> download(@RequestParam String path) {
        try {
            byte[] data = objectStorageService.download(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String name = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
            headers.setContentDispositionFormData("attachment", name);
            headers.setContentLength(data.length);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "文件读取失败: " + e.getMessage());
        }
    }
}
