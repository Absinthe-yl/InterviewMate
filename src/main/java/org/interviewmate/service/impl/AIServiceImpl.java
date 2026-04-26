package org.interviewmate.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.interviewmate.service.AIService;
import org.interviewmate.service.PromptTemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    // 主配置（用于面试）
    @Value("${ai.base-url:https://api.xzzzzz.com}")
    private String baseUrl;

    @Value("${ai.api-key:sk-gwsAzladZzBRoHKYhNoIzq4HFsjwiANQPS5SWbVY9c9FXO9f}")
    private String apiKey;

    @Value("${ai.model:glm-5.1}")
    private String model;

    // 备用配置
    @Value("${ai.backup.base-url:https://api.xzzzzz.com}")
    private String backupBaseUrl;

    @Value("${ai.backup.api-key:sk-EwMo4ryCvFmg7YtcrecX020i8OAkNhPohRK84bGaqwqG1IQa}")
    private String backupApiKey;

    @Value("${ai.backup.model:glm-5.1}")
    private String backupModel;

    // 对话AI配置（DeepSeek）
    @Value("${ai.chat.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String chatBaseUrl;

    @Value("${ai.chat.api-key:ee6feebc-77b0-40b4-bd08-2330066c6efd}")
    private String chatApiKey;

    @Value("${ai.chat.model:deepseek-v3-250324}")
    private String chatModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final PromptTemplateService promptTemplateService;

    // 记录当前使用的是主配置还是备用配置
    private volatile boolean usingBackup = false;

    @Override
    public String classifyKnowledge(String title, String content, List<String> availableCategories) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个技术文档分类专家。请根据以下知识点的标题和内容，选择最合适的分类。\n\n");
        prompt.append("可用分类：\n");
        for (int i = 0; i < availableCategories.size(); i++) {
            prompt.append((i + 1)).append(". ").append(availableCategories.get(i)).append("\n");
        }
        prompt.append("\n知识点标题：").append(title).append("\n");
        prompt.append("\n知识点内容（前500字）：\n");
        prompt.append(content.length() > 500 ? content.substring(0, 500) : content);
        prompt.append("\n\n请只返回分类名称，不要返回其他内容。");

        String response = callAI(prompt.toString(), 100);

        if (response != null) {
            response = response.trim();
            for (String category : availableCategories) {
                if (response.contains(category)) {
                    return category;
                }
            }
        }

        return availableCategories.isEmpty() ? null : availableCategories.get(0);
    }

    @Override
    public String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        String text = content
                .replaceAll("<[^>]+>", "")
                .replaceAll("#+\\s*", "")
                .replaceAll("\\*+", "")
                .replaceAll("`+", "")
                .replaceAll(">\\s*", "")
                .replaceAll("\\n+", " ")
                .trim();

        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }

    @Override
    public String inferPosition(String resumeText) {
        String template = promptTemplateService.getTemplateContent("POSITION_INFER");
        String prompt = template.replace("{resume_text}",
            resumeText.length() > 3000 ? resumeText.substring(0, 3000) : resumeText);

        String result = callAI(prompt, 50);
        return result != null ? result.trim() : "Java后端";
    }

    @Override
    public String generateQuestion(String positionType, String interviewType, String difficulty, String resumeText,
                                   String knowledgeContext, List<Map<String, String>> history, int round, int totalRounds) {
        // 根据面试类型选择不同的模板
        String templateKey = "INTERVIEW_QUESTION";
        if ("BAGU".equals(interviewType)) {
            templateKey = "INTERVIEW_QUESTION_BAGU";
        } else if ("PROJECT".equals(interviewType)) {
            templateKey = "INTERVIEW_QUESTION_PROJECT";
        } else if ("INTERNSHIP".equals(interviewType)) {
            templateKey = "INTERVIEW_QUESTION_INTERNSHIP";
        }

        String template = promptTemplateService.getTemplateContent(templateKey);

        // 构建历史对话
        StringBuilder historyStr = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            for (Map<String, String> h : history) {
                String role = h.get("role");
                String content = h.get("content");
                if (content != null && content.length() > 500) {
                    content = content.substring(0, 500) + "...";
                }
                historyStr.append(role.equals("assistant") ? "面试官" : "候选人")
                         .append("：").append(content).append("\n");
            }
        }

        // 替换模板变量
        String prompt = template
                .replace("{position_type}", positionType)
                .replace("{resume_text}", resumeText.length() > 2000 ? resumeText.substring(0, 2000) : resumeText)
                .replace("{knowledge_context}", knowledgeContext != null ? knowledgeContext : "暂无相关知识点")
                .replace("{difficulty}", difficulty)
                .replace("{round}", String.valueOf(round))
                .replace("{total_rounds}", String.valueOf(totalRounds))
                .replace("{history}", historyStr.toString());

        return callAI(prompt, 200);
    }

    @Override
    public String evaluateAnswer(String question, String answer, String positionType, String interviewType) {
        // 根据面试类型选择不同的评分模板
        String templateKey = "ANSWER_EVALUATE";
        if ("BAGU".equals(interviewType)) {
            templateKey = "ANSWER_EVALUATE_BAGU";
        } else if ("PROJECT".equals(interviewType)) {
            templateKey = "ANSWER_EVALUATE_PROJECT";
        } else if ("INTERNSHIP".equals(interviewType)) {
            templateKey = "ANSWER_EVALUATE_INTERNSHIP";
        }

        String template = promptTemplateService.getTemplateContent(templateKey);

        String prompt = template
                .replace("{position_type}", positionType)
                .replace("{question}", question)
                .replace("{answer}", answer.length() > 2000 ? answer.substring(0, 2000) : answer);

        return callAI(prompt, 500);
    }

    @Override
    public String generateReport(String positionType, String difficulty, int totalRounds, String roundsSummary) {
        String template = promptTemplateService.getTemplateContent("INTERVIEW_REPORT");

        String prompt = template
                .replace("{position_type}", positionType)
                .replace("{difficulty}", difficulty)
                .replace("{total_rounds}", String.valueOf(totalRounds))
                .replace("{rounds_summary}", roundsSummary);

        return callAI(prompt, 500);
    }

    @Override
    public String chat(String userMessage) {
        return callChatAI(userMessage);
    }

    private String callAI(String prompt, int maxTokens) {
        // 先尝试主配置，失败后切换到备用配置
        if (!usingBackup) {
            try {
                return doCallAI(baseUrl, apiKey, model, prompt, maxTokens);
            } catch (Exception e) {
                log.warn("主API调用失败，切换到备用API: {}", e.getMessage());
                usingBackup = true;
            }
        }

        // 使用备用配置
        try {
            String result = doCallAI(backupBaseUrl, backupApiKey, backupModel, prompt, maxTokens);
            // 备用成功后，下次尝试切回主配置
            usingBackup = false;
            return result;
        } catch (Exception e) {
            log.error("备用API也失败: {}", e.getMessage());
            // 备用也失败，重置状态，下次重新尝试主配置
            usingBackup = false;
            throw new RuntimeException("AI调用失败(主备均失败): " + e.getMessage());
        }
    }

    private String doCallAI(String url, String key, String modelName, String prompt, int maxTokens) {
        int maxRetries = 3;
        int retryDelay = 1000;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(key);

                Map<String, Object> message = new HashMap<>();
                message.put("role", "user");
                message.put("content", prompt);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", modelName);
                requestBody.put("messages", Collections.singletonList(message));
                requestBody.put("max_tokens", maxTokens);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                String apiUrl = url + "/v1/chat/completions";
                ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> messageResponse = (Map<String, Object>) choices.get(0).get("message");
                        return (String) messageResponse.get("content");
                    }
                }
                throw new RuntimeException("AI返回结果格式异常");
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                boolean isRateLimit = errorMsg != null && (errorMsg.contains("429") || errorMsg.contains("rate") || errorMsg.contains("limit"));

                if (attempt < maxRetries - 1 && isRateLimit) {
                    log.warn("AI API限流，等待{}ms后重试(第{}次)", retryDelay, attempt + 1);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    retryDelay *= 2;
                } else {
                    throw new RuntimeException("AI调用失败: " + e.getMessage(), e);
                }
            }
        }
        throw new RuntimeException("AI调用失败: 重试次数用尽");
    }

    /**
     * 调用对话AI（DeepSeek）
     */
    private String callChatAI(String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(chatApiKey);

            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是人工智能助手.");

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(systemMessage);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", chatModel);
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String apiUrl = chatBaseUrl + "/chat/completions";
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> messageResponse = (Map<String, Object>) choices.get(0).get("message");
                    return (String) messageResponse.get("content");
                }
            }
            throw new RuntimeException("对话AI返回结果格式异常");
        } catch (Exception e) {
            log.error("对话AI调用失败: {}", e.getMessage());
            throw new RuntimeException("对话AI调用失败: " + e.getMessage());
        }
    }
}