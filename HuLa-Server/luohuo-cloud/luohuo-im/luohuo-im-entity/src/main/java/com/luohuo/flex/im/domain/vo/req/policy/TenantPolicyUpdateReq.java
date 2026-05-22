package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "更新租户策略")
public class TenantPolicyUpdateReq implements Serializable {

    @Schema(description = "允许跨租户加好友")
    private Boolean allowCrossTenantFriend;

    @Schema(description = "允许跨租户群邀请")
    private Boolean allowCrossTenantGroupInvite;

    @Schema(description = "禁止成员创建群")
    private Boolean forbidCreateGroup;

    @Schema(description = "禁止群发")
    private Boolean forbidBroadcast;

    @Schema(description = "禁止成员互加好友")
    private Boolean forbidMemberAddFriend;
}
