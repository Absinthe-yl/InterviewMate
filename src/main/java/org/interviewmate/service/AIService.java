package org.interviewmate.service;

import org.interviewmate.entity.Category;

import java.util.List;
import java.util.Map;

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

    /**
     * 从简历推断岗位类型
     * @param resumeText 简历文本
     * @return 岗位类型
     */
    String inferPosition(String resumeText);

    /**
     * 生成面试问题
     * @param positionType 岗位类型
     * @param interviewType 面试类型 (COMPREHENSIVE/BAGU/PROJECT/INTERNSHIP)
     * @param difficulty 难度
     * @param resumeText 简历文本
     * @param knowledgeContext 知识库上下文
     * @param history 对话历史
     * @param round 当前轮次
     * @param totalRounds 总轮次
     * @return 面试问题
     */
    String generateQuestion(String positionType, String interviewType, String difficulty, String resumeText,
                           String knowledgeContext, List<Map<String, String>> history, int round, int totalRounds);

    /**
     * 评价回答
     * @param question 问题
     * @param answer 回答
     * @param positionType 岗位类型
     * @param interviewType 面试类型
     * @return 评价结果JSON (包含score, evaluation, feedback)
     */
    String evaluateAnswer(String question, String answer, String positionType, String interviewType);

    /**
     * 生成面试报告
     * @param positionType 岗位类型
     * @param difficulty 难度
     * @param totalRounds 总轮次
     * @param roundsSummary 各轮次摘要
     * @return 报告JSON (包含strengths, weaknesses, suggestions, overallComment)
     */
    String generateReport(String positionType, String difficulty, int totalRounds, String roundsSummary);

    /**
     * 对话AI（用于一般对话场景）
     * @param userMessage 用户消息
     * @return AI回复
     */
    String chat(String userMessage);
}