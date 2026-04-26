package org.interviewmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.interviewmate.entity.InterviewReport;

@Mapper
public interface InterviewReportMapper {
    int insert(InterviewReport report);
    InterviewReport findBySessionId(@Param("sessionId") Long sessionId);
    int deleteBySessionId(@Param("sessionId") Long sessionId);
}
