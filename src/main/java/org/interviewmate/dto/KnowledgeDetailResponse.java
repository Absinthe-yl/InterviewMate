package org.interviewmate.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String categoryName;
    private String tags;
    private String fileName;
    private Integer viewCount;
    private LocalDateTime createdAt;
}