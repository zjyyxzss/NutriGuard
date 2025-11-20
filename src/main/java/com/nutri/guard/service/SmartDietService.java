package com.nutri.guard.service;

import com.nutri.guard.config.RabbitMQConfig;
import com.nutri.guard.dto.AnalysisTask;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class SmartDietService {

    @Autowired
    private RabbitTemplate rabbitTemplate;



    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Redis中会话记忆的key前缀
    public static final String CHAT_MEMORY_KEY_PREFIX = "chat:memory:";

    // 会话记忆过期时间（30分钟）
    public static final long CHAT_MEMORY_TTL = 30;
    public static final TimeUnit CHAT_MEMORY_TTL_UNIT = TimeUnit.MINUTES;

    /**
     * 【核心生产者】智能分析并记录 - 异步模式
     * 职责：封装任务，发送到 MQ，立即返回。
     */
    public String analyzeAndRecord(Long userId, String userText, String conversationId) {

        // 1. 简单校验
        if (userText == null || userText.trim().isEmpty()) {
            return "请输入您的查询内容。";
        }

        // 2. 封装异步任务消息
        AnalysisTask task = new AnalysisTask();
        task.setUserId(userId);
        task.setUserText(userText);
        task.setConversationId(conversationId);

        // 3. 【核心】发送消息到 MQ，立即释放主线程
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                task // 发送 DTO 实例
        );

        // 4. 立即返回“处理中”状态，响应时间低于 10ms
        return "您的健康分析请求已接收，请稍后查看结果。";
    }


}