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
        <button :class="{ active: activeTab === 'prompts' }" @click="activeTab = 'prompts'">
          Prompt 管理
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

      <!-- Prompt 管理 -->
      <div v-if="activeTab === 'prompts'" class="prompt-section">
        <!-- 类型筛选 -->
        <div class="prompt-filter">
          <button
            v-for="cat in promptCategories"
            :key="cat.key"
            :class="['filter-btn', { active: promptFilter === cat.key }]"
            @click="promptFilter = cat.key"
          >
            {{ cat.name }}
            <span class="count">{{ getPromptCount(cat.key) }}</span>
          </button>
        </div>

        <div class="prompt-list">
          <div v-if="filteredPrompts.length === 0" class="empty">
            暂无模板
          </div>
          <div v-for="prompt in filteredPrompts" :key="prompt.templateKey" class="prompt-card">
            <div class="prompt-header">
              <div class="prompt-title-row">
                <h3>{{ prompt.templateName }}</h3>
                <span :class="['type-tag', getPromptType(prompt.templateKey)]">
                  {{ getPromptTypeName(prompt.templateKey) }}
                </span>
              </div>
              <span class="prompt-key">{{ prompt.templateKey }}</span>
            </div>
            <p class="prompt-desc">{{ prompt.description }}</p>
            <div class="prompt-content">
              <textarea
                v-model="prompt.editContent"
                rows="8"
                placeholder="模板内容..."
              ></textarea>
            </div>
            <div class="prompt-actions">
              <button class="save-btn" @click="handleSavePrompt(prompt)" :disabled="prompt.saving">
                {{ prompt.saving ? '保存中...' : '保存' }}
              </button>
              <button class="reset-btn" @click="handleResetPrompt(prompt)">重置</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPendingUsers, getAllUsers, approveUser, rejectUser } from '@/api/auth'
import { getPrompts, updatePrompt } from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const activeTab = ref('pending')
const pendingUsers = ref([])
const allUsers = ref([])
const prompts = ref([])
const promptFilter = ref('all')

// Prompt 分类配置
const promptCategories = [
  { key: 'all', name: '全部' },
  { key: 'common', name: '通用模板' },
  { key: 'comprehensive', name: '综合模拟' },
  { key: 'bagu', name: '八股模拟' },
  { key: 'project', name: '项目模拟' },
  { key: 'internship', name: '实习模拟' }
]

// 根据 templateKey 判断类型
function getPromptType(key) {
  if (key.includes('_BAGU')) return 'bagu'
  if (key.includes('_PROJECT')) return 'project'
  if (key.includes('_INTERNSHIP')) return 'internship'
  if (key.includes('INTERVIEW_QUESTION') || key.includes('ANSWER_EVALUATE')) return 'comprehensive'
  return 'common'
}

function getPromptTypeName(key) {
  const type = getPromptType(key)
  const names = {
    common: '通用',
    comprehensive: '综合',
    bagu: '八股',
    project: '项目',
    internship: '实习'
  }
  return names[type] || '通用'
}

// 筛选后的 prompts
const filteredPrompts = computed(() => {
  if (promptFilter.value === 'all') return prompts.value
  return prompts.value.filter(p => getPromptType(p.templateKey) === promptFilter.value)
})

// 获取某类型的数量
function getPromptCount(categoryKey) {
  if (categoryKey === 'all') return prompts.value.length
  return prompts.value.filter(p => getPromptType(p.templateKey) === categoryKey).length
}

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

async function loadPrompts() {
  try {
    const res = await getPrompts()
    prompts.value = (res.data || []).map(p => ({
      ...p,
      editContent: p.templateContent,
      saving: false
    }))
  } catch (e) {
    console.error('加载 Prompt 模板失败', e)
  }
}

async function handleSavePrompt(prompt) {
  prompt.saving = true
  try {
    await updatePrompt(prompt.templateKey, { templateContent: prompt.editContent })
    prompt.templateContent = prompt.editContent
    alert('保存成功')
  } catch (e) {
    alert('保存失败: ' + (e.message || '未知错误'))
  } finally {
    prompt.saving = false
  }
}

function handleResetPrompt(prompt) {
  prompt.editContent = prompt.templateContent
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
  } else if (newTab === 'prompts') {
    loadPrompts()
  }
})
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

.prompt-list {
  background: white;
  border-radius: 10px;
  padding: 20px;
}

.prompt-card {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 20px;
}

.prompt-card:last-child {
  margin-bottom: 0;
}

.prompt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.prompt-header h3 {
  margin: 0;
  color: #333;
}

.prompt-key {
  padding: 4px 10px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  font-family: monospace;
}

.prompt-desc {
  color: #888;
  font-size: 14px;
  margin-bottom: 15px;
}

.prompt-content textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-family: monospace;
  font-size: 13px;
  resize: vertical;
  min-height: 150px;
}

.prompt-content textarea:focus {
  outline: none;
  border-color: #667eea;
}

.prompt-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.save-btn {
  padding: 8px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.save-btn:hover:not(:disabled) {
  background: #5a6fd6;
}

.save-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.reset-btn {
  padding: 8px 20px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 5px;
  cursor: pointer;
}

.reset-btn:hover {
  background: #e8e8e8;
}

.prompt-section {
  background: white;
  border-radius: 10px;
  padding: 20px;
}

.prompt-filter {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 8px 16px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.filter-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.filter-btn .count {
  background: rgba(0,0,0,0.1);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
}

.filter-btn.active .count {
  background: rgba(255,255,255,0.2);
}

.prompt-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.type-tag {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.type-tag.common {
  background: #e2e3e5;
  color: #383d41;
}

.type-tag.comprehensive {
  background: #cce5ff;
  color: #004085;
}

.type-tag.bagu {
  background: #d4edda;
  color: #155724;
}

.type-tag.project {
  background: #fff3cd;
  color: #856404;
}

.type-tag.internship {
  background: #f8d7da;
  color: #721c24;
}
</style>