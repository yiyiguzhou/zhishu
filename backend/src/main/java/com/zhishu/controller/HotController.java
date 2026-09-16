package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.dto.HotResponse;
import com.zhishu.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hot")
@RequiredArgsConstructor
public class HotController {

    private final ContentService contentService;

    /** 热点模块：视频 + 文章。 */
    @GetMapping
    public ApiResponse<HotResponse> hot(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(contentService.hot(limit));
    }
}