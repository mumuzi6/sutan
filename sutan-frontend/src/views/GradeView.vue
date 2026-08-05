<script setup lang="ts">
import { ref } from 'vue'
import api from '../api'

interface LawRef {
  source: string
  articleNo: string
  content: string
}

interface GradeReport {
  conclusion: string
  hitPoints: string[]
  missedPoints: string[]
  lawCitationIssue: string
  logicFlaw: string
  suggestion: string
  score: number
  relatedLaws: LawRef[]
}

const question = ref('')
const answer = ref('')
const loading = ref(false)
const report = ref<GradeReport | null>(null)

const submit = async () => {
  if (!question.value.trim() || !answer.value.trim() || loading.value) return
  loading.value = true
  report.value = null

  try {
    const { data } = await api.post<GradeReport>('/grade/submit', {
      question: question.value,
      answer: answer.value,
      userId: 1
    })
    report.value = data
  } catch (e: any) {
    report.value = {
      conclusion: '批改失败',
      hitPoints: [],
      missedPoints: [],
      lawCitationIssue: 'N/A',
      logicFlaw: 'N/A',
      suggestion: '请求失败：' + (e.message || e),
      score: 0,
      relatedLaws: []
    }
  } finally {
    loading.value = false
  }
}

const scoreColor = (score: number) => {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}
</script>

<template>
  <div class="grade-view">
    <el-row :gutter="20">
      <!-- 输入区 -->
      <el-col :span="12">
        <el-card>
          <template #header>主观题作答</template>
          <el-input
            v-model="question"
            type="textarea"
            :rows="4"
            placeholder="题目：例如 论述善意取得的构成要件"
            style="margin-bottom: 12px"
          />
          <el-input
            v-model="answer"
            type="textarea"
            :rows="10"
            placeholder="在此输入你的作答..."
          />
          <el-button
            type="primary"
            :loading="loading"
            @click="submit"
            style="margin-top: 12px"
          >
            {{ loading ? '批改中...' : '提交批改' }}
          </el-button>
        </el-card>
      </el-col>

      <!-- 批改报告 -->
      <el-col :span="12">
        <el-card v-if="report">
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between">
              <span>阅卷报告</span>
              <div :style="{ color: scoreColor(report.score), fontSize: '24px', fontWeight: 'bold' }">
                {{ report.score }} 分
              </div>
            </div>
          </template>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="结论">{{ report.conclusion }}</el-descriptions-item>
            <el-descriptions-item label="法条引用">{{ report.lawCitationIssue }}</el-descriptions-item>
            <el-descriptions-item label="逻辑漏洞">{{ report.logicFlaw }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="report.hitPoints.length" style="margin-top: 16px">
            <div class="section-title" style="color: #67c23a">命中的采分点</div>
            <el-tag v-for="(p, i) in report.hitPoints" :key="i" type="success" style="margin: 4px">
              {{ p }}
            </el-tag>
          </div>

          <div v-if="report.missedPoints.length" style="margin-top: 12px">
            <div class="section-title" style="color: #f56c6c">缺失的采分点</div>
            <el-tag v-for="(p, i) in report.missedPoints" :key="i" type="danger" style="margin: 4px">
              {{ p }}
            </el-tag>
          </div>

          <el-alert
            v-if="report.suggestion"
            :title="report.suggestion"
            type="info"
            :closable="false"
            style="margin-top: 16px"
          />

          <div v-if="report.relatedLaws.length" style="margin-top: 16px">
            <div class="section-title" style="color: #409eff">相关法条（可溯源）</div>
            <el-card
              v-for="(law, i) in report.relatedLaws"
              :key="i"
              shadow="never"
              class="law-card"
            >
              <div class="law-source">{{ law.source }} {{ law.articleNo }}</div>
              <div class="law-content">{{ law.content }}</div>
            </el-card>
          </div>
        </el-card>
        <el-empty v-else description="提交作答后显示批改报告" />
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.grade-view { padding: 16px; }
.section-title { font-weight: bold; margin-bottom: 8px; }
.law-card { margin-bottom: 8px; background: #f9f9f9; }
.law-source { font-size: 13px; color: #666; margin-bottom: 4px; }
.law-content { font-size: 14px; color: #333; }
</style>
