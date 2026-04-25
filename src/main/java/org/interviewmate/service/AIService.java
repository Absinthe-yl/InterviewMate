package org.interviewmate.service;

import org.interviewmate.entity.Category;

import java.util.List;

public interface AIService {
    /**
     * 使用 AI 分析内容并返回最合适的分类名称
     * @param title 标题
     * @param content 内容
     * @param availableCategories 可用分类列表
     * @return 分类名称
     */
    String classifyKnowledge(String title, String content, List<String> availableCategories);

    /**
     * 生成知识点摘要
     * @param content 内容
     * @return 摘要
     */
    String generateSummary(String content);
}