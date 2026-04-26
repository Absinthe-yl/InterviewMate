package org.interviewmate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.interviewmate.common.Result;
import org.interviewmate.dto.PromptTemplateResponse;
import org.interviewmate.dto.PromptTemplateUpdateRequest;
import org.interviewmate.service.PromptTemplateService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/prompts")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<PromptTemplateResponse>> getAllTemplates() {
        List<PromptTemplateResponse> templates = promptTemplateService.getAllTemplates();
        return Result.success(templates);
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PromptTemplateResponse> getByKey(@PathVariable String key) {
        PromptTemplateResponse template = promptTemplateService.getByKey(key);
        return Result.success(template);
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateTemplate(
            @PathVariable String key,
            @Valid @RequestBody PromptTemplateUpdateRequest request) {
        promptTemplateService.updateTemplate(key, request);
        return Result.success("更新成功", null);
    }
}