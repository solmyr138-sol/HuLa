package com.luohuo.flex.im.core.policy.service;

import com.luohuo.flex.im.domain.vo.req.policy.TenantPolicyWhitelistAddReq;
import com.luohuo.flex.im.domain.vo.resp.policy.TenantPolicyWhitelistResp;

import java.util.List;

public interface TenantPolicyWhitelistService {

    List<TenantPolicyWhitelistResp> listCurrent();

    void add(Long operatorUid, TenantPolicyWhitelistAddReq req);

    void remove(Long operatorUid, Long id);
}
