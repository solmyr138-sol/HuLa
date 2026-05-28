package com.luohuo.flex.im.core.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.luohuo.basic.base.R;
import com.luohuo.basic.cache.repository.CachePlusOps;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.basic.tenant.core.aop.TenantIgnore;
import com.luohuo.basic.tenant.core.util.TenantUtils;
import com.luohuo.basic.utils.ArgumentAssert;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.luohuo.flex.base.entity.tenant.DefTenant;
import com.luohuo.flex.base.vo.update.tenant.DefTenantOfficialChannelUpdateVO;
import com.luohuo.flex.common.constant.DefValConstants;
import com.luohuo.flex.im.api.vo.OfficialChannelResp;
import com.luohuo.flex.common.cache.PresenceCacheKeyBuilder;
import com.luohuo.flex.im.core.chat.dao.ContactDao;
import com.luohuo.flex.im.core.chat.dao.GroupMemberDao;
import com.luohuo.flex.im.core.chat.dao.MessageDao;
import com.luohuo.flex.im.core.chat.dao.RoomDao;
import com.luohuo.flex.im.core.chat.dao.RoomGroupDao;
import com.luohuo.flex.im.core.chat.service.RoomService;
import com.luohuo.flex.im.core.chat.service.cache.GroupMemberCache;
import com.luohuo.flex.im.core.chat.service.cache.RoomGroupCache;
import com.luohuo.flex.im.core.tenant.service.OfficialChannelCleanupResult;
import com.luohuo.flex.im.core.tenant.service.EnterpriseOfficialChannelService;
import com.luohuo.flex.im.core.user.dao.UserDao;
import com.luohuo.flex.im.domain.entity.GroupMember;
import com.luohuo.flex.im.domain.entity.Room;
import com.luohuo.flex.im.domain.entity.RoomGroup;
import com.luohuo.flex.im.domain.entity.User;
import com.luohuo.flex.im.domain.enums.GroupRoleEnum;
import com.luohuo.flex.im.api.DefTenantApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseOfficialChannelServiceImpl implements EnterpriseOfficialChannelService {

    private static final String OFFICIAL_SUFFIX = "官方频道";

    private final RoomService roomService;
    private final RoomDao roomDao;
    private final RoomGroupDao roomGroupDao;
    private final GroupMemberDao groupMemberDao;
    private final UserDao userDao;
    private final ContactDao contactDao;
    private final CachePlusOps cachePlusOps;
    private final GroupMemberCache groupMemberCache;
    private final RoomGroupCache roomGroupCache;
    private final DefTenantApi defTenantApi;
    private final MessageDao messageDao;

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
            // 避免重复创建：同名官方频道已存在则复用最新的一条
            RoomGroup existed = findLatestOfficialChannel(tenantId, channelName);
            if (existed != null) {
                OfficialChannelResp resp = new OfficialChannelResp(existed.getRoomId(), existed.getId());
                persistTenantOfficialChannel(tenantId, resp);
                log.info("复用已存在的企业官方频道 tenantId={} roomId={} groupId={} name={}",
                        tenantId, resp.getRoomId(), resp.getGroupId(), channelName);
                return resp;
            }
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
            RoomGroup configured = TenantUtils.executeIgnore(() -> roomGroupDao.getById(tenant.getOfficialGroupId()));
            if (configured != null) {
                return new OfficialChannelResp(tenant.getOfficialRoomId(), tenant.getOfficialGroupId());
            }
            log.warn("租户配置的官方频道已失效 tenantId={} roomId={} groupId={}，将复用或重建",
                    tenantId, tenant.getOfficialRoomId(), tenant.getOfficialGroupId());
        }
        String name = StrUtil.blankToDefault(tenantName, tenant.getName());
        String channelName = buildChannelName(name);
        RoomGroup existed = findLatestOfficialChannel(tenantId, channelName);
        if (existed != null) {
            OfficialChannelResp resp = new OfficialChannelResp(existed.getRoomId(), existed.getId());
            persistTenantOfficialChannel(tenantId, resp);
            return resp;
        }
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
    @TenantIgnore
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
        // 注册链路需要幂等：优先复用/补齐企业官方频道（避免重复创建官方频道）
        return getOrCreateOfficialChannel(tenantId, tenant.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensureUserInOfficialChannel(Long tenantId, Long uid) {
        if (tenantId == null || uid == null || DefValConstants.DEF_TENANT_ID.equals(tenantId)) {
            return;
        }
        R<DefTenant> tenantR = defTenantApi.getById(tenantId);
        if (tenantR == null || tenantR.getData() == null) {
            log.warn("补齐官方频道成员时企业不存在 tenantId={} uid={}", tenantId, uid);
            return;
        }
        DefTenant tenant = tenantR.getData();
        OfficialChannelResp channel = getOrCreateOfficialChannel(tenantId, tenant.getName());

        Long previousTenant = ContextUtil.getTenantId();
        try {
            // 关键：写入 im_group_member 必须带上正确的 tenantId，否则后台按租户统计成员数会一直是 0
            ContextUtil.setTenantId(tenantId);
			// 历史脏数据兜底：room / room_group / group_member 的 tenant_id 可能为 null，会被租户插件过滤，导致通讯录群聊列表看不到
			TenantUtils.executeIgnore(() -> {
				roomDao.getBaseMapper().update(
						null,
						new UpdateWrapper<Room>()
								.eq("id", channel.getRoomId())
								.set("tenant_id", tenantId)
				);
				roomGroupDao.getBaseMapper().update(
						null,
						new UpdateWrapper<RoomGroup>()
								.eq("id", channel.getGroupId())
								.set("tenant_id", tenantId)
				);
				groupMemberDao.getBaseMapper().update(
						null,
						new UpdateWrapper<GroupMember>()
								.eq("group_id", channel.getGroupId())
								.eq("uid", uid)
								.set("tenant_id", tenantId)
								.set("de_friend", false)
				);
			});
            removeFromGlobalOfficialRoom(uid);
            joinOfficialChannel(channel.getRoomId(), channel.getGroupId(), uid);
        } finally {
            ContextUtil.setTenantId(previousTenant);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OfficialChannelCleanupResult migrateUsersAndCleanupDuplicates(Long tenantId) {
        ArgumentAssert.notNull(tenantId, "租户ID不能为空");
        ArgumentAssert.isFalse(DefValConstants.DEF_TENANT_ID.equals(tenantId), "默认租户无需处理");

        R<DefTenant> tenantR = defTenantApi.getById(tenantId);
        ArgumentAssert.isTrue(tenantR != null && tenantR.getData() != null, "企业不存在");
        DefTenant tenant = tenantR.getData();

        Long previousTenant = ContextUtil.getTenantId();
        try {
            // internal 清理/补齐接口没有走网关租户过滤，必须手动补上 tenantId 上下文
            ContextUtil.setTenantId(tenantId);

            // 1) 选择“最早创建”的官方频道作为保留对象（测试/清理场景），并回写到租户表
            String channelName = buildChannelName(tenant.getName());
            RoomGroup keep = findEarliestOfficialChannel(tenantId, channelName);
            OfficialChannelResp official;
            if (keep != null) {
                official = new OfficialChannelResp(keep.getRoomId(), keep.getId());
                persistTenantOfficialChannel(tenantId, official);
            } else {
                official = createOfficialChannel(tenantId, tenant.getName());
            }

            // 1.1) 历史脏数据修复：官方频道成员 tenant_id 可能为 null，导致 /im/room/group/list 被租户插件过滤看不到
            // 这里必须忽略租户插件，才能把 null tenant_id 的记录回填为当前 tenantId
            TenantUtils.executeIgnore(() -> {
                // 房间、群 tenant_id 可能也为 null（早期创建链路 tenant 上下文缺失），一起回填
                roomDao.getBaseMapper().update(
                        null,
                        new UpdateWrapper<Room>()
                                .eq("id", official.getRoomId())
                                .isNull("tenant_id")
                                .set("tenant_id", tenantId)
                );
                roomGroupDao.getBaseMapper().update(
                        null,
                        new UpdateWrapper<RoomGroup>()
                                .eq("id", official.getGroupId())
                                .isNull("tenant_id")
                                .set("tenant_id", tenantId)
                );
                groupMemberDao.getBaseMapper().update(
                        null,
                        new UpdateWrapper<GroupMember>()
                                .eq("group_id", official.getGroupId())
                                .isNull("tenant_id")
                                .set("tenant_id", tenantId)
                                .set("de_friend", false)
                );
            });

            // 2) 补齐：把当前租户所有用户确保加入官方频道（落库 + contact + cache）
            List<User> users = userDao.lambdaQuery().eq(User::getTenantId, tenantId).list();
            int ensured = 0;
            for (User user : users) {
                ensureUserInOfficialChannel(tenantId, user.getId());
                ensured++;
            }

            // 3) 清理重复的“官方频道”（同租户、同官方频道名称、排除保留的官方频道）
            List<RoomGroup> duplicates = roomGroupDao.lambdaQuery()
                    .eq(RoomGroup::getTenantId, tenantId)
                    .eq(RoomGroup::getName, channelName)
                    .ne(RoomGroup::getId, official.getGroupId())
                    .orderByDesc(RoomGroup::getCreateTime)
                    .list();

            List<Long> deletedRoomIds = new ArrayList<>();
            for (RoomGroup dup : duplicates) {
                try {
                    // 测试/清理：直接删除房间、群、成员、会话、消息，不发系统消息，避免触发跨库依赖
                    deleteRoomGroupRoomData(dup.getRoomId(), dup.getId(), dup.getAccount());
                    deletedRoomIds.add(dup.getRoomId());
                } catch (Exception e) {
                    log.warn("清理重复官方频道失败 tenantId={} roomId={} groupId={}", tenantId, dup.getRoomId(), dup.getId(), e);
                }
            }

            OfficialChannelCleanupResult result = new OfficialChannelCleanupResult();
            result.setTenantId(tenantId);
            result.setOfficialChannel(official);
            result.setEnsuredUsers(ensured);
            result.setDeletedDuplicateChannels(deletedRoomIds.size());
            result.setDeletedRoomIds(deletedRoomIds);
            return result;
        } finally {
            ContextUtil.setTenantId(previousTenant);
        }
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

    private RoomGroup findLatestOfficialChannel(Long tenantId, String channelName) {
        return roomGroupDao.lambdaQuery()
                .eq(RoomGroup::getTenantId, tenantId)
                .eq(RoomGroup::getName, channelName)
                .orderByDesc(RoomGroup::getCreateTime)
                .last("limit 1")
                .one();
    }

    private RoomGroup findEarliestOfficialChannel(Long tenantId, String channelName) {
        return roomGroupDao.lambdaQuery()
                .eq(RoomGroup::getTenantId, tenantId)
                .eq(RoomGroup::getName, channelName)
                .orderByAsc(RoomGroup::getCreateTime)
                .last("limit 1")
                .one();
    }

    private void deleteRoomGroupRoomData(Long roomId, Long groupId, String groupAccount) {
        // 1) 删除群缓存（账号搜索缓存 + roomId 缓存）
        try {
            roomGroupCache.delete(roomId);
            if (StrUtil.isNotBlank(groupAccount)) {
                roomGroupCache.evictGroup(groupAccount);
            }
        } catch (Exception ignored) {
            // best-effort
        }

        // 2) 删除会话（contact）
        contactDao.removeByRoomId(roomId, List.of());

        // 3) 删除群成员（group_member）
        groupMemberDao.removeByGroupId(groupId, List.of());
        groupMemberCache.evictMemberList(roomId);
        groupMemberCache.evictExceptMemberList(roomId);

        // 4) 删除消息（逻辑删除）
        messageDao.removeByRoomId(roomId, List.of());

        // 5) 删除 room_group + room
        roomGroupDao.removeById(groupId);
        roomService.removeById(roomId);

        // 6) presence cache
        cachePlusOps.del(PresenceCacheKeyBuilder.groupMembersKey(roomId));
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
        // 幂等保护：历史脏数据可能导致同一 (groupId, uid) 多条记录
        List<GroupMember> existed = groupMemberDao.lambdaQuery()
                .eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUid, uid)
                .orderByDesc(GroupMember::getId)
                .list();
        if (existed != null && !existed.isEmpty()) {
            if (existed.size() > 1) {
                // 保留最新一条，其余删除
                List<Long> removeIds = existed.stream().skip(1).map(GroupMember::getId).toList();
                groupMemberDao.removeByIds(removeIds);
            }
            // 通讯录-群聊列表会按 de_friend=0 过滤；历史数据 deFriend 可能为 null，需修正
            GroupMember latest = existed.get(0);
            if (latest.getDeFriend() == null || Boolean.TRUE.equals(latest.getDeFriend())) {
                latest.setDeFriend(false);
                groupMemberDao.updateById(latest);
            }
            return;
        }
        roomService.createGroupMember(groupId, uid);
        contactDao.refreshOrCreate(roomId, uid);
        cachePlusOps.sAdd(PresenceCacheKeyBuilder.groupMembersKey(roomId), uid);
        cachePlusOps.sAdd(PresenceCacheKeyBuilder.userGroupsKey(uid), roomId);
        groupMemberCache.evictMemberList(roomId);
        groupMemberCache.evictExceptMemberList(roomId);
    }
}
