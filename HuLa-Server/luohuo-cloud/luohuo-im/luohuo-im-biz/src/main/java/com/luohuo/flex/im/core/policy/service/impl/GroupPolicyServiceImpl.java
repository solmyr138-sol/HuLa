package com.luohuo.flex.im.core.policy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.basic.validator.utils.AssertUtil;
import com.luohuo.flex.im.core.chat.service.cache.GroupMemberCache;
import com.luohuo.flex.im.core.policy.dao.GroupMemberAclDao;
import com.luohuo.flex.im.core.policy.dao.GroupPolicyDao;
import com.luohuo.flex.im.core.policy.service.GroupPolicyService;
import com.luohuo.flex.im.core.policy.service.PolicyGuardService;
import com.luohuo.flex.im.domain.entity.GroupMember;
import com.luohuo.flex.im.domain.entity.GroupMemberAcl;
import com.luohuo.flex.im.domain.entity.GroupPolicy;
import com.luohuo.flex.im.domain.enums.GroupRoleEnum;
import com.luohuo.flex.im.domain.enums.SpeakIntervalSecEnum;
import com.luohuo.flex.im.domain.vo.req.policy.GroupMemberAclUpdateReq;
import com.luohuo.flex.im.domain.vo.req.policy.GroupMemberMuteReq;
import com.luohuo.flex.im.domain.vo.req.policy.GroupPolicyUpdateReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class GroupPolicyServiceImpl implements GroupPolicyService {

    @Resource
    private GroupPolicyDao groupPolicyDao;
    @Resource
    private GroupMemberAclDao groupMemberAclDao;
    @Resource
    private GroupMemberCache groupMemberCache;
    @Resource
    private PolicyGuardService policyGuardService;

    @Override
    public GroupPolicy getPolicy(Long roomId) {
        return policyGuardService.getGroupPolicy(roomId);
    }

    @Override
    public GroupPolicy updatePolicy(Long operatorUid, GroupPolicyUpdateReq req) {
        assertGroupAdmin(operatorUid, req.getRoomId());
        if (req.getSpeakIntervalSec() != null) {
            AssertUtil.isTrue(SpeakIntervalSecEnum.isValid(req.getSpeakIntervalSec()), "发言间隔不合法");
        }
        GroupPolicy policy = policyGuardService.getGroupPolicy(req.getRoomId());
        if (req.getJoinMode() != null) {
            policy.setJoinMode(req.getJoinMode());
        }
        if (req.getHistoryVisibleToNew() != null) {
            policy.setHistoryVisibleToNew(req.getHistoryVisibleToNew());
        }
        if (req.getGroupMuteAll() != null) {
            policy.setGroupMuteAll(req.getGroupMuteAll());
        }
        if (req.getAllowMemberAddFriend() != null) {
            policy.setAllowMemberAddFriend(req.getAllowMemberAddFriend());
        }
        if (req.getAllowMemberDm() != null) {
            policy.setAllowMemberDm(req.getAllowMemberDm());
        }
        if (req.getAllowMemberChangeNickname() != null) {
            policy.setAllowMemberChangeNickname(req.getAllowMemberChangeNickname());
        }
        if (req.getSpeakIntervalSec() != null) {
            policy.setSpeakIntervalSec(req.getSpeakIntervalSec());
        }
        policy.setUpdateBy(operatorUid);
        groupPolicyDao.updateById(policy);
        return policy;
    }

    @Override
    public GroupMemberAcl updateMemberMute(Long operatorUid, GroupMemberMuteReq req) {
        assertGroupAdmin(operatorUid, req.getRoomId());
        GroupMemberAcl acl = groupMemberAclDao.getByGroupIdAndUid(req.getRoomId(), req.getUid());
        if (acl == null) {
            acl = GroupMemberAcl.builder()
                    .groupId(req.getRoomId())
                    .uid(req.getUid())
                    .canEditAnyMessage(false)
                    .canRecallAnyMessage(false)
                    .mutedUntil(req.getMutedUntil())
                    .build();
            acl.setTenantId(ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L));
            acl.setCreateBy(operatorUid);
            groupMemberAclDao.save(acl);
            return acl;
        }
        acl.setMutedUntil(req.getMutedUntil());
        acl.setUpdateBy(operatorUid);
        groupMemberAclDao.updateById(acl);
        return acl;
    }

    @Override
    public GroupMemberAcl updateMemberAcl(Long operatorUid, GroupMemberAclUpdateReq req) {
        assertGroupAdmin(operatorUid, req.getRoomId());
        GroupMemberAcl acl = groupMemberAclDao.getByGroupIdAndUid(req.getRoomId(), req.getUid());
        if (acl == null) {
            acl = GroupMemberAcl.builder()
                    .groupId(req.getRoomId())
                    .uid(req.getUid())
                    .canEditAnyMessage(ObjectUtil.defaultIfNull(req.getCanEditAnyMessage(), false))
                    .canRecallAnyMessage(ObjectUtil.defaultIfNull(req.getCanRecallAnyMessage(), false))
                    .build();
            acl.setTenantId(ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L));
            acl.setCreateBy(operatorUid);
            groupMemberAclDao.save(acl);
            return acl;
        }
        if (req.getCanEditAnyMessage() != null) {
            acl.setCanEditAnyMessage(req.getCanEditAnyMessage());
        }
        if (req.getCanRecallAnyMessage() != null) {
            acl.setCanRecallAnyMessage(req.getCanRecallAnyMessage());
        }
        acl.setUpdateBy(operatorUid);
        groupMemberAclDao.updateById(acl);
        return acl;
    }

    private void assertGroupAdmin(Long operatorUid, Long roomId) {
        GroupMember member = groupMemberCache.getMemberDetail(roomId, operatorUid);
        AssertUtil.isNotEmpty(member, "您不是群成员");
        boolean admin = GroupRoleEnum.LEADER.getType().equals(member.getRoleId())
                || GroupRoleEnum.MANAGER.getType().equals(member.getRoleId());
        AssertUtil.isTrue(admin, "仅群主或管理员可操作");
    }
}
