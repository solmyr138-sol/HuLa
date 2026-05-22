package com.luohuo.flex.im.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.luohuo.basic.base.entity.TenantEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 群成员扩展权限
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_group_member_acl")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberAcl extends TenantEntity<Long> {

    private static final long serialVersionUID = 1L;

    @TableField("group_id")
    private Long groupId;

    @TableField("uid")
    private Long uid;

    @TableField("can_edit_any_message")
    private Boolean canEditAnyMessage;

    @TableField("can_recall_any_message")
    private Boolean canRecallAnyMessage;

    @TableField("muted_until")
    private LocalDateTime mutedUntil;
}
