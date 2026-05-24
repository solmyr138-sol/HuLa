package com.luohuo.flex.im.core.policy.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.basic.validator.utils.AssertUtil;
import com.luohuo.flex.im.core.policy.dao.TenantPolicyWhitelistDao;
import com.luohuo.flex.im.core.policy.service.TenantPolicyWhitelistService;
import com.luohuo.flex.im.core.user.dao.UserDao;
import com.luohuo.flex.im.domain.entity.TenantPolicyWhitelist;
import com.luohuo.flex.im.domain.entity.User;
import com.luohuo.flex.im.domain.vo.req.policy.TenantPolicyWhitelistAddReq;
import com.luohuo.flex.im.domain.vo.resp.policy.TenantPolicyWhitelistResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TenantPolicyWhitelistServiceImpl implements TenantPolicyWhitelistService {

    @Resource
    private TenantPolicyWhitelistDao whitelistDao;
    @Resource
    private UserDao userDao;

    @Override
    public List<TenantPolicyWhitelistResp> listCurrent() {
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        return whitelistDao.lambdaQuery()
                .eq(TenantPolicyWhitelist::getTenantId, tenantId)
                .list()
                .stream()
                .map(this::toResp)
                .collect(Collectors.toList());
    }

    @Override
    public void add(Long operatorUid, TenantPolicyWhitelistAddReq req) {
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        User user = resolveUser(req, tenantId);
        AssertUtil.isNotEmpty(user, "用户不存在，请填写注册用户列表中的账号或手机号");
        AssertUtil.isTrue(Objects.equals(user.getTenantId(), tenantId), "只能添加本企业成员");
        if (whitelistDao.isWhitelisted(tenantId, user.getId())) {
            AssertUtil.isFalse(true, "成员已在白名单中");
        }
        TenantPolicyWhitelist existing = whitelistDao.findByTenantAndUidIncludeDeleted(tenantId, user.getId());
        if (existing != null) {
            AssertUtil.isTrue(whitelistDao.restore(existing.getId(), operatorUid), "恢复白名单失败");
            return;
        }
        TenantPolicyWhitelist row = TenantPolicyWhitelist.builder()
                .imUid(user.getId())
                .build();
        row.setTenantId(tenantId);
        row.setCreateBy(operatorUid);
        whitelistDao.save(row);
    }

    @Override
    public void remove(Long operatorUid, Long id) {
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        TenantPolicyWhitelist row = whitelistDao.getById(id);
        AssertUtil.isNotEmpty(row, "记录不存在");
        AssertUtil.isTrue(Objects.equals(row.getTenantId(), tenantId), "无权操作");
        whitelistDao.removeById(id);
    }

    private User resolveUser(TenantPolicyWhitelistAddReq req, Long tenantId) {
        if (req.getImUid() != null) {
            User user = userDao.getById(req.getImUid());
            return user != null && Objects.equals(user.getTenantId(), tenantId) ? user : null;
        }
        if (StrUtil.isNotBlank(req.getAccountOrMobile())) {
            String kw = req.getAccountOrMobile().trim();
            return userDao.lambdaQuery()
                    .eq(User::getTenantId, tenantId)
                    .and(w -> w.eq(User::getAccount, kw).or().eq(User::getEmail, kw))
                    .one();
        }
        return null;
    }

    private TenantPolicyWhitelistResp toResp(TenantPolicyWhitelist row) {
        TenantPolicyWhitelistResp resp = new TenantPolicyWhitelistResp();
        resp.setId(row.getId());
        resp.setImUid(row.getImUid());
        User user = userDao.getById(row.getImUid());
        if (user != null) {
            resp.setAccount(user.getAccount());
            resp.setMobile(user.getEmail());
            resp.setNickname(user.getName());
        }
        return resp;
    }
}
