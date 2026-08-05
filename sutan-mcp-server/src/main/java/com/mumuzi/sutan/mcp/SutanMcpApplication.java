package com.mumuzi.sutan.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 诉探自研 MCP Server 启动类。
 * 将暴露 search_law / search_case / get_statute / grade_answer 等工具，
 * 供主 Agent（或外部 MCP 客户端）调用，体现"工具标准化 + 智能体生态"。
 *
 * TODO: W2 引入 spring-ai-starter-mcp-server-webmvc，按 MCP 协议暴露工具。
 */
@SpringBootApplication
public class SutanMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SutanMcpApplication.class, args);
    }
}
