package com.luohuo.flex.im.core.policy.service.impl;

import cn.hutool.core.util.ObjectUtil;
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
        User user = userDao.getById(req.getImUid());
        AssertUtil.isNotEmpty(user, "用户不存在");
        AssertUtil.isTrue(Objects.equals(user.getTenantId(), tenantId), "只能添加本企业成员");
        boolean exists = whitelistDao.isWhitelisted(tenantId, req.getImUid());
        AssertUtil.isFalse(exists, "成员已在白名单中");
        TenantPolicyWhitelist row = TenantPolicyWhitelist.builder()
                .tenantId(tenantId)
                .imUid(req.getImUid())
                .build();
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
