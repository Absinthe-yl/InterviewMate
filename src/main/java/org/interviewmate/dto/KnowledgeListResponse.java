package org.interviewmate.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeListResponse {
    private Long id;
    private String title;
    private String summary;
    private String categoryName;
    private String tags;
    private Integer viewCount;
    private LocalDateTime createdAt;
}