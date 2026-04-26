package org.interviewmate.dto;

import lombok.Data;

@Data
public class PromptTemplateResponse {
    private String templateKey;
    private String templateName;
    private String templateContent;
    private String description;
    private String updatedAt;
}