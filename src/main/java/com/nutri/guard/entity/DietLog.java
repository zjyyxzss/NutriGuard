package com.nutri.guard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 饮食日记 - 对应数据库 diet_log 表
 */
@Data
@TableName("diet_log")
public class DietLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String foodName;

    private Integer calories;

    /**
     * AI 给出的分析建议 (比如：热量超标，建议运动)
     */
    private String aiSuggestion;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}