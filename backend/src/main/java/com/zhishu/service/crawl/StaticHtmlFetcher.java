package com.zhishu.service.crawl;

import com.zhishu.config.CrawlerProperties;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

/** 静态 HTML 抓取：jsoup 下载，限超时/响应体上限，由 jsoup 自动跟随重定向。 */
@Component
@RequiredArgsConstructor
public class StaticHtmlFetcher {

    private final CrawlerProperties props;

    public String fetch(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
                    .timeout(props.getTimeoutMs())
                    .maxBodySize(props.getMaxBytes())
                    .followRedirects(true)
                    .ignoreHttpErrors(false)
                    .ignoreContentType(true)
                    .execute()
                    .body();
        } catch (Exception e) {
            throw new RuntimeException("静态抓取失败：" + e.getMessage(), e);
        }
    }
}