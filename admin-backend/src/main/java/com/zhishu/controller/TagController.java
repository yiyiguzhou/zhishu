package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.entity.Tag;
import com.zhishu.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ApiResponse<List<Tag>> list() {
        return ApiResponse.ok(tagService.list());
    }

    @PostMapping
    public ApiResponse<Tag> create(@RequestBody Tag tag) {
        return ApiResponse.ok(tagService.create(tag));
    }

    @PutMapping("/{id}")
    public ApiResponse<Tag> update(@PathVariable Long id, @RequestBody Tag tag) {
        return ApiResponse.ok(tagService.update(id, tag));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ApiResponse.ok();
    }
}