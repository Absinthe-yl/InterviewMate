<template>
  <div class="admin-container">
    <header class="header">
      <h1>InterviewMate 管理后台</h1>
      <div class="user-info">
        <span>{{ userInfo?.nickname || userInfo?.username }}</span>
        <span class="role-tag">管理员</span>
        <button @click="handleLogout">退出</button>
      </div>
    </header>

    <main class="main">
      <div class="tabs">
        <button :class="{ active: activeTab === 'pending' }" @click="activeTab = 'pending'">
          待审核用户
        </button>
        <button :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">
          全部用户
        </button>
      </div>

      <!-- 待审核用户列表 -->
      <div v-if="activeTab === 'pending'" class="user-list">
        <div v-if="pendingUsers.length === 0" class="empty">
          暂无待审核用户
        </div>
        <div v-for="user in pendingUsers" :key="user.id" class="user-card">
          <div class="user-info-row">
            <span class="username">{{ user.username }}</span>
            <span class="nickname">{{ user.nickname }}</span>
            <span class="time">{{ formatDate(user.createdAt) }}</span>
          </div>
          <div class="actions">
            <button class="approve-btn" @click="handleApprove(user.id)">通过</button>
            <button class="reject-btn" @click="handleReject(user.id)">拒绝</button>
          </div>
        </div>
      </div>

      <!-- 全部用户列表 -->
      <div v-if="activeTab === 'all'" class="user-list">
        <div v-for="user in allUsers" :key="user.id" class="user-card">
          <div class="user-info-row">
            <span class="username">{{ user.username }}</span>
            <span class="nickname">{{ user.nickname }}</span>
            <span :class="['status-tag', user.status.toLowerCase()]">{{ getStatusText(user.status) }}</span>
            <span :class="['role-tag', user.role.toLowerCase()]">{{ user.role }}</span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPendingUsers, getAllUsers, approveUser, rejectUser } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const activeTab = ref('pending')
const pendingUsers = ref([])
const allUsers = ref([])

onMounted(async () => {
  await userStore.fetchUserInfo()
  // 检查是否是管理员
  if (userInfo.value?.role !== 'ADMIN') {
    router.push('/')
    return
  }
  loadPendingUsers()
})

async function loadPendingUsers() {
  try {
    const res = await getPendingUsers()
    pendingUsers.value = res.data || []
  } catch (e) {
    console.error('加载待审核用户失败', e)
  }
}

async function loadAllUsers() {
  try {
    const res = await getAllUsers()
    allUsers.value = res.data || []
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
}

async function handleApprove(id) {
  try {
    await approveUser(id)
    loadPendingUsers()
    if (activeTab.value === 'all') {
      loadAllUsers()
    }
  } catch (e) {
    alert('操作失败: ' + (e.message || '未知错误'))
  }
}

async function handleReject(id) {
  try {
    await rejectUser(id)
    loadPendingUsers()
    if (activeTab.value === 'all') {
      loadAllUsers()
    }
  } catch (e) {
    alert('操作失败: ' + (e.message || '未知错误'))
  }
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

function getStatusText(status) {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    default: return status
  }
}

// 监听 tab 变化
watch(activeTab, (newTab) => {
  if (newTab === 'all') {
    loadAllUsers()
  }
})

import { watch } from 'vue'
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: #f5f5f5;
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

.user-info button {
  padding: 8px 16px;
  background: white;
  color: #667eea;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.main {
  padding: 20px 40px;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tabs button {
  padding: 10px 20px;
  background: white;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 5px;
  cursor: pointer;
}

.tabs button.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.user-list {
  background: white;
  border-radius: 10px;
  padding: 20px;
}

.empty {
  text-align: center;
  color: #999;
  padding: 40px;
}

.user-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.user-card:last-child {
  border-bottom: none;
}

.user-info-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.username {
  font-weight: bold;
  color: #333;
}

.nickname {
  color: #666;
}

.time {
  color: #999;
  font-size: 12px;
}

.actions {
  display: flex;
  gap: 10px;
}

.approve-btn {
  padding: 8px 16px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.reject-btn {
  padding: 8px 16px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.status-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.pending {
  background: #fff3cd;
  color: #856404;
}

.status-tag.approved {
  background: #d4edda;
  color: #155724;
}

.status-tag.rejected {
  background: #f8d7da;
  color: #721c24;
}

.role-tag.user {
  background: #e2e3e5;
  color: #383d41;
}

.role-tag.admin {
  background: #cce5ff;
  color: #004085;
}
</style>