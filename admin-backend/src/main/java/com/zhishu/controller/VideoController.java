package com.zhishu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhishu.common.ApiResponse;
import com.zhishu.dto.VideoCreateRequest;
import com.zhishu.entity.Video;
import com.zhishu.service.ImportService;
import com.zhishu.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final ImportService importService;

    @GetMapping
    public ApiResponse<Page<Video>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(videoService.list(page, size));
    }

    @PostMapping
    public ApiResponse<Video> create(@RequestBody VideoCreateRequest req) {
        return ApiResponse.ok(videoService.create(req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        videoService.delete(id);
        return ApiResponse.ok();
    }

    /** 批量导入：选作者 + 上传多个 mp4。 */
    @PostMapping("/import")
    public ApiResponse<List<Long>> importVideos(@RequestParam Long bloggerId,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam("files") List<MultipartFile> files) {
        return ApiResponse.ok(importService.importVideos(bloggerId, categoryId, files));
    }
}