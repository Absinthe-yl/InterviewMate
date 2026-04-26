package org.interviewmate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromptTemplateUpdateRequest {
    @NotBlank(message = "模板内容不能为空")
    private String templateContent;
}