package com.luohuo.flex.im.domain.vo.req.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "后台重置用户登录密码")
public class AdminUserResetPasswordReq implements Serializable {

    @NotNull(message = "用户ID不能为空")
    private Long uid;

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    private String password;

    @NotBlank(message = "请确认新密码")
    private String confirmPassword;
}
