package org.interviewmate.service;

import org.interviewmate.dto.PromptTemplateResponse;
import org.interviewmate.dto.PromptTemplateUpdateRequest;

import java.util.List;

public interface PromptTemplateService {
    List<PromptTemplateResponse> getAllTemplates();
    PromptTemplateResponse getByKey(String templateKey);
    void updateTemplate(String templateKey, PromptTemplateUpdateRequest request);
    String getTemplateContent(String templateKey);
}