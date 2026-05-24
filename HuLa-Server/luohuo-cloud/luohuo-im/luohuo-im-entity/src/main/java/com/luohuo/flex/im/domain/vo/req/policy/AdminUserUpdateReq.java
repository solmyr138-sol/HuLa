package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "后台编辑 IM 用户")
public class AdminUserUpdateReq implements Serializable {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "IM 用户 uid")
    private Long uid;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱（列表「手机」列实际存邮箱时可改此项）")
    private String email;

    @Schema(description = "0 正常 1 封禁")
    private Integer state;
}
