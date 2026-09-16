package com.zhishu.dto;

import com.zhishu.entity.Blogger;
import lombok.Data;

@Data
public class BloggerDTO {
    private Long id;
    private String name;
    private String avatar;
    private String introduction;

    public static BloggerDTO of(Blogger b) {
        BloggerDTO dto = new BloggerDTO();
        dto.setId(b.getId());
        dto.setName(b.getName());
        dto.setAvatar(b.getAvatar());
        dto.setIntroduction(b.getIntroduction());
        return dto;
    }
}