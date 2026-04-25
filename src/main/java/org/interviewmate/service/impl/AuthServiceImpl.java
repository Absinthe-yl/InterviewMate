package org.interviewmate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.interviewmate.common.exception.BusinessException;
import org.interviewmate.dto.LoginRequest;
import org.interviewmate.dto.LoginResponse;
import org.interviewmate.dto.RegisterRequest;
import org.interviewmate.entity.User;
import org.interviewmate.entity.UserRole;
import org.interviewmate.entity.UserStatus;
import org.interviewmate.mapper.UserMapper;
import org.interviewmate.security.JwtUtil;
import org.interviewmate.service.AuthService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void register(RegisterRequest request) {
        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null) {
            // 如果用户状态是 REJECTED，允许重新注册（删除旧记录）
            if (UserStatus.REJECTED.name().equals(existingUser.getStatus())) {
                userMapper.deleteById(existingUser.getId());
            } else if (UserStatus.PENDING.name().equals(existingUser.getStatus())) {
                throw new BusinessException("用户名已存在，正在等待审核");
            } else {
                throw new BusinessException("用户名已存在");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(UserRole.USER.name());  // 普通用户
        user.setStatus(UserStatus.PENDING.name());  // 需要管理员审核

        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("登录请求: username={}", request.getUsername());

        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            log.error("用户不存在: {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        log.info("查询到用户: id={}, username={}, status={}, role={}", user.getId(), user.getUsername(), user.getStatus(), user.getRole());

        // 检查用户状态
        if (UserStatus.PENDING.name().equals(user.getStatus())) {
            log.warn("用户待审核: {}", user.getUsername());
            throw new BusinessException("账号待审核，请等待管理员审批");
        }
        if (UserStatus.REJECTED.name().equals(user.getStatus())) {
            log.warn("用户已被拒绝: {}", user.getUsername());
            throw new BusinessException("账号申请已被拒绝");
        }

        // 验证密码
        log.info("输入密码: {}", request.getPassword());
        log.info("数据库密码: {}", user.getPassword());
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("密码匹配结果: {}", passwordMatches);

        if (!passwordMatches) {
            log.error("密码不匹配");
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        String tokenKey = "token:" + token;
        redisTemplate.opsForValue().set(tokenKey, "valid", 2, TimeUnit.HOURS);

        log.info("登录成功: username={}, token={}", user.getUsername(), token);
        return new LoginResponse(token);
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtUtil.validateToken(token)) {
            String tokenKey = "token:" + token;
            redisTemplate.opsForValue().set(tokenKey, "blacklisted", 2, TimeUnit.HOURS);
        }
    }
}