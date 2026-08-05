package com.mumuzi.sutan.backend.agent;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工具注册表：所有 Tool Bean 自动注册，供 ReActAgent 按名调度 + 生成工具说明给 LLM。
 */
@Component
public class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public ToolRegistry(java.util.List<Tool> toolBeans) {
        for (Tool t : toolBeans) {
            tools.put(t.name(), t);
        }
    }

    public Tool get(String name) {
        return tools.get(name);
    }

    /** 供 LLM 看的工具清单（写进 ReAct 系统提示） */
    public String toolDescriptions() {
        return tools.values().stream()
                .map(t -> "- " + t.name() + ": " + t.description())
                .collect(Collectors.joining("\n"));
    }
}
