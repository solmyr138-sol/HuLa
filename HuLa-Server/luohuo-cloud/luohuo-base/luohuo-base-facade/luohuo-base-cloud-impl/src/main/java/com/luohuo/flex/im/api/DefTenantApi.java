package com.luohuo.flex.im.api;

import com.luohuo.basic.base.R;
import com.luohuo.basic.constant.Constants;
import com.luohuo.flex.base.entity.tenant.DefTenant;
import com.luohuo.flex.im.api.hystrix.DefTenantApiFallback;
import org.springframework.cloud.openfeign.FeignClient;
import com.luohuo.flex.base.vo.update.tenant.DefTenantLogoUpdateVO;
import com.luohuo.flex.base.vo.update.tenant.DefTenantOfficialChannelUpdateVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 租户信息（走 base-server 的 luohuo_dev 库）
 */
@FeignClient(
        name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:luohuo-base-server}",
        fallback = DefTenantApiFallback.class)
public interface DefTenantApi {

    @GetMapping("/defTenant/{id}")
    R<DefTenant> getById(@PathVariable("id") Long id);

    @PutMapping("/defTenant/{id}/logo")
    R<Boolean> updateLogo(@PathVariable("id") Long id, @RequestBody DefTenantLogoUpdateVO vo);

    @PutMapping("/defTenant/{id}/official-channel")
    R<Boolean> updateOfficialChannel(@PathVariable("id") Long id, @RequestBody DefTenantOfficialChannelUpdateVO vo);
}
