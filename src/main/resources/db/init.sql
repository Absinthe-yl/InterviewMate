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

-- Category Table (一级分类)
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- Knowledge Table (知识点)
CREATE TABLE knowledge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识点ID',
    title VARCHAR(255) NOT NULL COMMENT '标题(从md文件提取)',
    content LONGTEXT NOT NULL COMMENT 'Markdown内容',
    summary VARCHAR(500) COMMENT '摘要',
    category_id BIGINT COMMENT '分类ID',
    tags VARCHAR(255) COMMENT '标签(逗号分隔)',
    file_name VARCHAR(255) COMMENT '原始文件名',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    status TINYINT DEFAULT 1 COMMENT '状态(1正常 0禁用)',
    created_by BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_title (title),
    INDEX idx_category (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识点表';


-- 初始化技术分类(一级目录)
INSERT INTO category (name, sort_order) VALUES
('Agent', 1),
('Docker和k8s', 2),
('JVM', 3),
('Java基础', 4),
('Linux', 5),
('MySQL', 6),
('Netty', 7),
('Redis', 8),
('SQL', 9),
('Spring全家桶', 10),
('分布式', 11),
('工程场景题', 12),
('并发编程', 13),
('智力题', 14),
('消息队列', 15),
('算法-Hot100', 16),
('算法-排序', 17),
('系统设计', 18),
('计算机操作系统', 19),
('计算机网络', 20),
('设计模式', 21),
('集合框架', 22),
('面经', 23);