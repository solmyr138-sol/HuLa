package com.luohuo.flex.im.core.policy.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohuo.flex.im.core.policy.mapper.GroupMemberAclMapper;
import com.luohuo.flex.im.domain.entity.GroupMemberAcl;
import org.springframework.stereotype.Service;

@Service
public class GroupMemberAclDao extends ServiceImpl<GroupMemberAclMapper, GroupMemberAcl> {

    public GroupMemberAcl getByGroupIdAndUid(Long groupId, Long uid) {
        return lambdaQuery()
                .eq(GroupMemberAcl::getGroupId, groupId)
                .eq(GroupMemberAcl::getUid, uid)
                .one();
    }
}
