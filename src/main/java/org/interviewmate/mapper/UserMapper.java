package org.interviewmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.interviewmate.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
    int insert(User user);
    User findByUsername(String username);
    User findById(Long id);
    int updateById(User user);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    List<User> findByStatus(@Param("status") String status);
    List<User> findAll();
}
