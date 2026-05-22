package com.luohuo.flex.im.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.luohuo.basic.base.entity.TenantEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_tenant_policy_whitelist")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantPolicyWhitelist extends TenantEntity<Long> {

    @TableField("im_uid")
    private Long imUid;
}
