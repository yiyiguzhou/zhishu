package com.zhishu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhishu.common.BusinessException;
import com.zhishu.common.DistributedLock;
import com.zhishu.dto.VideoCreateRequest;
import com.zhishu.entity.Video;
import com.zhishu.entity.VideoTag;
import com.zhishu.mapper.VideoMapper;
import com.zhishu.mapper.VideoTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoMapper videoMapper;
    private final VideoTagMapper videoTagMapper;
    private final DistributedLock lock;

    public Page<Video> list(int page, int size) {
        return videoMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Video>().orderByDesc(Video::getId));
    }

    public Video create(VideoCreateRequest req) {
        return lock.withLock("zhishu:admin:lock:video:create", 30000, () -> {
            if (req.getTitle() == null || req.getTitle().isBlank()) {
                throw new BusinessException(400, "标题不能为空");
            }
            Video video = new Video();
            video.setTitle(req.getTitle());
            video.setBloggerId(req.getBloggerId());
            video.setCategoryId(req.getCategoryId());
            video.setCover(req.getCover());
            video.setMediaKey(req.getMediaKey());
            video.setDuration(req.getDuration() == null ? 0 : req.getDuration());
            video.setHotScore(req.getHotScore() == null ? 0 : req.getHotScore());
            video.setSourceType(req.getSourceType() == null ? "minio" : req.getSourceType());
            videoMapper.insert(video);
            bindTags(video.getId(), req.getTagIds());
            return video;
        });
    }

    public void bindTags(Long videoId, java.util.List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            VideoTag vt = new VideoTag();
            vt.setVideoId(videoId);
            vt.setTagId(tagId);
            videoTagMapper.insert(vt);
        }
    }

    public void delete(Long id) {
        lock.withLock("zhishu:admin:lock:video:" + id, 30000, () -> {
            videoMapper.deleteById(id);
            return null;
        });
    }
}