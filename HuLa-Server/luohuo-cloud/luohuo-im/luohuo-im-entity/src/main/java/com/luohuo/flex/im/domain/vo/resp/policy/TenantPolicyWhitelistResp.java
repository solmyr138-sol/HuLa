package com.luohuo.flex.im.domain.vo.resp.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "租户策略白名单成员")
public class TenantPolicyWhitelistResp {

    private Long id;
    private Long imUid;
    private String account;
    private String mobile;
    private String nickname;
}
