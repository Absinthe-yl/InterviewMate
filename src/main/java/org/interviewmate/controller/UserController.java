package org.interviewmate.controller;

import lombok.RequiredArgsConstructor;
import org.interviewmate.dto.UserInfoResponse;
import org.interviewmate.service.UserService;
import org.interviewmate.common.Result;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserInfoResponse response = userService.getCurrentUser(userId);
        return Result.success(response);
    }
}
