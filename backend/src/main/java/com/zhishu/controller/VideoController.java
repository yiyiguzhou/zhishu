package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.dto.VideoDetailDTO;
import com.zhishu.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /** 播放详情页：作者 + 收藏状态 + 播放地址。 */
    @GetMapping("/{id}")
    public ApiResponse<VideoDetailDTO> detail(@PathVariable Long id) {
        return ApiResponse.ok(videoService.detail(id));
    }

    /** 播放地址（经 StreamSource 解析），前端可直接 video 播放。 */
    @GetMapping("/{id}/stream")
    public ApiResponse<Map<String, String>> stream(@PathVariable Long id) {
        String playUrl = videoService.detail(id).getPlayUrl();
        return ApiResponse.ok(Map.of("playUrl", playUrl == null ? "" : playUrl));
    }
}