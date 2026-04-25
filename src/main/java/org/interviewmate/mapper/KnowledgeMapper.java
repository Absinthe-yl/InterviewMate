package org.interviewmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.interviewmate.entity.Knowledge;

import java.util.List;

@Mapper
public interface KnowledgeMapper {
    int insert(Knowledge knowledge);
    int updateById(Knowledge knowledge);
    int deleteById(Long id);
    int deleteByIds(@Param("ids") List<Long> ids);
    int deleteAll();
    Knowledge findById(Long id);
    List<Knowledge> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("offset") int offset, @Param("limit") int limit);
    int count(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
    List<String> suggest(@Param("keyword") String keyword);
    int incrementViewCount(Long id);
}