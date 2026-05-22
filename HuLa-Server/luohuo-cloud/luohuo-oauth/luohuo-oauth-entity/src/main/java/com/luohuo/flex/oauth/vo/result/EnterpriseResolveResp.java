package com.luohuo.flex.oauth.vo.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企业邀请码解析结果")
public class EnterpriseResolveResp {
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "企业名称")
    private String tenantName;
}
