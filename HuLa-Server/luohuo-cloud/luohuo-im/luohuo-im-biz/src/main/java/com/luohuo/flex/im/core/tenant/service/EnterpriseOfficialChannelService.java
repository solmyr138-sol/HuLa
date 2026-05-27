package com.luohuo.flex.im.core.tenant.service;

import com.luohuo.flex.im.api.vo.OfficialChannelResp;

public interface EnterpriseOfficialChannelService {

    OfficialChannelResp createOfficialChannel(Long tenantId, String tenantName);

    OfficialChannelResp getOrCreateOfficialChannel(Long tenantId, String tenantName);

    int migrateTenantUsersFromGlobalRoom(Long tenantId);

    int migrateAllNonDefaultTenants();

    OfficialChannelResp resolveChannelForRegister(Long tenantId);
}
