package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "添加租户策略白名单")
public class TenantPolicyWhitelistAddReq {

    @NotNull
    private Long imUid;
}
