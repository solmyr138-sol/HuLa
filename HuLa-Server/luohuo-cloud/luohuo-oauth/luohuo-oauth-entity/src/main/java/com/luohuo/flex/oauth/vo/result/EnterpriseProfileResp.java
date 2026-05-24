package com.luohuo.flex.oauth.vo.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "当前用户所属企业信息")
public class EnterpriseProfileResp {

    private Long tenantId;
    private String inviteCode;
    private String tenantName;
    private String logo;
}
