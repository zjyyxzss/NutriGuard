🥗 NutriGuard - 智能膳食与健康管家 (AI Agent)基于 Spring AI + RAG + RabbitMQ 的异步多模态健康助手📖 项目简介NutriGuard 是一个 AI 原生 (AI-Native) 的智能应用。不同于传统的 CRUD 系统，它构建了一个具备 “感知、思考、记忆、行动” 能力的 AI Agent。
系统旨在帮助慢性病患者（如高血压、糖尿病）和健身人群管理饮食。用户只需通过自然语言（或图片/语音，规划中）输入饮食情况，系统即可利用 RAG (检索增强生成) 技术查询专业营养库，并利用 Function Calling (函数调用) 自动将饮食数据结构化并存入数据库。


🏗️ 系统架构 (Architecture)本项目采用 事件驱动的异步架构，解决了大模型推理延迟（Latency）导致的系统吞吐量瓶颈问题。代码段graph TD
    User[用户请求] -->|HTTP POST| API[Spring Boot Controller]
    API -->|极速返回 Ack| User
    API -->|异步投递| MQ[RabbitMQ 消息队列]
    
    subgraph "异步处理中心 (Consumer)"
        MQ --> Consumer[AI 消费者服务]
        Consumer -->|1.读取上下文| Redis[Redis (多轮对话记忆)]
        Consumer -->|2.检索知识 RAG| PG[PostgreSQL (pgvector 向量库)]
        Consumer -->|3.推理与决策| LLM[阿里通义千问 / OpenAI]
        
        LLM -- 4.触发工具调用 --> Function[Java DietService]
        Function -->|5.写入记录| DB[(业务数据库)]
    end

    
🛠️ 核心技术栈领域技术选型版本用途核心框架Spring Boot3.2.5基于 Jakarta EE 的现代 Web 框架。
AI 框架Spring AI0.8.1接入 LLM，管理 Prompt 和 Function Calling。
数据存储PostgreSQL16pgvector 插件：同时存储业务数据和 1536 维向量数据。消息队列RabbitMQLatest实现 AI 任务的异步解耦，削峰填谷。
缓存/记忆Redis7.0存储会话上下文 (Context)，实现多轮对话记忆。
模型服务DashScopeQwen兼容 OpenAI 协议的国产大模型服务。数据工程Fastjson2 / Hutool-数据清洗与结构化解析。


✨ 核心亮点与解决方案 1. 高可用异步架构 (Asynchronous Pipeline)痛点：大模型推理通常耗时 3-5 秒。传统的同步 HTTP 调用会导致 Tomcat 线程阻塞，极低并发下即可导致服务不可用 (Timeout)。解决方案：构建了 Producer-Consumer 模型。前端请求仅负责发送消息到 RabbitMQ，耗时不足 10ms；后台 Consumer 负责执行复杂的 RAG 和推理流程。成果：系统吞吐量不再受限于 LLM 的响应速度，实现了高可用。


2. RAG (检索增强生成) 消除幻觉痛点：通用大模型缺乏专业的营养学知识，容易一本正经地胡说八道。解决方案：向量化：使用  模型将食物描述转化为向量，存入 PostgreSQL。text-embedding-v1检索：用户提问时，先在向量库中检索最相似的营养知识 (Top-K)。增强：将检索到的权威数据注入 Prompt，强制 AI 基于事实回答。


3. AI Agent 与 Function Calling痛点：传统的 Chatbot 只能聊天，无法处理业务（如“记账”）。解决方案：通过 Spring AI 定义 Java 函数 () 并注册给 LLM。当 LLM 分析出用户意图是“记录”时，会自动回调该 Java 方法，实现自然语言到数据库事务的转化。recordDiet


4. 自研 Redis 多轮记忆机制 (Custom Memory)挑战：在异步线程中，Spring AI 的  难以维护上下文；且遇到了依赖包版本冲突 ()。ConversationChatClientNoClassDefFoundError解决方案：基于  手写了 List 结构 的记忆管理模块。
RedisTemplate每次请求前：读取 Redis List 最近 10 条记录构建 Context。每次响应后：将 User 和 Assistant 的消息追加到 List 末尾，并设置 TTL。价值：在绕过框架 Bug 的同时，实现了可控粒度的多轮对话记忆。


🚀 快速启动1. 环境准备 (Docker)本项目依赖 PostgreSQL (带向量插件)、Redis 和 RabbitMQ。
推荐使用 Docker 启动：Bash# 1. 启动 PostgreSQL (pgvector)
docker run -d --name nutri-pg -e POSTGRES_PASSWORD=password -p 5432:5432 pgvector/pgvector:pg16

# 2. 启动 Redis
docker run -d --name redis -p 6379:6379 redis:7.0

# 3. 启动 RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

2. 配置应用修改 ：src/main/resources/application.yml配置 。spring.ai.openai.api-key配置数据库和 MQ 的连接地址 (localhost 或 虚拟机 IP)。

3. 初始化知识库项目启动后，访问以下接口利用 AI 生成初始营养数据：GET http://localhost:8080/knowledge/generate?count=204. 开始对话发送 POST 请求到 ：http://localhost:8080/diet/analyzeJSON{
  "userId": 1,
  "text": "我刚吃了一份红烧肉，我有高血压，帮我记下来。",
  "conversationId": "session_001"
}
👨‍💻 作者Maintainer: 谢子硕
