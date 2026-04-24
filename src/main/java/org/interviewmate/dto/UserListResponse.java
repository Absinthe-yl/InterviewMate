package org.interviewmate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
