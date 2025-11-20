package com.nutri.guard.config;

import com.nutri.guard.entity.DietLog;
import com.nutri.guard.mapper.DietLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;
@Slf4j
@Configuration
public class ToolsConfig {

    @Autowired
    private DietLogMapper dietLogMapper;

    // 定义入参结构
    public record DietRecordRequest(Long userId, String foodName, Integer calories, String aiAdvice) {}

    /**
     * 工具函数：记录饮食日志
     * @Description  注解非常重要，这是给 AI 看的说明书，告诉它这个工具是干嘛的
     */
    @Bean
    @Description("当用户想要记录饮食，或者确认吃了某种食物时，调用此工具将数据存入数据库")
    public Function<DietRecordRequest, String> recordDiet() {
        return request -> {
            log.info("AI 正在调用 Java 方法保存数据: {}", request);

            DietLog log = new DietLog();
            log.setUserId(request.userId());
            log.setFoodName(request.foodName());
            log.setCalories(request.calories());
            log.setAiSuggestion(request.aiAdvice());

            dietLogMapper.insert(log);

            return "记录成功！已存入数据库。";
        };
    }
}