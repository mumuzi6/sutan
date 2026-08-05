# 诉探 Sutan

> 面向法考二战/主观题党的、**会查法条、会模拟阅卷、且能基于你笔记构建专属知识库的 Agent 导师**。

法学备考不缺题库和课程，缺一个"会推理、能溯源、像阅卷人一样给你逐点反馈"的 AI 导师。诉探用 Agent 多步推理 + 法条级强制溯源，把"答题→查法条→模拟阅卷→定位薄弱点"串成闭环。

## 当前状态

🚧 **WIP · 规划与脚手架阶段** —— 设计文档见 [`docs/`](./docs)，按两周 MVP 排期推进中。

## 为什么不一样

- **领域垂直**：法考/法律（不是又一个恋爱/面试/客服 Agent）。
- **自研编排内核**：仅用 Spring AI 做模型/向量/工具抽象，**ReAct 循环、Tool 注册、Memory 全自研**——懂原理而非调包。
- **法条级强制溯源**：每个答案附法条出处 + 案号 + 真题年份，降低幻觉（liability 兜底 + grounding 亮点）。
- **真实用户 + 运营数据**：网页零门槛上线，admin 看板亮真实注册/对话/DAU。
- **个人知识库（v2）**：用户上传笔记/讲义/错题，Agent 基于"官方库 ∪ 个人库"答疑。

## 技术栈

Spring Boot 3.5 · Java 21 · Spring AI 1.0 · DeepSeek · pgvector · BGE-M3/reranker · Vue3 + Vite · Langfuse · Docker / Serverless · MCP（自研）

详见 [`docs/技术方案与开发计划-诉探.md`](./docs/技术方案与开发计划-诉探.md)。

## MVP 范围（两周）

1. 法条级可溯源 Agent 答疑
2. 模拟阅卷人主观题批改
3. 用户/会话/统计表 + admin 看板
4. 上线 + 冷启动获取真实用户

## 路线图

- [x] W1-D1~2 脚手架 + Docker + CI
- [x] W1-D3~4 语料入库 + embedding
- [x] W1-D5~7 RAG + 手写 ReAct + 工具
- [x] W2-D1~2 模拟阅卷人 + 批改页
- [ ] W2-D3~4 用户/统计/admin + Langfuse
- [ ] W2-D5 部署上线 + 冷启动

## 快速开始

> 需本地 JDK 21 + Maven 3.9+。embedding 用 SiliconFlow 免费 BGE-M3（https://siliconflow.cn）。

```bash
# 1. 起 PostgreSQL + pgvector
docker compose up -d postgres

# 2. 配置密钥（chat + embedding）
export DEEPSEEK_API_KEY=sk-xxxx          # DeepSeek
export EMBEDDING_API_KEY=sk-xxxx         # SiliconFlow

# 3. 构建运行后端
mvn -pl sutan-backend -am spring-boot:run

# 4. 入库种子法条 → 检索验证
curl -X POST http://localhost:8080/api/admin/ingest
curl "http://localhost:8080/api/rag/search?q=善意取得&topK=3"

# 验证
curl http://localhost:8080/api/health        # 服务+DB 探活
curl http://localhost:8080/api/ai/ping       # DeepSeek 烟雾测试
open http://localhost:8080/doc.html          # Knife4j 文档
```

## License

MIT
