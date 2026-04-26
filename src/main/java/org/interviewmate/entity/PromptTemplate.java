package org.interviewmate.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PromptTemplate {
    private Long id;
    private String templateKey;
    private String templateName;
    private String templateContent;
    private String description;
    private LocalDateTime updatedAt;
}