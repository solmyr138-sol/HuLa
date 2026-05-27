package com.luohuo.flex.im.api.hystrix;

import com.luohuo.basic.base.R;
import com.luohuo.flex.im.api.EnterpriseChannelApi;
import com.luohuo.flex.im.api.vo.OfficialChannelCreateVO;
import com.luohuo.flex.im.api.vo.OfficialChannelResp;
import org.springframework.stereotype.Component;

@Component
public class EnterpriseChannelApiFallback implements EnterpriseChannelApi {

    @Override
    public R<OfficialChannelResp> createOfficialChannel(OfficialChannelCreateVO vo) {
        return R.timeout();
    }

    @Override
    public R<Integer> migrateFromGlobalRoom(Long tenantId) {
        return R.timeout();
    }

    @Override
    public R<Integer> migrateAllNonDefaultTenants() {
        return R.timeout();
    }
}
