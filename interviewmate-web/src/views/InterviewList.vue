<template>
  <div class="interview-container">
    <header class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>模拟面试</h1>
      <div class="header-right">
        <button @click="handleLogout">退出</button>
      </div>
    </header>

    <main class="content">
      <div class="start-card" @click="goToStart">
        <div class="icon">🎯</div>
        <h2>开始新面试</h2>
        <p>上传简历，AI 将根据您的背景进行模拟面试</p>
      </div>

      <h3 class="section-title">历史记录</h3>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="history.length === 0" class="empty">
        暂无面试记录，开始您的第一次面试吧！
      </div>

      <div v-else class="history-list">
        <div v-for="item in history" :key="item.id" class="history-item" @click="viewDetail(item)">
          <div class="item-header">
            <span class="position">{{ item.positionType }}</span>
            <span :class="['status', item.status.toLowerCase()]">{{ statusText(item.status) }}</span>
          </div>
          <div class="item-info">
            <span class="difficulty">难度: {{ item.difficultyLevel }}</span>
            <span class="rounds">轮次: {{ item.currentRound }}/{{ item.totalRounds }}</span>
            <span v-if="item.totalScore" class="score">得分: {{ item.totalScore }}</span>
          </div>
          <div class="item-time">{{ formatTime(item.createdAt) }}</div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getHistory, deleteSession } from '@/api/interview'

const router = useRouter()
const userStore = useUserStore()

const history = ref([])
const loading = ref(false)

onMounted(async () => {
  await userStore.fetchUserInfo()
  await loadHistory()
})

async function loadHistory() {
  loading.value = true
  try {
    const res = await getHistory()
    history.value = res.data || []
  } catch (e) {
    console.error('加载历史记录失败', e)
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/')
}

function goToStart() {
  router.push('/interview/start')
}

function viewDetail(item) {
  if (item.status === 'COMPLETED') {
    router.push(`/interview/report/${item.id}`)
  } else if (item.status === 'IN_PROGRESS') {
    router.push(`/interview/${item.id}`)
  }
}

function statusText(status) {
  const map = {
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'INIT': '未开始'
  }
  return map[status] || status
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.interview-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  display: flex;
  align-items: center;
  padding: 20px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  gap: 20px;
}

.back-btn {
  background: rgba(255,255,255,0.2);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 5px;
  cursor: pointer;
}

.header h1 {
  flex: 1;
  font-size: 24px;
}

.header-right button {
  padding: 8px 16px;
  background: white;
  color: #667eea;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.content {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.start-card {
  background: white;
  border-radius: 10px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 30px;
}

.start-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.2);
}

.start-card .icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.start-card h2 {
  color: #333;
  margin-bottom: 10px;
}

.start-card p {
  color: #666;
}

.section-title {
  color: #333;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 3px solid #667eea;
}

.loading, .empty {
  text-align: center;
  padding: 60px;
  color: #999;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.history-item {
  background: white;
  border-radius: 10px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.history-item:hover {
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.position {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.status {
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
}

.status.in_progress {
  background: #fff3e0;
  color: #ff9800;
}

.status.completed {
  background: #e8f5e9;
  color: #4caf50;
}

.status.init {
  background: #f5f5f5;
  color: #999;
}

.item-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.score {
  color: #667eea;
  font-weight: bold;
}

.item-time {
  font-size: 12px;
  color: #999;
}
</style>
