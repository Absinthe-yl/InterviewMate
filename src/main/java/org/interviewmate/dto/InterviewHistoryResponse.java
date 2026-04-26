package org.interviewmate.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InterviewHistoryResponse {
    private Long id;
    private String positionType;
    private String interviewType;
    private String difficultyLevel;
    private Integer totalRounds;
    private Integer currentRound;
    private String status;
    private BigDecimal totalScore;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
