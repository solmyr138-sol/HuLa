package com.luohuo.flex.im.api.hystrix;

import com.luohuo.basic.exception.BizException;
import com.luohuo.flex.base.vo.update.tenant.DefUserPasswordResetVO;
import org.springframework.stereotype.Component;
import com.luohuo.basic.base.R;
import com.luohuo.flex.model.entity.system.SysUser;
import com.luohuo.flex.model.vo.result.UserQuery;
import com.luohuo.flex.im.api.DefUserApi;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户API熔断
 *
 * @author 乾乾
 * @date 2019/07/23
 */
@Component
public class DefUserApiFallback implements DefUserApi {
    @Override
    public R<List<Long>> findAllUserId() {
        return R.timeout();
    }

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return Map.of();
    }

    @Override
    public R<SysUser> getById(UserQuery userQuery) {
        return R.timeout();
    }

    @Override
    public R<Boolean> logout(Long userId, String token) {
        return R.timeout();
    }

    @Override
    public R<Boolean> resetPassword(DefUserPasswordResetVO data) {
        throw BizException.wrap("重置密码失败，base 服务不可用");
    }

    @Override
    public R<Boolean> updateState(Long id, Boolean state) {
        throw BizException.wrap("更新用户状态失败，base 服务不可用");
    }
}
