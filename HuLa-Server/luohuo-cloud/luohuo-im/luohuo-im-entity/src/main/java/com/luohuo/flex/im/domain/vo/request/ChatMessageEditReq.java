package com.luohuo.flex.im.domain.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "编辑消息请求")
public class ChatMessageEditReq {
    @NotNull
    @Schema(description = "消息ID")
    private Long msgId;

    @NotNull
    @Schema(description = "新消息体(与发送时相同结构)")
    private Object body;

    @NotNull
    @Schema(description = "消息类型")
    private Integer msgType;
}
