package com.luohuo.basic.tenant.core.web;

import cn.hutool.core.util.StrUtil;
import com.luohuo.basic.boot.utils.WebUtils;
import com.luohuo.basic.context.ContextConstants;
import com.luohuo.basic.context.ContextUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Function;

/**
 * 根据请求 Host 解析租户（需业务层注入 {@link #tenantIdResolver}）。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class TenantDomainWebFilter extends OncePerRequestFilter {

    private final Function<String, Long> tenantIdResolver;

    public TenantDomainWebFilter(Function<String, Long> tenantIdResolver) {
        this.tenantIdResolver = tenantIdResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            if (ContextUtil.getTenantId() == null) {
                String tenantHeader = WebUtils.getHeader(request, ContextConstants.HEADER_TENANT_ID);
                if (StrUtil.isNotEmpty(tenantHeader)) {
                    ContextUtil.setTenantId(Long.parseLong(tenantHeader));
                } else {
                    String host = request.getServerName();
                    Long tenantId = tenantIdResolver.apply(host);
                    if (tenantId != null) {
                        ContextUtil.setTenantId(tenantId);
                    }
                }
            }
            chain.doFilter(request, response);
        } finally {
            ContextUtil.clearTenantContext();
        }
    }
}
