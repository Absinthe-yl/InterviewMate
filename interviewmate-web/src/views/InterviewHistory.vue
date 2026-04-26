<template>
  <div class="history-container">
    <header class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>面试记录</h1>
      <div class="header-right">
        <button @click="handleLogout">退出</button>
      </div>
    </header>

    <main class="content">
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总面试次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.avgScore || '--' }}</div>
          <div class="stat-label">平均得分</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <select v-model="filter.type" @change="loadHistory">
          <option value="">全部类型</option>
          <option value="COMPREHENSIVE">综合模拟</option>
          <option value="BAGU">八股模拟</option>
          <option value="PROJECT">项目模拟</option>
          <option value="INTERNSHIP">实习模拟</option>
        </select>
        <select v-model="filter.status" @change="loadHistory">
          <option value="">全部状态</option>
          <option value="IN_PROGRESS">进行中</option>
          <option value="COMPLETED">已完成</option>
        </select>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="history.length === 0" class="empty">
        <p>暂无面试记录</p>
        <button class="start-btn" @click="goToStart">开始面试</button>
      </div>

      <div v-else class="history-list">
        <div v-for="item in history" :key="item.id" class="history-item" @click="viewDetail(item)">
          <div class="item-header">
            <div class="item-title">
              <span class="position">{{ item.positionType }}</span>
              <span class="type-tag" :class="getTypeClass(item.interviewType)">
                {{ getTypeName(item.interviewType) }}
              </span>
            </div>
            <span :class="['status', item.status.toLowerCase()]">{{ statusText(item.status) }}</span>
          </div>
          <div class="item-info">
            <span class="difficulty">难度: {{ difficultyText(item.difficultyLevel) }}</span>
            <span class="rounds">轮次: {{ item.currentRound }}/{{ item.totalRounds }}</span>
            <span v-if="item.totalScore" class="score">得分: {{ item.totalScore }}</span>
          </div>
          <div class="item-footer">
            <span class="item-time">{{ formatTime(item.createdAt) }}</span>
            <button class="delete-btn" @click.stop="confirmDelete(item)">删除</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getHistory, deleteSession } from '@/api/interview'

const router = useRouter()
const userStore = useUserStore()

const history = ref([])
const loading = ref(false)

const filter = reactive({
  type: '',
  status: ''
})

const stats = computed(() => {
  const total = history.value.length
  const completed = history.value.filter(h => h.status === 'COMPLETED').length
  const scores = history.value.filter(h => h.totalScore).map(h => h.totalScore)
  const avgScore = scores.length > 0
    ? (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1)
    : null
  return { total, completed, avgScore }
})

onMounted(async () => {
  await userStore.fetchUserInfo()
  await loadHistory()
})

async function loadHistory() {
  loading.value = true
  try {
    const res = await getHistory()
    let list = res.data || []

    // 前端筛选
    if (filter.type) {
      list = list.filter(h => h.interviewType === filter.type)
    }
    if (filter.status) {
      list = list.filter(h => h.status === filter.status)
    }

    history.value = list
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

async function confirmDelete(item) {
  if (confirm('确定要删除这条面试记录吗？')) {
    try {
      await deleteSession(item.id)
      await loadHistory()
    } catch (e) {
      alert('删除失败: ' + (e.message || '未知错误'))
    }
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

function difficultyText(level) {
  const map = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return map[level] || level
}

function getTypeName(type) {
  const map = {
    'COMPREHENSIVE': '综合',
    'BAGU': '八股',
    'PROJECT': '项目',
    'INTERNSHIP': '实习'
  }
  return map[type] || '综合'
}

function getTypeClass(type) {
  const map = {
    'COMPREHENSIVE': 'type-comprehensive',
    'BAGU': 'type-bagu',
    'PROJECT': 'type-project',
    'INTERNSHIP': 'type-internship'
  }
  return map[type] || 'type-comprehensive'
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
.history-container {
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

.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.filter-bar select {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  background: white;
  font-size: 14px;
}

.loading, .empty {
  text-align: center;
  padding: 60px;
  color: #999;
  background: white;
  border-radius: 10px;
}

.start-btn {
  margin-top: 15px;
  padding: 10px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
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

.item-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.position {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.type-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.type-comprehensive {
  background: #e3f2fd;
  color: #1976d2;
}

.type-bagu {
  background: #fff3e0;
  color: #f57c00;
}

.type-project {
  background: #e8f5e9;
  color: #388e3c;
}

.type-internship {
  background: #fce4ec;
  color: #c2185b;
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
  margin-bottom: 10px;
}

.score {
  color: #667eea;
  font-weight: bold;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-time {
  font-size: 12px;
  color: #999;
}

.delete-btn {
  padding: 5px 12px;
  background: #fff;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.delete-btn:hover {
  background: #e74c3c;
  color: white;
}
</style>
