package com.mumuzi.sutan.backend.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * 用户服务：匿名注册（生成 open_id）+ 查询。
 * MVP 不做鉴权，仅用 open_id 标识用户；后续可接手机号/微信。
 */
@Service
public class UserService {

    private final JdbcTemplate jdbc;

    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 匿名注册，返回 open_id + user_id */
    public Map<String, Object> registerAnonymous() {
        String openId = "anon_" + System.currentTimeMillis();
        jdbc.update("insert into users (open_id, role) values (?, 'user')", openId);
        Long id = jdbc.queryForObject("select id from users where open_id = ?", Long.class, openId);
        return Map.of("userId", id, "openId", openId);
    }

    public Optional<Long> findByOpenId(String openId) {
        try {
            Long id = jdbc.queryForObject("select id from users where open_id = ?", Long.class, openId);
            return Optional.ofNullable(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
