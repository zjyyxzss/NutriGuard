package com.nutri.guard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 营养知识库 - 对应数据库 food_knowledge 表
 */
@Data
@TableName("food_knowledge")
public class FoodKnowledge {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 食物名称
     */
    private String foodName;

    /**
     * 详细描述 (包含热量、营养成分、禁忌等文本信息)
     * RAG 的时候，大模型主要读这个字段
     */
    private String description;


}