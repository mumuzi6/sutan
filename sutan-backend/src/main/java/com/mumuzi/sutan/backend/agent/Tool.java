package com.mumuzi.sutan.backend.agent;

import com.mumuzi.sutan.backend.rag.Citation;

import java.util.List;

/**
 * Agent 可调用的工具抽象。
 * 手写工具体系（不依赖 Spring AI 的 Function Calling），
 * 是"懂原理非调包"的核心体现——ReAct 循环自己解析、自己调度。
 */
public interface Tool {

    /** 工具名，供 LLM 在 Action 中引用 */
    String name();

    /** 工具描述（写给 LLM 看，决定何时调用） */
    String description();

    /**
     * 执行工具。
     *
     * @param input LLM 在 Action Input 中给出的自然语言参数
     * @return 观察结果 + 可溯源引用
     */
    ToolResult execute(String input);

    /** 工具执行结果：观察文本 + 溯源引用 */
    record ToolResult(String observation, List<Citation> citations) {
        public static ToolResult of(String observation) {
            return new ToolResult(observation, List.of());
        }

        public static ToolResult of(String observation, List<Citation> citations) {
            return new ToolResult(observation, citations == null ? List.of() : citations);
        }
    }
}
