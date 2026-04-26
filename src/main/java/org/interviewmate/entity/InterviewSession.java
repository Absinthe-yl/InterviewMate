package org.interviewmate.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InterviewSession {
    private Long id;
    private Long userId;
    private String resumeFileName;
    private String resumeData;
    private String positionType;
    private String interviewType;  // COMPREHENSIVE/BAGU/PROJECT/INTERNSHIP
    private String difficultyLevel;
    private Integer totalRounds;
    private Integer currentRound;
    private String status;
    private BigDecimal totalScore;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
