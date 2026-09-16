package com.zhishu.dto;

import com.zhishu.entity.Category;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String catKey;
    private String catType;

    public static CategoryDTO of(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setCatKey(c.getCatKey());
        dto.setCatType(c.getCatType());
        return dto;
    }
}