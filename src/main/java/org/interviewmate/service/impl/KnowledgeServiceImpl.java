package org.interviewmate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.interviewmate.dto.KnowledgeDetailResponse;
import org.interviewmate.dto.KnowledgeListResponse;
import org.interviewmate.dto.PageResult;
import org.interviewmate.entity.Category;
import org.interviewmate.entity.Knowledge;
import org.interviewmate.mapper.CategoryMapper;
import org.interviewmate.mapper.KnowledgeMapper;
import org.interviewmate.service.AIService;
import org.interviewmate.service.KnowledgeService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final CategoryMapper categoryMapper;
    private final AIService aiService;

    @Override
    public PageResult<KnowledgeListResponse> search(String keyword, Long categoryId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Knowledge> list = knowledgeMapper.search(keyword, categoryId, offset, pageSize);
        int total = knowledgeMapper.count(keyword, categoryId);

        List<KnowledgeListResponse> responseList = list.stream().map(this::toListResponse).collect(Collectors.toList());
        return new PageResult<>(responseList, total, page, pageSize);
    }

    @Override
    public KnowledgeDetailResponse getById(Long id) {
        Knowledge knowledge = knowledgeMapper.findById(id);
        if (knowledge == null) {
            throw new RuntimeException("知识点不存在");
        }

        knowledgeMapper.incrementViewCount(id);
        return toDetailResponse(knowledge);
    }

    @Override
    public List<String> suggest(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return knowledgeMapper.suggest(keyword.trim());
    }

    @Override
    public Long upload(MultipartFile file, Long categoryId, String tags, Long userId) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".md")) {
            throw new RuntimeException("只支持 Markdown 文件");
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String title = fileName.substring(0, fileName.length() - 3);
            String summary = aiService.generateSummary(content);

            Knowledge knowledge = new Knowledge();
            knowledge.setTitle(title);
            knowledge.setContent(content);
            knowledge.setSummary(summary);
            knowledge.setCategoryId(categoryId);
            knowledge.setTags(tags);
            knowledge.setFileName(fileName);
            knowledge.setCreatedBy(userId);

            knowledgeMapper.insert(knowledge);
            log.info("上传知识点成功: id={}, title={}, categoryId={}", knowledge.getId(), title, categoryId);

            return knowledge.getId();
        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new RuntimeException("读取文件失败: " + e.getMessage());
        }
    }

    @Override
    public Long uploadWithAIClassify(MultipartFile file, String tags, Long userId) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".md")) {
            throw new RuntimeException("只支持 Markdown 文件");
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            // 检查内容是否为空或只有空白字符
            if (content.trim().isEmpty()) {
                throw new RuntimeException("文件内容为空");
            }

            // 从内容提取标题（优先），否则从文件名提取
            String title = extractTitleFromPath(fileName, content);

            // 如果标题还是空，跳过
            if (title == null || title.trim().isEmpty()) {
                throw new RuntimeException("无法提取标题");
            }

            // 获取所有分类
            List<Category> categories = categoryMapper.findAll();
            List<String> categoryNames = categories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());

            // AI 分类（根据标题）
            String categoryName = aiService.classifyKnowledge(title, content, categoryNames);
            log.info("AI分类结果: title={}, category={}", title, categoryName);

            // 查找分类ID
            Long categoryId = null;
            for (Category cat : categories) {
                if (cat.getName().equals(categoryName)) {
                    categoryId = cat.getId();
                    break;
                }
            }

            String summary = aiService.generateSummary(content);

            Knowledge knowledge = new Knowledge();
            knowledge.setTitle(title);
            knowledge.setContent(content);
            knowledge.setSummary(summary);
            knowledge.setCategoryId(categoryId);
            knowledge.setTags(tags);
            knowledge.setFileName(fileName);
            knowledge.setCreatedBy(userId);

            knowledgeMapper.insert(knowledge);
            log.info("AI分类上传成功: id={}, title={}, category={}", knowledge.getId(), title, categoryName);

            return knowledge.getId();
        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new RuntimeException("读取文件失败: " + e.getMessage());
        }
    }

    private String extractTitleFromPath(String filePath, String content) {
        // 优先从内容中提取第一个 # 标题
        if (content != null && !content.isEmpty()) {
            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("# ")) {
                    return line.substring(2).trim();
                }
            }
        }

        // 否则从文件路径提取文件名作为标题
        String name = filePath;
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        if (name.contains("\\")) {
            name = name.substring(name.lastIndexOf("\\") + 1);
        }
        if (name.toLowerCase().endsWith(".md")) {
            name = name.substring(0, name.length() - 3);
        }
        return name;
    }

    @Override
    public List<Long> batchUploadWithAIClassify(List<MultipartFile> files, Long userId) {
        List<Long> ids = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Long id = uploadWithAIClassify(file, null, userId);
                ids.add(id);
            } catch (Exception e) {
                log.error("批量上传失败: file={}", file.getOriginalFilename(), e);
            }
        }
        return ids;
    }

    @Override
    public void update(Long id, String title, String content, Long categoryId, String tags) {
        Knowledge knowledge = knowledgeMapper.findById(id);
        if (knowledge == null) {
            throw new RuntimeException("知识点不存在");
        }

        if (title != null) knowledge.setTitle(title);
        if (content != null) {
            knowledge.setContent(content);
            knowledge.setSummary(aiService.generateSummary(content));
        }
        if (categoryId != null) knowledge.setCategoryId(categoryId);
        if (tags != null) knowledge.setTags(tags);

        knowledgeMapper.updateById(knowledge);
    }

    @Override
    public void delete(Long id) {
        knowledgeMapper.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            knowledgeMapper.deleteByIds(ids);
        }
    }

    @Override
    public void deleteAll() {
        knowledgeMapper.deleteAll();
    }

    private KnowledgeListResponse toListResponse(Knowledge k) {
        KnowledgeListResponse response = new KnowledgeListResponse();
        response.setId(k.getId());
        response.setTitle(k.getTitle());
        response.setSummary(k.getSummary());
        response.setTags(k.getTags());
        response.setViewCount(k.getViewCount());
        response.setCreatedAt(k.getCreatedAt());

        if (k.getCategoryId() != null) {
            Category category = categoryMapper.findById(k.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
            }
        }
        return response;
    }

    private KnowledgeDetailResponse toDetailResponse(Knowledge k) {
        KnowledgeDetailResponse response = new KnowledgeDetailResponse();
        response.setId(k.getId());
        response.setTitle(k.getTitle());
        response.setContent(k.getContent());
        response.setSummary(k.getSummary());
        response.setTags(k.getTags());
        response.setFileName(k.getFileName());
        response.setViewCount(k.getViewCount() + 1);
        response.setCreatedAt(k.getCreatedAt());

        if (k.getCategoryId() != null) {
            Category category = categoryMapper.findById(k.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
            }
        }
        return response;
    }
}