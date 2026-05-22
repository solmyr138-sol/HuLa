package com.luohuo.flex.im.core.admin.service;

import com.luohuo.flex.im.domain.vo.req.policy.AdminImUserPageReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserBanReq;
import com.luohuo.flex.im.domain.vo.req.policy.AdminUserUnbanReq;
import com.luohuo.flex.im.domain.vo.req.policy.UserDeletionLogPageReq;
import com.luohuo.flex.im.domain.vo.res.PageBaseResp;
import com.luohuo.flex.im.domain.vo.resp.policy.AdminImUserResp;
import com.luohuo.flex.im.domain.vo.resp.policy.UserDeletionLogResp;

/**
 * 后台用户管理
 */
public interface AdminUserService {

    void banUser(Long operatorId, AdminUserBanReq req);

    void unbanUser(Long operatorId, AdminUserUnbanReq req);

    PageBaseResp<UserDeletionLogResp> listDeletionLogs(UserDeletionLogPageReq req);

    PageBaseResp<AdminImUserResp> pageImUsers(AdminImUserPageReq req);
}
