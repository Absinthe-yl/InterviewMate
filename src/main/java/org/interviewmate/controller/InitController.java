package org.interviewmate.controller;

import lombok.RequiredArgsConstructor;
import org.interviewmate.entity.User;
import org.interviewmate.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class InitController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/admin")
    public String initAdmin() {
        // 检查是否已存在
        User existing = userMapper.findByUsername("admin");
        if (existing != null) {
            return "管理员账号已存在";
        }

        // 创建管理员
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("管理员");
        admin.setRole("ADMIN");
        admin.setStatus("APPROVED");

        userMapper.insert(admin);
        return "管理员账号创建成功: admin / admin123";
    }

    @PostMapping("/reset-admin")
    public String resetAdmin() {
        User admin = userMapper.findByUsername("admin");
        if (admin == null) {
            // 创建新管理员
            admin = new User();
            admin.setUsername("admin");
            admin.setNickname("管理员");
            admin.setRole("ADMIN");
            admin.setStatus("APPROVED");
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.insert(admin);
            return "管理员账号创建成功: admin / admin123";
        }

        // 更新密码
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        admin.setStatus("APPROVED");
        userMapper.updateById(admin);

        return "管理员密码已重置: admin / admin123";
    }
}