package org.interviewmate.service.impl;

import lombok.RequiredArgsConstructor;
import org.interviewmate.dto.LoginRequest;
import org.interviewmate.dto.LoginResponse;
import org.interviewmate.dto.RegisterRequest;
import org.interviewmate.entity.User;
import org.interviewmate.mapper.UserMapper;
import org.interviewmate.security.JwtUtil;
import org.interviewmate.service.AuthService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void register(RegisterRequest request) {
        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());

        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(String.valueOf(user.getId()), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        String tokenKey = "token:" + token;
        redisTemplate.opsForValue().set(tokenKey, "valid", 2, TimeUnit.HOURS);

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
