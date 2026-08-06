package com.mumuzi.sutan.backend.controller;

import com.mumuzi.sutan.backend.user.SessionService;
import com.mumuzi.sutan.backend.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户接口：匿名注册 + 会话历史。
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户", description = "注册与会话管理")
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;

    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    @Operation(summary = "匿名注册，返回 userId + openId")
    public Map<String, Object> register() {
        return userService.registerAnonymous();
    }

    @GetMapping("/{userId}/sessions/{sessionId}/messages")
    @Operation(summary = "查询会话历史消息")
    public List<Map<String, Object>> messages(@PathVariable Long userId, @PathVariable Long sessionId) {
        return sessionService.getMessages(sessionId);
    }
}
