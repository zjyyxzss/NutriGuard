package com.nutri.guard.controller;


import com.nutri.guard.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KnowledgeController {
    @Autowired
    private KnowledgeService knowledgeService;


    @GetMapping("/knowledge/init")
    public String init() {
        knowledgeService.initKnowledgeBase();
        return "知识库初始化完成！请去数据库检查 vector_store 表。";
    }


    @GetMapping("/knowledge/generate")
    public String generateData(@RequestParam(defaultValue = "10") int count) {
        try {
            int insertedCount = knowledgeService.generateDataFromAI(count);
            return "成功通过 AI 生成并同步了 " + insertedCount + " 条专业营养知识！";
        } catch (Exception e) {
            return "数据生成和同步失败：" + e.getMessage();
        }
    }

}
