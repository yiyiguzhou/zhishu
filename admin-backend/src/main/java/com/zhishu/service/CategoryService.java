package com.zhishu.service;

import com.zhishu.common.BusinessException;
import com.zhishu.common.DistributedLock;
import com.zhishu.entity.Category;
import com.zhishu.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final DistributedLock lock;

    public List<Category> list() {
        return categoryMapper.selectList(null);
    }

    public Category create(Category category) {
        return lock.withLock("zhishu:admin:lock:category:create", 30000, () -> {
            if (category.getName() == null || category.getName().isBlank()) {
                throw new BusinessException(400, "分类名不能为空");
            }
            if (category.getCatKey() == null || category.getCatKey().isBlank()) {
                throw new BusinessException(400, "catKey 不能为空");
            }
            if (category.getCatType() == null) {
                category.setCatType("video_tech");
            }
            categoryMapper.insert(category);
            return category;
        });
    }

    public Category update(Long id, Category category) {
        return lock.withLock("zhishu:admin:lock:category:" + id, 30000, () -> {
            if (categoryMapper.selectById(id) == null) {
                throw new BusinessException(404, "分类不存在");
            }
            category.setId(id);
            categoryMapper.updateById(category);
            return categoryMapper.selectById(id);
        });
    }

    public void delete(Long id) {
        lock.withLock("zhishu:admin:lock:category:" + id, 30000, () -> {
            categoryMapper.deleteById(id);
            return null;
        });
    }
}