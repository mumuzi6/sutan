package com.mumuzi.sutan.backend.agent;

import com.mumuzi.sutan.backend.rag.Citation;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 单步执行记录：思考、行动、观察。
 * 用于 SSE 流式推送 + 调试 + 可观测。
 */
public class AgentStep {

    /** LLM 的思考过程 */
    public String thought;
    /** LLM 选择的工具名（Final Answer 时为 null） */
    public String action;
    /** LLM 给工具的输入 */
    public String actionInput;
    /** 工具执行后的观察结果 */
    public String observation;
    /** 本步收集到的溯源引用 */
    public List<Citation> citations = new ArrayList<>();

    public boolean isFinalAnswer() {
        return action == null || "Final Answer".equalsIgnoreCase(action);
    }
}
