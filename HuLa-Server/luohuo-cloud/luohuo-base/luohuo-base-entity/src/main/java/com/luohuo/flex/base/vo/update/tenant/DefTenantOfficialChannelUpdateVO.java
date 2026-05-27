package com.luohuo.flex.base.vo.update.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新企业官方频道ID")
public class DefTenantOfficialChannelUpdateVO {

    @NotNull
    private Long officialRoomId;

    @NotNull
    private Long officialGroupId;
}
