package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "后台解封用户")
public class AdminUserUnbanReq implements Serializable {

    @NotNull
    @Schema(description = "IM用户uid")
    private Long uid;
}
