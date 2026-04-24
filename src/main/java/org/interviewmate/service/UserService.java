package org.interviewmate.service;

import org.interviewmate.dto.UserInfoResponse;
import org.interviewmate.dto.UserListResponse;

import java.util.List;

public interface UserService {
    UserInfoResponse getCurrentUser(Long userId);
    List<UserListResponse> getPendingUsers();
    List<UserListResponse> getAllUsers();
    void approveUser(Long userId);
    void rejectUser(Long userId);
}