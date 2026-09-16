package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.dto.BloggerDTO;
import com.zhishu.dto.CategoryDTO;
import com.zhishu.dto.VideoDTO;
import com.zhishu.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final ContentService contentService;

    /** 方式1：按技术分类（harness/mcp/rag...）。type=video_tech */
    @GetMapping("/categories")
    public ApiResponse<List<CategoryDTO>> categories(
            @RequestParam(defaultValue = "video_tech") String type) {
        return ApiResponse.ok(contentService.categories(type));
    }

    /** 按技术分类取视频。 */
    @GetMapping("/categories/{key}/videos")
    public ApiResponse<List<VideoDTO>> videosByCategory(@PathVariable String key) {
        return ApiResponse.ok(contentService.videosByCategory(key));
    }

    /** 方式2：博主（分类的第二种形态）。 */
    @GetMapping("/bloggers")
    public ApiResponse<List<BloggerDTO>> bloggers() {
        return ApiResponse.ok(contentService.bloggers());
    }

    /** 按博主取视频。 */
    @GetMapping("/bloggers/{id}/videos")
    public ApiResponse<List<VideoDTO>> videosByBlogger(@PathVariable Long id) {
        return ApiResponse.ok(contentService.videosByBlogger(id));
    }
}