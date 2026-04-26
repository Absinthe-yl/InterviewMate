package org.interviewmate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotBlank(message = "回答不能为空")
    private String answer;
}
