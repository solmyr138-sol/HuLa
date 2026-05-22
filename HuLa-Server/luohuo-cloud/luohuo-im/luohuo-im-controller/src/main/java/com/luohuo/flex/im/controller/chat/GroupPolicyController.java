package com.luohuo.flex.im.controller.chat;

import com.luohuo.basic.base.R;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.flex.im.core.policy.service.GroupPolicyService;
import com.luohuo.flex.im.domain.entity.GroupMemberAcl;
import com.luohuo.flex.im.domain.entity.GroupPolicy;
import com.luohuo.flex.im.domain.vo.req.IdReqVO;
import com.luohuo.flex.im.domain.vo.req.policy.GroupMemberAclUpdateReq;
import com.luohuo.flex.im.domain.vo.req.policy.GroupMemberMuteReq;
import com.luohuo.flex.im.domain.vo.req.policy.GroupPolicyUpdateReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/room/group/policy")
@Tag(name = "群策略接口")
@Slf4j
public class GroupPolicyController {

    @Resource
    private GroupPolicyService groupPolicyService;

    @GetMapping
    @Operation(summary = "获取群策略")
    public R<GroupPolicy> getPolicy(@Valid IdReqVO request) {
        return R.success(groupPolicyService.getPolicy(request.getId()));
    }

    @PutMapping
    @Operation(summary = "更新群策略")
    public R<GroupPolicy> updatePolicy(@Valid @RequestBody GroupPolicyUpdateReq req) {
        return R.success(groupPolicyService.updatePolicy(ContextUtil.getUid(), req));
    }

    @PutMapping("/member/mute")
    @Operation(summary = "设置群成员禁言")
    public R<GroupMemberAcl> updateMemberMute(@Valid @RequestBody GroupMemberMuteReq req) {
        return R.success(groupPolicyService.updateMemberMute(ContextUtil.getUid(), req));
    }

    @PutMapping("/member/acl")
    @Operation(summary = "设置群成员扩展权限")
    public R<GroupMemberAcl> updateMemberAcl(@Valid @RequestBody GroupMemberAclUpdateReq req) {
        return R.success(groupPolicyService.updateMemberAcl(ContextUtil.getUid(), req));
    }
}
