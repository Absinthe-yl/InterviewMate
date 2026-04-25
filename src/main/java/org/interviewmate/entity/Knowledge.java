package org.interviewmate.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Knowledge {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Long categoryId;
    private String tags;
    private String fileName;
    private Integer viewCount;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}