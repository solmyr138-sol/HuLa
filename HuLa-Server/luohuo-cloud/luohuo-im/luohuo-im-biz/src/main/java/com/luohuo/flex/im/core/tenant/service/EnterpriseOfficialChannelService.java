package com.luohuo.flex.im.core.tenant.service;

import com.luohuo.flex.im.api.vo.OfficialChannelResp;

public interface EnterpriseOfficialChannelService {

    OfficialChannelResp createOfficialChannel(Long tenantId, String tenantName);

    OfficialChannelResp getOrCreateOfficialChannel(Long tenantId, String tenantName);

    int migrateTenantUsersFromGlobalRoom(Long tenantId);

    int migrateAllNonDefaultTenants();

    OfficialChannelResp resolveChannelForRegister(Long tenantId);

    void ensureUserInOfficialChannel(Long tenantId, Long uid);

    /**
     * 将租户下所有用户补齐到企业官方频道，并清理重复创建的官方频道（仅保留一个）。
     * @return 清理结果（迁移人数、删除频道数等）
     */
    OfficialChannelCleanupResult migrateUsersAndCleanupDuplicates(Long tenantId);
}
