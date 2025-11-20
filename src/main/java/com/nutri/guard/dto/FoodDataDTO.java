package com.nutri.guard.dto;

import lombok.Data;
import java.util.List;

/**
 * LLM 返回的结构化数据列表
 */
@Data
public class FoodDataDTO {

    // 内部列表，用于承载所有生成的食物记录
    private List<FoodRecord> foodRecords;

    @Data
    public static class FoodRecord {
        private String foodName;
        private String description; // 详细描述：包含热量、禁忌等信息
    }
}