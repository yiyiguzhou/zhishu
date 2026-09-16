package com.zhishu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhishu.common.BusinessException;
import com.zhishu.common.UserContext;
import com.zhishu.dto.FavoriteRequest;
import com.zhishu.dto.HistoryDTO;
import com.zhishu.dto.UserDTO;
import com.zhishu.entity.Article;
import com.zhishu.entity.Favorite;
import com.zhishu.entity.History;
import com.zhishu.entity.User;
import com.zhishu.entity.Video;
import com.zhishu.mapper.ArticleMapper;
import com.zhishu.mapper.FavoriteMapper;
import com.zhishu.mapper.HistoryMapper;
import com.zhishu.mapper.UserMapper;
import com.zhishu.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final HistoryMapper historyMapper;
    private final VideoMapper videoMapper;
    private final ArticleMapper articleMapper;

    public UserDTO profile() {
        User user = userMapper.selectById(UserContext.require());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return UserDTO.of(user);
    }

    // ---------- 收藏 ----------

    public void addFavorite(FavoriteRequest req) {
        Long userId = UserContext.require();
        ensureTargetExists(req.getTargetType(), req.getTargetId());

        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, req.getTargetType())
                .eq(Favorite::getTargetId, req.getTargetId()));
        if (count != null && count > 0) {
            return; // 幂等
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setTargetType(req.getTargetType());
        f.setTargetId(req.getTargetId());
        f.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(f);
    }

    public void removeFavorite(String targetType, Long targetId) {
        Long userId = UserContext.require();
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId));
    }

    /** 收藏列表（按收藏时间倒序）。骨架先取 target_type=video。 */
    public List<HistoryDTO> listFavorites() {
        Long userId = UserContext.require();
        return favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt))
                .stream().map(this::toHistoryDTO)
                .collect(Collectors.toList());
    }

    // ---------- 浏览历史 ----------

    public List<HistoryDTO> listHistory() {
        Long userId = UserContext.require();
        return historyMapper.selectList(new LambdaQueryWrapper<History>()
                        .eq(History::getUserId, userId)
                        .orderByDesc(History::getLastWatchedAt))
                .stream().map(this::toHistoryDTO)
                .collect(Collectors.toList());
    }

    /** 上报浏览历史（记录 + 刷新最近观看时间），幂等。 */
    public void addHistory(String targetType, Long targetId, Integer progress) {
        Long userId = UserContext.require();
        ensureTargetExists(targetType, targetId);

        History history = historyMapper.selectOne(new LambdaQueryWrapper<History>()
                .eq(History::getUserId, userId)
                .eq(History::getTargetType, targetType)
                .eq(History::getTargetId, targetId));
        LocalDateTime now = LocalDateTime.now();
        if (history == null) {
            history = new History();
            history.setUserId(userId);
            history.setTargetType(targetType);
            history.setTargetId(targetId);
            history.setWatchedProgress(progress == null ? 0 : progress);
            history.setLastWatchedAt(now);
            historyMapper.insert(history);
        } else {
            history.setWatchedProgress(progress == null ? history.getWatchedProgress() : progress);
            history.setLastWatchedAt(now);
            historyMapper.updateById(history);
        }
    }

    private HistoryDTO toHistoryDTO(Object fav) {
        HistoryDTO dto = new HistoryDTO();
        String type;
        Long tid;
        if (fav instanceof Favorite f) {
            type = f.getTargetType();
            tid = f.getTargetId();
            dto.setId(f.getId());
            dto.setLastWatchedAt(f.getCreatedAt());
        } else if (fav instanceof History h) {
            type = h.getTargetType();
            tid = h.getTargetId();
            dto.setId(h.getId());
            dto.setWatchedProgress(h.getWatchedProgress());
            dto.setLastWatchedAt(h.getLastWatchedAt());
        } else {
            return dto;
        }
        dto.setTargetType(type);
        dto.setTargetId(tid);
        if ("video".equals(type)) {
            Video v = videoMapper.selectById(tid);
            if (v != null) {
                dto.setTitle(v.getTitle());
                dto.setCover(v.getCover());
            }
        } else if ("article".equals(type)) {
            Article a = articleMapper.selectById(tid);
            if (a != null) {
                dto.setTitle(a.getTitle());
                dto.setCover(a.getCover());
            }
        }
        return dto;
    }

    private void ensureTargetExists(String type, Long id) {
        if ("video".equals(type)) {
            if (videoMapper.selectById(id) == null) {
                throw new BusinessException(404, "视频不存在");
            }
        } else if ("article".equals(type)) {
            if (articleMapper.selectById(id) == null) {
                throw new BusinessException(404, "文章不存在");
            }
        }
    }
}