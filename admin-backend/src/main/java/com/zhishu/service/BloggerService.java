package com.zhishu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhishu.common.BusinessException;
import com.zhishu.common.DistributedLock;
import com.zhishu.entity.Blogger;
import com.zhishu.mapper.BloggerMapper;
import com.zhishu.mapper.VideoMapper;
import com.zhishu.entity.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BloggerService {

    private final BloggerMapper bloggerMapper;
    private final VideoMapper videoMapper;
    private final DistributedLock lock;

    public List<Blogger> list() {
        return bloggerMapper.selectList(null);
    }

    public Blogger create(Blogger blogger) {
        return lock.withLock("zhishu:admin:lock:blogger:create", 30000, () -> {
            if (blogger.getName() == null || blogger.getName().isBlank()) {
                throw new BusinessException(400, "作者名不能为空");
            }
            bloggerMapper.insert(blogger);
            return blogger;
        });
    }

    public Blogger update(Long id, Blogger blogger) {
        return lock.withLock("zhishu:admin:lock:blogger:" + id, 30000, () -> {
            Blogger exist = bloggerMapper.selectById(id);
            if (exist == null) {
                throw new BusinessException(404, "作者不存在");
            }
            blogger.setId(id);
            bloggerMapper.updateById(blogger);
            return bloggerMapper.selectById(id);
        });
    }

    public void delete(Long id) {
        lock.withLock("zhishu:admin:lock:blogger:" + id, 30000, () -> {
            Long vcount = videoMapper.selectCount(
                    new LambdaQueryWrapper<Video>().eq(Video::getBloggerId, id));
            if (vcount != null && vcount > 0) {
                throw new BusinessException(400, "该作者下仍有视频，不能删除");
            }
            bloggerMapper.deleteById(id);
            return null;
        });
    }
}