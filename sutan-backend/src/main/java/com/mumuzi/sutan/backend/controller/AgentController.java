package com.mumuzi.sutan.backend.controller;

import com.mumuzi.sutan.backend.agent.AgentResult;
import com.mumuzi.sutan.backend.agent.AgentStep;
import com.mumuzi.sutan.backend.agent.ReActAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Agent 对外 API：同步对话 + SSE 流式（逐步推送思考过程）。
 */
@RestController
@RequestMapping("/api/agent")
@Tag(name = "Agent 对话", description = "ReAct Agent 同步与流式接口")
public class AgentController {

    private final ReActAgent reactAgent;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AgentController(ReActAgent reactAgent) {
        this.reactAgent = reactAgent;
    }

    @PostMapping("/chat")
    @Operation(summary = "同步对话：返回最终答案+步骤+溯源")
    public Map<String, Object> chat(@RequestBody Map<String, String> body) {
        String query = body.getOrDefault("q", "");
        AgentResult result = reactAgent.run(query);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("answer", result.getAnswer());
        resp.put("grounded", result.isGrounded());
        if (result.getGroundNote() != null) resp.put("groundNote", result.getGroundNote());
        resp.put("citations", result.getCitations());
        resp.put("steps", result.getSteps().stream().map(this::stepMap).toList());
        return resp;
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    @Operation(summary = "SSE 流式：异步推送 step/answer/done 事件")
    public SseEmitter stream(@RequestParam String q) {
        // SseEmitter 是 Spring MVC 原生 SSE，兼容 spring-boot-starter-web（非 WebFlux）
        SseEmitter emitter = new SseEmitter(120_000L);
        executor.execute(() -> {
            try {
                AgentResult result = reactAgent.run(q);
                for (AgentStep s : result.getSteps()) {
                    emitter.send(SseEmitter.event()
                            .name("step")
                            .data(stepMap(s)));
                }
                emitter.send(SseEmitter.event()
                        .name("answer")
                        .data(Map.of(
                                "answer", result.getAnswer(),
                                "grounded", result.isGrounded(),
                                "citations", result.getCitations()
                        )));
                emitter.send(SseEmitter.event().name("done").data("ok"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private Map<String, Object> stepMap(AgentStep s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("thought", s.thought);
        m.put("action", s.action);
        if (s.actionInput != null) m.put("actionInput", s.actionInput);
        if (s.observation != null) m.put("observation", s.observation);
        return m;
    }
}
