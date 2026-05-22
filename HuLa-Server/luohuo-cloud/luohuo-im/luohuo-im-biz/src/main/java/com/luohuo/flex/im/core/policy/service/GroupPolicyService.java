package com.luohuo.flex.im.core.policy.service;

import com.luohuo.flex.im.domain.entity.GroupMemberAcl;
import com.luohuo.flex.im.domain.entity.GroupPolicy;
import com.luohuo.flex.im.domain.vo.req.policy.GroupMemberAclUpdateReq;
import com.luohuo.flex.im.domain.vo.req.policy.GroupMemberMuteReq;
import com.luohuo.flex.im.domain.vo.req.policy.GroupPolicyUpdateReq;

/**
 * 群策略与成员 ACL 管理
 */
public interface GroupPolicyService {

    GroupPolicy getPolicy(Long roomId);

    GroupPolicy updatePolicy(Long operatorUid, GroupPolicyUpdateReq req);

    GroupMemberAcl updateMemberMute(Long operatorUid, GroupMemberMuteReq req);

    GroupMemberAcl updateMemberAcl(Long operatorUid, GroupMemberAclUpdateReq req);
}
