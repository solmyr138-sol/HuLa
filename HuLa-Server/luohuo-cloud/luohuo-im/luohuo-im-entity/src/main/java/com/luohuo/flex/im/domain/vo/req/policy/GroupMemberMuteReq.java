package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "群成员禁言")
public class GroupMemberMuteReq implements Serializable {

    @NotNull
    @Schema(description = "群房间ID")
    private Long roomId;

    @NotNull
    @Schema(description = "成员uid")
    private Long uid;

    @Schema(description = "禁言截止时间，null表示解除禁言")
    private LocalDateTime mutedUntil;
}
