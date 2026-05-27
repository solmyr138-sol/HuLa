package com.luohuo.flex.controller.tenant;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.luohuo.basic.annotation.log.WebLog;
import com.luohuo.basic.base.R;
import com.luohuo.basic.base.request.PageParams;
import com.luohuo.basic.database.mybatis.conditions.Wraps;
import com.luohuo.basic.database.mybatis.conditions.query.QueryWrap;
import com.luohuo.basic.tenant.core.aop.TenantIgnore;
import com.luohuo.basic.utils.ArgumentAssert;
import com.luohuo.basic.utils.BeanPlusUtil;
import com.luohuo.flex.base.entity.tenant.DefTenant;
import com.luohuo.basic.utils.ArgumentAssert;
import com.luohuo.flex.im.api.EnterpriseChannelApi;
import com.luohuo.flex.im.api.vo.OfficialChannelCreateVO;
import com.luohuo.flex.base.service.tenant.DefTenantService;
import com.luohuo.flex.base.vo.query.tenant.DefTenantPageQuery;
import com.luohuo.flex.base.vo.save.tenant.DefTenantSaveVO;
import com.luohuo.flex.base.vo.update.tenant.DefTenantUpdateVO;
import com.luohuo.flex.model.enumeration.system.DefTenantStatusEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 平台总后台（devOperation）— 跨租户企业管理，忽略租户隔离
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/tenant")
@Tag(name = "平台-企业管理")
@TenantIgnore
public class PlatformTenantController {

    private final DefTenantService defTenantService;
    private final EnterpriseChannelApi enterpriseChannelApi;

    @PostMapping("/page")
    @Operation(summary = "企业分页列表")
    public R<IPage<DefTenant>> page(@RequestBody @Valid PageParams<DefTenantPageQuery> params) {
        IPage<DefTenant> page = params.buildPage(DefTenant.class);
        QueryWrap<DefTenant> wrap = Wraps.q(null, params.getExtra(), DefTenant.class);
        if (params.getModel() != null && StrUtil.isNotBlank(params.getModel().getName())) {
            wrap.lambda().like(DefTenant::getName, params.getModel().getName());
        }
        wrap.lambda().orderByDesc(DefTenant::getCreateTime);
        return R.success(defTenantService.page(page, wrap));
    }

    @PostMapping
    @Operation(summary = "创建企业")
    @WebLog("平台创建企业")
    public R<DefTenant> create(@RequestBody @Valid DefTenantSaveVO saveVO) {
        DefTenant tenant = new DefTenant();
        tenant.setName(saveVO.getName());
        tenant.setContactName(StrUtil.blankToDefault(saveVO.getContactPerson(), ""));
        tenant.setContactMobile(saveVO.getContactPhone());
        tenant.setInviteCode(generateInviteCode());
        tenant.setState(true);
        tenant.setStatus(DefTenantStatusEnum.NORMAL.getCode());
        tenant.setPackageId(0L);
        tenant.setExpireTime(LocalDateTime.of(2099, 12, 31, 23, 59, 59));
        tenant.setAccountCount(0);
        defTenantService.getSuperManager().save(tenant);
        if (tenant.getId() != null && tenant.getId() > 1L) {
            OfficialChannelCreateVO channelReq = new OfficialChannelCreateVO();
            channelReq.setTenantId(tenant.getId());
            channelReq.setTenantName(tenant.getName());
            var channelR = enterpriseChannelApi.createOfficialChannel(channelReq);
            ArgumentAssert.isTrue(channelR != null && channelR.getData() != null, "创建企业官方频道失败");
        }
        return R.success(defTenantService.getById(tenant.getId()));
    }

    @PutMapping
    @Operation(summary = "更新企业")
    @WebLog("平台更新企业")
    public R<DefTenant> update(@RequestBody @Valid DefTenantUpdateVO updateVO) {
        DefTenant tenant = BeanPlusUtil.toBean(updateVO, DefTenant.class);
        defTenantService.updateById(tenant);
        return R.success(defTenantService.getById(updateVO.getId()));
    }

    @PutMapping("/{id}/invite-code")
    @Operation(summary = "重置企业邀请码")
    public R<String> resetInviteCode(@PathVariable Long id) {
        DefTenant tenant = defTenantService.getById(id);
        ArgumentAssert.notNull(tenant, "企业不存在");
        String code = generateInviteCode();
        tenant.setInviteCode(code);
        defTenantService.updateById(tenant);
        return R.success(code);
    }

    @GetMapping("/stats")
    @Operation(summary = "平台概览统计")
    public R<PlatformStatsResp> stats() {
        long total = defTenantService.list(Wraps.<DefTenant>lbQ()).size();
        long active = defTenantService.list(Wraps.<DefTenant>lbQ().eq(DefTenant::getState, true)).size();
        PlatformStatsResp resp = new PlatformStatsResp();
        resp.setTenantTotal(total);
        resp.setTenantActive(active);
        return R.success(resp);
    }

    private String generateInviteCode() {
        for (int i = 0; i < 20; i++) {
            String code = RandomUtil.randomStringUpper(6);
            if (defTenantService.getByInviteCode(code) == null) {
                return code;
            }
        }
        return RandomUtil.randomStringUpper(8);
    }

    @Data
    public static class PlatformStatsResp {
        private Long tenantTotal;
        private Long tenantActive;
    }
}
