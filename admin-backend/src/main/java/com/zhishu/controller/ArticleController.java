package com.zhishu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhishu.common.ApiResponse;
import com.zhishu.dto.ArticleCreateRequest;
import com.zhishu.entity.Article;
import com.zhishu.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<Page<Article>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(articleService.list(page, size));
    }

    /** 三种方式创建：manual/raw/fetch。 */
    @PostMapping
    public ApiResponse<Long> create(@RequestBody ArticleCreateRequest req) {
        return ApiResponse.ok(articleService.create(req));
    }

    /** 上下线：valid 1/0。 */
    @PutMapping("/{id}/valid")
    public ApiResponse<Void> setValid(@PathVariable Long id, @RequestParam Integer valid) {
        articleService.setValid(id, valid);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ApiResponse.ok();
    }
}