package org.interviewmate.service;

import org.interviewmate.dto.KnowledgeDetailResponse;
import org.interviewmate.dto.KnowledgeListResponse;
import org.interviewmate.dto.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeService {
    PageResult<KnowledgeListResponse> search(String keyword, Long categoryId, int page, int pageSize);
    KnowledgeDetailResponse getById(Long id);
    List<String> suggest(String keyword);
    Long upload(MultipartFile file, Long categoryId, String tags, Long userId);
    void update(Long id, String title, String content, Long categoryId, String tags);
    void delete(Long id);
    void deleteByIds(List<Long> ids);
    void deleteAll();

    // AI 智能分类上传
    Long uploadWithAIClassify(MultipartFile file, String tags, Long userId);
    List<Long> batchUploadWithAIClassify(List<MultipartFile> files, Long userId);
}