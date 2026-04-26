package org.interviewmate.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StartInterviewResponse {
    private Long sessionId;
    private String firstQuestion;
    private Integer totalRounds;
    private Integer currentRound;
}
