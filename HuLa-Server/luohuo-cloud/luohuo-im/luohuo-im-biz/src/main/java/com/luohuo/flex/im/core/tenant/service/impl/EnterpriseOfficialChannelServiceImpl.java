package com.luohuo.flex.im.core.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.luohuo.basic.base.R;
import com.luohuo.basic.cache.repository.CachePlusOps;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.basic.tenant.core.aop.TenantIgnore;
import com.luohuo.basic.utils.ArgumentAssert;
import com.luohuo.flex.base.entity.tenant.DefTenant;
import com.luohuo.flex.base.vo.update.tenant.DefTenantOfficialChannelUpdateVO;
import com.luohuo.flex.common.constant.DefValConstants;
import com.luohuo.flex.im.api.vo.OfficialChannelResp;
import com.luohuo.flex.common.cache.PresenceCacheKeyBuilder;
import com.luohuo.flex.im.core.chat.dao.ContactDao;
import com.luohuo.flex.im.core.chat.dao.GroupMemberDao;
import com.luohuo.flex.im.core.chat.dao.RoomGroupDao;
import com.luohuo.flex.im.core.chat.service.RoomService;
import com.luohuo.flex.im.core.chat.service.cache.GroupMemberCache;
import com.luohuo.flex.im.core.chat.service.cache.RoomGroupCache;
import com.luohuo.flex.im.core.tenant.service.EnterpriseOfficialChannelService;
import com.luohuo.flex.im.core.user.dao.UserDao;
import com.luohuo.flex.im.domain.entity.GroupMember;
import com.luohuo.flex.im.domain.entity.RoomGroup;
import com.luohuo.flex.im.domain.entity.User;
import com.luohuo.flex.im.domain.enums.GroupRoleEnum;
import com.luohuo.flex.im.api.DefTenantApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseOfficialChannelServiceImpl implements EnterpriseOfficialChannelService {

    private static final String OFFICIAL_SUFFIX = "官方频道";

    private final RoomService roomService;
    private final RoomGroupDao roomGroupDao;
    private final GroupMemberDao groupMemberDao;
    private final UserDao userDao;
    private final ContactDao contactDao;
    private final CachePlusOps cachePlusOps;
    private final GroupMemberCache groupMemberCache;
    private final RoomGroupCache roomGroupCache;
    private final DefTenantApi defTenantApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TenantIgnore
    public OfficialChannelResp createOfficialChannel(Long tenantId, String tenantName) {
        ArgumentAssert.notNull(tenantId, "租户ID不能为空");
        ArgumentAssert.isFalse(DefValConstants.DEF_TENANT_ID.equals(tenantId), "默认租户不创建企业官方频道");
        ArgumentAssert.notEmpty(tenantName, "企业名称不能为空");

        Long previousTenant = ContextUtil.getTenantId();
        try {
            ContextUtil.setTenantId(tenantId);
            String channelName = buildChannelName(tenantName);
            RoomGroup roomGroup = roomService.createEnterpriseOfficialGroupRoom(DefValConstants.DEF_BOT_ID, channelName);
            OfficialChannelResp resp = new OfficialChannelResp(roomGroup.getRoomId(), roomGroup.getId());
            persistTenantOfficialChannel(tenantId, resp);
            log.info("已创建企业官方频道 tenantId={} roomId={} groupId={} name={}",
                    tenantId, resp.getRoomId(), resp.getGroupId(), channelName);
            return resp;
        } finally {
            ContextUtil.setTenantId(previousTenant);
        }
    }

    @Override
    @TenantIgnore
    public OfficialChannelResp getOrCreateOfficialChannel(Long tenantId, String tenantName) {
        if (DefValConstants.DEF_TENANT_ID.equals(tenantId)) {
            return new OfficialChannelResp(DefValConstants.DEF_ROOM_ID, DefValConstants.DEF_GROUP_ID);
        }
        R<DefTenant> tenantR = defTenantApi.getById(tenantId);
        ArgumentAssert.isTrue(tenantR != null && tenantR.getData() != null, "企业不存在");
        DefTenant tenant = tenantR.getData();
        if (tenant.getOfficialRoomId() != null && tenant.getOfficialGroupId() != null) {
            return new OfficialChannelResp(tenant.getOfficialRoomId(), tenant.getOfficialGroupId());
        }
        String name = StrUtil.blankToDefault(tenantName, tenant.getName());
        return createOfficialChannel(tenantId, name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TenantIgnore
    public int migrateTenantUsersFromGlobalRoom(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "租户ID不能为空");
        ArgumentAssert.isFalse(DefValConstants.DEF_TENANT_ID.equals(tenantId), "默认租户无需迁移");

        R<DefTenant> tenantR = defTenantApi.getById(tenantId);
        ArgumentAssert.isTrue(tenantR != null && tenantR.getData() != null, "企业不存在");
        DefTenant tenant = tenantR.getData();

        OfficialChannelResp channel = getOrCreateOfficialChannel(tenantId, tenant.getName());
        List<User> users = userDao.lambdaQuery().eq(User::getTenantId, tenantId).list();
        int moved = 0;
        for (User user : users) {
            removeFromGlobalOfficialRoom(user.getId());
            joinOfficialChannel(channel.getRoomId(), channel.getGroupId(), user.getId());
            moved++;
        }
        log.info("企业 {} 已从全局官方群迁移 {} 名用户至官方频道 roomId={}", tenantId, moved, channel.getRoomId());
        return moved;
    }

    @Override
    @TenantIgnore
    public int migrateAllNonDefaultTenants() {
        int total = 0;
        List<User> foreignMembers = userDao.lambdaQuery()
                .in(User::getId, groupMemberDao.getMemberUidList(DefValConstants.DEF_GROUP_ID, null))
                .ne(User::getTenantId, DefValConstants.DEF_TENANT_ID)
                .list();
        List<Long> tenantIds = foreignMembers.stream().map(User::getTenantId).distinct().toList();
        for (Long tenantId : tenantIds) {
            total += migrateTenantUsersFromGlobalRoom(tenantId);
        }
        return total;
    }

    @Override
    public OfficialChannelResp resolveChannelForRegister(Long tenantId) {
        if (DefValConstants.DEF_TENANT_ID.equals(tenantId)) {
            return new OfficialChannelResp(DefValConstants.DEF_ROOM_ID, DefValConstants.DEF_GROUP_ID);
        }
        R<DefTenant> tenantR = defTenantApi.getById(tenantId);
        if (tenantR == null || tenantR.getData() == null) {
            log.warn("注册时未找到企业 tenantId={}，回退全局官方群", tenantId);
            return new OfficialChannelResp(DefValConstants.DEF_ROOM_ID, DefValConstants.DEF_GROUP_ID);
        }
        DefTenant tenant = tenantR.getData();
        if (tenant.getOfficialRoomId() != null && tenant.getOfficialGroupId() != null) {
            return new OfficialChannelResp(tenant.getOfficialRoomId(), tenant.getOfficialGroupId());
        }
        return createOfficialChannel(tenantId, tenant.getName());
    }

    private void persistTenantOfficialChannel(Long tenantId, OfficialChannelResp resp) {
        DefTenantOfficialChannelUpdateVO vo = new DefTenantOfficialChannelUpdateVO();
        vo.setOfficialRoomId(resp.getRoomId());
        vo.setOfficialGroupId(resp.getGroupId());
        R<Boolean> updated = defTenantApi.updateOfficialChannel(tenantId, vo);
        ArgumentAssert.isTrue(updated != null && Boolean.TRUE.equals(updated.getData()), "保存企业官方频道ID失败");
    }

    private String buildChannelName(String tenantName) {
        String trimmed = StrUtil.trim(tenantName);
        if (StrUtil.isBlank(trimmed)) {
            return "企业" + OFFICIAL_SUFFIX;
        }
        if (trimmed.endsWith(OFFICIAL_SUFFIX)) {
            return trimmed;
        }
        return trimmed + OFFICIAL_SUFFIX;
    }

    private boolean isMemberOfRoom(Long uid, Long roomId) {
        RoomGroup roomGroup = roomGroupDao.getByRoomId(roomId);
        if (roomGroup == null) {
            return false;
        }
        GroupMember member = groupMemberDao.getMemberByGroupId(roomGroup.getId(), uid);
        return member != null;
    }

    private boolean removeFromGlobalOfficialRoom(Long uid) {
        if (!isMemberOfRoom(uid, DefValConstants.DEF_ROOM_ID)) {
            return false;
        }
        groupMemberDao.lambdaUpdate()
                .eq(GroupMember::getGroupId, DefValConstants.DEF_GROUP_ID)
                .eq(GroupMember::getUid, uid)
                .remove();
        contactDao.removeByRoomId(DefValConstants.DEF_ROOM_ID, List.of(uid));
        cachePlusOps.sRem(PresenceCacheKeyBuilder.groupMembersKey(DefValConstants.DEF_ROOM_ID), uid);
        cachePlusOps.sRem(PresenceCacheKeyBuilder.userGroupsKey(uid), DefValConstants.DEF_ROOM_ID);
        groupMemberCache.evictMemberList(DefValConstants.DEF_ROOM_ID);
        groupMemberCache.evictExceptMemberList(DefValConstants.DEF_ROOM_ID);
        return true;
    }

    private void joinOfficialChannel(Long roomId, Long groupId, Long uid) {
        if (isMemberOfRoom(uid, roomId)) {
            return;
        }
        roomService.createGroupMember(groupId, uid);
        contactDao.refreshOrCreate(roomId, uid);
        cachePlusOps.sAdd(PresenceCacheKeyBuilder.groupMembersKey(roomId), uid);
        cachePlusOps.sAdd(PresenceCacheKeyBuilder.userGroupsKey(uid), roomId);
        groupMemberCache.evictMemberList(roomId);
    }
}
