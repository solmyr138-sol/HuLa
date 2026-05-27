package com.luohuo.flex.im.api;

import com.luohuo.basic.base.R;
import com.luohuo.flex.im.api.hystrix.EnterpriseChannelApiFallback;
import com.luohuo.flex.im.api.vo.OfficialChannelCreateVO;
import com.luohuo.flex.im.api.vo.OfficialChannelResp;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "luohuo-im-server", fallback = EnterpriseChannelApiFallback.class)
public interface EnterpriseChannelApi {

    @PostMapping("/internal/enterprise-channel/create")
    R<OfficialChannelResp> createOfficialChannel(@Valid @RequestBody OfficialChannelCreateVO vo);

    @PostMapping("/internal/enterprise-channel/migrate-from-global/{tenantId}")
    R<Integer> migrateFromGlobalRoom(@PathVariable("tenantId") Long tenantId);

    @PostMapping("/internal/enterprise-channel/migrate-all-non-default")
    R<Integer> migrateAllNonDefaultTenants();
}
