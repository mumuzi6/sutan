<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '../api'

interface Dashboard {
  today: { date: string; messages: number; registers: number; dau: number }
  total: { users: number; messages: number; submissions: number }
  trend7d: Array<{ stat_date: string; dau: number; msg_count: number; register_count: number }>
}

const dashboard = ref<Dashboard | null>(null)
const loading = ref(false)

const fetch = async () => {
  loading.value = true
  try {
    const { data } = await api.get<Dashboard>('/admin/stats')
    dashboard.value = data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(fetch)
</script>

<template>
  <div class="admin-view" v-loading="loading">
    <h2>运营看板</h2>

    <el-row :gutter="20" v-if="dashboard">
      <!-- 今日 -->
      <el-col :span="8">
        <el-card>
          <template #header>今日 ({{ dashboard.today.date }})</template>
          <el-descriptions :column="1">
            <el-descriptions-item label="对话数">{{ dashboard.today.messages }}</el-descriptions-item>
            <el-descriptions-item label="新注册">{{ dashboard.today.registers }}</el-descriptions-item>
            <el-descriptions-item label="DAU">{{ dashboard.today.dau }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 累计 -->
      <el-col :span="8">
        <el-card>
          <template #header>累计</template>
          <el-descriptions :column="1">
            <el-descriptions-item label="总用户">{{ dashboard.total.users }}</el-descriptions-item>
            <el-descriptions-item label="总对话">{{ dashboard.total.messages }}</el-descriptions-item>
            <el-descriptions-item label="总批改">{{ dashboard.total.submissions }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 近7天趋势 -->
      <el-col :span="8">
        <el-card>
          <template #header>近 7 天趋势</template>
          <el-table :data="dashboard.trend7d" size="small" style="width: 100%">
            <el-table-column prop="stat_date" label="日期" width="110" />
            <el-table-column prop="dau" label="DAU" width="70" />
            <el-table-column prop="msg_count" label="对话" width="70" />
            <el-table-column prop="register_count" label="注册" width="70" />
          </el-table>
          <el-empty v-if="!dashboard.trend7d.length" description="暂无趋势数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-button type="primary" @click="fetch" style="margin-top: 16px">刷新</el-button>
  </div>
</template>

<style scoped>
.admin-view { padding: 16px; }
h2 { margin-bottom: 16px; }
</style>
