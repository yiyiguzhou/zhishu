package com.zhishu.service.crawl;

import com.zhishu.dto.FetchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * 抓取编排：UrlGuard 校验 → JS 渲染（可用则先试，失败回退静态）→ 正文提取。
 * 抽不到正文时抛异常（编排放有 rawText 则兜底）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleFetcher {

    private final UrlGuard urlGuard;
    private final StaticHtmlFetcher staticFetcher;
    private final JsRenderFetcher jsRenderFetcher;
    private final ContentExtractor extractor;

    public FetchResult fetch(String url) {
        urlGuard.validate(url);
        String html = null;
        boolean rendered = false;

        if (jsRenderFetcher.available()) {
            try {
                html = jsRenderFetcher.render(url);
                rendered = true;
                log.info("JS 渲染抓取 {} 成功", url);
            } catch (Exception e) {
                log.warn("JS 渲染失败，回退静态抓取：{}", e.getMessage());
            }
        }
        if (html == null) {
            html = staticFetcher.fetch(url);
        }

        String[] r = extractor.extract(html);
        String title = r[0];
        String content = r[1];
        log.info("网页抓取完成 url={} 正文长度={} jsRender={}", url, content.length(), rendered);
        return new FetchResult(title, content, rendered);
    }
}