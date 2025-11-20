package com.nutri.guard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SmartDietService {

    @Autowired
    private ChatClient chatClient; // AI 客户端

    @Autowired
    private VectorStore vectorStore; // 向量数据库客户端

    /**
     * 核心方法：智能分析并记录
     * @param userId 用户ID
     * @param userText 用户的自然语言输入 (例如："我刚吃了一碗红烧肉，我有高血压，帮我记一下")
     */
    public String analyzeAndRecord(Long userId, String userText) {

        // 1. RAG 检索：拿着用户的话，去数据库找最相关的营养知识

        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.query(userText).withTopK(2)
        );

        String knowledge = similarDocs.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

       log.info("RAG 检索到的知识：{}", knowledge);

        // 2. 构造 Prompt (提示词工程)
        String systemText = """
                你是一个专业且谨慎的营养师助手。
                你的回答必须严格基于【已知知识】。
                【用户健康档案】：用户ID={userId}。用户的高风险标签包括：高血压、高血脂。
                【已知知识】{knowledge}【核心指令】：1. 必须优先完成用户提出的所有分析和对比问题。
                2. 只有当用户**明确要求执行一个动作**时（如“请帮我记录”或“现在就保存”），你才能调用 'recordDiet' 工具。
                3. 在调用工具之前，必须先给出分析和建议。
                请针对用户的提问，生成最终的分析和回复。
                """;

        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(systemText);
        Prompt prompt = new Prompt(List.of(
                systemPrompt.createMessage(Map.of("knowledge", knowledge, "userId", userId)),
                new UserMessage(userText)
        ), OpenAiChatOptions.builder().withFunctions(Set.of("recordDiet")).build()); // 启用 Function Calling

        // 3. 调用大模型
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
}