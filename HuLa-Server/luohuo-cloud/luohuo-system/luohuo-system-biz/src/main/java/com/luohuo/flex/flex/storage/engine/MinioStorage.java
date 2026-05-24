package com.luohuo.flex.flex.storage.engine;

import com.alibaba.fastjson.JSONObject;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class MinioStorage {

    private final Map<String, String> config;

    public MinioStorage(Map<String, String> config) {
        this.config = config;
    }

    /** 服务端 putObject，供移动端/桌面经网关上传（不经预签名 PUT） */
    public JSONObject uploadObject(MultipartFile file, String bizType) throws Exception {
        String endpoint = this.config.getOrDefault("minioEndpoint", "");
        String accessKey = this.config.getOrDefault("minioAccessKey", "");
        String secretKey = this.config.getOrDefault("minioSecretKey", "");
        String bucket = this.config.getOrDefault("minioBucket", "");
        String urlPrefix = this.config.getOrDefault("minioUrlPrefix", "");

        String safeBiz = bizType == null || bizType.isBlank() ? "default" : bizType.trim();
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = extractExtension(original);
        String objectKey = safeBiz + "/" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = guessContentType(original);
        }

        byte[] body = file.getBytes();
        MinioClient client = buildClient(endpoint, accessKey, secretKey);
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .stream(new ByteArrayInputStream(body), body.length, -1)
                        .contentType(contentType)
                        .build());

        String downloadUrl = buildDownloadUrl(urlPrefix, endpoint, bucket, objectKey);

        JSONObject json = new JSONObject();
        json.put("url", downloadUrl);
        json.put("downloadUrl", downloadUrl);
        json.put("objectKey", objectKey);
        return json;
    }

    public JSONObject presignPut(String objectKey) {
        String endpoint = this.config.getOrDefault("minioEndpoint", "");
        String accessKey = this.config.getOrDefault("minioAccessKey", "");
        String secretKey = this.config.getOrDefault("minioSecretKey", "");
        String bucket = this.config.getOrDefault("minioBucket", "");
        String urlPrefix = this.config.getOrDefault("minioUrlPrefix", "");

        try {
            String clientEndpoint = resolveClientEndpoint(endpoint, urlPrefix);
            MinioClient client = buildClient(clientEndpoint, accessKey, secretKey);

            String uploadUrl = client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(60 * 60)
                            .build());

            String downloadUrl = buildDownloadUrl(urlPrefix, endpoint, bucket, objectKey);

            JSONObject json = new JSONObject();
            json.put("uploadUrl", uploadUrl);
            json.put("downloadUrl", downloadUrl);
            json.put("objectKey", objectKey);
            return json;
        } catch (Exception e) {
            log.error("[Minio presignPut] 生成预签名失败, objectKey:{}", objectKey, e);
            throw new RuntimeException("MinIO 预签名失败: " + e.getMessage(), e);
        }
    }

    /** 预签名 URL 的 Host 必须与客户端可访问地址一致，优先从 minioUrlPrefix 解析。 */
    private static String resolveClientEndpoint(String endpoint, String urlPrefix) {
        if (urlPrefix != null && !urlPrefix.isBlank()) {
            try {
                String normalized = urlPrefix.trim();
                if (normalized.endsWith("/")) {
                    normalized = normalized.substring(0, normalized.length() - 1);
                }
                URI uri = URI.create(normalized);
                if (uri.getHost() != null) {
                    boolean secure = "https".equalsIgnoreCase(uri.getScheme());
                    int port = uri.getPort();
                    if (port <= 0) {
                        port = secure ? 443 : 9000;
                    }
                    return uri.getScheme() + "://" + uri.getHost() + ":" + port;
                }
            } catch (Exception ignored) {
            }
        }
        return endpoint;
    }

    private static String buildDownloadUrl(String urlPrefix, String endpoint, String bucket, String objectKey) {
        if (urlPrefix != null && !urlPrefix.isBlank()) {
            return urlPrefix.endsWith("/") ? (urlPrefix + objectKey) : (urlPrefix + "/" + objectKey);
        }
        String base = endpoint.endsWith("/") ? endpoint : (endpoint + "/");
        return base + bucket + "/" + objectKey;
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

    private static String guessContentType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }

    private static MinioClient buildClient(String endpoint, String accessKey, String secretKey) {
        String normalized = endpoint == null ? "" : endpoint.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return MinioClient.builder()
                .endpoint(normalized)
                .credentials(trim(accessKey), trim(secretKey))
                .build();
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
