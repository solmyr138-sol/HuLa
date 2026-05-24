package com.luohuo.flex.im.core.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.basic.validator.utils.AssertUtil;
import com.luohuo.basic.base.R;
import com.luohuo.flex.base.entity.tenant.DefTenant;
import com.luohuo.flex.im.api.DefTenantApi;
import com.luohuo.flex.im.core.admin.service.AdminTenantService;
import com.luohuo.flex.im.core.user.dao.UserDao;
import com.luohuo.flex.im.domain.entity.User;
import com.luohuo.flex.im.domain.vo.resp.admin.AdminTenantInfoResp;
import com.luohuo.flex.base.vo.update.tenant.DefTenantLogoUpdateVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminTenantServiceImpl implements AdminTenantService {

    @Resource
    private DefTenantApi defTenantApi;
    @Resource
    private UserDao userDao;

    @Value("${hula.enterprise.server-url:}")
    private String serverUrl;

    @Value("${hula.enterprise.app-download-url:}")
    private String appDownloadUrl;

    @Override
    public AdminTenantInfoResp getCurrentTenantInfo() {
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        R<DefTenant> tenantR = defTenantApi.getById(tenantId);
        DefTenant tenant = tenantR != null ? tenantR.getData() : null;
        AssertUtil.isNotEmpty(tenant, "租户不存在");
        long registered = userDao.count(new LambdaQueryWrapper<User>().eq(User::getTenantId, tenantId));
        return AdminTenantInfoResp.builder()
                .tenantId(tenantId)
                .inviteCode(tenant.getInviteCode())
                .tenantName(tenant.getName())
                .logo(StrUtil.blankToDefault(tenant.getLogo(), ""))
                .creditCode(StrUtil.blankToDefault(tenant.getWebsite(), ""))
                .registeredCount(registered)
                .accountLimit(ObjectUtil.defaultIfNull(tenant.getAccountCount(), 0))
                .expireTime(tenant.getExpireTime())
                .serverUrl(StrUtil.blankToDefault(serverUrl, tenant.getWebsite()))
                .appDownloadUrl(appDownloadUrl)
                .build();
    }

    @Override
    public void updateLogo(Long operatorId, String logo) {
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        DefTenantLogoUpdateVO vo = new DefTenantLogoUpdateVO();
        vo.setLogo(logo != null ? logo.trim() : "");
        R<Boolean> result = defTenantApi.updateLogo(tenantId, vo);
        AssertUtil.isTrue(result != null && Boolean.TRUE.equals(result.getData()), "更新企业LOGO失败");
    }
}

