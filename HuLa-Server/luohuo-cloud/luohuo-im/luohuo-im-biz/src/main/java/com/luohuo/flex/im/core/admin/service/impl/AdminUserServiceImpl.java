package com.luohuo.flex.im.core.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.basic.validator.utils.AssertUtil;
import com.luohuo.flex.im.common.enums.NormalOrNoEnum;
import com.luohuo.flex.im.core.admin.service.AdminUserService;
import com.luohuo.flex.im.core.policy.dao.UserDeletionLogDao;
import com.luohuo.flex.im.core.user.dao.BlackDao;
import com.luohuo.flex.im.core.user.dao.UserDao;
import com.luohuo.flex.im.domain.entity.Black;
import com.luohuo.flex.im.domain.entity.User;
import com.luohuo.flex.im.domain.entity.UserDeletionLog;
import com.luohuo.flex.im.domain.enums.BlackTypeEnum;
import com.luohuo.flex.im.domain.vo.req.policy.AdminImUserPageReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserBanReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserUnbanReq;
import com.luohuo.flex.im.domain.vo.req.policy.UserDeletionLogPageReq;
import com.luohuo.flex.im.domain.vo.req.user.BlackAddReq;
import com.luohuo.flex.im.domain.vo.req.user.BlackRemoveReq;
import com.luohuo.flex.im.domain.vo.res.PageBaseResp;
import com.luohuo.flex.im.domain.vo.resp.policy.AdminImUserResp;
import com.luohuo.flex.im.domain.vo.resp.policy.UserDeletionLogResp;
import com.luohuo.flex.im.core.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private UserDao userDao;
    @Resource
    private UserService userService;
    @Resource
    private BlackDao blackDao;
    @Resource
    private UserDeletionLogDao userDeletionLogDao;

    @Override
    public void banUser(Long operatorId, AdminUserBanReq req) {
        User user = userDao.getById(req.getUid());
        AssertUtil.isNotEmpty(user, "用户不存在");
        BlackAddReq blackReq = new BlackAddReq();
        blackReq.setType(BlackTypeEnum.UID.getType());
        blackReq.setTarget(String.valueOf(req.getUid()));
        blackReq.setDeadline(ObjectUtil.defaultIfNull(req.getDeadlineMinutes(), 0L));
        userService.addBlack(blackReq);
        User update = new User();
        update.setId(req.getUid());
        update.setState(NormalOrNoEnum.NOT_NORMAL.getStatus());
        userDao.updateById(update);
    }

    @Override
    public void unbanUser(Long operatorId, AdminUserUnbanReq req) {
        User user = userDao.getById(req.getUid());
        AssertUtil.isNotEmpty(user, "用户不存在");
        Black black = blackDao.lambdaQuery()
                .eq(Black::getType, BlackTypeEnum.UID.getType())
                .eq(Black::getTarget, String.valueOf(req.getUid()))
                .one();
        if (black != null) {
            BlackRemoveReq removeReq = new BlackRemoveReq();
            removeReq.setId(black.getId());
            userService.removeBlack(removeReq);
        }
        User update = new User();
        update.setId(req.getUid());
        update.setState(NormalOrNoEnum.NORMAL.getStatus());
        userDao.updateById(update);
    }

    @Override
    public PageBaseResp<UserDeletionLogResp> listDeletionLogs(UserDeletionLogPageReq req) {
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        LambdaQueryWrapper<UserDeletionLog> wrapper = new LambdaQueryWrapper<UserDeletionLog>()
                .eq(UserDeletionLog::getTenantId, tenantId)
                .orderByDesc(UserDeletionLog::getCreateTime);
        IPage<UserDeletionLog> page = userDeletionLogDao.page(req.plusPage(), wrapper);
        List<UserDeletionLogResp> list = page.getRecords().stream().map(log -> {
            UserDeletionLogResp resp = new UserDeletionLogResp();
            BeanUtils.copyProperties(log, resp);
            return resp;
        }).collect(Collectors.toList());
        return PageBaseResp.init(page, list);
    }

    @Override
    public PageBaseResp<AdminImUserResp> pageImUsers(AdminImUserPageReq req) {
        if (Boolean.TRUE.equals(req.getDeletedOnly())) {
            UserDeletionLogPageReq logReq = new UserDeletionLogPageReq();
            logReq.setPageNo(req.getPageNo());
            logReq.setPageSize(req.getPageSize());
            PageBaseResp<UserDeletionLogResp> logs = listDeletionLogs(logReq);
            List<AdminImUserResp> list = logs.getList().stream().map(log -> {
                AdminImUserResp resp = new AdminImUserResp();
                resp.setUid(log.getImUid());
                resp.setAccount(log.getAccount());
                resp.setNickname(log.getAccount());
                resp.setDeletedTime(log.getCreateTime());
                return resp;
            }).collect(Collectors.toList());
            return PageBaseResp.init(logs.getPageNo(), logs.getPageSize(), logs.getTotalRecords(), list);
        }
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getTenantId, tenantId)
                .orderByDesc(User::getCreateTime);
        if (req.getState() != null) {
            wrapper.eq(User::getState, req.getState());
        }
        if (StrUtil.isNotBlank(req.getKeyword())) {
            String kw = req.getKeyword().trim();
            wrapper.and(w -> w.like(User::getAccount, kw).or().like(User::getName, kw).or().like(User::getEmail, kw));
        }
        IPage<User> page = userDao.page(req.plusPage(), wrapper);
        List<AdminImUserResp> list = page.getRecords().stream().map(u -> {
            AdminImUserResp resp = new AdminImUserResp();
            resp.setUid(u.getId());
            resp.setAccount(u.getAccount());
            resp.setNickname(u.getName());
            resp.setMobile(u.getEmail());
            resp.setState(u.getState());
            resp.setRegisterTime(u.getCreateTime());
            return resp;
        }).collect(Collectors.toList());
        return PageBaseResp.init(page, list);
    }
}
