package org.interviewmate.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.interviewmate.common.exception.BusinessException;
import org.interviewmate.dto.*;
import org.interviewmate.entity.InterviewReport;
import org.interviewmate.entity.InterviewRound;
import org.interviewmate.entity.InterviewSession;
import org.interviewmate.entity.Knowledge;
import org.interviewmate.mapper.InterviewReportMapper;
import org.interviewmate.mapper.InterviewRoundMapper;
import org.interviewmate.mapper.InterviewSessionMapper;
import org.interviewmate.mapper.KnowledgeMapper;
import org.interviewmate.service.AIService;
import org.interviewmate.service.FileParserService;
import org.interviewmate.service.InterviewService;
import org.interviewmate.service.QuestionPreGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewRoundMapper roundMapper;
    private final InterviewReportMapper reportMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final AIService aiService;
    private final FileParserService fileParserService;
    private final QuestionPreGenerator questionPreGenerator;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int DEFAULT_TOTAL_ROUNDS = 5;
    private static final int CONTEXT_TTL_MINUTES = 30;

    @Override
    public StartInterviewResponse startInterview(Long userId, MultipartFile resumeFile, String positionType, String interviewType, String difficulty, Integer totalRounds) {
        // 1. 确定面试类型（默认综合模拟）
        String type = (interviewType != null && !interviewType.isEmpty()) ? interviewType : "COMPREHENSIVE";

        // 2. 读取简历内容
        String resumeText = "";
        String fileName = "";
        if (resumeFile != null && !resumeFile.isEmpty()) {
            fileName = resumeFile.getOriginalFilename();
            String fullResumeText = fileParserService.parseFile(resumeFile);
            // 根据面试类型提取简历相关部分
            resumeText = extractResumeByType(fullResumeText, type);
        }

        // 3. AI 推断岗位类型（如果没有指定）
        String inferredPosition = positionType;
        if (inferredPosition == null || inferredPosition.isEmpty()) {
            if (!resumeText.isEmpty()) {
                inferredPosition = aiService.inferPosition(resumeText);
            } else {
                inferredPosition = "Java后端";
            }
        }

        // 4. 从简历提取关键词，搜索知识库（八股模拟也使用知识库）
        String knowledgeContext = "";
        if ("BAGU".equals(type)) {
            // 八股模拟：根据岗位类型生成默认知识库关键词
            List<String> defaultKeywords = getDefaultKeywordsByPosition(inferredPosition);
            List<Knowledge> relatedKnowledge = knowledgeMapper.searchByKeywords(defaultKeywords, 10);
            knowledgeContext = buildKnowledgeContext(relatedKnowledge);
        } else if (!resumeText.isEmpty()) {
            List<String> keywords = extractKeywords(resumeText);
            if (!keywords.isEmpty()) {
                List<Knowledge> relatedKnowledge = knowledgeMapper.searchByKeywords(keywords, 10);
                knowledgeContext = buildKnowledgeContext(relatedKnowledge);
            }
        }

        // 5. 确定面试轮次（3-20轮）
        int rounds = (totalRounds != null && totalRounds >= 3 && totalRounds <= 20) ? totalRounds : DEFAULT_TOTAL_ROUNDS;

        // 6. 创建会话
        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setResumeFileName(fileName);
        session.setResumeData(resumeText.length() > 5000 ? resumeText.substring(0, 5000) : resumeText);
        session.setPositionType(inferredPosition);
        session.setInterviewType(type);
        session.setDifficultyLevel(difficulty != null ? difficulty : "MEDIUM");
        session.setTotalRounds(rounds);
        session.setCurrentRound(0);
        session.setStatus("IN_PROGRESS");
        session.setStartedAt(LocalDateTime.now());
        sessionMapper.insert(session);

        // 7. 缓存上下文到 Redis
        Map<String, Object> context = new HashMap<>();
        context.put("sessionId", session.getId());
        context.put("round", 0);
        context.put("positionType", session.getPositionType());
        context.put("interviewType", type);
        context.put("difficulty", session.getDifficultyLevel());
        context.put("resumeText", resumeText);
        context.put("knowledgeContext", knowledgeContext);
        context.put("history", new ArrayList<>());
        saveContext(session.getId(), context);

        // 8. 生成第一个问题
        String firstQuestion = aiService.generateQuestion(
                session.getPositionType(),
                type,
                session.getDifficultyLevel(),
                resumeText,
                knowledgeContext,
                Collections.emptyList(),
                1,
                rounds
        );

        // 9. 保存第一轮问题
        InterviewRound round = new InterviewRound();
        round.setSessionId(session.getId());
        round.setRoundNumber(1);
        round.setQuestion(firstQuestion);
        round.setStatus("ASKING");
        roundMapper.insert(round);

        // 10. 更新会话当前轮次
        session.setCurrentRound(1);
        sessionMapper.updateById(session);

        // 11. 返回响应
        StartInterviewResponse response = new StartInterviewResponse();
        response.setSessionId(session.getId());
        response.setFirstQuestion(firstQuestion);
        response.setTotalRounds(rounds);
        response.setCurrentRound(1);
        return response;
    }

    @Override
    public AnswerResponse answer(Long sessionId, String answer) {
        // 1. 获取会话
        InterviewSession session = sessionMapper.findById(sessionId);
        if (session == null) {
            throw new BusinessException("面试会话不存在");
        }
        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new BusinessException("面试已结束");
        }

        // 2. 获取当前轮次
        List<InterviewRound> rounds = roundMapper.findBySessionId(sessionId);
        InterviewRound currentRound = rounds.stream()
                .filter(r -> r.getRoundNumber().equals(session.getCurrentRound()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("当前轮次不存在"));

        // 3. 保存用户回答
        currentRound.setUserAnswer(answer);
        currentRound.setStatus("ANSWERED");

        // 4. AI 评分
        String interviewType = session.getInterviewType() != null ? session.getInterviewType() : "COMPREHENSIVE";
        String evaluationJson = aiService.evaluateAnswer(
                currentRound.getQuestion(),
                answer,
                session.getPositionType(),
                interviewType
        );

        // 清理 AI 返回的 JSON（去除 markdown 代码块）
        evaluationJson = cleanJsonResponse(evaluationJson);

        BigDecimal score = BigDecimal.ZERO;
        String feedback = "";
        String evaluation = "";
        try {
            JsonNode jsonNode = objectMapper.readTree(evaluationJson);
            if (jsonNode.has("score")) {
                score = new BigDecimal(jsonNode.get("score").asText()).setScale(2, RoundingMode.HALF_UP);
            }
            if (jsonNode.has("feedback")) {
                feedback = jsonNode.get("feedback").asText();
            }
            if (jsonNode.has("evaluation")) {
                evaluation = jsonNode.get("evaluation").asText();
            }
        } catch (Exception e) {
            log.warn("解析评分结果失败: {}", e.getMessage());
            feedback = evaluationJson;
        }

        currentRound.setScore(score);
        currentRound.setFeedback(feedback);
        currentRound.setAiEvaluation(evaluation);
        currentRound.setStatus("EVALUATED");
        roundMapper.updateById(currentRound);

        // 5. 更新上下文
        Map<String, Object> context = getContext(sessionId);
        if (context != null) {
            List<Map<String, String>> history = (List<Map<String, String>>) context.get("history");
            if (history == null) history = new ArrayList<>();
            history.add(Map.of("role", "assistant", "content", currentRound.getQuestion()));
            history.add(Map.of("role", "user", "content", answer));
            context.put("history", history);
            context.put("round", session.getCurrentRound());
            saveContext(sessionId, context);
        }

        // 6. 构建响应
        AnswerResponse response = new AnswerResponse();
        response.setCurrentRound(session.getCurrentRound());
        response.setTotalRounds(session.getTotalRounds());
        response.setRoundScore(score);
        response.setRoundFeedback(feedback);

        // 7. 判断是否结束
        if (session.getCurrentRound() >= session.getTotalRounds()) {
            // 面试结束
            endInterview(session, rounds);
            response.setIsFinished(true);
            response.setTotalScore(session.getTotalScore());
        } else {
            // 生成下一题
            String resumeText = context != null ? (String) context.get("resumeText") : "";
            String knowledgeContext = context != null ? (String) context.get("knowledgeContext") : "";
            List<Map<String, String>> history = context != null ? (List<Map<String, String>>) context.get("history") : Collections.emptyList();

            int nextRoundNum = session.getCurrentRound() + 1;
            String nextQuestion;

            // 尝试获取预生成的问题
            String cachedQuestion = questionPreGenerator.getCachedQuestion(sessionId);
            if (cachedQuestion != null) {
                log.info("使用预生成的问题: sessionId={}, round={}", sessionId, nextRoundNum);
                nextQuestion = cachedQuestion;
                questionPreGenerator.deleteCachedQuestion(sessionId);
            } else {
                // 同步生成（兜底）
                log.info("同步生成问题: sessionId={}, round={}", sessionId, nextRoundNum);
                nextQuestion = aiService.generateQuestion(
                        session.getPositionType(),
                        interviewType,
                        session.getDifficultyLevel(),
                        resumeText,
                        knowledgeContext,
                        history,
                        nextRoundNum,
                        session.getTotalRounds()
                );
            }

            // 保存下一轮
            InterviewRound nextRound = new InterviewRound();
            nextRound.setSessionId(sessionId);
            nextRound.setRoundNumber(nextRoundNum);
            nextRound.setQuestion(nextQuestion);
            nextRound.setStatus("ASKING");
            roundMapper.insert(nextRound);

            // 更新会话
            session.setCurrentRound(nextRoundNum);
            sessionMapper.updateById(session);

            // 异步预生成下下个问题（如果不是最后一轮）
            if (nextRoundNum < session.getTotalRounds()) {
                List<Map<String, String>> newHistory = new ArrayList<>(history);
                newHistory.add(Map.of("role", "assistant", "content", nextQuestion));
                questionPreGenerator.preGenerateQuestion(
                        sessionId, session, resumeText, knowledgeContext,
                        newHistory, nextRoundNum + 1
                );
            }

            response.setNextQuestion(nextQuestion);
            response.setIsFinished(false);
        }

        return response;
    }

    private void endInterview(InterviewSession session, List<InterviewRound> rounds) {
        // 1. 筛选已回答的轮次（有分数的轮次）
        List<InterviewRound> answeredRounds = rounds.stream()
                .filter(r -> r.getScore() != null)
                .collect(Collectors.toList());

        // 2. 计算总分（只基于已回答的轮次）
        BigDecimal totalScore = BigDecimal.ZERO;
        if (!answeredRounds.isEmpty()) {
            totalScore = answeredRounds.stream()
                    .map(InterviewRound::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(answeredRounds.size()), 2, RoundingMode.HALF_UP);
        }

        session.setTotalScore(totalScore);
        session.setStatus("COMPLETED");
        session.setEndedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        // 3. 生成报告（只包含已回答的轮次）
        StringBuilder summary = new StringBuilder();
        summary.append("面试共").append(session.getTotalRounds()).append("轮，实际完成").append(answeredRounds.size()).append("轮\n\n");

        for (InterviewRound round : answeredRounds) {
            summary.append("第").append(round.getRoundNumber()).append("轮:\n");
            summary.append("问题: ").append(round.getQuestion()).append("\n");
            summary.append("回答: ").append(round.getUserAnswer() != null ? round.getUserAnswer() : "未回答").append("\n");
            summary.append("得分: ").append(round.getScore()).append("\n\n");
        }

        // 如果有未回答的轮次，也记录下来
        List<InterviewRound> unansweredRounds = rounds.stream()
                .filter(r -> r.getScore() == null)
                .collect(Collectors.toList());
        if (!unansweredRounds.isEmpty()) {
            summary.append("【未完成的轮次】\n");
            for (InterviewRound round : unansweredRounds) {
                summary.append("第").append(round.getRoundNumber()).append("轮: ").append(round.getQuestion()).append("\n");
            }
        }

        String reportJson = aiService.generateReport(
                session.getPositionType(),
                session.getDifficultyLevel(),
                answeredRounds.size(),  // 使用实际完成的轮次数
                summary.toString()
        );

        // 清理 AI 返回的 JSON（去除 markdown 代码块）
        reportJson = cleanJsonResponse(reportJson);

        InterviewReport report = new InterviewReport();
        report.setSessionId(session.getId());
        try {
            JsonNode jsonNode = objectMapper.readTree(reportJson);
            if (jsonNode.has("strengths")) report.setStrengths(jsonNode.get("strengths").asText());
            if (jsonNode.has("weaknesses")) report.setWeaknesses(jsonNode.get("weaknesses").asText());
            if (jsonNode.has("suggestions")) report.setSuggestions(jsonNode.get("suggestions").asText());
            if (jsonNode.has("overallComment")) report.setOverallComment(jsonNode.get("overallComment").asText());
        } catch (Exception e) {
            log.warn("解析报告失败: {}", e.getMessage());
            report.setOverallComment(reportJson);
        }
        reportMapper.insert(report);

        // 3. 删除 Redis 缓存
        redisTemplate.delete("interview:context:" + session.getId());
    }

    @Override
    public InterviewSessionResponse getSession(Long sessionId) {
        InterviewSession session = sessionMapper.findById(sessionId);
        if (session == null) {
            throw new BusinessException("面试会话不存在");
        }

        List<InterviewRound> rounds = roundMapper.findBySessionId(sessionId);

        InterviewSessionResponse response = new InterviewSessionResponse();
        response.setId(session.getId());
        response.setResumeFileName(session.getResumeFileName());
        response.setPositionType(session.getPositionType());
        response.setDifficultyLevel(session.getDifficultyLevel());
        response.setTotalRounds(session.getTotalRounds());
        response.setCurrentRound(session.getCurrentRound());
        response.setStatus(session.getStatus());
        response.setTotalScore(session.getTotalScore());
        response.setStartedAt(session.getStartedAt());
        response.setEndedAt(session.getEndedAt());

        List<InterviewSessionResponse.RoundDetail> roundDetails = new ArrayList<>();
        for (InterviewRound round : rounds) {
            InterviewSessionResponse.RoundDetail detail = new InterviewSessionResponse.RoundDetail();
            detail.setRoundNumber(round.getRoundNumber());
            detail.setQuestionCategory(round.getQuestionCategory());
            detail.setQuestion(round.getQuestion());
            detail.setUserAnswer(round.getUserAnswer());
            detail.setScore(round.getScore());
            detail.setFeedback(round.getFeedback());
            roundDetails.add(detail);
        }
        response.setRounds(roundDetails);

        return response;
    }

    @Override
    public List<InterviewHistoryResponse> getHistory(Long userId) {
        List<InterviewSession> sessions = sessionMapper.findByUserId(userId);
        List<InterviewHistoryResponse> history = new ArrayList<>();

        for (InterviewSession session : sessions) {
            InterviewHistoryResponse response = new InterviewHistoryResponse();
            response.setId(session.getId());
            response.setPositionType(session.getPositionType());
            response.setInterviewType(session.getInterviewType());
            response.setDifficultyLevel(session.getDifficultyLevel());
            response.setTotalRounds(session.getTotalRounds());
            response.setCurrentRound(session.getCurrentRound());
            response.setStatus(session.getStatus());
            response.setTotalScore(session.getTotalScore());
            response.setCreatedAt(session.getCreatedAt());
            response.setEndedAt(session.getEndedAt());
            history.add(response);
        }

        return history;
    }

    @Override
    public InterviewReportResponse getReport(Long sessionId) {
        InterviewSession session = sessionMapper.findById(sessionId);
        if (session == null) {
            throw new BusinessException("面试会话不存在");
        }

        InterviewReport report = reportMapper.findBySessionId(sessionId);
        if (report == null) {
            throw new BusinessException("面试报告不存在");
        }

        InterviewReportResponse response = new InterviewReportResponse();
        response.setSessionId(sessionId);
        response.setPositionType(session.getPositionType());
        response.setDifficultyLevel(session.getDifficultyLevel());
        response.setTotalScore(session.getTotalScore());
        response.setTotalRounds(session.getTotalRounds());
        response.setStrengths(report.getStrengths());
        response.setWeaknesses(report.getWeaknesses());
        response.setSuggestions(report.getSuggestions());
        response.setOverallComment(report.getOverallComment());
        response.setCreatedAt(report.getCreatedAt());

        return response;
    }

    @Override
    public void deleteSession(Long sessionId) {
        reportMapper.deleteBySessionId(sessionId);
        roundMapper.deleteBySessionId(sessionId);
        sessionMapper.deleteById(sessionId);
        redisTemplate.delete("interview:context:" + sessionId);
    }

    @Override
    public void endInterview(Long sessionId) {
        InterviewSession session = sessionMapper.findById(sessionId);
        if (session == null) {
            throw new BusinessException("面试会话不存在");
        }
        if ("COMPLETED".equals(session.getStatus())) {
            throw new BusinessException("面试已结束");
        }

        // 获取所有轮次
        List<InterviewRound> rounds = roundMapper.findBySessionId(sessionId);

        // 结束面试
        endInterview(session, rounds);
    }

    private void saveContext(Long sessionId, Map<String, Object> context) {
        try {
            String json = objectMapper.writeValueAsString(context);
            redisTemplate.opsForValue().set("interview:context:" + sessionId, json, CONTEXT_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("保存上下文失败", e);
        }
    }

    private Map<String, Object> getContext(Long sessionId) {
        try {
            String json = redisTemplate.opsForValue().get("interview:context:" + sessionId);
            if (json != null) {
                return objectMapper.readValue(json, Map.class);
            }
        } catch (Exception e) {
            log.error("获取上下文失败", e);
        }
        return null;
    }

    /**
     * 从简历文本中提取关键词
     */
    private List<String> extractKeywords(String text) {
        // 常见技术关键词
        String[] techKeywords = {
            "Java", "Spring", "SpringBoot", "MyBatis", "MySQL", "Redis", "Kafka",
            "RabbitMQ", "Docker", "Kubernetes", "K8s", "Linux", "Nginx", "Tomcat",
            "Vue", "React", "JavaScript", "TypeScript", "HTML", "CSS", "Node.js",
            "Python", "Django", "Flask", "Go", "Golang", "微服务", "分布式",
            "算法", "数据结构", "JVM", "并发", "多线程", "锁", "事务",
            "索引", "SQL", "NoSQL", "MongoDB", "Elasticsearch", "ES",
            "Netty", "RPC", "Dubbo", "Feign", "Gateway", "Nacos", "Sentinel",
            "设计模式", "单例", "工厂", "代理", "观察者", "策略",
            "Git", "Maven", "Gradle", "Jenkins", "CI/CD", "DevOps",
            "RESTful", "API", "JSON", "XML", "HTTP", "HTTPS", "TCP", "UDP",
            "消息队列", "缓存", "负载均衡", "高可用", "高并发"
        };

        Set<String> found = new LinkedHashSet<>();
        String upperText = text.toUpperCase();

        for (String keyword : techKeywords) {
            if (upperText.contains(keyword.toUpperCase())) {
                found.add(keyword);
            }
        }

        return new ArrayList<>(found).stream().limit(10).collect(Collectors.toList());
    }

    /**
     * 构建知识库上下文
     */
    private String buildKnowledgeContext(List<Knowledge> knowledgeList) {
        if (knowledgeList == null || knowledgeList.isEmpty()) {
            return "暂无相关知识点";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(knowledgeList.size(), 5); i++) {
            Knowledge k = knowledgeList.get(i);
            sb.append(i + 1).append(". ").append(k.getTitle()).append("\n");
            if (k.getSummary() != null && !k.getSummary().isEmpty()) {
                sb.append("   ").append(k.getSummary()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 清理 AI 返回的 JSON，去除 markdown 代码块包裹
     */
    private String cleanJsonResponse(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }

        String trimmed = response.trim();

        // 去除 ```json ... ``` 或 ``` ... ``` 包裹
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }

        return trimmed.trim();
    }

    /**
     * 根据面试类型提取简历相关部分
     */
    private String extractResumeByType(String fullResume, String interviewType) {
        if (fullResume == null || fullResume.isEmpty()) {
            return "";
        }

        switch (interviewType) {
            case "PROJECT":
                // 项目模拟：只提取项目经历部分
                return extractProjectSection(fullResume);
            case "INTERNSHIP":
                // 实习模拟：只提取实习经历部分
                return extractInternshipSection(fullResume);
            case "BAGU":
                // 八股模拟：不需要简历
                return "";
            default:
                // 综合模拟：使用完整简历
                return fullResume;
        }
    }

    /**
     * 提取简历中的项目经历部分
     */
    private String extractProjectSection(String resume) {
        StringBuilder projectSection = new StringBuilder();

        // 常见的项目经历标题关键词
        String[] projectKeywords = {"项目经历", "项目经验", "项目", "Project", "项目描述", "项目名称"};

        // 按行分割简历
        String[] lines = resume.split("\n");
        boolean inProjectSection = false;
        int projectCount = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // 检测是否进入项目经历部分
            for (String keyword : projectKeywords) {
                if (line.contains(keyword) && line.length() < 30) {
                    inProjectSection = true;
                    projectSection.append("【项目经历】\n");
                    break;
                }
            }

            // 检测是否离开项目经历部分（遇到其他大标题）
            if (inProjectSection && isOtherSectionTitle(line)) {
                inProjectSection = false;
            }

            // 收集项目经历内容
            if (inProjectSection && !line.isEmpty()) {
                projectSection.append(line).append("\n");
                projectCount++;
            }
        }

        // 如果没有找到明确的项目经历标题，尝试通过项目特征识别
        if (projectSection.length() == 0) {
            projectSection.append(extractProjectsByPattern(resume));
        }

        return projectSection.length() > 0 ? projectSection.toString() : "未找到项目经历";
    }

    /**
     * 提取简历中的实习经历部分
     */
    private String extractInternshipSection(String resume) {
        StringBuilder internshipSection = new StringBuilder();

        // 常见的实习经历标题关键词
        String[] internshipKeywords = {"实习经历", "实习经验", "实习", "Internship", "实习公司", "实习岗位"};

        String[] lines = resume.split("\n");
        boolean inInternshipSection = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            for (String keyword : internshipKeywords) {
                if (line.contains(keyword) && line.length() < 30) {
                    inInternshipSection = true;
                    internshipSection.append("【实习经历】\n");
                    break;
                }
            }

            if (inInternshipSection && isOtherSectionTitle(line)) {
                inInternshipSection = false;
            }

            if (inInternshipSection && !line.isEmpty()) {
                internshipSection.append(line).append("\n");
            }
        }

        return internshipSection.length() > 0 ? internshipSection.toString() : "未找到实习经历";
    }

    /**
     * 判断是否是其他章节标题（用于判断是否离开当前章节）
     */
    private boolean isOtherSectionTitle(String line) {
        String[] otherTitles = {
            "教育经历", "教育背景", "技能", "专业技能", "技能清单",
            "工作经历", "工作经验", "自我评价", "个人简介",
            "获奖经历", "荣誉", "证书", "联系方式"
        };

        for (String title : otherTitles) {
            if (line.contains(title) && line.length() < 30) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过模式识别提取项目内容（当没有明确标题时）
     */
    private String extractProjectsByPattern(String resume) {
        StringBuilder result = new StringBuilder();
        String[] lines = resume.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            // 项目特征：包含项目名称、技术栈、时间等
            if (trimmed.contains("项目") ||
                trimmed.contains("系统") ||
                trimmed.contains("平台") ||
                trimmed.contains("开发") ||
                trimmed.contains("实现") ||
                trimmed.contains("负责") ||
                trimmed.contains("技术栈") ||
                trimmed.contains("Spring") ||
                trimmed.contains("Vue") ||
                trimmed.contains("React")) {
                result.append(trimmed).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * 根据岗位类型获取默认知识库关键词（用于八股模拟）
     */
    private List<String> getDefaultKeywordsByPosition(String positionType) {
        // 各岗位的常见技术栈关键词
        Map<String, List<String>> positionKeywords = new HashMap<>();

        positionKeywords.put("Java后端", Arrays.asList(
            "Java", "Spring", "SpringBoot", "MyBatis", "JVM", "并发", "多线程",
            "MySQL", "Redis", "集合", "HashMap", "线程池", "锁", "事务"
        ));

        positionKeywords.put("前端开发", Arrays.asList(
            "JavaScript", "Vue", "React", "TypeScript", "HTML", "CSS",
            "浏览器", "HTTP", "异步", "Promise", "DOM", "事件"
        ));

        positionKeywords.put("Python后端", Arrays.asList(
            "Python", "Django", "Flask", "MySQL", "Redis",
            "异步", "装饰器", "生成器", "GIL", "GC"
        ));

        positionKeywords.put("Go后端", Arrays.asList(
            "Go", "Golang", "goroutine", "channel", "GC",
            "并发", "切片", "map", "interface", "反射"
        ));

        positionKeywords.put("算法工程师", Arrays.asList(
            "算法", "数据结构", "数组", "链表", "树", "图",
            "排序", "查找", "动态规划", "贪心", "回溯"
        ));

        positionKeywords.put("系统设计", Arrays.asList(
            "分布式", "微服务", "缓存", "消息队列", "负载均衡",
            "高并发", "高可用", "CAP", "一致性", "限流"
        ));

        // 返回对应岗位的关键词，如果没匹配到则返回 Java 后端的关键词
        return positionKeywords.getOrDefault(positionType, positionKeywords.get("Java后端"));
    }
}