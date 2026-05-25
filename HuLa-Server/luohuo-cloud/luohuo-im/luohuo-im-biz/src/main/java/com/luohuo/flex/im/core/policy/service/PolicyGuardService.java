package com.luohuo.flex.im.core.policy.service;

import com.luohuo.flex.im.domain.entity.GroupMemberAcl;
import com.luohuo.flex.im.domain.entity.GroupPolicy;
import com.luohuo.flex.im.domain.entity.Message;
import com.luohuo.flex.im.domain.entity.TenantPolicy;
import com.luohuo.flex.im.domain.vo.request.ChatMessageReq;

import java.util.List;

/**
 * 租户/群策略守卫
 */
public interface PolicyGuardService {

    TenantPolicy getTenantPolicy(Long tenantId);

    GroupPolicy getGroupPolicy(Long roomId);

    GroupMemberAcl getMemberAcl(Long roomId, Long uid);

    void assertSameTenant(Long uid1, Long uid2);

    void assertCanAddFriend(Long uid, Long targetUid);

    void assertCanInviteToGroup(Long inviterUid, Long roomId, List<Long> inviteeUids);

    void assertCanSendGroupMessage(Long uid, Long roomId, ChatMessageReq req);

    void assertCanBroadcast(Long uid);

    void assertCanCreateGroup(Long uid);

    void assertCanEditMessage(Long uid, Message msg);

    void assertCanRecallMessage(Long uid, Message msg);
}
