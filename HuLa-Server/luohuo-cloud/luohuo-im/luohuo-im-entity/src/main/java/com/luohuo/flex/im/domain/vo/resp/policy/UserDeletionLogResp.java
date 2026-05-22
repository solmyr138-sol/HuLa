package com.luohuo.flex.im.domain.vo.resp.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户注销日志")
public class UserDeletionLogResp implements Serializable {

    private Long id;
    private Long imUid;
    private Long defUserId;
    private Long tenantId;
    private String account;
    private String reason;
    private Long operatorId;
    private LocalDateTime createTime;
}
