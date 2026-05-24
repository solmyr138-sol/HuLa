package com.luohuo.flex.im.core.policy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luohuo.flex.im.domain.entity.TenantPolicyWhitelist;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantPolicyWhitelistMapper extends BaseMapper<TenantPolicyWhitelist> {

    /** 含已软删记录，用于避免 uk_tenant_uid 重复插入 */
    @Select("SELECT * FROM im_tenant_policy_whitelist WHERE tenant_id = #{tenantId} AND im_uid = #{imUid} LIMIT 1")
    TenantPolicyWhitelist selectByTenantAndUidIncludeDeleted(@Param("tenantId") Long tenantId, @Param("imUid") Long imUid);

    @Update("UPDATE im_tenant_policy_whitelist SET is_del = 0, create_by = #{operatorUid}, update_by = #{operatorUid}, update_time = NOW(3) WHERE id = #{id}")
    int restoreById(@Param("id") Long id, @Param("operatorUid") Long operatorUid);
}

