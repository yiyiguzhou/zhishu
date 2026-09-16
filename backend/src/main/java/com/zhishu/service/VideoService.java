package com.zhishu.service;

import com.zhishu.common.BusinessException;
import com.zhishu.common.UserContext;
import com.zhishu.dto.VideoDetailDTO;
import com.zhishu.entity.Blogger;
import com.zhishu.entity.Video;
import com.zhishu.mapper.BloggerMapper;
import com.zhishu.mapper.FavoriteMapper;
import com.zhishu.mapper.VideoMapper;
import com.zhishu.service.media.StreamSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 播放详情页数据组装。favorited 依赖当前登录态（未登录返回 false）。
 */
@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoMapper videoMapper;
    private final BloggerMapper bloggerMapper;
    private final FavoriteMapper favoriteMapper;
    private final StreamSource streamSource;

    public VideoDetailDTO detail(Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            throw new BusinessException(404, "视频不存在");
        }
        Blogger blogger = video.getBloggerId() == null ? null : bloggerMapper.selectById(video.getBloggerId());

        VideoDetailDTO dto = new VideoDetailDTO();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setCover(video.getCover());
        dto.setDuration(video.getDuration());
        dto.setCategoryKey(video.getCategoryKey());
        dto.setBloggerId(video.getBloggerId());
        dto.setAuthorName(blogger != null ? blogger.getName() : null);
        dto.setAuthorAvatar(blogger != null ? blogger.getAvatar() : null);
        dto.setPlayUrl(streamSource.resolvePlayUrl(video));
        dto.setFavorited(isFavorited(id));
        dto.setFavoriteCount(favoriteCount(id));
        return dto;
    }

    private boolean isFavorited(Long videoId) {
        Long userId = UserContext.get();
        if (userId == null) {
            return false;
        }
        return favoriteMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zhishu.entity.Favorite>()
                        .eq(com.zhishu.entity.Favorite::getUserId, userId)
                        .eq(com.zhishu.entity.Favorite::getTargetType, "video")
                        .eq(com.zhishu.entity.Favorite::getTargetId, videoId)) > 0;
    }

    private long favoriteCount(Long videoId) {
        Long c = favoriteMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.zhishu.entity.Favorite>()
                        .eq(com.zhishu.entity.Favorite::getTargetType, "video")
                        .eq(com.zhishu.entity.Favorite::getTargetId, videoId));
        return c == null ? 0 : c;
    }
}