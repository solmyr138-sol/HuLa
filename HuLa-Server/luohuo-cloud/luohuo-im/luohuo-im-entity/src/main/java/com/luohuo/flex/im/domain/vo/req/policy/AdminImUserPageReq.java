package com.luohuo.flex.im.domain.vo.req.policy;

import com.luohuo.flex.im.domain.vo.req.PageBaseReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "后台IM用户分页查询")
public class AdminImUserPageReq extends PageBaseReq {

    @Schema(description = "账号/昵称/手机关键字")
    private String keyword;

    @Schema(description = "用户状态筛选")
    private Integer state;

    @Schema(description = "是否仅注销用户")
    private Boolean deletedOnly;
}
