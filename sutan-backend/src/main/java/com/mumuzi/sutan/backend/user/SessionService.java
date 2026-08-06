package com.mumuzi.sutan.backend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 会话服务：创建会话 + 保存消息（含溯源引用 JSON）。
 * 消息持久化是"真实用户运营数据"的基础。
 */
@Service
public class SessionService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public SessionService(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    /** 创建会话，返回 session_id */
    public Long createSession(Long userId, String title) {
        jdbc.update("insert into sessions (user_id, title) values (?, ?)",
                userId, title == null ? "新对话" : title);
        return jdbc.queryForObject("select max(id) from sessions where user_id = ?", Long.class, userId);
    }

    /** 保存一条消息 */
    public void saveMessage(Long sessionId, String role, String content, List<Map<String, Object>> citations, Integer tokenCost) {
        try {
            String citationsJson = citations != null && !citations.isEmpty()
                    ? objectMapper.writeValueAsString(citations) : null;
            jdbc.update("insert into messages (session_id, role, content, citations_json, token_cost) values (?,?,?,?,?)",
                    sessionId, role, content, citationsJson, tokenCost);
        } catch (Exception e) {
            // 存库失败不阻断主流程
        }
    }

    /** 查询会话历史消息 */
    public List<Map<String, Object>> getMessages(Long sessionId) {
        return jdbc.queryForList("select id, role, content, citations_json, created_at from messages where session_id = ? order by id", sessionId);
    }
}
