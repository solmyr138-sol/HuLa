package com.luohuo.flex.im.controller.admin;

import com.luohuo.basic.base.R;
import com.luohuo.basic.context.ContextUtil;
import com.luohuo.flex.im.core.admin.service.AdminTenantService;
import com.luohuo.flex.im.domain.vo.req.admin.AdminTenantLogoUpdateReq;
import com.luohuo.flex.im.domain.vo.resp.admin.AdminTenantInfoResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tenant")
@Tag(name = "后台企业管理")
public class AdminTenantController {

    @Resource
    private AdminTenantService adminTenantService;

    @GetMapping("/info")
    @Operation(summary = "企业信息")
    public R<AdminTenantInfoResp> getTenantInfo() {
        return R.success(adminTenantService.getCurrentTenantInfo());
    }

    @PutMapping("/logo")
    @Operation(summary = "更新企业LOGO")
    public R<Void> updateLogo(@Valid @RequestBody AdminTenantLogoUpdateReq req) {
        adminTenantService.updateLogo(ContextUtil.getUid(), req.getLogo());
        return R.success();
    }
}

