package org.interviewmate.service;

import org.interviewmate.entity.InterviewSession;

import java.util.List;
import java.util.Map;

public interface QuestionPreGenerator {

    /**
     * 异步预生成下一个问题
     * @param sessionId 会话ID
     * @param session 会话信息
     * @param resumeText 简历文本
     * @param knowledgeContext 知识库上下文
     * @param history 对话历史
     * @param nextRound 下一轮次
     */
    void preGenerateQuestion(Long sessionId, InterviewSession session,
                             String resumeText, String knowledgeContext,
                             List<Map<String, String>> history, int nextRound);

    /**
     * 获取预生成的问题
     * @param sessionId 会话ID
     * @return 预生成的问题，如果没有则返回null
     */
    String getCachedQuestion(Long sessionId);

    /**
     * 删除缓存的问题
     * @param sessionId 会话ID
     */
    void deleteCachedQuestion(Long sessionId);
}
