package com.zhishu.dto;

import com.zhishu.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private LocalDateTime createdAt;

    public static UserDTO of(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setPhone(u.getPhone());
        dto.setNickname(u.getNickname());
        dto.setAvatar(u.getAvatar());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }
}