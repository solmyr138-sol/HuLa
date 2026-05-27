package com.luohuo.flex.im.api.hystrix;

import com.luohuo.basic.base.R;
import com.luohuo.basic.exception.BizException;
import com.luohuo.flex.base.entity.tenant.DefTenant;
import com.luohuo.flex.base.vo.update.tenant.DefTenantLogoUpdateVO;
import com.luohuo.flex.base.vo.update.tenant.DefTenantOfficialChannelUpdateVO;
import com.luohuo.flex.im.api.DefTenantApi;
import org.springframework.stereotype.Component;

@Component
public class DefTenantApiFallback implements DefTenantApi {

    @Override
    public R<DefTenant> getById(Long id) {
        throw BizException.wrap("查询企业信息失败，请确认 luohuo-base-server 已启动");
    }

    @Override
    public R<Boolean> updateLogo(Long id, DefTenantLogoUpdateVO vo) {
        throw BizException.wrap("更新企业LOGO失败，请确认 luohuo-base-server 已启动");
    }

    @Override
    public R<Boolean> updateOfficialChannel(Long id, DefTenantOfficialChannelUpdateVO vo) {
        throw BizException.wrap("更新企业官方频道失败，请确认 luohuo-base-server 已启动");
    }
}
