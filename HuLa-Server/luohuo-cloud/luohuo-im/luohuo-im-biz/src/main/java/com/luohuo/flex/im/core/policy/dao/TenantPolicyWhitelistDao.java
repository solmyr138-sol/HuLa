package com.luohuo.flex.im.core.policy.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohuo.flex.im.core.policy.mapper.TenantPolicyWhitelistMapper;
import com.luohuo.flex.im.domain.entity.TenantPolicyWhitelist;
import org.springframework.stereotype.Service;

@Service
public class TenantPolicyWhitelistDao extends ServiceImpl<TenantPolicyWhitelistMapper, TenantPolicyWhitelist> {

    public boolean isWhitelisted(Long tenantId, Long imUid) {
        return lambdaQuery()
                .eq(TenantPolicyWhitelist::getTenantId, tenantId)
                .eq(TenantPolicyWhitelist::getImUid, imUid)
                .exists();
    }

    public TenantPolicyWhitelist findByTenantAndUidIncludeDeleted(Long tenantId, Long imUid) {
        return baseMapper.selectByTenantAndUidIncludeDeleted(tenantId, imUid);
    }

    public boolean restore(Long id, Long operatorUid) {
        return baseMapper.restoreById(id, operatorUid) > 0;
    }
}
