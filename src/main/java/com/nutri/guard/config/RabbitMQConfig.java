package com.nutri.guard.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 定义常量，方便 Service 和 Consumer 引用
    public static final String QUEUE_NAME = "diet.analysis.queue";     // 队列名
    public static final String EXCHANGE_NAME = "diet.exchange";        // 交换机名
    public static final String ROUTING_KEY = "diet.routing.key";       // 路由键

    /**
     * 1. 定义队列 (持久化)
     */
    @Bean
    public Queue dietQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    /**
     * 2. 定义直连交换机
     */
    @Bean
    public DirectExchange dietExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    /**
     * 3. 将队列绑定到交换机
     */
    @Bean
    public Binding bindingDietQueue(Queue dietQueue, DirectExchange dietExchange) {
        return BindingBuilder.bind(dietQueue).to(dietExchange).with(ROUTING_KEY);
    }

    /**
     * 4. 【核心配置】使用 JSON 消息转换器
     * 作用：发送消息时自动转 JSON，接收时自动转对象。
     * 避免 Java 默认序列化带来的安全问题和乱码。
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}