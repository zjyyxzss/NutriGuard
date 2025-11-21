package com.nutri.guard.controller;

import com.nutri.guard.entity.UserProfile;
import com.nutri.guard.service.SmartDietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
public class DietController {

    @Autowired
    private SmartDietService smartDietService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 智能饮食分析接口
     * URL: http://localhost:8080/diet/analyze
     * 参数: userId=1, text=我吃了一盘红烧肉
     */
    @PostMapping("/diet/analyze")
    public String analyze(@RequestParam Long userId,
                          @RequestParam String text,
                          // 允许前端传 ID，如果不传则生成一个
                          @RequestParam(required = false) String conversationId) {

        if (conversationId == null || conversationId.isEmpty()) {

            conversationId = "user_" + userId + "_default";
        }

        return smartDietService.analyzeAndRecord(userId, text, conversationId);
    }
    @GetMapping("/diet/history")
    public List<String> getHistory(@RequestParam Long userId,
                                   @RequestParam(required = false) String conversationId) {
        // 1. 确保 ID 逻辑和 analyze 接口一致
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = "user_" + userId + "_default";
        }

        // 2. 从 Redis 获取所有历史记录 (返回的是 JSON 字符串列表)
        String memoryKey = "chat:history:" + conversationId;
        return stringRedisTemplate.opsForList().range(memoryKey, 0, -1);
    }
    @GetMapping("/diet/conversations")
    public Set<String> getConversations(@RequestParam Long userId) {
        return smartDietService.getUserConversations(userId);
    }
    // 【新增】删除会话接口
    @DeleteMapping("/diet/conversation")
    public String deleteConversation(@RequestParam Long userId, @RequestParam String conversationId) {
        smartDietService.deleteConversation(userId, conversationId);
        return "删除成功";
    }

    // 【新增】获取档案
    @GetMapping("/user/profile")
    public UserProfile getProfile(@RequestParam Long userId) {
        return smartDietService.getProfile(userId);
    }

    // 【新增】保存档案
    @PostMapping("/user/profile")
    public String saveProfile(@RequestBody UserProfile profile) {
        smartDietService.saveProfile(profile);
        return "保存成功";
    }
}