package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "添加租户策略白名单")
public class TenantPolicyWhitelistAddReq {

    @Schema(description = "IM 用户 UID（与 accountOrMobile 二选一）")
    private Long imUid;

    @Schema(description = "账号或手机号（与 imUid 二选一）")
    private String accountOrMobile;
}
