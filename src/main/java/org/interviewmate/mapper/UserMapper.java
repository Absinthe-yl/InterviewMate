package org.interviewmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.interviewmate.entity.User;

@Mapper
public interface UserMapper {
    int insert(User user);
    User findByUsername(String username);
    User findById(Long id);
    int updateById(User user);
}
