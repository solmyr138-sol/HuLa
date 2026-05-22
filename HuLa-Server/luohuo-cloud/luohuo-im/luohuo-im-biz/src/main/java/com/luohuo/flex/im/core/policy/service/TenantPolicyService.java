package com.luohuo.flex.im.core.policy.service;

import com.luohuo.flex.im.domain.entity.TenantPolicy;
import com.luohuo.flex.im.domain.vo.req.policy.TenantPolicyUpdateReq;

/**
 * 租户策略管理
 */
public interface TenantPolicyService {

    TenantPolicy getCurrentTenantPolicy();

    TenantPolicy updateCurrentTenantPolicy(Long operatorUid, TenantPolicyUpdateReq req);
}
