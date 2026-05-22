package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "群成员扩展权限")
public class GroupMemberAclUpdateReq implements Serializable {

    @NotNull
    @Schema(description = "群房间ID")
    private Long roomId;

    @NotNull
    @Schema(description = "成员uid")
    private Long uid;

    @Schema(description = "可编辑任意消息")
    private Boolean canEditAnyMessage;

    @Schema(description = "可撤回任意消息")
    private Boolean canRecallAnyMessage;
}
