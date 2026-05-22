package com.luohuo.flex.im.core.policy.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohuo.flex.im.core.policy.mapper.GroupPolicyMapper;
import com.luohuo.flex.im.domain.entity.GroupPolicy;
import org.springframework.stereotype.Service;

@Service
public class GroupPolicyDao extends ServiceImpl<GroupPolicyMapper, GroupPolicy> {

    public GroupPolicy getByRoomId(Long roomId) {
        return lambdaQuery().eq(GroupPolicy::getRoomId, roomId).one();
    }
}
