package com.nutri.guard.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 异步分析任务的消息体
 */
@Data
public class AnalysisTask implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userText; // 用户的自然语言输入
    private String conversationId; // 会话 ID

}