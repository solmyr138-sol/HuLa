package com.luohuo.flex.controller.tenant;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.luohuo.basic.annotation.log.WebLog;
import com.luohuo.basic.base.R;
import com.luohuo.basic.base.request.PageParams;
import com.luohuo.basic.database.mybatis.conditions.Wraps;
import com.luohuo.basic.database.mybatis.conditions.query.QueryWrap;
import com.luohuo.basic.tenant.core.aop.TenantIgnore;
import com.luohuo.basic.utils.ArgumentAssert;
import com.luohuo.flex.base.entity.tenant.DefTenant;
import com.luohuo.flex.base.entity.tenant.DefUser;
import com.luohuo.flex.base.service.tenant.DefTenantService;
import com.luohuo.flex.base.service.tenant.DefUserService;
import com.luohuo.flex.base.vo.query.tenant.DefTenantPageQuery;
import com.luohuo.flex.base.vo.result.tenant.PlatformTenantCreateResp;
import com.luohuo.flex.base.vo.save.tenant.DefTenantSaveVO;
import com.luohuo.flex.base.vo.update.tenant.DefTenantUpdateVO;
import com.luohuo.flex.im.api.EnterpriseChannelApi;
import com.luohuo.flex.im.api.ImUserApi;
import com.luohuo.flex.im.api.vo.OfficialChannelCreateVO;
import com.luohuo.flex.im.api.vo.UserRegisterVo;
import com.luohuo.flex.model.enumeration.system.DefTenantStatusEnum;
import com.luohuo.flex.im.enums.UserTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台总后台（devOperation）— 跨租户企业管理，忽略租户隔离
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/tenant")
@Tag(name = "平台-企业管理")
@TenantIgnore
public class PlatformTenantController {

    private static final int DEFAULT_ACCOUNT_LIMIT = 500;

    private static final String IM_SERVER_NAME = "luohuo-im-server";

    private final DefTenantService defTenantService;
    private final DefUserService defUserService;
    private final DiscoveryClient discoveryClient;
    private final EnterpriseChannelApi enterpriseChannelApi;
    private final ImUserApi imUserApi;

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
    @Transactional(rollbackFor = Exception.class)
    public R<PlatformTenantCreateResp> create(@RequestBody @Valid DefTenantSaveVO saveVO) {
        String inviteCode = generateInviteCode();
        int accountLimit = resolveAccountLimit(saveVO.getAccountLimit());

        DefTenant tenant = new DefTenant();
        tenant.setName(saveVO.getName());
        tenant.setContactName(StrUtil.blankToDefault(saveVO.getContactPerson(), ""));
        tenant.setContactMobile(saveVO.getContactPhone());
        tenant.setInviteCode(inviteCode);
        tenant.setState(true);
        tenant.setStatus(DefTenantStatusEnum.NORMAL.getCode());
        tenant.setPackageId(0L);
        tenant.setExpireTime(LocalDateTime.of(2099, 12, 31, 23, 59, 59));
        tenant.setAccountCount(0);
        tenant.setAccountLimit(accountLimit);
        defTenantService.getSuperManager().save(tenant);

        List<String> warnings = new ArrayList<>();
        boolean imReachable = isImServerReachable();
        if (imReachable) {
            tryCreateOfficialChannel(tenant, warnings);
        } else {
            warnings.add("官方频道未创建（luohuo-im-server 未就绪），请启动 IM 服务后补建");
        }

        String adminUsername = "admin_" + inviteCode;
        String adminPassword = defUserService.createEnterpriseAdminUser(tenant.getId(), inviteCode);
        DefUser adminUser = defUserService.getUserByUsername(2, adminUsername);
        if (imReachable) {
            trySyncImUser(adminUser, warnings);
        } else {
            warnings.add("企业 IM 账号未同步（luohuo-im-server 未就绪），管理账号密码仍可用于企业后台登录");
        }

        DefTenant saved = defTenantService.getById(tenant.getId());
        String loginHint = "用于「企业传书管理后台」登录；请求头 tenant-id 请填企业 ID：" + tenant.getId();
        if (!warnings.isEmpty()) {
            loginHint = loginHint + "。注意：" + String.join("；", warnings);
        }
        PlatformTenantCreateResp resp = PlatformTenantCreateResp.builder()
                .tenant(saved)
                .adminUsername(adminUsername)
                .adminPassword(adminPassword)
                .adminLoginHint(loginHint)
                .warnings(warnings)
                .build();
        return R.success(resp);
    }

    @PutMapping
    @Operation(summary = "更新企业")
    @WebLog("平台更新企业")
    public R<DefTenant> update(@RequestBody @Valid DefTenantUpdateVO updateVO) {
        DefTenant tenant = defTenantService.getById(updateVO.getId());
        ArgumentAssert.notNull(tenant, "企业不存在");
        tenant.setName(updateVO.getName());
        if (updateVO.getContactPerson() != null) {
            tenant.setContactName(updateVO.getContactPerson());
        }
        if (updateVO.getContactPhone() != null) {
            tenant.setContactMobile(updateVO.getContactPhone());
        }
        if (updateVO.getState() != null) {
            tenant.setState(updateVO.getState());
        }
        if (updateVO.getAccountLimit() != null) {
            tenant.setAccountLimit(resolveAccountLimit(updateVO.getAccountLimit()));
        }
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

    @DeleteMapping("/{id}")
    @Operation(summary = "删除企业")
    @WebLog("平台删除企业")
    public R<Boolean> delete(@PathVariable Long id) {
        ArgumentAssert.notNull(id, "企业ID不能为空");
        ArgumentAssert.isTrue(id > 1L, "默认租户不可删除");
        DefTenant tenant = defTenantService.getById(id);
        ArgumentAssert.notNull(tenant, "企业不存在");
        return R.success(defTenantService.delete(List.of(id)));
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

    private static final int IM_HEALTH_TIMEOUT_MS = 2000;

    private boolean isImServerReachable() {
        try {
            var instances = discoveryClient.getInstances(IM_SERVER_NAME);
            if (instances == null || instances.isEmpty()) {
                return false;
            }
            for (ServiceInstance inst : instances) {
                if (pingImHealth(inst)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("探测 IM 服务可用性失败", e);
            return false;
        }
    }

    private boolean pingImHealth(ServiceInstance inst) {
        String base = "http://" + inst.getHost() + ":" + inst.getPort();
        for (String path : new String[]{"/actuator/health", "/im/actuator/health"}) {
            try {
                var resp = HttpRequest.get(base + path).timeout(IM_HEALTH_TIMEOUT_MS).execute();
                if (resp.isOk() && StrUtil.containsIgnoreCase(resp.body(), "UP")) {
                    return true;
                }
            } catch (Exception ignored) {
                // try next path / instance
            }
        }
        return false;
    }

    private void tryCreateOfficialChannel(DefTenant tenant, List<String> warnings) {
        if (tenant.getId() == null || tenant.getId() <= 1L) {
            return;
        }
        try {
            OfficialChannelCreateVO channelReq = new OfficialChannelCreateVO();
            channelReq.setTenantId(tenant.getId());
            channelReq.setTenantName(tenant.getName());
            var channelR = enterpriseChannelApi.createOfficialChannel(channelReq);
            if (channelR == null || channelR.getData() == null) {
                String msg = channelR != null && StrUtil.isNotBlank(channelR.getMsg())
                        ? channelR.getMsg() : "IM 服务不可用";
                log.warn("创建企业官方频道未成功 tenantId={} msg={}", tenant.getId(), msg);
                warnings.add("官方频道未创建（" + msg + "），可稍后启动 luohuo-im-server 后补建");
            }
        } catch (Throwable e) {
            log.warn("创建企业官方频道异常 tenantId={}", tenant.getId(), e);
            warnings.add("官方频道未创建（IM 服务异常），企业与管理账号已创建");
        }
    }

    private void trySyncImUser(DefUser adminUser, List<String> warnings) {
        try {
            UserRegisterVo userRegisterVo = new UserRegisterVo();
            userRegisterVo.setAccount(adminUser.getUsername());
            userRegisterVo.setUserId(adminUser.getId());
            userRegisterVo.setEmail(StrUtil.blankToDefault(adminUser.getMobile(), adminUser.getUsername()));
            userRegisterVo.setName(adminUser.getNickName());
            userRegisterVo.setSex(adminUser.getSex());
            userRegisterVo.setAvatar(adminUser.getAvatar());
            userRegisterVo.setTenantId(adminUser.getTenantId());
            userRegisterVo.setUserType(UserTypeEnum.NORMAL.getValue());
            var registerR = imUserApi.register(userRegisterVo);
            if (!Boolean.TRUE.equals(registerR != null ? registerR.getData() : null)) {
                String msg = registerR != null && StrUtil.isNotBlank(registerR.getMsg())
                        ? registerR.getMsg() : "IM 服务不可用";
                log.warn("同步 IM 管理员失败 userId={} msg={}", adminUser.getId(), msg);
                warnings.add("企业后台 IM 账号未同步（" + msg + "），请启动 luohuo-im-server 后联系运维补同步");
            }
        } catch (Throwable e) {
            log.warn("同步 IM 管理员异常 userId={}", adminUser.getId(), e);
            warnings.add("企业后台 IM 账号未同步，管理账号密码仍可用于后续补同步");
        }
    }

    private int resolveAccountLimit(Integer accountLimit) {
        if (accountLimit == null) {
            return DEFAULT_ACCOUNT_LIMIT;
        }
        ArgumentAssert.isTrue(accountLimit > 0, "注册人数上限须大于 0");
        return accountLimit;
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
