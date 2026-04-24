package org.interviewmate.controller;

import lombok.RequiredArgsConstructor;
import org.interviewmate.entity.User;
import org.interviewmate.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/check-admin")
    public Map<String, Object> checkAdmin() {
        Map<String, Object> result = new HashMap<>();

        User admin = userMapper.findByUsername("admin");
        if (admin == null) {
            result.put("found", false);
            return result;
        }

        result.put("found", true);
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("password_hash", admin.getPassword());
        result.put("role", admin.getRole());
        result.put("status", admin.getStatus());

        // 测试密码匹配
        String rawPassword = "admin123";
        boolean matches = passwordEncoder.matches(rawPassword, admin.getPassword());
        result.put("password_matches", matches);

        // 生成新的密码哈希
        String newHash = passwordEncoder.encode(rawPassword);
        result.put("new_password_hash", newHash);

        return result;
    }
}