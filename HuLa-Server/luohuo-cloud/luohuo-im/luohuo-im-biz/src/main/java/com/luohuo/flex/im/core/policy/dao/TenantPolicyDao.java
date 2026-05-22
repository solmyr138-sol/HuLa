package com.luohuo.flex.im.core.policy.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohuo.flex.im.core.policy.mapper.TenantPolicyMapper;
import com.luohuo.flex.im.domain.entity.TenantPolicy;
import org.springframework.stereotype.Service;

@Service
public class TenantPolicyDao extends ServiceImpl<TenantPolicyMapper, TenantPolicy> {

    public TenantPolicy getByTenantId(Long tenantId) {
        return lambdaQuery().eq(TenantPolicy::getTenantId, tenantId).one();
    }
}
