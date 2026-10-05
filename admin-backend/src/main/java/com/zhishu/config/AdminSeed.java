package com.zhishu.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhishu.entity.AdminUser;
import com.zhishu.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** 启动时 Seeder：若无 admin 账号，创建初始超管 admin/admin123。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeed implements CommandLineRunner {

    private final AdminUserMapper adminUserMapper;

    @Override
    public void run(String... args) {
        Long count = adminUserMapper.selectCount(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, "admin"));
        if (count != null && count > 0) {
            return;
        }
        AdminUser admin = new AdminUser();
        admin.setUsername("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
        admin.setCreatedAt(java.time.LocalDateTime.now());
        adminUserMapper.insert(admin);
        log.info("已创建初始管理员 admin/admin123（请首次登录后修改密码）");
    }
}