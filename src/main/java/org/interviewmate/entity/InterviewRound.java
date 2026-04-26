package org.interviewmate.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InterviewRound {
    private Long id;
    private Long sessionId;
    private Integer roundNumber;
    private String questionCategory;
    private String question;
    private String userAnswer;
    private String aiEvaluation;
    private BigDecimal score;
    private String feedback;
    private String status;
    private LocalDateTime createdAt;
}
