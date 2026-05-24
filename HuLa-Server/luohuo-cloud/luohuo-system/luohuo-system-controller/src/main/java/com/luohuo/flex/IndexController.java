package com.luohuo.flex;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.luohuo.basic.base.R;
import com.luohuo.flex.entity.Config;
import com.luohuo.flex.entity.Init;
import com.luohuo.flex.mapper.ConfigMapper;
import com.luohuo.flex.service.SysConfigService;
import com.luohuo.flex.flex.storage.LocalFileStorageService;
import com.luohuo.flex.flex.storage.StorageDriver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * system模块公开 服务
 *
 * @author 乾乾
 * @date 2025年07月11日00:50:36
 */
@Slf4j
@RestController
@RequestMapping("/anyTenant")
@AllArgsConstructor
@Tag(name = "系统服务")
public class IndexController {

	private final SysConfigService sysConfigService;
	private final StorageDriver storageDriver;
	private final LocalFileStorageService localFileStorageService;
	private final ConfigMapper configMapper;

	@GetMapping("/config/init")
	@Operation(summary = "获取系统全局配置")
	public R<Init> init() {
		return R.success(sysConfigService.getSystemInit());
	}

    @Operation(summary = "获取统一直传凭证（根据引擎返回 七牛token 或 MinIO预签名）")
    @GetMapping("/ossToken")
    public R<JSONObject> token(@RequestParam(required = false, defaultValue = "chat") String scene, @RequestParam(required = false) String fileName) {
        return R.success(storageDriver.getToken(scene, fileName));
    }

	@GetMapping("/storage/provider")
	@Operation(summary = "获取默认存储提供者")
	public R<JSONObject> storageProvider() {
		var provider = sysConfigService.get("storageDefault");
		var json = new JSONObject();
		json.put("provider", provider == null || provider.isBlank() ? "qiniu" : provider);
		json.put("ready", storageDriver.isStorageReady());
		return R.success(json);
	}

	@PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "服务端文件上传（MinIO 直传或本地落盘）")
	public R<JSONObject> uploadLocalFile(
			@RequestPart("file") MultipartFile file,
			@RequestParam(value = "bizType", defaultValue = "avatar") String bizType) throws Exception {
		if (storageDriver.isStorageReady() && storageDriver.isMinioEngine()) {
			return R.success(storageDriver.uploadViaMinio(file, bizType));
		}
		if (storageDriver.isStorageReady()) {
			return R.fail("七牛云请使用客户端直传");
		}
		String relative = localFileStorageService.upload(file, bizType);
		var json = new JSONObject();
		json.put("path", relative);
		json.put("url", "system/anyTenant/file/access/" + relative);
		return R.success(json);
	}

	@GetMapping("/file/access/**")
	@Operation(summary = "访问服务端本地已上传文件")
	public ResponseEntity<Resource> accessLocalFile(HttpServletRequest request) throws Exception {
		String uri = request.getRequestURI();
		String marker = "/file/access/";
		int idx = uri.indexOf(marker);
		if (idx < 0) {
			return ResponseEntity.notFound().build();
		}
		String relative = uri.substring(idx + marker.length());
		Resource resource = localFileStorageService.load(relative);
		return ResponseEntity.ok()
				.header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}

	@GetMapping("/config/list")
	@Operation(summary = "获取配置列表")
	public R<List<Config>> configList(
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String configName,
			@RequestParam(required = false) String configKey) {
		var wrapper = Wrappers.<Config>lambdaQuery()
				.eq(type != null, Config::getType, type)
				.like(configName != null, Config::getConfigName, configName)
				.like(configKey != null, Config::getConfigKey, configKey)
				.orderByDesc(Config::getId);
		return R.success(configMapper.selectList(wrapper));
	}

	@PutMapping("/config/update")
	@Operation(summary = "更新配置")
	public R<Boolean> updateConfig(@RequestBody Config config) {
		int rows = configMapper.updateById(config);
		if (rows > 0) {
			sysConfigService.clearConfigCache();
			sysConfigService.resetConfigCache();
		}
		return R.success(rows > 0);
	}

	@PutMapping("/config/batchUpdate")
	@Operation(summary = "批量更新配置")
	public R<Boolean> batchUpdateConfig(@RequestBody List<Config> configs) {
		for (Config config : configs) {
			configMapper.updateById(config);
		}
		sysConfigService.clearConfigCache();
		sysConfigService.resetConfigCache();
		return R.success(true);
	}
}
