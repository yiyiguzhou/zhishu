package com.zhishu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhishu.dto.ArticleDTO;
import com.zhishu.dto.BloggerDTO;
import com.zhishu.dto.CategoryDTO;
import com.zhishu.dto.HotResponse;
import com.zhishu.dto.VideoDTO;
import com.zhishu.entity.Article;
import com.zhishu.entity.Blogger;
import com.zhishu.entity.Category;
import com.zhishu.entity.Video;
import com.zhishu.mapper.ArticleMapper;
import com.zhishu.mapper.BloggerMapper;
import com.zhishu.mapper.CategoryMapper;
import com.zhishu.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final VideoMapper videoMapper;
    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final BloggerMapper bloggerMapper;

    /** 热点模块：按 hot_score 取视频与文章。 */
    public HotResponse hot(int limit) {
        int lim = Math.min(Math.max(limit, 1), 50);
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .orderByDesc(Video::getHotScore).last("LIMIT " + lim));
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getHotScore).last("LIMIT " + lim));
        Map<Long, Blogger> bloggers = bloggerMap();
        Map<Long, Category> categories = categoryMap();
        List<VideoDTO> vs = videos.stream()
                .map(v -> toVideoDTO(v, bloggers, categories))
                .collect(Collectors.toList());
        List<ArticleDTO> as = articles.stream()
                .map(a -> toArticleDTO(a, bloggers))
                .collect(Collectors.toList());
        return new HotResponse(vs, as);
    }

    /** 分类列表，type = video_tech（按技术）| blogger（按博主）。 */
    public List<CategoryDTO> categories(String type) {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                        .eq(Category::getCatType, type)
                        .orderByAsc(Category::getId))
                .stream().map(CategoryDTO::of).collect(Collectors.toList());
    }

    /** 按技术分类取视频：key 是 category.cat_key，视频按 category_id 关联。 */
    public List<VideoDTO> videosByCategory(String key) {
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getCatKey, key));
        if (category == null) {
            return Collections.emptyList();
        }
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .eq(Video::getCategoryId, category.getId())
                .orderByDesc(Video::getHotScore));
        return videos.stream().map(v -> toVideoDTO(v, bloggerMap(), categoryMap())).collect(Collectors.toList());
    }

    /** 博主列表。 */
    public List<BloggerDTO> bloggers() {
        return bloggerMapper.selectList(new LambdaQueryWrapper<Blogger>().orderByAsc(Blogger::getId))
                .stream().map(BloggerDTO::of).collect(Collectors.toList());
    }

    /** 按博主取视频。 */
    public List<VideoDTO> videosByBlogger(Long bloggerId) {
        List<Video> videos = videoMapper.selectList(new LambdaQueryWrapper<Video>()
                .eq(Video::getBloggerId, bloggerId)
                .orderByDesc(Video::getHotScore));
        return videos.stream().map(v -> toVideoDTO(v, bloggerMap(), categoryMap())).collect(Collectors.toList());
    }

    private Map<Long, Blogger> bloggerMap() {
        return bloggerMapper.selectList(null).stream()
                .collect(Collectors.toMap(Blogger::getId, Function.identity()));
    }

    private Map<Long, Category> categoryMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    private VideoDTO toVideoDTO(Video v, Map<Long, Blogger> bloggers, Map<Long, Category> categories) {
        VideoDTO dto = new VideoDTO();
        dto.setId(v.getId());
        dto.setTitle(v.getTitle());
        dto.setCover(v.getCover());
        dto.setDuration(v.getDuration());
        Category c = categories.get(v.getCategoryId());
        dto.setCategoryKey(c != null ? c.getCatKey() : null);
        dto.setHotScore(v.getHotScore());
        dto.setCreatedAt(v.getCreatedAt());
        Blogger b = bloggers.get(v.getBloggerId());
        dto.setAuthorName(b != null ? b.getName() : null);
        return dto;
    }

    private ArticleDTO toArticleDTO(Article a, Map<Long, Blogger> bloggers) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setCover(a.getCover());
        dto.setCategoryKey(a.getCategoryKey());
        dto.setHotScore(a.getHotScore());
        dto.setCreatedAt(a.getCreatedAt());
        Blogger b = bloggers.get(a.getBloggerId());
        dto.setAuthorName(b != null ? b.getName() : null);
        return dto;
    }
}