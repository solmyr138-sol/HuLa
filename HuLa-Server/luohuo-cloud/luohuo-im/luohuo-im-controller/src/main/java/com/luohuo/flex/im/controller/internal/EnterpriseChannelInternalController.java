package com.luohuo.flex.im.controller.internal;

import com.luohuo.basic.base.R;
import com.luohuo.basic.tenant.core.aop.TenantIgnore;
import com.luohuo.flex.im.api.vo.OfficialChannelCreateVO;
import com.luohuo.flex.im.api.vo.OfficialChannelResp;
import com.luohuo.flex.im.core.tenant.service.EnterpriseOfficialChannelService;
import com.luohuo.flex.im.core.tenant.service.OfficialChannelCleanupResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@TenantIgnore
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/enterprise-channel")
@Tag(name = "内部-企业官方频道")
public class EnterpriseChannelInternalController {

    private final EnterpriseOfficialChannelService enterpriseOfficialChannelService;

    @PostMapping("/create")
    @Operation(summary = "创建企业官方频道")
    public R<OfficialChannelResp> createOfficialChannel(@Valid @RequestBody OfficialChannelCreateVO vo) {
        return R.success(enterpriseOfficialChannelService.createOfficialChannel(vo.getTenantId(), vo.getTenantName()));
    }

    @PostMapping("/migrate-from-global/{tenantId}")
    @Operation(summary = "将企业用户从全局官方群迁出并加入本企业官方频道")
    public R<Integer> migrateFromGlobalRoom(@PathVariable Long tenantId) {
        return R.success(enterpriseOfficialChannelService.migrateTenantUsersFromGlobalRoom(tenantId));
    }

    @PostMapping("/migrate-all-non-default")
    @Operation(summary = "迁移所有非默认租户用户离开全局官方群")
    public R<Integer> migrateAllNonDefaultTenants() {
        return R.success(enterpriseOfficialChannelService.migrateAllNonDefaultTenants());
    }

    @PostMapping("/migrate-and-clean/{tenantId}")
    @Operation(summary = "补齐租户用户到官方频道，并清理重复官方频道")
    public R<OfficialChannelCleanupResult> migrateAndClean(@PathVariable Long tenantId) {
        return R.success(enterpriseOfficialChannelService.migrateUsersAndCleanupDuplicates(tenantId));
    }
}
