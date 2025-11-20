package com.nutri.guard.controller;

import com.nutri.guard.service.SmartDietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DietController {

    @Autowired
    private SmartDietService smartDietService;

    /**
     * 智能饮食分析接口
     * URL: http://localhost:8080/diet/analyze
     * 参数: userId=1, text=我吃了一盘红烧肉
     */
    @PostMapping("/diet/analyze")
    public String analyze(@RequestParam Long userId, @RequestParam String text) {
        return smartDietService.analyzeAndRecord(userId, text, null);
    }
}