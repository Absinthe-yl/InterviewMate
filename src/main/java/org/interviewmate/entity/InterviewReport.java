package org.interviewmate.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewReport {
    private Long id;
    private Long sessionId;
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private String overallComment;
    private LocalDateTime createdAt;
}
