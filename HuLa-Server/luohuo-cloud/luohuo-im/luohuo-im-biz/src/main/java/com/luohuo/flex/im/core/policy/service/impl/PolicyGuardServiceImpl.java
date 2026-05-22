package com.luohuo.flex.im.core.policy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.basic.validator.utils.AssertUtil;
import com.luohuo.flex.im.core.chat.service.cache.GroupMemberCache;
import com.luohuo.flex.im.core.policy.dao.GroupMemberAclDao;
import com.luohuo.flex.im.core.policy.dao.GroupPolicyDao;
import com.luohuo.flex.im.core.policy.dao.TenantPolicyDao;
import com.luohuo.flex.im.core.policy.dao.TenantPolicyWhitelistDao;
import com.luohuo.flex.im.core.policy.service.PolicyGuardService;
import com.luohuo.flex.im.core.user.dao.UserDao;
import com.luohuo.flex.im.domain.entity.GroupMember;
import com.luohuo.flex.im.domain.entity.GroupMemberAcl;
import com.luohuo.flex.im.domain.entity.GroupPolicy;
import com.luohuo.flex.im.domain.entity.Message;
import com.luohuo.flex.im.domain.entity.TenantPolicy;
import com.luohuo.flex.im.domain.entity.User;
import com.luohuo.flex.im.domain.entity.msg.TextMsgReq;
import com.luohuo.flex.im.domain.enums.GroupJoinModeEnum;
import com.luohuo.flex.im.domain.enums.GroupRoleEnum;
import com.luohuo.flex.im.domain.enums.MessageTypeEnum;
import com.luohuo.flex.im.domain.enums.SpeakIntervalSecEnum;
import com.luohuo.flex.im.domain.vo.request.ChatMessageReq;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class PolicyGuardServiceImpl implements PolicyGuardService {

    private static final Long AT_ALL_UID = 0L;
    private static final String SPEAK_INTERVAL_KEY = "im:group:speak:";

    @Resource
    private TenantPolicyDao tenantPolicyDao;
    @Resource
    private TenantPolicyWhitelistDao tenantPolicyWhitelistDao;
    @Resource
    private GroupPolicyDao groupPolicyDao;
    @Resource
    private GroupMemberAclDao groupMemberAclDao;
    @Resource
    private UserDao userDao;
    @Resource
    private GroupMemberCache groupMemberCache;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public TenantPolicy getTenantPolicy(Long tenantId) {
        TenantPolicy policy = tenantPolicyDao.getByTenantId(tenantId);
        if (policy != null) {
            return policy;
        }
        return TenantPolicy.builder()
                .tenantId(tenantId)
                .allowCrossTenantFriend(false)
                .allowCrossTenantGroupInvite(false)
                .forbidCreateGroup(false)
                .forbidBroadcast(false)
                .forbidMemberAddFriend(false)
                .build();
    }

    @Override
    public GroupPolicy getGroupPolicy(Long roomId) {
        GroupPolicy policy = groupPolicyDao.getByRoomId(roomId);
        if (policy != null) {
            return policy;
        }
        GroupPolicy created = GroupPolicy.builder()
                .roomId(roomId)
                .joinMode(GroupJoinModeEnum.OPEN.getType())
                .historyVisibleToNew(true)
                .groupMuteAll(false)
                .allowMemberAddFriend(true)
                .allowMemberDm(true)
                .allowMemberChangeNickname(true)
                .speakIntervalSec(SpeakIntervalSecEnum.UNLIMITED.getSeconds())
                .tenantId(ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L))
                .build();
        groupPolicyDao.save(created);
        return created;
    }

    @Override
    public GroupMemberAcl getMemberAcl(Long roomId, Long uid) {
        return groupMemberAclDao.getByGroupIdAndUid(roomId, uid);
    }

    @Override
    public void assertSameTenant(Long uid1, Long uid2) {
        User user1 = userDao.getById(uid1);
        User user2 = userDao.getById(uid2);
        AssertUtil.isNotEmpty(user1, "用户不存在");
        AssertUtil.isNotEmpty(user2, "用户不存在");
        AssertUtil.isTrue(Objects.equals(user1.getTenantId(), user2.getTenantId()), "不允许跨租户操作");
    }

    @Override
    public void assertCanAddFriend(Long uid, Long targetUid) {
        User user = userDao.getById(uid);
        User target = userDao.getById(targetUid);
        AssertUtil.isNotEmpty(user, "用户不存在");
        AssertUtil.isNotEmpty(target, "用户不存在");
        if (isPolicyWhitelisted(user.getTenantId(), uid)) {
            return;
        }
        TenantPolicy policy = getTenantPolicy(user.getTenantId());
        AssertUtil.isFalse(Boolean.TRUE.equals(policy.getForbidMemberAddFriend()), "租户已禁止成员互加好友");
        if (!Objects.equals(user.getTenantId(), target.getTenantId())) {
            AssertUtil.isTrue(Boolean.TRUE.equals(policy.getAllowCrossTenantFriend()), "不允许跨租户加好友");
        }
    }

    @Override
    public void assertCanInviteToGroup(Long inviterUid, Long roomId, List<Long> inviteeUids) {
        GroupMember inviter = groupMemberCache.getMemberDetail(roomId, inviterUid);
        AssertUtil.isNotEmpty(inviter, "您不是群成员");
        GroupPolicy groupPolicy = getGroupPolicy(roomId);
        GroupJoinModeEnum joinMode = GroupJoinModeEnum.of(groupPolicy.getJoinMode());
        if (joinMode == GroupJoinModeEnum.ADMIN_ONLY) {
            AssertUtil.isTrue(isGroupAdmin(inviter), "仅管理员可邀请成员");
        } else if (joinMode == GroupJoinModeEnum.MEMBER_INVITE) {
            AssertUtil.isTrue(isGroupMember(inviter), "您不是群成员");
        }
        if (CollUtil.isEmpty(inviteeUids)) {
            return;
        }
        User inviterUser = userDao.getById(inviterUid);
        TenantPolicy tenantPolicy = getTenantPolicy(inviterUser.getTenantId());
        for (Long inviteeUid : inviteeUids) {
            User invitee = userDao.getById(inviteeUid);
            AssertUtil.isNotEmpty(invitee, "被邀请用户不存在");
            if (!Objects.equals(inviterUser.getTenantId(), invitee.getTenantId())) {
                AssertUtil.isTrue(Boolean.TRUE.equals(tenantPolicy.getAllowCrossTenantGroupInvite()), "不允许跨租户群邀请");
            }
        }
    }

    @Override
    public void assertCanSendGroupMessage(Long uid, Long roomId, ChatMessageReq req) {
        if (req != null && req.isSkip()) {
            return;
        }
        GroupMember member = groupMemberCache.getMemberDetail(roomId, uid);
        AssertUtil.isNotEmpty(member, "您不是群成员");
        GroupPolicy groupPolicy = getGroupPolicy(roomId);
        boolean admin = isGroupAdmin(member);
        if (Boolean.TRUE.equals(groupPolicy.getGroupMuteAll()) && !admin) {
            AssertUtil.isTrue(false, "全员禁言中");
        }
        GroupMemberAcl acl = getMemberAcl(roomId, uid);
        if (acl != null && acl.getMutedUntil() != null && acl.getMutedUntil().isAfter(LocalDateTime.now())) {
            AssertUtil.isTrue(false, "您已被禁言");
        }
        Integer intervalSec = groupPolicy.getSpeakIntervalSec();
        if (!admin && intervalSec != null && intervalSec > 0 && SpeakIntervalSecEnum.isValid(intervalSec)) {
            String key = SPEAK_INTERVAL_KEY + roomId + ":" + uid;
            Boolean allowed = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(intervalSec));
            AssertUtil.isTrue(Boolean.TRUE.equals(allowed), "发言过于频繁，请稍后再试");
        }
        if (req != null) {
            assertCanBroadcast(uid, req);
        }
    }

    @Override
    public void assertCanBroadcast(Long uid) {
        User user = userDao.getById(uid);
        AssertUtil.isNotEmpty(user, "用户不存在");
        if (isPolicyWhitelisted(user.getTenantId(), uid)) {
            return;
        }
        TenantPolicy policy = getTenantPolicy(user.getTenantId());
        AssertUtil.isFalse(Boolean.TRUE.equals(policy.getForbidBroadcast()), "租户已禁止群发操作");
    }

    @Override
    public void assertCanBroadcast(Long uid, ChatMessageReq req) {
        assertCanBroadcast(uid);
        if (req == null) {
            return;
        }
        if (req.isPushMessage()) {
            AssertUtil.isTrue(false, "租户已禁止系统通知类群发");
        }
        Integer msgType = req.getMsgType();
        if (MessageTypeEnum.SYSTEM.getType().equals(msgType) || MessageTypeEnum.NOTICE.getType().equals(msgType)) {
            AssertUtil.isTrue(false, "租户已禁止系统通知类群发");
        }
        if (containsAtAll(req)) {
            AssertUtil.isTrue(false, "租户已禁止@所有人");
        }
    }

    @Override
    public void assertCanCreateGroup(Long uid) {
        User user = userDao.getById(uid);
        AssertUtil.isNotEmpty(user, "用户不存在");
        if (isPolicyWhitelisted(user.getTenantId(), uid)) {
            return;
        }
        TenantPolicy policy = getTenantPolicy(user.getTenantId());
        AssertUtil.isFalse(Boolean.TRUE.equals(policy.getForbidCreateGroup()), "租户已禁止创建群聊");
    }

    private boolean isPolicyWhitelisted(Long tenantId, Long uid) {
        return tenantPolicyWhitelistDao.isWhitelisted(tenantId, uid);
    }

    @Override
    public void assertCanEditMessage(Long uid, Message msg) {
        AssertUtil.isNotEmpty(msg, "消息有误");
        GroupMemberAcl acl = getMemberAcl(msg.getRoomId(), uid);
        if (acl != null && Boolean.TRUE.equals(acl.getCanEditAnyMessage())) {
            return;
        }
        AssertUtil.isTrue(Objects.equals(uid, msg.getFromUid()), "抱歉,您没有权限");
    }

    @Override
    public void assertCanRecallMessage(Long uid, Message msg) {
        AssertUtil.isNotEmpty(msg, "消息有误");
        AssertUtil.notEqual(msg.getType(), MessageTypeEnum.RECALL.getType(), "消息无法撤回");
        GroupMemberAcl acl = getMemberAcl(msg.getRoomId(), uid);
        if (acl != null && Boolean.TRUE.equals(acl.getCanRecallAnyMessage())) {
            return;
        }
        GroupMember member = groupMemberCache.getMemberDetail(msg.getRoomId(), uid);
        if (member != null && isGroupAdmin(member)) {
            return;
        }
        boolean self = Objects.equals(uid, msg.getFromUid());
        AssertUtil.isTrue(self, "抱歉,您没有权限");
        long between = Duration.between(msg.getCreateTime(), LocalDateTime.now()).toMinutes();
        AssertUtil.isTrue(between < 2, "超过2分钟的消息不能撤回");
    }

    private boolean isGroupAdmin(GroupMember member) {
        return GroupRoleEnum.LEADER.getType().equals(member.getRoleId())
                || GroupRoleEnum.MANAGER.getType().equals(member.getRoleId());
    }

    private boolean isGroupMember(GroupMember member) {
        return member != null;
    }

    private boolean containsAtAll(ChatMessageReq req) {
        if (MessageTypeEnum.TEXT.getType().equals(req.getMsgType()) && req.getBody() != null) {
            TextMsgReq text = BeanUtil.toBean(req.getBody(), TextMsgReq.class);
            return CollUtil.isNotEmpty(text.getAtUidList()) && text.getAtUidList().contains(AT_ALL_UID);
        }
        if (MessageTypeEnum.AIT.getType().equals(req.getMsgType()) && req.getBody() != null) {
            TextMsgReq text = BeanUtil.toBean(req.getBody(), TextMsgReq.class);
            return CollUtil.isNotEmpty(text.getAtUidList()) && text.getAtUidList().contains(AT_ALL_UID);
        }
        return false;
    }
}
