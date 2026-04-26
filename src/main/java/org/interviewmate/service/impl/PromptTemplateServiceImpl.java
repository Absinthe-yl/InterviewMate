package org.interviewmate.service.impl;

import lombok.RequiredArgsConstructor;
import org.interviewmate.common.exception.BusinessException;
import org.interviewmate.dto.PromptTemplateResponse;
import org.interviewmate.dto.PromptTemplateUpdateRequest;
import org.interviewmate.entity.PromptTemplate;
import org.interviewmate.mapper.PromptTemplateMapper;
import org.interviewmate.service.PromptTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final PromptTemplateMapper promptTemplateMapper;

    @Override
    public List<PromptTemplateResponse> getAllTemplates() {
        return promptTemplateMapper.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PromptTemplateResponse getByKey(String templateKey) {
        PromptTemplate template = promptTemplateMapper.findByKey(templateKey);
        if (template == null) {
            throw new BusinessException("模板不存在: " + templateKey);
        }
        return toResponse(template);
    }

    @Override
    public void updateTemplate(String templateKey, PromptTemplateUpdateRequest request) {
        PromptTemplate template = promptTemplateMapper.findByKey(templateKey);
        if (template == null) {
            throw new BusinessException("模板不存在: " + templateKey);
        }
        template.setTemplateContent(request.getTemplateContent());
        promptTemplateMapper.updateByKey(template);
    }

    @Override
    public String getTemplateContent(String templateKey) {
        PromptTemplate template = promptTemplateMapper.findByKey(templateKey);
        if (template == null) {
            throw new BusinessException("模板不存在: " + templateKey);
        }
        return template.getTemplateContent();
    }

    private PromptTemplateResponse toResponse(PromptTemplate template) {
        PromptTemplateResponse response = new PromptTemplateResponse();
        response.setTemplateKey(template.getTemplateKey());
        response.setTemplateName(template.getTemplateName());
        response.setTemplateContent(template.getTemplateContent());
        response.setDescription(template.getDescription());
        response.setUpdatedAt(template.getUpdatedAt() != null ? template.getUpdatedAt().toString() : null);
        return response;
    }
}