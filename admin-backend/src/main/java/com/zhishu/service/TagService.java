package com.zhishu.service;

import com.zhishu.common.BusinessException;
import com.zhishu.common.DistributedLock;
import com.zhishu.entity.Tag;
import com.zhishu.mapper.TagMapper;
import com.zhishu.mapper.VideoTagMapper;
import com.zhishu.entity.VideoTag;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final VideoTagMapper videoTagMapper;
    private final DistributedLock lock;

    public List<Tag> list() {
        return tagMapper.selectList(null);
    }

    public Tag create(Tag tag) {
        return lock.withLock("zhishu:admin:lock:tag:create", 30000, () -> {
            if (tag.getName() == null || tag.getName().isBlank()) {
                throw new BusinessException(400, "标签名不能为空");
            }
            tagMapper.insert(tag);
            return tag;
        });
    }

    public Tag update(Long id, Tag tag) {
        return lock.withLock("zhishu:admin:lock:tag:" + id, 30000, () -> {
            if (tagMapper.selectById(id) == null) {
                throw new BusinessException(404, "标签不存在");
            }
            tag.setId(id);
            tagMapper.updateById(tag);
            return tagMapper.selectById(id);
        });
    }

    public void delete(Long id) {
        lock.withLock("zhishu:admin:lock:tag:" + id, 30000, () -> {
            Long cnt = videoTagMapper.selectCount(
                    new LambdaQueryWrapper<VideoTag>().eq(VideoTag::getTagId, id));
            if (cnt != null && cnt > 0) {
                throw new BusinessException(400, "该标签已被视频使用，不能删除");
            }
            tagMapper.deleteById(id);
            return null;
        });
    }
}