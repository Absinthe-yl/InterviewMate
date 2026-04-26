package org.interviewmate.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AnswerResponse {
    private Integer currentRound;
    private Integer totalRounds;
    private BigDecimal roundScore;
    private String roundFeedback;
    private String nextQuestion;
    private Boolean isFinished;
    private BigDecimal totalScore;
}
