package com.luohuo.flex.im.controller.admin;

import com.luohuo.basic.base.R;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.flex.im.core.policy.service.TenantPolicyService;
import com.luohuo.flex.im.domain.entity.TenantPolicy;
import com.luohuo.flex.im.domain.vo.req.policy.TenantPolicyUpdateReq;
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
@RequestMapping("/admin/tenant/policy")
@Tag(name = "租户策略接口")
@Slf4j
public class TenantPolicyController {

    @Resource
    private TenantPolicyService tenantPolicyService;

    @GetMapping
    @Operation(summary = "获取当前租户策略")
    public R<TenantPolicy> getPolicy() {
        return R.success(tenantPolicyService.getCurrentTenantPolicy());
    }

    @PutMapping
    @Operation(summary = "更新当前租户策略")
    public R<TenantPolicy> updatePolicy(@Valid @RequestBody TenantPolicyUpdateReq req) {
        return R.success(tenantPolicyService.updateCurrentTenantPolicy(ContextUtil.getUid(), req));
    }
}
