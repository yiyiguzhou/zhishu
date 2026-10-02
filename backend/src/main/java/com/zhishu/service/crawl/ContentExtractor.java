package com.zhishu.service.crawl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * 正文提取：自写轻量启发式——取 title + 正文密度最高的节点。
 * 剔除 nav/script/style/iframe/ad 等噪声，输出纯文本。
 */
@Component
@RequiredArgsConstructor
public class ContentExtractor {

    private static final String[] NOISE_TAGS = {
            "script", "style", "noscript", "iframe", "svg", "nav", "header", "footer", "aside", "form"
    };
    private static final String[] NOISE_CLASS_HINT = {
            "comment", "advert", "ad-", "share", "sidebar", "menu", "related", "recommend", "footer", "breadcrumb", "toolbar", "pagination"
    };

    /** 从 HTML 提取 (title, 正文纯文本)；正文为空抛异常。 */
    public String[] extract(String html) {
        Document doc = Jsoup.parse(html);
        String title = doc.title() != null ? doc.title().trim() : null;
        Element body = doc.body();
        if (body == null) {
            return new String[]{title, doc.text()};
        }
        Element best = pickMainElement(body);
        String content = best != null ? best.text() : doc.text();
        content = content.replace(' ', ' ').trim();
        if (content.length() < 40) {
            throw new RuntimeException("正文提取过短，可能不是文章页");
        }
        return new String[]{title, content};
    }

    /** 选正文密度（文本长度/标签数）最高的块级元素，作为正文容器。 */
    private Element pickMainElement(Element root) {
        Elements candidates = root.select("article, main, [class*=content], [class*=article], [id*=content], [id*=article]");
        Element best = null;
        double bestScore = -1;
        for (Element c : candidates) {
            if (isNoise(c)) {
                continue;
            }
            double score = textScore(c);
            if (score > bestScore) {
                bestScore = score;
                best = c;
            }
        }
        if (best == null) {
            best = root;
        }
        return best;
    }

    private double textScore(Element el) {
        String text = el.text();
        int tagCount = el.select("*").size();
        if (tagCount == 0) {
            return 0;
        }
        return (double) text.length() / tagCount;
    }

    private boolean isNoise(Element el) {
        String cls = el.className() == null ? "" : el.className().toLowerCase();
        for (String hint : NOISE_CLASS_HINT) {
            if (cls.contains(hint)) {
                return true;
            }
        }
        return false;
    }
}