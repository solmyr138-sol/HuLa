package com.luohuo.flex.oauth.vo.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.luohuo.basic.annotation.constraints.NotEmptyPattern;

import static com.luohuo.basic.utils.ValidatorUtil.REGEX_MOBILE;

/**
 * IM 企业码 + 手机号注册（无邮箱/短信验证码）
 */
@Data
@Schema(description = "企业码手机号注册")
public class ImEnterpriseRegisterVO {

    @NotEmpty(message = "请填写企业邀请码")
    @Size(max = 32, message = "企业邀请码过长")
    private String enterpriseCode;

    @NotEmptyPattern(regexp = REGEX_MOBILE, message = "请输入11位合法的手机号")
    @NotEmpty(message = "请填写手机号")
    private String mobile;

    @NotEmpty(message = "请填写密码")
    @Size(min = 6, max = 64, message = "密码长度不能小于{min}且超过{max}个字符")
    private String password;

    @NotEmpty(message = "请填写确认密码")
    private String confirmPassword;

    @NotNull(message = "请选择系统类型")
    private Integer systemType = 2;

    @Schema(description = "昵称，可选")
    private String nickName;
}
