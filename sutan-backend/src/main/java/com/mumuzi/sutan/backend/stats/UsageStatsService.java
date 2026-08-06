package com.mumuzi.sutan.backend.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用量统计服务：定时聚合 UV/DAU/对话数/注册数 到 usage_stats 表。
 * 这是 admin 看板的数据源，也是简历"真实用户运营数据"的核武器。
 */
@Service
public class UsageStatsService {

    private static final Logger log = LoggerFactory.getLogger(UsageStatsService.class);

    private final JdbcTemplate jdbc;

    public UsageStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 每天凌晨 1 点聚合前一天数据 */
    @Scheduled(cron = "0 0 1 * * ?")
    public void aggregateDaily() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        aggregateForDate(yesterday);
    }

    /** 手动触发聚合指定日期（供 admin 接口调用） */
    public void aggregateForDate(LocalDate date) {
        try {
            Integer registerCount = jdbc.queryForObject(
                    "select count(*) from users where date(created_at) = ?", Integer.class, date);
            Integer msgCount = jdbc.queryForObject(
                    "select count(*) from messages where date(created_at) = ?", Integer.class, date);
            Integer dau = jdbc.queryForObject(
                    "select count(distinct s.user_id) from messages m join sessions s on m.session_id = s.id where date(m.created_at) = ?",
                    Integer.class, date);

            registerCount = registerCount == null ? 0 : registerCount;
            msgCount = msgCount == null ? 0 : msgCount;
            dau = dau == null ? 0 : dau;

            jdbc.update("""
                    insert into usage_stats (stat_date, uv, dau, msg_count, register_count)
                    values (?, ?, ?, ?, ?)
                    on conflict (stat_date) do update set
                      uv = excluded.uv, dau = excluded.dau,
                      msg_count = excluded.msg_count, register_count = excluded.register_count
                    """, date, dau, dau, msgCount, registerCount);

            log.info("统计聚合完成 {}：注册{} 对话{} DAU{}", date, registerCount, msgCount, dau);
        } catch (Exception e) {
            log.error("统计聚合失败: {}", e.getMessage());
        }
    }

    /** 获取看板数据：今日 + 累计 */
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        // 今日
        Integer todayMsg = jdbc.queryForObject(
                "select count(*) from messages where date(created_at) = ?", Integer.class, today);
        Integer todayReg = jdbc.queryForObject(
                "select count(*) from users where date(created_at) = ?", Integer.class, today);
        Integer todayDau = jdbc.queryForObject(
                "select count(distinct s.user_id) from messages m join sessions s on m.session_id = s.id where date(m.created_at) = ?",
                Integer.class, today);

        dashboard.put("today", Map.of(
                "date", today.toString(),
                "messages", todayMsg == null ? 0 : todayMsg,
                "registers", todayReg == null ? 0 : todayReg,
                "dau", todayDau == null ? 0 : todayDau));

        // 累计
        Integer totalUsers = jdbc.queryForObject("select count(*) from users", Integer.class);
        Integer totalMsgs = jdbc.queryForObject("select count(*) from messages", Integer.class);
        Integer totalSubmissions = jdbc.queryForObject("select count(*) from submissions", Integer.class);

        dashboard.put("total", Map.of(
                "users", totalUsers == null ? 0 : totalUsers,
                "messages", totalMsgs == null ? 0 : totalMsgs,
                "submissions", totalSubmissions == null ? 0 : totalSubmissions));

        // 近 7 天趋势
        List<Map<String, Object>> trend = jdbc.queryForList("""
                select stat_date, dau, msg_count, register_count
                from usage_stats
                where stat_date >= current_date - 7
                order by stat_date
                """);
        dashboard.put("trend7d", trend);

        return dashboard;
    }
}
