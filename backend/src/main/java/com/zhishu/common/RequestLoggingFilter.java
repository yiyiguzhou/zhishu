package com.zhishu.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 业务请求访问日志：方法、路径（手机号参数脱敏）、状态码、耗时、登录用户。
 * 仅记录 /api/**；不记录请求体，避免密码/验证码/密钥落日志。
 * userId 由 AuthInterceptor 写入请求属性（过滤器在链返回时 UserContext 已被清理）。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - start;
            Object userId = request.getAttribute("userId");
            String query = request.getQueryString();
            log.info("请求 {} {}?{} -> {} {}ms uid={}",
                    request.getMethod(), request.getRequestURI(), mask(query),
                    response.getStatus(), cost, userId == null ? "-" : userId);
        }
    }

    /** query 中 phone 参数中间 4 位脱敏，其余查询原样。 */
    private String mask(String query) {
        if (query == null) {
            return "";
        }
        return query.replaceAll("(phone=\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}
