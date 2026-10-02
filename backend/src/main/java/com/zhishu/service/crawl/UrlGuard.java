package com.zhishu.service.crawl;

import com.zhishu.common.BusinessException;
import com.zhishu.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * SSRF 防护：仅放行白名单域名的 http/https URL，并解析 DNS 拒绝回环/私网地址，
 * 防止借后端抓取内网服务（含 DNS rebinding）。
 */
@Component
@RequiredArgsConstructor
public class UrlGuard {

    private final CrawlerProperties props;

    /** 校验 URL 是否可抓；返回规范化后的 java.net.URI。 */
    public URI validate(String url) {
        URI uri = parse(url);
        checkScheme(uri);
        checkHostAndResolve(uri);
        return uri;
    }

    private URI parse(String url) {
        try {
            return URI.create(url.trim());
        } catch (Exception e) {
            throw new BusinessException(400, "URL 不合法");
        }
    }

    private void checkScheme(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new BusinessException(400, "仅支持 http/https");
        }
    }

    private void checkHostAndResolve(URI uri) {
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new BusinessException(400, "URL 缺少主机名");
        }
        int port = uri.getPort();
        if (port != -1 && port != 80 && port != 443) {
            throw new BusinessException(400, "仅允许 80/443 端口");
        }
        if (!isAllowed(host)) {
            throw new BusinessException(403, "域名不在白名单");
        }
        resolveCheck(host);
    }

    private boolean isAllowed(String host) {
        String h = host.toLowerCase();
        for (String d : props.getAllowedDomains()) {
            String domain = d.trim().toLowerCase();
            if (domain.isEmpty()) {
                continue;
            }
            if (h.equals(domain)) {
                return true;
            }
            if (props.isAllowSubdomains() && h.endsWith("." + domain)) {
                return true;
            }
        }
        return false;
    }

    private void resolveCheck(String host) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isLoopbackAddress() || addr.isAnyLocalAddress()
                    || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()
                    || addr.isMulticastAddress()) {
                throw new BusinessException(403, "域名解析到内网地址，已拒绝");
            }
        } catch (UnknownHostException e) {
            throw new BusinessException(502, "域名无法解析");
        }
    }
}