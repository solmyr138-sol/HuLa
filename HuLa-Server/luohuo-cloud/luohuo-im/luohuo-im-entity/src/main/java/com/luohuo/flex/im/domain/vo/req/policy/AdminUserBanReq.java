package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "后台封禁用户")
public class AdminUserBanReq implements Serializable {

    @NotNull
    @Schema(description = "IM用户uid")
    private Long uid;

    @Schema(description = "封禁原因")
    private String reason;

    @Schema(description = "截止时间（分钟），0或null表示永久")
    private Long deadlineMinutes;
}
