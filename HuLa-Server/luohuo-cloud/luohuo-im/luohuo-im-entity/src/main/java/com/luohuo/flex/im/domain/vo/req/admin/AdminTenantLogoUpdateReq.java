package com.luohuo.flex.im.domain.vo.req.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新企业LOGO")
public class AdminTenantLogoUpdateReq {

    @Schema(description = "LOGO 访问地址，空字符串表示清除")
    private String logo;
}
