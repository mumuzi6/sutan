package com.mumuzi.sutan.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 诉探后端启动类。
 * 负责 Agent 编排、RAG 检索、模拟阅卷与对外 API。
 */
@SpringBootApplication
@EnableScheduling
public class SutanBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SutanBackendApplication.class, args);
    }
}
