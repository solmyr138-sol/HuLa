package com.luohuo.flex.base.vo.result.tenant;

import com.luohuo.flex.base.entity.tenant.DefTenant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 平台总后台创建企业响应（含一次性展示的管理员凭据）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "平台创建企业结果")
public class PlatformTenantCreateResp {

    @Schema(description = "企业信息")
    private DefTenant tenant;

    @Schema(description = "企业传书管理后台登录用户名（systemType=2）")
    private String adminUsername;

    @Schema(description = "管理员初始密码（仅创建时返回一次）")
    private String adminPassword;

    @Schema(description = "登录说明")
    private String adminLoginHint;

    @Schema(description = "非阻断性告警（如 IM 未启动）")
    private List<String> warnings;
}
