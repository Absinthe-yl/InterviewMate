package org.interviewmate.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InterviewSessionResponse {
    private Long id;
    private String resumeFileName;
    private String positionType;
    private String difficultyLevel;
    private Integer totalRounds;
    private Integer currentRound;
    private String status;
    private BigDecimal totalScore;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private List<RoundDetail> rounds;

    @Data
    public static class RoundDetail {
        private Integer roundNumber;
        private String questionCategory;
        private String question;
        private String userAnswer;
        private BigDecimal score;
        private String feedback;
    }
}
