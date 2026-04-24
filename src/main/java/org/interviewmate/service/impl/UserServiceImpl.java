package org.interviewmate.service.impl;

import lombok.RequiredArgsConstructor;
import org.interviewmate.dto.UserInfoResponse;
import org.interviewmate.dto.UserListResponse;
import org.interviewmate.entity.User;
import org.interviewmate.entity.UserStatus;
import org.interviewmate.mapper.UserMapper;
import org.interviewmate.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserInfoResponse getCurrentUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getNickname(), user.getRole(), user.getStatus());
    }

    @Override
    public List<UserListResponse> getPendingUsers() {
        List<User> users = userMapper.findByStatus(UserStatus.PENDING.name());
        return users.stream()
                .map(u -> new UserListResponse(u.getId(), u.getUsername(), u.getNickname(), u.getRole(), u.getStatus(), u.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserListResponse> getAllUsers() {
        List<User> users = userMapper.findAll();
        return users.stream()
                .map(u -> new UserListResponse(u.getId(), u.getUsername(), u.getNickname(), u.getRole(), u.getStatus(), u.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void approveUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.updateStatus(userId, UserStatus.APPROVED.name());
    }

    @Override
    public void rejectUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.updateStatus(userId, UserStatus.REJECTED.name());
    }
}
