package com.zhishu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhishu.common.BusinessException;
import com.zhishu.common.DistributedLock;
import com.zhishu.dto.ArticleCreateRequest;
import com.zhishu.entity.Article;
import com.zhishu.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final DistributedLock lock;
    private final RestClient backendRestClient;

    /** 分页列表（含已下线，供后台管理）。 */
    public Page<Article> list(int page, int size) {
        return articleMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Article>().orderByDesc(Article::getId));
    }

    /** 三种方式创建：manual / raw / fetch。raw、fetch 走 backend 的 AI 总结。 */
    public Long create(ArticleCreateRequest req) {
        String mode = req.getMode() == null ? "manual" : req.getMode();
        return lock.withLock("zhishu:admin:lock:article:create", 30000, () -> {
            Article article = new Article();
            article.setTitle(req.getTitle());
            article.setBloggerId(req.getBloggerId());
            article.setCover(req.getCover());
            article.setSourceTitle(req.getSourceTitle());
            article.setAuthorName(req.getAuthorName());
            article.setCategoryKey(req.getCategoryKey());
            article.setHotScore(req.getHotScore() == null ? 0 : req.getHotScore());
            article.setPublishedAt(req.getPublishedAt() == null
                    ? java.time.LocalDateTime.now() : req.getPublishedAt());
            article.setStatus("published");
            article.setValid(1);

            switch (mode) {
                case "manual" -> {
                    if (req.getContentMd() == null || req.getContentMd().isBlank()) {
                        throw new BusinessException(400, "手动模式需提供 contentMd");
                    }
                    article.setContentMd(req.getContentMd());
                    article.setSummary(summary(req.getContentMd()));
                }
                case "raw", "fetch" -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("title", req.getTitle());
                    if ("raw".equals(mode)) {
                        body.put("rawText", req.getRawText());
                    } else {
                        body.put("sourceUrl", req.getSourceUrl());
                        body.put("fetch", true);
                    }
                    body.put("categoryKey", req.getCategoryKey());
                    body.put("hotScore", req.getHotScore() == null ? 0 : req.getHotScore());
                    // 调 backend 生成 contentMd + summary，返回新文章 id（backend 会落主库）
                    Long backendId = backendRestClient.post()
                            .uri("/api/articles/ingest")
                            .body(body)
                            .retrieve()
                            .body(BackendIdResponse.class)
                            .getData();
                    log.info("AI 文章生成 backend id={}", backendId);
                    // backend 已落库，这里不再重复插入；直接返回 backend id
                    return backendId;
                }
                default -> throw new BusinessException(400, "不支持的创建模式");
            }

            articleMapper.insert(article);
            log.info("手动文章入库 id={}", article.getId());
            return article.getId();
        });
    }

    /** 上下线：valid 1/0。 */
    public void setValid(Long id, Integer valid) {
        lock.withLock("zhishu:admin:lock:article:" + id, 30000, () -> {
            Article article = articleMapper.selectById(id);
            if (article == null) {
                throw new BusinessException(404, "文章不存在");
            }
            article.setValid(valid == null || valid == 0 ? 0 : 1);
            articleMapper.updateById(article);
            return null;
        });
    }

    public void delete(Long id) {
        lock.withLock("zhishu:admin:lock:article:" + id, 30000, () -> {
            articleMapper.deleteById(id);
            return null;
        });
    }

    private String summary(String md) {
        String oneLine = md.replace('\n', ' ').trim();
        return oneLine.length() > 120 ? oneLine.substring(0, 120) : oneLine;
    }

    // backend ingest 返回结构
    public static class BackendIdResponse {
        private int code;
        private Long data;
        public Long getData() { return data; }
        public int getCode() { return code; }
    }
}