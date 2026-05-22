package com.luohuo.flex.im.controller.admin;

import com.luohuo.basic.base.R;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.flex.im.core.policy.service.TenantPolicyWhitelistService;
import com.luohuo.flex.im.domain.vo.req.policy.TenantPolicyWhitelistAddReq;
import com.luohuo.flex.im.domain.vo.resp.policy.TenantPolicyWhitelistResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/tenant/policy/whitelist")
@Tag(name = "租户策略白名单")
public class TenantPolicyWhitelistController {

    @Resource
    private TenantPolicyWhitelistService whitelistService;

    @GetMapping
    @Operation(summary = "白名单列表")
    public R<List<TenantPolicyWhitelistResp>> list() {
        return R.success(whitelistService.listCurrent());
    }

    @PostMapping
    @Operation(summary = "添加白名单成员")
    public R<Void> add(@Valid @RequestBody TenantPolicyWhitelistAddReq req) {
        whitelistService.add(ContextUtil.getUid(), req);
        return R.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "移除白名单成员")
    public R<Void> remove(@PathVariable Long id) {
        whitelistService.remove(ContextUtil.getUid(), id);
        return R.success();
    }
}
