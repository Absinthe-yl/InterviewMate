package org.interviewmate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.interviewmate.entity.InterviewSession;
import org.interviewmate.service.AIService;
import org.interviewmate.service.QuestionPreGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionPreGeneratorImpl implements QuestionPreGenerator {

    private final AIService aiService;
    private final StringRedisTemplate redisTemplate;

    private static final String CACHE_KEY_PREFIX = "interview:next_question:";
    private static final int CACHE_TTL_MINUTES = 30;

    @Override
    @Async("aiTaskExecutor")
    public void preGenerateQuestion(Long sessionId, InterviewSession session,
                                    String resumeText, String knowledgeContext,
                                    List<Map<String, String>> history, int nextRound) {
        try {
            log.info("开始预生成问题: sessionId={}, nextRound={}", sessionId, nextRound);

            String interviewType = session.getInterviewType() != null ? session.getInterviewType() : "COMPREHENSIVE";

            String question = aiService.generateQuestion(
                    session.getPositionType(),
                    interviewType,
                    session.getDifficultyLevel(),
                    resumeText,
                    knowledgeContext,
                    history,
                    nextRound,
                    session.getTotalRounds()
            );

            // 存入Redis缓存
            String key = CACHE_KEY_PREFIX + sessionId;
            redisTemplate.opsForValue().set(key, question, CACHE_TTL_MINUTES, TimeUnit.MINUTES);

            log.info("预生成问题完成: sessionId={}, nextRound={}", sessionId, nextRound);
        } catch (Exception e) {
            log.error("预生成问题失败: sessionId={}, error={}", sessionId, e.getMessage());
        }
    }

    @Override
    public String getCachedQuestion(Long sessionId) {
        String key = CACHE_KEY_PREFIX + sessionId;
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteCachedQuestion(Long sessionId) {
        String key = CACHE_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}