<template>
  <div class="home-container">
    <header class="header">
      <h1>InterviewMate</h1>
      <div class="user-info">
        <span>{{ userInfo?.nickname || userInfo?.username }}</span>
        <span v-if="isAdmin" class="role-tag">管理员</span>
        <button v-if="isAdmin" class="admin-btn" @click="goToAdmin">管理后台</button>
        <button @click="handleLogout">退出</button>
      </div>
    </header>
    <main class="main">
      <div class="welcome">
        <h2>欢迎使用 InterviewMate</h2>
        <p>AI 面试模拟平台 - 帮助你更好地准备面试</p>
      </div>
      <div class="features">
        <div class="feature-card clickable" @click="goToStartInterview">
          <h3>模拟面试</h3>
          <p>AI 模拟真实面试场景，提供即时反馈</p>
          <span class="available">点击进入</span>
        </div>
        <div class="feature-card clickable" @click="goToKnowledge">
          <h3>知识库</h3>
          <p>八股文、算法、系统设计等知识整理</p>
          <span class="available">点击进入</span>
        </div>
        <div class="feature-card clickable" @click="goToHistory">
          <h3>面试记录</h3>
          <p>记录每次模拟面试，追踪学习进度</p>
          <span class="available">点击进入</span>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

onMounted(() => {
  userStore.fetchUserInfo()
})

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}

function goToAdmin() {
  router.push('/admin')
}

function goToKnowledge() {
  router.push('/knowledge')
}

function goToStartInterview() {
  router.push('/interview/start')
}

function goToHistory() {
  router.push('/interview/history')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header h1 {
  font-size: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.role-tag {
  padding: 4px 12px;
  background: rgba(255,255,255,0.2);
  border-radius: 4px;
  font-size: 12px;
}

.admin-btn {
  padding: 8px 16px;
  background: rgba(255,255,255,0.2);
  color: white;
  border: 1px solid rgba(255,255,255,0.5);
  border-radius: 5px;
  cursor: pointer;
}

.admin-btn:hover {
  background: rgba(255,255,255,0.3);
}

.user-info button:not(.admin-btn) {
  padding: 8px 16px;
  background: white;
  color: #667eea;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.main {
  padding: 40px;
}

.welcome {
  text-align: center;
  margin-bottom: 40px;
}

.welcome h2 {
  color: #333;
  margin-bottom: 10px;
}

.welcome p {
  color: #666;
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.feature-card {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.feature-card h3 {
  color: #667eea;
  margin-bottom: 15px;
}

.feature-card p {
  color: #666;
  margin-bottom: 15px;
}

.coming-soon {
  display: inline-block;
  padding: 5px 10px;
  background: #f0f0f0;
  color: #999;
  border-radius: 5px;
  font-size: 12px;
}

.available {
  display: inline-block;
  padding: 5px 10px;
  background: #667eea;
  color: white;
  border-radius: 5px;
  font-size: 12px;
}

.feature-card.clickable {
  cursor: pointer;
}

.feature-card.clickable:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}
</style>