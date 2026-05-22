package com.luohuo.flex.im.domain.vo.resp.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "后台IM用户列表项")
public class AdminImUserResp {

    private Long uid;
    private String account;
    private String mobile;
    private String nickname;
    private Integer state;
    private LocalDateTime registerTime;
    private LocalDateTime deletedTime;
}
