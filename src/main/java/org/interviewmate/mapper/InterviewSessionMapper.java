package org.interviewmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.interviewmate.entity.InterviewSession;

import java.util.List;

@Mapper
public interface InterviewSessionMapper {
    int insert(InterviewSession session);
    InterviewSession findById(@Param("id") Long id);
    List<InterviewSession> findByUserId(@Param("userId") Long userId);
    int updateById(InterviewSession session);
    int deleteById(@Param("id") Long id);
}
