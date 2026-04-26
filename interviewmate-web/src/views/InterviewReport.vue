<template>
  <div class="report-container">
    <header class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>面试报告</h1>
      <div class="header-right">
        <button @click="handleLogout">退出</button>
      </div>
    </header>

    <main class="content" v-if="report">
      <div class="score-card">
        <div class="score-circle" :style="{ background: scoreColor }">
          <span class="score-value">{{ report.totalScore }}</span>
          <span class="score-label">总分</span>
        </div>
        <div class="score-info">
          <h2>{{ report.positionType }}</h2>
          <p>难度: {{ difficultyText }}</p>
          <p>轮次: {{ report.totalRounds }} 轮</p>
        </div>
      </div>

      <div class="report-section">
        <h3>💪 优势</h3>
        <p>{{ report.strengths || '暂无' }}</p>
      </div>

      <div class="report-section">
        <h3>📈 待改进</h3>
        <p>{{ report.weaknesses || '暂无' }}</p>
      </div>

      <div class="report-section">
        <h3>📚 学习建议</h3>
        <p>{{ report.suggestions || '暂无' }}</p>
      </div>

      <div class="report-section">
        <h3>💬 综合评价</h3>
        <p>{{ report.overallComment || '暂无' }}</p>
      </div>

      <div class="actions">
        <button class="retry-btn" @click="retry">再次面试</button>
        <button class="home-btn" @click="goHome">返回首页</button>
      </div>
    </main>

    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getReport } from '@/api/interview'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const report = ref(null)

onMounted(async () => {
  await userStore.fetchUserInfo()
  await loadReport()
})

async function loadReport() {
  try {
    const res = await getReport(route.params.id)
    report.value = res.data
  } catch (e) {
    alert('加载报告失败')
    router.push('/interview')
  }
}

const difficultyText = computed(() => {
  const map = { 'EASY': '简单', 'MEDIUM': '中等', 'HARD': '困难' }
  return map[report.value?.difficultyLevel] || report.value?.difficultyLevel
})

const scoreColor = computed(() => {
  const score = report.value?.totalScore || 0
  if (score >= 80) return 'linear-gradient(135deg, #4caf50, #8bc34a)'
  if (score >= 60) return 'linear-gradient(135deg, #ff9800, #ffc107)'
  return 'linear-gradient(135deg, #f44336, #e91e63)'
})

function goBack() {
  router.push('/interview')
}

function goHome() {
  router.push('/')
}

function retry() {
  router.push('/interview/start')
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.report-container {
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
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 20px;
}

.score-card {
  background: white;
  border-radius: 10px;
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 20px;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.score-value {
  font-size: 36px;
  font-weight: bold;
}

.score-label {
  font-size: 14px;
  opacity: 0.9;
}

.score-info h2 {
  color: #333;
  margin-bottom: 10px;
}

.score-info p {
  color: #666;
  margin-bottom: 5px;
}

.report-section {
  background: white;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 15px;
}

.report-section h3 {
  color: #333;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.report-section p {
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

.actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.retry-btn {
  flex: 1;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
}

.home-btn {
  flex: 1;
  padding: 14px;
  background: white;
  color: #667eea;
  border: 1px solid #667eea;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
}

.loading {
  text-align: center;
  padding: 100px;
  color: #999;
}
</style>
