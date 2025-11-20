package com.nutri.guard.service.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutri.guard.config.RabbitMQConfig;
import com.nutri.guard.dto.AnalysisTask;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DietAnalysisConsumer {

    @Autowired
    private ChatClient chatClient;
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    // 定义记忆保留的轮数（例如保留最近 10 条）
    private static final int MEMORY_SIZE = 10;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleAnalysisTask(AnalysisTask task) {
        String convId = task.getConversationId();
        String userText = task.getUserText();
        String memoryKey = "chat:history:" + convId;

       log.info("开始处理多轮对话, ConvID: {}", convId);

        try {
            // 1. RAG 检索 (不变)
            List<Document> similarDocs = vectorStore.similaritySearch(
                    SearchRequest.query(userText).withTopK(2)
            );
            String knowledge = similarDocs.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n"));

            // 2. 准备系统预设 (System Message)
            String systemText = """
                你是一个专业的营养师助手。请利用【已知知识】回答用户。
                【已知知识】：
                {knowledge}
                你的任务：分析风险并必要时记录饮食。
                """;
            SystemPromptTemplate systemPrompt = new SystemPromptTemplate(systemText);
            Message systemMessage = systemPrompt.createMessage(Map.of("knowledge", knowledge, "userId", task.getUserId()));


            List<Message> historyMessages = new ArrayList<>();

            // 从 Redis List 中获取最后 N 条记录 (range: -N 到 -1)
            List<String> historyJsonList = redisTemplate.opsForList().range(memoryKey, -MEMORY_SIZE, -1);

            if (historyJsonList != null) {
                for (String json : historyJsonList) {
                    try {
                        // 将 JSON 转回简单的存储对象，再转为 Spring AI Message
                        SimpleHistoryMsg msgDto = objectMapper.readValue(json, SimpleHistoryMsg.class);
                        if ("user".equals(msgDto.role)) {
                            historyMessages.add(new UserMessage(msgDto.content));
                        } else {
                            historyMessages.add(new AssistantMessage(msgDto.content));
                        }
                    } catch (Exception e) {
                        // 忽略错误的历史记录
                        System.err.println("解析历史记录失败: " + e.getMessage());
                    }
                }
            }

            // 3. 合并 Prompt：[系统设定] + [历史 10 条] + [当前用户提问]
            List<Message> allMessages = new ArrayList<>();
            allMessages.add(systemMessage);
            allMessages.addAll(historyMessages);
            allMessages.add(new UserMessage(userText));

            // 4. 调用 AI
            Prompt prompt = new Prompt(allMessages,
                    OpenAiChatOptions.builder().withFunctions(Set.of("recordDiet")).build());

            String aiResponse = chatClient.call(prompt).getResult().getOutput().getContent();


            SimpleHistoryMsg userMsgDto = new SimpleHistoryMsg("user", userText);
            redisTemplate.opsForList().rightPush(memoryKey, objectMapper.writeValueAsString(userMsgDto));

            //  保存 AI 的回复
            SimpleHistoryMsg aiMsgDto = new SimpleHistoryMsg("assistant", aiResponse);
            redisTemplate.opsForList().rightPush(memoryKey, objectMapper.writeValueAsString(aiMsgDto));

            //  设置过期时间 (例如 1 小时)
            redisTemplate.expire(memoryKey, 1, TimeUnit.DAYS);

            log.info("处理完成, ConvID: {}", convId);

        } catch (Exception e) {
            log.error("AI 处理失败, ConvID: {}, 错误信息: {}", convId, e.getMessage());
        }
    }

    /**
     * 内部类：用于简单存储历史消息的结构 (Role + Content)
     * 必须是 static 且有无参构造，方便 Jackson 序列化
     */
    public static class SimpleHistoryMsg {
        public String role;
        public String content;

        public SimpleHistoryMsg() {}
        public SimpleHistoryMsg(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}