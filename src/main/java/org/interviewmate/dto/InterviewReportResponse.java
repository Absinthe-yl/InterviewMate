package org.interviewmate.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InterviewReportResponse {
    private Long sessionId;
    private String positionType;
    private String difficultyLevel;
    private BigDecimal totalScore;
    private Integer totalRounds;
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private String overallComment;
    private LocalDateTime createdAt;
}
