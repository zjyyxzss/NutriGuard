package com.nutri.guard.dto;

import lombok.Data;

/**
 * 对应前端发送的 JSON 请求体
 */
@Data
public class DietAnalysisRequest {
    private Long userId;
    private String userText;
    private String conversationId;
}