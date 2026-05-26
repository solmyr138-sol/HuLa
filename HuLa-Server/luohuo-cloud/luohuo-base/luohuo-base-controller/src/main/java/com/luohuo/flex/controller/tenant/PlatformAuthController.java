package com.luohuo.flex.controller.tenant;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.SecureUtil;
import com.luohuo.basic.base.R;
import com.luohuo.basic.tenant.core.aop.TenantIgnore;
import com.luohuo.flex.base.entity.tenant.DefUser;
import com.luohuo.flex.base.manager.tenant.DefUserManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.luohuo.basic.context.ContextConstants.*;

/**
 * 平台总后台登录（走真实 Sa-Token）
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/anyTenant")
@Tag(name = "平台-认证")
@TenantIgnore
public class PlatformAuthController {

    private final DefUserManager defUserManager;

    @PostMapping("/platformLogin")
    @Operation(summary = "平台总后台登录")
    public R<PlatformLoginResult> login(@RequestBody @Validated PlatformLoginParam param) {
        DefUser user = defUserManager.findUserForLogin(1, param.getUsername(), false);
        if (user == null) {
            return R.fail("用户名或密码错误");
        }

        String hash = SecureUtil.sha256(param.getPassword() + user.getSalt());
        if (!hash.equalsIgnoreCase(user.getPassword())) {
            return R.fail("用户名或密码错误");
        }

        StpUtil.login(user.getId(), "PLATFORM_PC");

        var session = StpUtil.getTokenSession();
        session.setLoginId(user.getId());
        session.set(JWT_KEY_SYSTEM_TYPE, "10");
        session.set(JWT_KEY_U_ID, user.getId());
        session.set(JWT_KEY_TOP_COMPANY_ID, 0L);
        session.set(JWT_KEY_COMPANY_ID, 0L);
        session.set(JWT_KEY_DEPT_ID, 0L);
        session.set(HEADER_TENANT_ID, user.getTenantId() != null ? user.getTenantId() : 0L);

        PlatformLoginResult result = new PlatformLoginResult();
        result.setToken(StpUtil.getTokenValue());
        result.setNickName(user.getNickName());
        return R.success(result);
    }

    @Data
    public static class PlatformLoginParam {
        @NotBlank(message = "请输入用户名")
        private String username;
        @NotBlank(message = "请输入密码")
        private String password;
    }

    @Data
    public static class PlatformLoginResult {
        private String token;
        private String nickName;
    }
}
