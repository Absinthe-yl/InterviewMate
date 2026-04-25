package org.interviewmate.service.impl;

import lombok.RequiredArgsConstructor;
import org.interviewmate.dto.CategoryResponse;
import org.interviewmate.entity.Category;
import org.interviewmate.mapper.CategoryMapper;
import org.interviewmate.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryMapper.findAll();
        return categories.stream().map(c -> {
            CategoryResponse response = new CategoryResponse();
            response.setId(c.getId());
            response.setName(c.getName());
            response.setKnowledgeCount(categoryMapper.countKnowledgeByCategoryId(c.getId()));
            return response;
        }).collect(Collectors.toList());
    }
}