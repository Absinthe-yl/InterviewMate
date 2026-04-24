-- InterviewMate Database Initialization Script
-- 每次更新请重新执行此脚本（会删除并重建表）

DROP DATABASE IF EXISTS interviewmate;
CREATE DATABASE interviewmate DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE interviewmate;

-- User Table
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色(ADMIN/USER)',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态(PENDING/APPROVED/REJECTED)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 初始化管理员账号 (密码: admin123)
-- BCrypt hash generated for 'admin123'
INSERT INTO user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'ADMIN', 'APPROVED');
