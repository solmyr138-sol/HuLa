package com.luohuo.flex.flex.storage;

import com.luohuo.flex.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

/**
 * 未配置 OSS 时，将文件保存到服务端本地目录。
 */
@Service
@RequiredArgsConstructor
public class LocalFileStorageService {

    private final SysConfigService sysConfigService;

    public String upload(MultipartFile file, String bizType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String safeBiz = sanitizeSegment(bizType == null || bizType.isBlank() ? "default" : bizType);
        String ext = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path root = resolveRootDir();
        Path dir = root.resolve(safeBiz);
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName);
        file.transferTo(target);

        return safeBiz + "/" + fileName;
    }

    public Resource load(String relativePath) throws IOException {
        String safe = sanitizeRelativePath(relativePath);
        Path file = resolveRootDir().resolve(safe).normalize();
        Path root = resolveRootDir().normalize();
        if (!file.startsWith(root)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        if (!Files.isRegularFile(file)) {
            throw new IOException("文件不存在");
        }
        return new FileSystemResource(file);
    }

    private Path resolveRootDir() {
        String configured = sysConfigService.get("localUploadDir");
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured.trim());
        }
        return Paths.get(System.getProperty("user.home", "."), "hula-uploads");
    }

    private static String sanitizeSegment(String segment) {
        String s = segment.replace("\\", "/").trim();
        if (s.contains("..") || s.contains("/")) {
            throw new IllegalArgumentException("非法业务类型");
        }
        return s;
    }

    private static String sanitizeRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("非法文件路径");
        }
        String normalized = relativePath.replace("\\", "/").trim();
        if (normalized.startsWith("/") || normalized.contains("..")) {
            throw new IllegalArgumentException("非法文件路径");
        }
        return normalized;
    }

    private static String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".bin";
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (ext.length() > 10 || ext.contains("/") || ext.contains("\\")) {
            return ".bin";
        }
        return ext;
    }
}
