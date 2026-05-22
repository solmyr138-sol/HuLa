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
 * 群策略
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_group_policy")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupPolicy extends TenantEntity<Long> {

    private static final long serialVersionUID = 1L;

    @TableField("room_id")
    private Long roomId;

    @TableField("join_mode")
    private Integer joinMode;

    @TableField("history_visible_to_new")
    private Boolean historyVisibleToNew;

    @TableField("group_mute_all")
    private Boolean groupMuteAll;

    @TableField("allow_member_add_friend")
    private Boolean allowMemberAddFriend;

    @TableField("allow_member_dm")
    private Boolean allowMemberDm;

    @TableField("allow_member_change_nickname")
    private Boolean allowMemberChangeNickname;

    @TableField("speak_interval_sec")
    private Integer speakIntervalSec;
}
