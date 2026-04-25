package org.interviewmate.dto;

import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private Integer knowledgeCount;
}