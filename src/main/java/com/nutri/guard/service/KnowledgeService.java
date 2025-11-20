package com.nutri.guard.service;

import cn.hutool.core.util.StrUtil;

import com.nutri.guard.entity.FoodKnowledge;
import com.nutri.guard.mapper.FoodKnowledgeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class KnowledgeService {


    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private ChatClient chatClient;

    @Autowired
    private FoodKnowledgeMapper foodKnowledgeMapper;

    /**
     * 初始化知识库：将 MySQL/PG 里的文本数据，向量化后存入 VectorStore
     * (实际项目中，这个方法可以通过接口触发，或者定时任务触发)
     */
    public void initKnowledgeBase() {
        // 1. 模拟一些初始化数据
        if (foodKnowledgeMapper.selectCount(null) == 0) {
            createSampleData();
        }

        // 2. 从数据库查出所有知识
        List<FoodKnowledge> foods = foodKnowledgeMapper.selectList(null);

        // 3. 转换为 Spring AI 的 Document 对象
        List<Document> documents = foods.stream()
                .map(food -> {

                    return new Document(
                            food.getDescription(),
                            Map.of("id", food.getId(), "name", food.getFoodName()) // 元数据
                    );
                })
                .collect(Collectors.toList());

        // 4. 存入向量数据库

        vectorStore.add(documents);

        log.info("知识库初始化完成，共加载 {} 条数据", documents.size());
    }


    private void createSampleData() {
        saveFood("红烧肉", "红烧肉：传统中式菜肴。每100g热量约450千卡。脂肪含量高，钠含量高。高血压、高血脂人群应少吃或不吃。");
        saveFood("清炒西兰花", "清炒西兰花：健康蔬菜。每100g热量约35千卡。富含维生素C和膳食纤维。适合减肥和高血压人群食用。");
        saveFood("可乐", "可乐：碳酸饮料。每100ml热量约43千卡。含糖量极高。糖尿病患者禁止饮用，减肥人群应避免。");
        saveFood("燕麦片", "燕麦片：全谷物食品。每100g热量约370千卡。富含膳食纤维，升糖指数低(GI值低)。适合糖尿病患者作为主食。");
    }

    /**
     * 保存食物知识信息
     * @param name 食物名称
     * @param desc 食物描述信息
     */
    private void saveFood(String name, String desc) {
        FoodKnowledge fk = new FoodKnowledge();
        fk.setFoodName(name);
        fk.setDescription(desc);
        // 插入到数据库
        foodKnowledgeMapper.insert(fk);
    }

    @Transactional
    public int generateDataFromAI(int count) {

        // 1. 构造一个更简单、更可靠的 Prompt
        String promptText = """
            你是一个专业营养数据库管理员。请生成 {count} 种常见的中国和世界食物记录。
            对于每种食物，请在 description 中详细说明其热量、脂肪含量以及对高血压、糖尿病等患者的食用建议。
            请严格按照【食物名称 | 专业详细的描述】的格式输出。
            每条记录必须占据单独的一行，请不要添加任何解释性文字或标题。
            请开始生成。
            """;

        PromptTemplate template = new PromptTemplate(promptText);
        Prompt prompt = template.create(Map.of("count", count));

        // 2. 调用 LLM 并获取原始文本
        String rawResponse = chatClient.call(prompt).getResult().getOutput().getContent();

        int insertedCount = 0;

        // 3. 使用字符串分割进行解析
        List<String> lines = StrUtil.split(rawResponse, '\n');

        for (String line : lines) {
            List<String> parts = StrUtil.split(line, '|');

            if (parts.size() >= 2) {
                String foodName = parts.get(0).trim();
                String description = parts.get(1).trim();

                // 4. 存入 FoodKnowledge 表
                FoodKnowledge fk = new FoodKnowledge();
                fk.setFoodName(foodName);
                fk.setDescription(description);

                foodKnowledgeMapper.insert(fk);
                insertedCount++;
            }
        }

        // 5. 触发向量化和 RAG 引擎的更新
        initKnowledgeBase();

        return insertedCount;
    }
}