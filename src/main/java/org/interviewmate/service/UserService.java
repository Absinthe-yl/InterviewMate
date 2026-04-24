package org.interviewmate.service;

import org.interviewmate.dto.UserInfoResponse;

public interface UserService {
    UserInfoResponse getCurrentUser(Long userId);
}
