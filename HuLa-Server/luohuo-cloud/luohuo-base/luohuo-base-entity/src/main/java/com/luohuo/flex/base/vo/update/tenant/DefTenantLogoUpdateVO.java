package com.luohuo.flex.base.vo.update.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "更新企业LOGO")
public class DefTenantLogoUpdateVO implements Serializable {

    @Schema(description = "LOGO 访问地址，传空字符串表示清除")
    private String logo;
}
