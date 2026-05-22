package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "更新群策略")
public class GroupPolicyUpdateReq implements Serializable {

    @NotNull
    @Schema(description = "群房间ID")
    private Long roomId;

    @Schema(description = "加入模式 1仅管理员 2成员可邀请 3不限制")
    private Integer joinMode;

    @Schema(description = "新成员可见历史消息")
    private Boolean historyVisibleToNew;

    @Schema(description = "全员禁言")
    private Boolean groupMuteAll;

    @Schema(description = "允许群内加好友")
    private Boolean allowMemberAddFriend;

    @Schema(description = "允许群内私聊")
    private Boolean allowMemberDm;

    @Schema(description = "允许改群昵称")
    private Boolean allowMemberChangeNickname;

    @Schema(description = "发言间隔秒")
    private Integer speakIntervalSec;
}
