package org.interviewmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.interviewmate.entity.InterviewRound;

import java.util.List;

@Mapper
public interface InterviewRoundMapper {
    int insert(InterviewRound round);
    InterviewRound findById(@Param("id") Long id);
    List<InterviewRound> findBySessionId(@Param("sessionId") Long sessionId);
    int updateById(InterviewRound round);
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}
