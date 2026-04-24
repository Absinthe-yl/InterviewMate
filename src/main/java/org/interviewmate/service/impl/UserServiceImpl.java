package org.interviewmate.service.impl;

import lombok.RequiredArgsConstructor;
import org.interviewmate.dto.UserInfoResponse;
import org.interviewmate.entity.User;
import org.interviewmate.mapper.UserMapper;
import org.interviewmate.service.UserService;
import org.springframework.stereotype.Service;

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
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getNickname());
    }
}
