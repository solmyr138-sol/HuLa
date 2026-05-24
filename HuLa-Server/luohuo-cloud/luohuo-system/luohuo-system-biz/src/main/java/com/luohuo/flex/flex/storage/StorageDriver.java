package com.luohuo.flex.flex.storage;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.luohuo.flex.entity.Config;
import com.luohuo.flex.flex.storage.engine.QiNiuStorage;
import com.luohuo.flex.flex.storage.engine.MinioStorage;
import com.luohuo.flex.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.luohuo.flex.service.SysConfigService;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StorageDriver {

    private final SysConfigService sysConfigService;
    private final SysConfigMapper configMapper;

    /** MinIO 密钥以数据库为准，避免 Redis 缓存旧 SecretKey 导致签名失败 */
    private String storageConfig(String key) {
        Config row = configMapper.selectOne(
                Wrappers.<Config>lambdaQuery().eq(Config::getConfigKey, key).last("LIMIT 1"));
        if (row != null && StrUtil.isNotBlank(row.getConfigValue())) {
            return row.getConfigValue().trim();
        }
        String cached = sysConfigService.get(key);
        return cached == null ? "" : cached.trim();
    }

    public String currentEngine() {
        String e = storageConfig("storageDefault");
        return e.isBlank() ? "qiniu" : e;
    }

    public boolean isMinioEngine() {
        return "minio".equals(currentEngine());
    }

    /** 服务端直传 MinIO（避免客户端预签名 PUT 签名不一致） */
    public JSONObject uploadViaMinio(org.springframework.web.multipart.MultipartFile file, String bizType) throws Exception {
        if (!isStorageReady() || !isMinioEngine()) {
            throw new IllegalStateException("MinIO 未配置");
        }
        return new MinioStorage(getConfig()).uploadObject(file, bizType);
    }

    public Map<String, String> getConfig() {
        Map<String, String> cfg = new HashMap<>();
        cfg.put("storagePrefix", sysConfigService.get("qnStorageName"));
        cfg.put("qnUploadUrl", sysConfigService.get("qnUploadUrl"));
        cfg.put("qnAccessKey", sysConfigService.get("qnAccessKey"));
        cfg.put("qnSecretKey", sysConfigService.get("qnSecretKey"));
        cfg.put("qnStorageName", sysConfigService.get("qnStorageName"));
        cfg.put("minioEndpoint", storageConfig("minioEndpoint"));
        cfg.put("minioAccessKey", storageConfig("minioAccessKey"));
        cfg.put("minioSecretKey", storageConfig("minioSecretKey"));
        cfg.put("minioBucket", storageConfig("minioBucket"));
        cfg.put("minioUrlPrefix", storageConfig("minioUrlPrefix"));
        return cfg;
    }

    /**
     * 对象存储（七牛 / MinIO）是否已配置可用密钥。
     * 未配置时不应走 OSS 直传，应改由服务端本地落盘。
     */
    public boolean isStorageReady() {
        String engine = currentEngine();
        Map<String, String> cfg = getConfig();
        return switch (engine) {
            case "minio" -> isAllNotBlank(
                    cfg.get("minioEndpoint"),
                    cfg.get("minioAccessKey"),
                    cfg.get("minioSecretKey"),
                    cfg.get("minioBucket"));
            case "qiniu" -> isAllNotBlank(
                    cfg.get("qnAccessKey"),
                    cfg.get("qnSecretKey"),
                    cfg.get("qnStorageName"));
            default -> false;
        };
    }

    private static boolean isAllNotBlank(String... values) {
        if (values == null) {
            return false;
        }
        for (String v : values) {
            if (v == null || v.isBlank()) {
                return false;
            }
        }
        return true;
    }

    public JSONObject getToken(String scene, String fileName) {
        if (!isStorageReady()) {
            throw new IllegalStateException("对象存储未配置，请使用服务端文件上传");
        }
        String engine = currentEngine();
        Map<String, String> cfg = getConfig();
        switch (engine) {
            case "qiniu" -> {
                return new QiNiuStorage(cfg).upToken();
            }
            case "minio" -> {
                String safeScene = scene == null || scene.isBlank() ? "chat" : scene;
                String safeName = fileName == null || fileName.isBlank() ? "file" : fileName;
                String objectKey = safeScene + "/" + System.currentTimeMillis() + "_" + safeName;
                return new MinioStorage(cfg).presignPut(objectKey);
            }
            default -> throw new IllegalArgumentException("不支持的存储引擎: " + engine);
        }
    }
}
