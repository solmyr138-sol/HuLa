package com.luohuo.flex.im.domain.vo.resp.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "企业管理后台-企业信息")
public class AdminTenantInfoResp {

    private Long tenantId;
    private String inviteCode;
    private String tenantName;
    private String creditCode;
    private Long registeredCount;
    private Integer accountLimit;
    private LocalDateTime expireTime;
    private String serverUrl;
    private String appDownloadUrl;
}
