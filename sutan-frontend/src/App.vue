<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const activeIndex = ref(route.path)

const handleSelect = (key: string) => {
  router.push(key)
}
</script>

<template>
  <el-container style="min-height: 100vh">
    <el-header style="background: #2c3e50; display: flex; align-items: center">
      <span style="color: #fff; font-size: 20px; font-weight: bold; margin-right: 40px">
        诉探 · 法考 Agent 导师
      </span>
      <el-menu
        :default-active="activeIndex"
        mode="horizontal"
        background-color="#2c3e50"
        text-color="#fff"
        active-text-color="#ffd04b"
        @select="handleSelect"
      >
        <el-menu-item index="/chat">AI 对话</el-menu-item>
        <el-menu-item index="/grade">主观题批改</el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>
