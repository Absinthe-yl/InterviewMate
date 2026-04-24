package org.interviewmate.controller;

import lombok.RequiredArgsConstructor;
import org.interviewmate.common.Result;
import org.interviewmate.dto.UserListResponse;
import org.interviewmate.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserListResponse>> getPendingUsers() {
        List<UserListResponse> users = userService.getPendingUsers();
        return Result.success(users);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserListResponse>> getAllUsers() {
        List<UserListResponse> users = userService.getAllUsers();
        return Result.success(users);
    }

    @PutMapping("/users/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approveUser(@PathVariable Long id) {
        userService.approveUser(id);
        return Result.success("审核通过", null);
    }

    @PutMapping("/users/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> rejectUser(@PathVariable Long id) {
        userService.rejectUser(id);
        return Result.success("已拒绝", null);
    }
}
