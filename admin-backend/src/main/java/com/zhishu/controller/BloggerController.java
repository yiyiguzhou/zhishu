package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.entity.Blogger;
import com.zhishu.service.BloggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bloggers")
@RequiredArgsConstructor
public class BloggerController {

    private final BloggerService bloggerService;

    @GetMapping
    public ApiResponse<List<Blogger>> list() {
        return ApiResponse.ok(bloggerService.list());
    }

    @PostMapping
    public ApiResponse<Blogger> create(@RequestBody Blogger blogger) {
        return ApiResponse.ok(bloggerService.create(blogger));
    }

    @PutMapping("/{id}")
    public ApiResponse<Blogger> update(@PathVariable Long id, @RequestBody Blogger blogger) {
        return ApiResponse.ok(bloggerService.update(id, blogger));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bloggerService.delete(id);
        return ApiResponse.ok();
    }
}