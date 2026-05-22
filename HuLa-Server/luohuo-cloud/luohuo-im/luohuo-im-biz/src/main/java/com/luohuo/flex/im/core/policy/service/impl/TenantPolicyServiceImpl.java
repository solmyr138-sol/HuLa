package com.luohuo.flex.im.core.policy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.flex.im.core.policy.dao.TenantPolicyDao;
import com.luohuo.flex.im.core.policy.service.PolicyGuardService;
import com.luohuo.flex.im.core.policy.service.TenantPolicyService;
import com.luohuo.flex.im.domain.entity.TenantPolicy;
import com.luohuo.flex.im.domain.vo.req.policy.TenantPolicyUpdateReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TenantPolicyServiceImpl implements TenantPolicyService {

    @Resource
    private TenantPolicyDao tenantPolicyDao;
    @Resource
    private PolicyGuardService policyGuardService;

    @Override
    public TenantPolicy getCurrentTenantPolicy() {
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        return policyGuardService.getTenantPolicy(tenantId);
    }

    @Override
    public TenantPolicy updateCurrentTenantPolicy(Long operatorUid, TenantPolicyUpdateReq req) {
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        TenantPolicy policy = tenantPolicyDao.getByTenantId(tenantId);
        if (policy == null) {
            policy = policyGuardService.getTenantPolicy(tenantId);
            policy.setTenantId(tenantId);
            policy.setCreateBy(operatorUid);
            tenantPolicyDao.save(policy);
        }
        if (req.getAllowCrossTenantFriend() != null) {
            policy.setAllowCrossTenantFriend(req.getAllowCrossTenantFriend());
        }
        if (req.getAllowCrossTenantGroupInvite() != null) {
            policy.setAllowCrossTenantGroupInvite(req.getAllowCrossTenantGroupInvite());
        }
        if (req.getForbidCreateGroup() != null) {
            policy.setForbidCreateGroup(req.getForbidCreateGroup());
        }
        if (req.getForbidBroadcast() != null) {
            policy.setForbidBroadcast(req.getForbidBroadcast());
        }
        if (req.getForbidMemberAddFriend() != null) {
            policy.setForbidMemberAddFriend(req.getForbidMemberAddFriend());
        }
        policy.setUpdateBy(operatorUid);
        tenantPolicyDao.updateById(policy);
        return policy;
    }
}
