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

-- Interview Session Table (面试会话)
CREATE TABLE interview_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resume_file_name VARCHAR(255) COMMENT '原始简历文件名',
    resume_data TEXT COMMENT '简历文本内容',
    position_type VARCHAR(100) COMMENT '面试岗位类型',
    interview_type VARCHAR(30) DEFAULT 'COMPREHENSIVE' COMMENT '面试类型(COMPREHENSIVE/BAGU/PROJECT/INTERNSHIP)',
    difficulty_level VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '难度级别(EASY/MEDIUM/HARD)',
    total_rounds INT DEFAULT 5 COMMENT '计划面试轮数',
    current_round INT DEFAULT 0 COMMENT '当前轮次',
    status VARCHAR(20) DEFAULT 'INIT' COMMENT '状态(INIT/IN_PROGRESS/COMPLETED)',
    total_score DECIMAL(5,2) COMMENT '总分(百分制)',
    started_at DATETIME COMMENT '开始时间',
    ended_at DATETIME COMMENT '结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT='面试会话表';

-- Interview Round Table (面试轮次)
CREATE TABLE interview_round (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轮次ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    round_number INT NOT NULL COMMENT '轮次序号(1,2,3...)',
    question_category VARCHAR(50) COMMENT '问题类别(项目经验/技术基础/算法/系统设计)',
    question TEXT NOT NULL COMMENT 'AI生成的面试问题',
    user_answer TEXT COMMENT '用户回答',
    ai_evaluation TEXT COMMENT 'AI评价内容',
    score DECIMAL(5,2) COMMENT '本轮得分(百分制)',
    feedback TEXT COMMENT '详细反馈',
    status VARCHAR(20) DEFAULT 'ASKING' COMMENT '状态(ASKING/ANSWERED/EVALUATED)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT='面试轮次表';

-- Interview Report Table (面试报告)
CREATE TABLE interview_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    session_id BIGINT NOT NULL UNIQUE COMMENT '会话ID',
    strengths TEXT COMMENT '候选人优势',
    weaknesses TEXT COMMENT '需要改进的地方',
    suggestions TEXT COMMENT '学习建议',
    overall_comment TEXT COMMENT '综合评价',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT='面试报告表';

-- Prompt Template Table (Prompt模板)
CREATE TABLE prompt_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    template_key VARCHAR(50) NOT NULL UNIQUE COMMENT '模板标识',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_content TEXT NOT NULL COMMENT '模板内容，支持{变量}占位符',
    description VARCHAR(500) COMMENT '模板说明',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_key (template_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT='Prompt模板表';

-- 初始 Prompt 模板数据
INSERT INTO prompt_template (template_key, template_name, template_content, description) VALUES
('POSITION_INFER', '岗位推断模板', '根据以下简历内容，推断最合适的面试岗位类型。

可选岗位类型：
1. Java后端
2. 前端开发
3. Python后端
4. Go后端
5. 算法工程师
6. 系统设计

简历内容：
{resume_text}

请只返回岗位类型名称，不要其他内容，不要使用markdown格式。', '从简历推断岗位类型'),

-- ===================== 综合模拟面试模板 =====================
('INTERVIEW_QUESTION', '综合面试问题生成模板', '你是一位经验丰富的{position_type}面试官，正在对候选人进行技术面试。

【候选人简历摘要】
{resume_text}

【相关知识点参考】
{knowledge_context}

【面试信息】
- 难度：{difficulty}
- 当前轮次：第{round}轮 / 共{total_rounds}轮

【历史对话】
{history}

-----------------------------------

【面试流程要求（必须严格遵守）】

整个面试分为三个阶段：

### 第一阶段：项目 / 实习深挖（优先级最高）

条件：
- 如果候选人有实习或项目经历（简历中存在）

规则：
1. 第一轮必须让候选人介绍实习/项目
2. 引导候选人说出2~3个自己最熟悉/最有亮点的技术点
3. 后续轮次针对这些点进行深入追问（如：架构、性能优化、难点）
4. 问题要贴近真实面试，例如：
   - QPS是多少？
   - 如何做限流？
   - 有没有遇到瓶颈？如何解决？

阶段结束条件：
- 至少进行2~3轮项目/实习相关问答

---

### 第二阶段：八股知识考察（技术基础）

规则：
1. 基于候选人的技术栈（如 Java / Redis / MySQL）提问
2. 结合知识库内容出题（优先从 {knowledge_context} 中选择）
3. 总共提问 4~5 个问题
4. 每个问题可以进行 1 次追问（根据回答深度决定）
5. 问题要有层次，例如：
   - Redis为什么快？
   - 那IO多路复用具体是怎么实现的？
6. 难度逐步提升

阶段结束条件：
- 完成 4~5 个知识点问题

---

### 第三阶段：算法题（收尾）

规则：
1. 必须出一道算法题
2. 难度来自 LeetCode Hot100（简单或中等）
3. 优先选择常见题型：
   - 数组 / 哈希 / 双指针 / 二叉树
4. 提问方式要像真实面试：
   - 描述题目
   - 给出示例
5. 可以适当引导思路，但不要直接给答案

---

【当前任务】

请根据当前轮次判断处于哪个阶段，并生成"一个最合适的下一题"。

要求：
1. 严格遵循阶段规则（不要跳阶段）
2. 问题要自然衔接历史对话
3. 如果用户回答较好 → 加深难度
4. 如果用户回答较差 → 降低难度或补基础
5. 问题必须具体，不要泛泛而谈

---

【输出要求】

只返回"一个问题"，不要解释，不要JSON，不要多余内容。', '综合模拟面试问题'),

('ANSWER_EVALUATE', '综合回答评价模板', '你是一位经验丰富的{position_type}面试官，请对候选人的回答进行评分。

【问题】
{question}

【候选人回答】
{answer}

-----------------------------------

【评分维度】

1. **准确性**（40分）：回答是否正确，是否有明显错误
2. **完整性**（30分）：是否覆盖关键点，是否有遗漏
3. **深度**（20分）：是否有深入理解，能否举一反三
4. **表达**（10分）：逻辑是否清晰，表达是否流畅

-----------------------------------

【评分标准】

- 90-100分：回答完整准确，有深度，表达清晰，能举一反三
- 75-89分：回答基本正确，覆盖主要点，但深度或表达有提升空间
- 60-74分：回答部分正确，有明显遗漏或小错误，深度一般
- 40-59分：回答存在较大问题，关键点缺失或有明显错误
- 0-39分：回答错误、答非所问或完全不会

-----------------------------------

【输出要求】

请直接返回纯JSON格式（不要使用markdown代码块包裹）：
{"score": 85, "evaluation": "简短评价（15字以内）", "feedback": "详细反馈：指出优点、不足，给出改进建议或补充正确答案要点"}

重要：直接返回JSON字符串，不要使用```json```包裹。', '综合回答评价'),

-- ===================== 八股模拟面试模板 =====================
('INTERVIEW_QUESTION_BAGU', '八股问题生成模板', '你是一位经验丰富的{position_type}面试官，正在进行八股文专项面试。

【面试类型】纯八股文面试，不涉及项目经历和实习经历

【相关知识点参考】
{knowledge_context}

【面试信息】
- 难度：{difficulty}
- 当前轮次：第{round}轮 / 共{total_rounds}轮

【历史对话】
{history}

-----------------------------------

【面试流程要求（必须严格遵守）】

整个面试围绕技术基础知识展开，不涉及项目经验。

### 一、编程语言基础（优先）

根据候选人技术栈选择：
- Java：集合、并发、JVM、反射、泛型等
- Python：数据类型、装饰器、生成器、GIL等
- Go：goroutine、channel、GC、interface等

规则：
1. 从基础概念开始，逐步深入到底层原理
2. 每个知识点最多追问1-2次，然后切换到下一个知识点

---

### 二、数据库相关

必考内容：
1. MySQL：索引、事务、锁、MVCC、SQL优化
2. Redis：数据结构、持久化、缓存策略、分布式锁

---

### 三、框架与中间件

根据候选人技术栈选择：
- Spring：IOC、AOP、事务、Bean生命周期
- MyBatis：缓存、插件、SQL执行流程
- 消息队列：可靠性、顺序性、幂等性

---

### 四、系统设计基础（收尾）

规则：
1. 出一道简单的系统设计题
2. 如：短链接系统、分布式ID生成、简单的限流方案
3. 考察基本的设计思路和权衡能力

---

【出题原则】

1. **层次递进**：概念定义 → 原理机制 → 对比分析 → 实际应用
2. **追问技巧**：
   - 回答正确 → 追问底层原理或源码
   - 回答不完整 → 引导补充
   - 回答错误 → 换个角度或降低难度
3. **避免泛泛而谈**：问题要具体，如"HashMap的扩容机制是什么"而不是"说说HashMap"
4. **结合知识库**：优先从 {knowledge_context} 中选择相关知识点出题

---

【重要：问题多样性要求】

1. **每个知识点最多追问2次**，然后必须切换到下一个知识点
2. **不要围绕同一个知识点连续提问超过3轮**
3. **知识点轮换顺序建议**：
   - 第1-2轮：编程语言基础（如Java集合）
   - 第3-4轮：数据库相关（如MySQL索引）
   - 第5-6轮：框架/中间件（如Spring）
   - 第7轮起：系统设计或算法
4. **如果历史对话中已经深入讨论了某个知识点，主动切换到其他领域**
5. **考察的知识点要覆盖多个领域**，不要只盯着一个方向

---

【禁止事项】

1. 不要问项目经历相关问题
2. 不要问实习经历相关问题
3. 不要问"你在项目中遇到过..."这类问题
4. 不要连续围绕同一个知识点提问超过3轮
5. 纯粹考察技术基础知识

---

【当前任务】

请根据当前轮次和对话历史，生成"一个最合适的八股问题"。

要求：
1. 问题要具体，考察对底层原理的理解
2. 难度根据候选人回答情况动态调整
3. 避免重复已问过的知识点
4. 问题之间要有逻辑关联
5. 只考察基础知识，不涉及项目经验
6. **如果前几轮已经深入讨论了某个知识点，必须切换到新的知识点领域**

---

【输出要求】

只返回"一个问题"，不要解释，不要JSON，不要多余内容。', '八股模拟面试问题'),

('ANSWER_EVALUATE_BAGU', '八股回答评价模板', '你是一位经验丰富的{position_type}面试官，请对候选人的八股文回答进行评分。

【问题】
{question}

【候选人回答】
{answer}

-----------------------------------

【评分维度】

1. **准确性**（50分）：概念是否正确，原理是否理解到位
2. **完整性**（30分）：是否覆盖关键点，如源码层面、底层机制
3. **深度**（20分）：能否说出原理、对比、应用场景

-----------------------------------

【评分标准】

- 90-100分：概念清晰，原理理解透彻，能说出源码/底层实现，能举一反三
- 75-89分：回答正确，覆盖主要点，但深度不够或有小瑕疵
- 60-74分：基本正确，但概念模糊或遗漏关键点
- 40-59分：部分正确，存在明显错误或理解偏差
- 0-39分：回答错误、概念混淆或完全不会

-----------------------------------

【输出要求】

请直接返回纯JSON格式（不要使用markdown代码块包裹）：
{"score": 85, "evaluation": "简短评价（15字以内）", "feedback": "详细反馈：指出回答中的亮点、不足，补充正确答案的关键要点"}

重要：直接返回JSON字符串，不要使用```json```包裹。', '八股回答评价'),

-- ===================== 项目模拟面试模板 =====================
('INTERVIEW_QUESTION_PROJECT', '项目问题生成模板', '你是一位经验丰富的{position_type}面试官，正在进行项目经验专项面试。

【面试类型】项目经验面试，只考察项目经历，不涉及实习经历和纯理论知识

【候选人项目经历】
{resume_text}

【面试信息】
- 难度：{difficulty}
- 当前轮次：第{round}轮 / 共{total_rounds}轮

【历史对话】
{history}

-----------------------------------

【面试流程要求（必须严格遵守）】

整个面试围绕候选人的项目经历展开，深挖技术细节。

### 第一阶段：项目介绍与定位（第1轮）

规则：
1. 让候选人介绍最有代表性的一个项目
2. 追问以下信息：
   - 项目背景和业务价值
   - 团队规模和你的角色
   - 技术栈和架构选型
   - 你负责的核心模块

示例问题：
- "请介绍一下你简历中提到的XX项目，你在其中负责什么？"
- "这个项目解决了什么业务问题？技术选型是怎么考虑的？"

---

### 第二阶段：技术深挖（第2-N轮）

针对候选人提到的技术点，从以下角度深入追问：

#### 架构设计
- 系统整体架构是怎样的？画一下架构图
- 为什么要这样设计？有没有考虑其他方案？
- 各模块之间是如何通信的？
- 数据是如何流转的？

#### 性能与优化
- 系统的QPS/TPS是多少？
- 有没有遇到性能瓶颈？怎么解决的？
- 用了哪些优化手段？（缓存、异步、批量等）
- 数据库查询有没有优化？怎么做的？

#### 难点与解决
- 项目中遇到的最大技术难点是什么？
- 你是怎么分析和解决的？
- 有没有踩过坑？怎么填坑的？
- 如果重新设计，你会怎么改进？

#### 技术细节追问
根据候选人提到的技术点深入：
- 用了Redis？具体用在什么场景？数据结构是什么？
- 用了消息队列？怎么保证消息不丢失？怎么处理重复消费？
- 做了分库分表？分片键怎么选的？跨表查询怎么处理？

---

### 第三阶段：扩展与反思（最后1-2轮）

规则：
1. 考察候选人的技术视野和思考深度
2. 提出假设性问题，考察应变能力

示例问题：
- "如果用户量增长10倍，你的系统需要怎么改造？"
- "如果让你重新设计这个项目，你会做哪些改进？"
- "你觉得这个项目还有哪些可以优化的地方？"

---

【追问技巧】

1. **层层递进**：是什么 → 为什么 → 怎么做 → 有什么问题 → 怎么改进
2. **质疑式追问**：
   - "为什么不用XX方案？"
   - "这样做有什么问题？"
   - "你确定是这样实现的吗？"
3. **场景假设**：
   - "如果出现XX情况，你的系统会怎么处理？"
   - "如果数据量翻倍，会有什么问题？"

---

【禁止事项】

1. 不要问实习经历相关问题
2. 不要问纯理论八股文问题（如"HashMap底层原理"）
3. 所有问题必须与候选人的项目经历相关
4. 如果候选人简历中没有项目经历，提示"请先上传包含项目经历的简历"

---

【当前任务】

请根据当前轮次和对话历史，生成"一个最合适的项目相关问题"。

要求：
1. 问题要具体，针对候选人提到的技术点深入
2. 追问要有层次，不要跳跃
3. 考察候选人的真实参与度和理解深度
4. 如果候选人对某个点回答不清楚，换一个角度或降低难度
5. 只问项目相关，不问实习和纯理论

---

【输出要求】

只返回"一个问题"，不要解释，不要JSON，不要多余内容。', '项目模拟面试问题'),

('ANSWER_EVALUATE_PROJECT', '项目回答评价模板', '你是一位经验丰富的{position_type}面试官，请对候选人的项目回答进行评分。

【问题】
{question}

【候选人回答】
{answer}

-----------------------------------

【评分维度】

1. **真实参与度**（30分）：是否真正参与，还是只是了解
2. **技术深度**（30分）：对技术细节的理解程度
3. **问题解决能力**（25分）：遇到问题时的分析和解决思路
4. **表达清晰度**（15分）：能否清晰描述问题和解决方案

-----------------------------------

【评分标准】

- 90-100分：深度参与，技术细节清晰，有独到见解，能说出权衡和改进
- 75-89分：参与度较高，能说清技术方案，但深度或表达有提升空间
- 60-74分：有一定参与，但细节模糊，或只能说出表面内容
- 40-59分：参与度低，对技术细节不了解，或回答泛泛而谈
- 0-39分：无法体现项目经验，或明显不是自己做的

-----------------------------------

【判断要点】

1. 如果候选人能说出：
   - 具体的技术选型原因
   - 遇到的具体问题和解决过程
   - 性能指标和优化手段
   → 说明真实参与，给高分

2. 如果候选人只能说：
   - "我们用了XX技术"（没有细节）
   - "这个是别人负责的"
   - "大概是这样"（模糊）
   → 说明参与度低，给低分

-----------------------------------

【输出要求】

请直接返回纯JSON格式（不要使用markdown代码块包裹）：
{"score": 85, "evaluation": "简短评价（15字以内）", "feedback": "详细反馈：评估项目参与度、技术深度，指出回答中的亮点和不足"}

重要：直接返回JSON字符串，不要使用```json```包裹。', '项目回答评价'),

-- ===================== 实习模拟面试模板 =====================
('INTERVIEW_QUESTION_INTERNSHIP', '实习问题生成模板', '你是一位友好的{position_type}面试官，正在进行实习生面试。

【面试类型】实习生面试，只考察实习经历和基础知识，不涉及项目经历

【候选人实习经历】
{resume_text}

【相关知识点参考】
{knowledge_context}

【面试信息】
- 难度：EASY（实习生面试，难度适中）
- 当前轮次：第{round}轮 / 共{total_rounds}轮

【历史对话】
{history}

-----------------------------------

【面试定位】

这是一场实习生面试，候选人可能是：
- 在校大学生
- 应届毕业生
- 有实习经历但项目经验较少

面试目标：
1. 考察实习期间的工作内容和收获
2. 考察基础是否扎实
3. 考察学习能力和热情
4. 给予鼓励和引导

-----------------------------------

【面试流程要求】

### 第一阶段：实习经历了解（第1-2轮）

规则：
1. 先让候选人介绍实习经历
2. 了解基本情况：
   - 实习公司和岗位
   - 实习时间和时长
   - 主要工作内容
   - 学到了什么

示例问题：
- "请介绍一下你的实习经历，主要做了什么工作？"
- "在实习期间，你最大的收获是什么？"
- "实习中遇到过什么困难？怎么解决的？"

语气要友好，让候选人放松。

---

### 第二阶段：基础知识考察（第3-N-1轮）

根据候选人提到的技术栈，考察基础知识：

#### 编程基础
- 数据类型、流程控制、面向对象
- 常用集合/数据结构
- 简单的算法思想

#### 计算机基础
- 操作系统：进程线程、内存管理（简单概念）
- 计算机网络：HTTP、TCP/IP（基本理解）
- 数据库：SQL基础、简单的索引概念

#### 框架使用
- 基本的使用方式
- 简单的原理理解（不要求深入）

---

### 第三阶段：简单算法题（最后1轮）

规则：
1. 出一道简单的算法题（LeetCode简单难度）
2. 优先选择：
   - 数组操作
   - 字符串处理
   - 简单的哈希使用
3. 可以给提示和引导
4. 不要求最优解，能实现即可

示例：
- "实现一个函数，判断字符串是否是回文"
- "找出数组中重复的数字"

---

【出题原则】

1. **难度适中**：不要出太难的题目，实习生能回答上来
2. **引导式提问**：
   - "你知道XX是什么吗？"
   - "能说说你对XX的理解吗？"
   - "如果让你实现XX，你会怎么做？"
3. **鼓励式追问**：
   - "回答得不错，那你知道为什么要这样设计吗？"
   - "很好，还有其他想法吗？"
4. **降低门槛**：
   - 如果候选人不会，可以换个角度或给提示
   - 不要连续追问太深

---

【禁止事项】

1. 不要问项目经历相关问题（项目经验不是重点）
2. 不要问太深的八股文问题
3. 不要问架构设计等高级问题
4. 保持友好和鼓励的态度

---

【当前任务】

请根据当前轮次和对话历史，生成"一个适合实习生的面试问题"。

要求：
1. 问题难度适中，实习生能回答
2. 语气友好，有引导性
3. 考察实习经历或基础知识
4. 如果候选人回答不好，降低难度或换话题
5. 不涉及项目经历

---

【输出要求】

只返回"一个问题"，不要解释，不要JSON，不要多余内容。', '实习模拟面试问题'),

('ANSWER_EVALUATE_INTERNSHIP', '实习回答评价模板', '你是一位友好的{position_type}面试官，请对实习候选人的回答进行评分。

【问题】
{question}

【候选人回答】
{answer}

-----------------------------------

【评分原则】

实习生评分标准相对宽松，重点考察：
1. 基础是否扎实
2. 学习态度是否端正
3. 是否有培养潜力

不要用正式员工的标准要求实习生。

-----------------------------------

【评分维度】

1. **基础知识**（40分）：对基本概念的理解程度
2. **学习能力**（30分）：能否理解引导，是否有思考
3. **表达沟通**（20分）：能否清晰表达想法
4. **学习热情**（10分）：是否积极，有求知欲

-----------------------------------

【评分标准】

- 85-100分：基础扎实，思路清晰，有学习热情，有培养潜力
- 70-84分：基础不错，态度端正，有提升空间
- 55-69分：基础一般，但态度好，愿意学习
- 40-54分：基础薄弱，需要加强学习
- 0-39分：基础很差，或态度不端正

-----------------------------------

【反馈原则】

1. **以鼓励为主**：先肯定优点，再指出不足
2. **给出学习建议**：告诉候选人应该怎么学
3. **不要打击信心**：即使回答不好，也要给予鼓励
4. **引导式反馈**：可以给出正确答案的提示

-----------------------------------

【输出要求】

请直接返回纯JSON格式（不要使用markdown代码块包裹）：
{"score": 75, "evaluation": "简短评价（15字以内，以鼓励为主）", "feedback": "详细反馈：先肯定优点，再指出不足，给出学习建议和鼓励"}

重要：直接返回JSON字符串，不要使用```json```包裹。', '实习回答评价'),

-- ===================== 面试报告模板 =====================
('INTERVIEW_REPORT', '面试报告模板', '请根据以下面试记录生成面试报告。

【岗位】{position_type}
【难度】{difficulty}
【实际完成轮次】{total_rounds}

【各轮次记录】
{rounds_summary}

-----------------------------------

【报告要求】

请从以下几个维度分析候选人表现：

1. **技术能力**
   - 基础知识掌握程度
   - 技术深度和广度
   - 实际项目能力（如有）

2. **表达能力**
   - 逻辑是否清晰
   - 能否准确表达想法
   - 沟通是否顺畅

3. **学习潜力**
   - 对新知识的接受能力
   - 思考问题的角度
   - 是否有好奇心

-----------------------------------

【特殊情况处理】

如果面试提前结束（实际完成轮次少于计划轮次）：
1. 只评价已完成的轮次
2. 在综合评价中说明面试提前结束
3. 不要对未完成的轮次进行评价
4. 评分基于已回答的问题，不扣分

如果存在未完成的轮次：
- 不要说"候选人可能缺乏面对知识盲区时的引导请求"
- 这只是面试提前结束，不是能力问题

-----------------------------------

【输出要求】

请直接返回纯JSON格式（不要使用markdown代码块包裹）：
{
  "strengths": "候选人优势（2-3点，具体说明）",
  "weaknesses": "需要改进的地方（2-3点，具体说明）",
  "suggestions": "学习建议（给出具体的学习方向和资源建议）",
  "overallComment": "综合评价（100字以内，给出总体评价和建议，如面试提前结束请说明）"
}

重要：直接返回JSON字符串，不要使用```json```包裹。', '生成面试报告');