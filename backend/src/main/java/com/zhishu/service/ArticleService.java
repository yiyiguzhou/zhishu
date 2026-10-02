package com.zhishu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhishu.common.BusinessException;
import com.zhishu.dto.ArticleDTO;
import com.zhishu.dto.ArticleDetailDTO;
import com.zhishu.dto.ArticleIngestRequest;
import com.zhishu.dto.FetchResult;
import com.zhishu.entity.Article;
import com.zhishu.entity.Blogger;
import com.zhishu.mapper.ArticleMapper;
import com.zhishu.mapper.BloggerMapper;
import com.zhishu.service.crawl.ArticleFetcher;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private static final Parser MARKDOWN_PARSER;
    private static final HtmlRenderer HTML_RENDERER;
    static {
        MutableDataSet options = new MutableDataSet();
        MARKDOWN_PARSER = Parser.builder(options).build();
        HTML_RENDERER = HtmlRenderer.builder(options).build();
    }

    private final ArticleMapper articleMapper;
    private final BloggerMapper bloggerMapper;
    private final ArticleSummaryService summaryService;
    private final ArticleFetcher articleFetcher;

    /** 文章列表：仅 published，hot_score + 时间衰减排序。 */
    public List<ArticleDTO> list(String categoryKey, int limit) {
        int lim = Math.min(Math.max(limit, 1), 50);
        List<Article> candidates = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "published")
                .eq(categoryKey != null && !categoryKey.isBlank(), Article::getCategoryKey, categoryKey)
                .orderByDesc(Article::getHotScore)
                .last("LIMIT " + (lim * 3)));
        Map<Long, Blogger> bloggers = bloggerMap();
        List<ArticleDTO> result = candidates.stream()
                .sorted(Comparator.comparingDouble(this::score).reversed())
                .limit(lim)
                .map(a -> toDTO(a, bloggers))
                .collect(Collectors.toList());
        log.info("文章列表 category={} 共{}条", categoryKey, result.size());
        return result;
    }

    /** 文章详情：contentMd → contentHtml（供小程序 rich-text）。 */
    public ArticleDetailDTO detail(Long id) {
        Article a = articleMapper.selectById(id);
        if (a == null) {
            throw new BusinessException(404, "文章不存在");
        }
        ArticleDetailDTO dto = new ArticleDetailDTO();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setCover(a.getCover());
        dto.setSummary(a.getSummary());
        dto.setContentMd(a.getContentMd());
        dto.setContentHtml(a.getContentMd() == null ? null : HTML_RENDERER.render(MARKDOWN_PARSER.parse(a.getContentMd())));
        dto.setSourceUrl(a.getSourceUrl());
        dto.setSourceTitle(a.getSourceTitle());
        dto.setCategoryKey(a.getCategoryKey());
        dto.setHotScore(a.getHotScore());
        dto.setPublishedAt(a.getPublishedAt());
        dto.setCreatedAt(a.getCreatedAt());
        // 作者：优先外部来源 author_name，缺则回退内部 blogger.name
        dto.setAuthorName(a.getAuthorName());
        if (dto.getAuthorName() == null && a.getBloggerId() != null) {
            Blogger b = bloggerMapper.selectById(a.getBloggerId());
            if (b != null) {
                dto.setAuthorName(b.getName());
            }
        }
        return dto;
    }

    /** AI 总结录入：支持 rawText 人工粘贴 或 fetch=true 自动抓取 sourceUrl。 */
    public Long ingest(ArticleIngestRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new BusinessException(400, "title 不能为空");
        }
        boolean fetch = Boolean.TRUE.equals(req.getFetch());
        String sourceUrl = req.getSourceUrl();
        String rawText = req.getRawText();

        if (fetch) {
            if (sourceUrl == null || sourceUrl.isBlank()) {
                throw new BusinessException(400, "fetch=true 时 sourceUrl 不能为空");
            }
            FetchResult fr = articleFetcher.fetch(sourceUrl);
            rawText = fr.getContent();
            // 抓取的标题在请求未给 sourceTitle 时回填
            if ((req.getSourceTitle() == null || req.getSourceTitle().isBlank()) && fr.getTitle() != null) {
                req.setSourceTitle(fr.getTitle());
            }
        } else if (rawText == null || rawText.isBlank()) {
            throw new BusinessException(400, "rawText 不能为空（或设 fetch=true 自动抓取）");
        }

        String contentMd = summaryService.summarize(rawText);
        Article a = new Article();
        a.setTitle(req.getTitle());
        a.setBloggerId(req.getBloggerId());
        a.setCover(req.getCover());
        a.setSourceUrl(sourceUrl);
        a.setSourceTitle(req.getSourceTitle());
        a.setAuthorName(req.getAuthorName());
        a.setCategoryKey(req.getCategoryKey());
        a.setHotScore(req.getHotScore() != null ? req.getHotScore() : 0);
        a.setContentMd(contentMd);
        a.setSummary(buildSummary(contentMd));
        a.setPublishedAt(req.getPublishedAt());
        a.setStatus("published");
        articleMapper.insert(a);
        log.info("AI 生成文章入库 id={} title={} fetch={}", a.getId(), a.getTitle(), fetch);
        return a.getId();
    }

    /** 热度时间衰减：score = hot_score * exp(-0.05 * ageDays)，半衰期约 13.9 天。 */
    private double score(Article a) {
        LocalDateTime t = a.getPublishedAt() != null ? a.getPublishedAt() : a.getCreatedAt();
        if (t == null) {
            t = LocalDateTime.now();
        }
        long ageDays = ChronoUnit.DAYS.between(t, LocalDateTime.now());
        if (ageDays < 0) {
            ageDays = 0;
        }
        int hs = a.getHotScore() != null ? a.getHotScore() : 0;
        return hs * Math.exp(-0.05 * ageDays);
    }

    private String buildSummary(String md) {
        if (md == null) {
            return null;
        }
        String oneLine = md.replace('\n', ' ').trim();
        return oneLine.length() > 120 ? oneLine.substring(0, 120) : oneLine;
    }

    private Map<Long, Blogger> bloggerMap() {
        return bloggerMapper.selectList(null).stream()
                .collect(Collectors.toMap(Blogger::getId, Function.identity()));
    }

    private ArticleDTO toDTO(Article a, Map<Long, Blogger> bloggers) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setCover(a.getCover());
        dto.setCategoryKey(a.getCategoryKey());
        dto.setHotScore(a.getHotScore());
        dto.setSummary(a.getSummary());
        dto.setPublishedAt(a.getPublishedAt());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setAuthorName(a.getAuthorName());
        if (dto.getAuthorName() == null) {
            Blogger b = bloggers.get(a.getBloggerId());
            if (b != null) {
                dto.setAuthorName(b.getName());
            }
        }
        return dto;
    }
}