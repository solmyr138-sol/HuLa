package com.luohuo.flex.im.domain.vo.req.policy;

import com.luohuo.flex.im.domain.vo.req.PageBaseReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户注销日志分页")
public class UserDeletionLogPageReq extends PageBaseReq {

    @Schema(description = "账号关键字")
    private String keyword;
}
