package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.dto.ArticleDTO;
import com.zhishu.dto.ArticleDetailDTO;
import com.zhishu.dto.ArticleIngestRequest;
import com.zhishu.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /** 文章列表：热点时间衰减排序，可按分类过滤。 */
    @GetMapping
    public ApiResponse<List<ArticleDTO>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(articleService.list(category, limit));
    }

    /** 文章详情：含 Markdown 正文与转换后的 HTML。 */
    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailDTO> detail(@PathVariable Long id) {
        return ApiResponse.ok(articleService.detail(id));
    }

    /** AI 总结录入（MVP）：提交原文，后端调用大模型生成 Markdown 总结并入库。 */
    @PostMapping("/ingest")
    public ApiResponse<Long> ingest(@RequestBody ArticleIngestRequest request) {
        return ApiResponse.ok(articleService.ingest(request));
    }
}