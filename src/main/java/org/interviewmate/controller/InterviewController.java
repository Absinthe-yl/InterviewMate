package org.interviewmate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.interviewmate.common.Result;
import org.interviewmate.dto.*;
import org.interviewmate.service.InterviewService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<StartInterviewResponse> startInterview(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart(value = "resume", required = false) MultipartFile resume,
            @RequestParam(value = "positionType", required = false) String positionType,
            @RequestParam(value = "interviewType", required = false, defaultValue = "COMPREHENSIVE") String interviewType,
            @RequestParam(value = "difficulty", required = false, defaultValue = "MEDIUM") String difficulty,
            @RequestParam(value = "totalRounds", required = false, defaultValue = "5") Integer totalRounds
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        StartInterviewResponse response = interviewService.startInterview(userId, resume, positionType, interviewType, difficulty, totalRounds);
        return Result.success("面试开始", response);
    }

    @PostMapping("/answer")
    public Result<AnswerResponse> answer(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long sessionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        AnswerResponse response = interviewService.answer(sessionId, request.getAnswer());
        return Result.success(response);
    }

    @GetMapping("/session/{id}")
    public Result<InterviewSessionResponse> getSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        InterviewSessionResponse response = interviewService.getSession(id);
        return Result.success(response);
    }

    @GetMapping("/history")
    public Result<List<InterviewHistoryResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<InterviewHistoryResponse> history = interviewService.getHistory(userId);
        return Result.success(history);
    }

    @GetMapping("/report/{id}")
    public Result<InterviewReportResponse> getReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        InterviewReportResponse response = interviewService.getReport(id);
        return Result.success(response);
    }

    @DeleteMapping("/session/{id}")
    public Result<Void> deleteSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        interviewService.deleteSession(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/end/{id}")
    public Result<Void> endInterview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        interviewService.endInterview(id);
        return Result.success("面试已结束", null);
    }
}
