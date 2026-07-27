package com.konli.qms.api.common.controller;

import com.konli.qms.common.api.R;
import com.konli.qms.common.exception.BusinessException;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 通用文件上传/下载(存于 logs/files,按相对文件名访问,防路径穿越)。 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private static final String BASE = System.getProperty("user.dir")
            + File.separator + "logs" + File.separator + "files";

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String stored = UUID.randomUUID().toString().replace("-", "") + ext;
        File dir = new File(BASE);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException(500, "无法创建文件目录");
        }
        file.transferTo(new File(dir, stored));
        Map<String, String> res = new HashMap<>();
        res.put("path", stored);
        res.put("fileName", original != null ? original : stored);
        return R.ok(res);
    }

    @GetMapping("/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> download(@RequestParam String path) {
        try {
            String safeName = Paths.get(path).getFileName().toString();
            Path base = Paths.get(BASE).toAbsolutePath().normalize();
            Path full = base.resolve(safeName).normalize();
            if (!full.startsWith(base) || !Files.exists(full)) {
                throw new BusinessException(404, "文件不存在");
            }
            byte[] data = Files.readAllBytes(full);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", safeName);
            headers.setContentLength(data.length);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "文件读取失败: " + e.getMessage());
        }
    }
}
