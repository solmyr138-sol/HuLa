package com.luohuo.flex.base.mapper.application;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.luohuo.basic.base.mapper.SuperMapper;
import com.luohuo.flex.base.entity.application.DefTenantApplicationRel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * 租户的应用
 * </p>
 *
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefTenantApplicationRelMapper extends SuperMapper<DefTenantApplicationRel> {

    /**
     * 物理删除（该表无 is_del 字段，不可走逻辑删除）
     */
    @Delete("""
            <script>
            DELETE FROM def_tenant_application_rel
            WHERE tenant_id IN
            <foreach collection="tenantIds" item="id" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    int physicalDeleteByTenantIds(@Param("tenantIds") List<Long> tenantIds);
}
