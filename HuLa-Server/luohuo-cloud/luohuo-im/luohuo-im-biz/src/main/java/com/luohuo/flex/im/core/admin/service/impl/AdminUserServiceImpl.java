package com.luohuo.flex.im.core.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.luohuo.basic.base.R;
import com.luohuo.basic.exception.BizException;
import com.luohuo.flex.base.vo.update.tenant.DefUserPasswordResetVO;
import com.luohuo.flex.im.api.DefUserApi;
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
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserResetPasswordReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserUnbanReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserUpdateReq;
import com.luohuo.flex.im.domain.vo.req.policy.UserDeletionLogPageReq;
import com.luohuo.flex.im.domain.vo.req.user.BlackAddReq;
import com.luohuo.flex.im.domain.vo.req.user.BlackRemoveReq;
import com.luohuo.flex.im.domain.vo.res.PageBaseResp;
import com.luohuo.flex.im.domain.vo.resp.policy.AdminImUserResp;
import com.luohuo.flex.im.domain.vo.resp.policy.UserDeletionLogResp;
import com.luohuo.flex.im.core.user.service.UserService;
import com.luohuo.flex.model.entity.base.IpInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
    @Resource
    private DefUserApi defUserApi;

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
        if (StrUtil.isNotBlank(req.getKeyword())) {
            String kw = req.getKeyword().trim();
            wrapper.and(w -> w.like(UserDeletionLog::getAccount, kw));
        }
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
            logReq.setKeyword(req.getKeyword());
            PageBaseResp<UserDeletionLogResp> logs = listDeletionLogs(logReq);
            List<AdminImUserResp> list = logs.getList().stream().map(log -> {
                AdminImUserResp resp = new AdminImUserResp();
                resp.setUid(log.getImUid());
                resp.setDefUserId(log.getDefUserId());
                resp.setAccount(log.getAccount());
                resp.setDeletedTime(log.getCreateTime());
                resp.setState(-1);
                User imUser = log.getImUid() != null ? userDao.getById(log.getImUid()) : null;
                if (imUser != null) {
                    resp.setNickname(imUser.getName());
                    resp.setMobile(imUser.getEmail());
                    resp.setLastLoginIp(resolveLastLoginIp(imUser.getIpInfo()));
                    if (StrUtil.isBlank(resp.getAccount())) {
                        resp.setAccount(imUser.getAccount());
                    }
                } else {
                    resp.setNickname(log.getAccount());
                }
                return resp;
            }).collect(Collectors.toList());
            return PageBaseResp.init(logs.getPageNo(), logs.getPageSize(), logs.getTotalRecords(), list);
        }
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        List<Long> deletedUids = userDeletionLogDao.lambdaQuery()
                .eq(UserDeletionLog::getTenantId, tenantId)
                .select(UserDeletionLog::getImUid)
                .list()
                .stream()
                .map(UserDeletionLog::getImUid)
                .filter(ObjectUtil::isNotNull)
                .distinct()
                .toList();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getTenantId, tenantId)
                .orderByDesc(User::getCreateTime);
        if (CollUtil.isNotEmpty(deletedUids)) {
            wrapper.notIn(User::getId, deletedUids);
        }
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
            resp.setDefUserId(u.getUserId());
            resp.setAccount(u.getAccount());
            resp.setNickname(u.getName());
            resp.setMobile(u.getEmail());
            resp.setState(u.getState());
            resp.setRegisterTime(u.getCreateTime());
            resp.setLastLoginIp(resolveLastLoginIp(u.getIpInfo()));
            return resp;
        }).collect(Collectors.toList());
        return PageBaseResp.init(page, list);
    }

    private static String resolveLastLoginIp(IpInfo ipInfo) {
        if (ipInfo == null) {
            return null;
        }
        if (StrUtil.isNotBlank(ipInfo.getUpdateIp())) {
            return ipInfo.getUpdateIp();
        }
        return ipInfo.getCreateIp();
    }

    @Override
    public void updateUser(Long operatorId, AdminUserUpdateReq req) {
        User user = userDao.getById(req.getUid());
        AssertUtil.isNotEmpty(user, "用户不存在");
        Long tenantId = ObjectUtil.defaultIfNull(ContextUtil.getTenantId(), 1L);
        AssertUtil.isTrue(Objects.equals(user.getTenantId(), tenantId), "只能操作本企业用户");
        boolean deleted = userDeletionLogDao.lambdaQuery()
                .eq(UserDeletionLog::getTenantId, tenantId)
                .eq(UserDeletionLog::getImUid, req.getUid())
                .exists();
        AssertUtil.isFalse(deleted, "用户已注销，请在注销用户列表查看");

        User update = new User();
        update.setId(req.getUid());
        if (StrUtil.isNotBlank(req.getNickname())) {
            update.setName(req.getNickname().trim());
        }
        if (req.getEmail() != null) {
            update.setEmail(req.getEmail().trim());
        }
        userDao.updateById(update);

        if (req.getState() != null) {
            if (NormalOrNoEnum.NOT_NORMAL.getStatus().equals(req.getState())) {
                AdminUserBanReq banReq = new AdminUserBanReq();
                banReq.setUid(req.getUid());
                banUser(operatorId, banReq);
            } else if (NormalOrNoEnum.NORMAL.getStatus().equals(req.getState())) {
                AdminUserUnbanReq unbanReq = new AdminUserUnbanReq();
                unbanReq.setUid(req.getUid());
                unbanUser(operatorId, unbanReq);
            }
            if (user.getUserId() != null) {
                R<Boolean> stateR = defUserApi.updateState(user.getUserId(), req.getState() == 0);
                if (stateR == null || !Boolean.TRUE.equals(stateR.getData())) {
                    throw BizException.wrap(stateR != null ? stateR.getMsg() : "同步登录账号状态失败");
                }
            }
        }
    }

    @Override
    public void resetPassword(Long operatorId, AdminUserResetPasswordReq req) {
        AssertUtil.isTrue(Objects.equals(req.getPassword(), req.getConfirmPassword()), "两次密码不一致");
        User user = userDao.getById(req.getUid());
        AssertUtil.isNotEmpty(user, "用户不存在");
        AssertUtil.isNotEmpty(user.getUserId(), "用户未绑定登录账号，无法重置密码");
        DefUserPasswordResetVO vo = DefUserPasswordResetVO.builder()
                .id(user.getUserId())
                .isUseSystemPassword(false)
                .password(req.getPassword())
                .confirmPassword(req.getConfirmPassword())
                .build();
        R<Boolean> result = defUserApi.resetPassword(vo);
        if (result == null || !Boolean.TRUE.equals(result.getData())) {
            throw BizException.wrap(result != null ? result.getMsg() : "重置密码失败");
        }
    }
}
