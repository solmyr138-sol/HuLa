package com.luohuo.flex.im.controller.admin;

import com.luohuo.basic.base.R;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.flex.im.core.admin.service.AdminUserService;
import com.luohuo.flex.im.domain.vo.req.policy.AdminImUserPageReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserBanReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserResetPasswordReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserUnbanReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserUpdateReq;
import com.luohuo.flex.im.domain.vo.req.policy.UserDeletionLogPageReq;
import com.luohuo.flex.im.domain.vo.res.PageBaseResp;
import com.luohuo.flex.im.domain.vo.resp.policy.AdminImUserResp;
import com.luohuo.flex.im.domain.vo.resp.policy.UserDeletionLogResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/user")
@Tag(name = "后台用户管理接口")
@Slf4j
public class AdminUserController {

    @Resource
    private AdminUserService adminUserService;

    @PutMapping("/ban")
    @Operation(summary = "封禁用户")
    public R<Void> banUser(@Valid @RequestBody AdminUserBanReq req) {
        adminUserService.banUser(ContextUtil.getUid(), req);
        return R.success();
    }

    @PutMapping("/unban")
    @Operation(summary = "解封用户")
    public R<Void> unbanUser(@Valid @RequestBody AdminUserUnbanReq req) {
        adminUserService.unbanUser(ContextUtil.getUid(), req);
        return R.success();
    }

    @GetMapping("/deletion-log")
    @Operation(summary = "用户注销审计列表")
    public R<PageBaseResp<UserDeletionLogResp>> listDeletionLogs(@Valid UserDeletionLogPageReq req) {
        return R.success(adminUserService.listDeletionLogs(req));
    }

    @GetMapping("/page")
    @Operation(summary = "注册用户分页")
    public R<PageBaseResp<AdminImUserResp>> pageUsers(@Valid AdminImUserPageReq req) {
        return R.success(adminUserService.pageImUsers(req));
    }

    @PutMapping("/update")
    @Operation(summary = "编辑用户资料与状态")
    public R<Void> updateUser(@Valid @RequestBody AdminUserUpdateReq req) {
        adminUserService.updateUser(ContextUtil.getUid(), req);
        return R.success();
    }

    @PutMapping("/reset-password")
    @Operation(summary = "重置用户登录密码")
    public R<Void> resetPassword(@Valid @RequestBody AdminUserResetPasswordReq req) {
        adminUserService.resetPassword(ContextUtil.getUid(), req);
        return R.success();
    }
}
