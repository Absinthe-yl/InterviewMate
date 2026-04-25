package org.interviewmate.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.interviewmate.service.AIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    @Value("${ai.base-url:https://api.xzzzzz.com}")
    private String baseUrl;

    @Value("${ai.api-key:sk-gwsAzladZzBRoHKYhNoIzq4HFsjwiANQPS5SWbVY9c9FXO9f}")
    private String apiKey;

    @Value("${ai.model:glm-5.1}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String classifyKnowledge(String title, String content, List<String> availableCategories) {
        String prompt = buildClassifyPrompt(title, content, availableCategories);
        String response = callAI(prompt);

        // 从响应中提取分类名称
        if (response != null) {
            response = response.trim();
            // 检查返回的分类是否在可用列表中
            for (String category : availableCategories) {
                if (response.contains(category)) {
                    return category;
                }
            }
        }

        // 默认返回第一个分类
        return availableCategories.isEmpty() ? null : availableCategories.get(0);
    }

    @Override
    public String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // 简单处理：取前200字符
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

    private String buildClassifyPrompt(String title, String content, List<String> categories) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个技术文档分类专家。请根据以下知识点的标题和内容，选择最合适的分类。\n\n");
        prompt.append("可用分类：\n");
        for (int i = 0; i < categories.size(); i++) {
            prompt.append((i + 1)).append(". ").append(categories.get(i)).append("\n");
        }
        prompt.append("\n知识点标题：").append(title).append("\n");
        prompt.append("\n知识点内容（前500字）：\n");
        prompt.append(content.length() > 500 ? content.substring(0, 500) : content);
        prompt.append("\n\n请只返回分类名称，不要返回其他内容。");

        return prompt.toString();
    }

    private String callAI(String prompt) {
        int maxRetries = 3;
        int retryDelay = 1000; // 初始延迟1秒

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                Map<String, Object> message = new HashMap<>();
                message.put("role", "user");
                message.put("content", prompt);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("messages", Collections.singletonList(message));
                requestBody.put("max_tokens", 100);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

                String url = baseUrl + "/v1/chat/completions";
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

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
                // 检查是否是限流错误
                boolean isRateLimit = errorMsg != null && (errorMsg.contains("429") || errorMsg.contains("rate") || errorMsg.contains("limit"));

                if (attempt < maxRetries - 1 && isRateLimit) {
                    log.warn("AI API限流，等待{}ms后重试(第{}次)", retryDelay, attempt + 1);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    retryDelay *= 2; // 指数退避
                } else {
                    log.error("调用AI失败: {}", e.getMessage());
                    throw new RuntimeException("AI分类失败: " + e.getMessage());
                }
            }
        }
        throw new RuntimeException("AI分类失败: 重试次数用尽");
    }
}