package org.interviewmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.interviewmate.entity.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {
    int insert(Category category);
    Category findById(Long id);
    List<Category> findAll();
    int countKnowledgeByCategoryId(Long categoryId);
}