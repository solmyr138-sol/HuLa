package com.luohuo.flex.im.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.luohuo.basic.base.entity.TenantEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 租户 IM 策略
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_tenant_policy")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantPolicy extends TenantEntity<Long> {

    private static final long serialVersionUID = 1L;

    @TableField("allow_cross_tenant_friend")
    private Boolean allowCrossTenantFriend;

    @TableField("allow_cross_tenant_group_invite")
    private Boolean allowCrossTenantGroupInvite;

    @TableField("forbid_create_group")
    private Boolean forbidCreateGroup;

    @TableField("forbid_broadcast")
    private Boolean forbidBroadcast;

    @TableField("forbid_member_add_friend")
    private Boolean forbidMemberAddFriend;
}
