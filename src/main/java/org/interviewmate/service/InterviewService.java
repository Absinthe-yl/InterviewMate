package org.interviewmate.service;

import org.interviewmate.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InterviewService {
    /**
     * 开始面试
     */
    StartInterviewResponse startInterview(Long userId, MultipartFile resumeFile, String positionType, String interviewType, String difficulty, Integer totalRounds);

    /**
     * 回答问题
     */
    AnswerResponse answer(Long sessionId, String answer);

    /**
     * 获取会话详情
     */
    InterviewSessionResponse getSession(Long sessionId);

    /**
     * 获取面试历史
     */
    List<InterviewHistoryResponse> getHistory(Long userId);

    /**
     * 获取面试报告
     */
    InterviewReportResponse getReport(Long sessionId);

    /**
     * 删除面试记录
     */
    void deleteSession(Long sessionId);

    /**
     * 手动结束面试
     */
    void endInterview(Long sessionId);
}
