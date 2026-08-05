import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import ChatView from './views/ChatView.vue'
import GradeView from './views/GradeView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/chat' },
    { path: '/chat', name: 'chat', component: ChatView },
    { path: '/grade', name: 'grade', component: GradeView }
  ]
})

createApp(App).use(router).use(ElementPlus).mount('#app')
