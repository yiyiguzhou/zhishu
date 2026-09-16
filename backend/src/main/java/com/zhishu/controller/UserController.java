package com.zhishu.controller;

import com.zhishu.common.ApiResponse;
import com.zhishu.dto.FavoriteRequest;
import com.zhishu.dto.HistoryDTO;
import com.zhishu.dto.UserDTO;
import com.zhishu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 账号信息。 */
    @GetMapping("/profile")
    public ApiResponse<UserDTO> profile() {
        return ApiResponse.ok(userService.profile());
    }

    // ---------- 收藏 ----------

    @GetMapping("/favorites")
    public ApiResponse<List<HistoryDTO>> favorites() {
        return ApiResponse.ok(userService.listFavorites());
    }

    @PostMapping("/favorites")
    public ApiResponse<Void> addFavorite(@Valid @RequestBody FavoriteRequest req) {
        userService.addFavorite(req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/favorites/{targetType}/{targetId}")
    public ApiResponse<Void> removeFavorite(
            @PathVariable String targetType, @PathVariable Long targetId) {
        userService.removeFavorite(targetType, targetId);
        return ApiResponse.ok();
    }

    // ---------- 浏览历史 ----------

    @GetMapping("/history")
    public ApiResponse<List<HistoryDTO>> history() {
        return ApiResponse.ok(userService.listHistory());
    }

    /** 上报浏览历史（播放页调用，记录观看进度）。 */
    @PostMapping("/history")
    public ApiResponse<Void> addHistory(@RequestBody Map<String, Object> body) {
        String type = String.valueOf(body.get("targetType"));
        Long targetId = Long.valueOf(String.valueOf(body.get("targetId")));
        Integer progress = body.get("watchedProgress") == null
                ? null : ((Number) body.get("watchedProgress")).intValue();
        userService.addHistory(type, targetId, progress);
        return ApiResponse.ok();
    }
}