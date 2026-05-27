package com.luohuo.flex.im.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建企业官方频道")
public class OfficialChannelCreateVO {

    @NotNull
    private Long tenantId;

    @NotBlank
    private String tenantName;
}
