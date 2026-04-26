package org.interviewmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.interviewmate.entity.PromptTemplate;

import java.util.List;

@Mapper
public interface PromptTemplateMapper {
    List<PromptTemplate> findAll();
    PromptTemplate findByKey(@Param("templateKey") String templateKey);
    int updateByKey(PromptTemplate template);
}