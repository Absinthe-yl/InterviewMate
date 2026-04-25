package org.interviewmate.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}