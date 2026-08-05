<script setup lang="ts">
import { ref, nextTick } from 'vue'
import api from '../api'

interface Citation {
  docType: string
  source: string
  articleNo: string
  content: string
  score: string | null
}

interface Step {
  thought: string
  action: string
  actionInput?: string
  observation?: string
}

interface AgentResponse {
  answer: string
  grounded: boolean
  groundNote?: string
  citations: Citation[]
  steps: Step[]
}

const input = ref('')
const loading = ref(false)
const answer = ref('')
const grounded = ref(true)
const groundNote = ref('')
const citations = ref<Citation[]>([])
const steps = ref<Step[]>([])
const chatBox = ref<HTMLElement>()

const ask = async () => {
  if (!input.value.trim() || loading.value) return
  loading.value = true
  answer.value = ''
  citations.value = []
  steps.value = []
  grounded.value = true
  groundNote.value = ''

  try {
    const { data } = await api.post<AgentResponse>('/agent/chat', { q: input.value })
    answer.value = data.answer
    grounded.value = data.grounded
    groundNote.value = data.groundNote || ''
    citations.value = data.citations || []
    steps.value = data.steps || []
  } catch (e: any) {
    answer.value = '请求失败：' + (e.message || e)
  } finally {
    loading.value = false
    await nextTick()
    chatBox.value?.scrollTo({ top: chatBox.value.scrollHeight, behavior: 'smooth' })
  }
}
</script>

<template>
  <div class="chat-view">
    <div ref="chatBox" class="chat-box">
      <el-card v-if="answer" class="answer-card">
        <template #header>
          <span>诉探回答</span>
          <el-tag v-if="!grounded" type="warning" size="small" style="margin-left: 8px">
            未溯源
          </el-tag>
        </template>
        <div class="answer-text">{{ answer }}</div>
        <el-alert v-if="groundNote" :title="groundNote" type="warning" :closable="false" style="margin-top: 12px" />

        <!-- 溯源引用 -->
        <div v-if="citations.length" class="citations">
          <div class="section-title">溯源引用（法条级可溯源）</div>
          <el-card v-for="(c, i) in citations" :key="i" shadow="never" class="citation-card">
            <div class="citation-source">
              <el-tag size="small">{{ c.docType }}</el-tag>
              {{ c.source }} {{ c.articleNo }}
            </div>
            <div class="citation-content">{{ c.content }}</div>
          </el-card>
        </div>

        <!-- 推理步骤 -->
        <div v-if="steps.length" class="steps">
          <el-collapse>
            <el-collapse-item title="推理步骤（ReAct）" name="steps">
              <div v-for="(s, i) in steps" :key="i" class="step">
                <div class="step-thought">Thought: {{ s.thought }}</div>
                <div v-if="s.action" class="step-action">Action: {{ s.action }}</div>
                <div v-if="s.observation" class="step-obs">Observation: {{ s.observation }}</div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>
    </div>

    <div class="input-area">
      <el-input
        v-model="input"
        type="textarea"
        :rows="2"
        placeholder="问诉探，例如：善意取得的构成要件是什么？"
        @keydown.enter.exact.prevent="ask"
      />
      <el-button type="primary" :loading="loading" @click="ask" style="margin-top: 12px">
        {{ loading ? '推理中...' : '提问' }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.chat-view { display: flex; flex-direction: column; height: calc(100vh - 60px); }
.chat-box { flex: 1; overflow-y: auto; padding: 16px; }
.answer-card { margin-bottom: 16px; }
.answer-text { white-space: pre-wrap; line-height: 1.8; }
.citations { margin-top: 16px; }
.section-title { font-weight: bold; margin-bottom: 8px; color: #409eff; }
.citation-card { margin-bottom: 8px; background: #f9f9f9; }
.citation-source { font-size: 13px; color: #666; margin-bottom: 4px; }
.citation-content { font-size: 14px; color: #333; }
.step { padding: 8px 0; border-bottom: 1px solid #eee; font-size: 13px; }
.step-thought { color: #409eff; }
.step-action { color: #e6a23c; }
.step-obs { color: #67c23a; }
.input-area { padding: 16px; border-top: 1px solid #eee; }
</style>
