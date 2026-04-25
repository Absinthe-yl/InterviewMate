package org.interviewmate.controller;

import lombok.RequiredArgsConstructor;
import org.interviewmate.common.Result;
import org.interviewmate.dto.CategoryResponse;
import org.interviewmate.dto.KnowledgeDetailResponse;
import org.interviewmate.dto.KnowledgeListResponse;
import org.interviewmate.dto.PageResult;
import org.interviewmate.service.CategoryService;
import org.interviewmate.service.KnowledgeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final CategoryService categoryService;

    @GetMapping("/search")
    public Result<PageResult<KnowledgeListResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<KnowledgeListResponse> result = knowledgeService.search(keyword, categoryId, page, pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<KnowledgeDetailResponse> getById(@PathVariable Long id) {
        KnowledgeDetailResponse response = knowledgeService.getById(id);
        return Result.success(response);
    }

    @GetMapping("/suggest")
    public Result<List<String>> suggest(@RequestParam String keyword) {
        List<String> suggestions = knowledgeService.suggest(keyword);
        return Result.success(suggestions);
    }

    @GetMapping("/category/{categoryId}")
    public Result<PageResult<KnowledgeListResponse>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<KnowledgeListResponse> result = knowledgeService.search(null, categoryId, page, pageSize);
        return Result.success(result);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "tags", required = false) String tags,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Long id = knowledgeService.upload(file, categoryId, tags, userId);
        return Result.success("上传成功", id);
    }

    @PostMapping("/upload/ai")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> uploadWithAI(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tags", required = false) String tags,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Long id = knowledgeService.uploadWithAIClassify(file, tags, userId);
        return Result.success("AI分类上传成功", id);
    }

    @PostMapping("/upload/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Long>> batchUpload(
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<Long> ids = knowledgeService.batchUploadWithAIClassify(files, userId);
        return Result.success("批量上传成功，共" + ids.size() + "个文件", ids);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tags) {
        knowledgeService.update(id, title, content, categoryId, tags);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        knowledgeService.deleteByIds(ids);
        return Result.success("批量删除成功", null);
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAll() {
        knowledgeService.deleteAll();
        return Result.success("已清空知识库", null);
    }

    @GetMapping("/categories")
    public Result<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
}