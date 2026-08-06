package com.mumuzi.sutan.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 烟雾测试接口：验证 LLM 已接入并可调用。
 * 注意：未配置 AI_API_KEY 时调用会失败，属预期（启动不依赖密钥）。
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI 烟雾测试", description = "验证 LLM 接入")
public class AiSmokeController {

    private final ChatModel chatModel;

    public AiSmokeController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ping")
    @Operation(summary = "DeepSeek 调用烟雾测试")
    public Map<String, Object> ping(
            @RequestParam(value = "q", defaultValue = "用一句话解释民法上的'善意取得'") String question) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", question);
        try {
            String answer = chatModel.call(question);
            result.put("answer", answer);
            result.put("ok", true);
        } catch (Exception e) {
            result.put("ok", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
