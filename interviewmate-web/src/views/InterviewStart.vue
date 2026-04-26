<template>
  <div class="start-container">
    <header class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>开始面试</h1>
      <div class="header-right">
        <button @click="handleLogout">退出</button>
      </div>
    </header>

    <main class="content">
      <div class="form-card">
        <h2>面试设置</h2>

        <!-- 面试类型选择 -->
        <div class="form-group">
          <label>面试类型</label>
          <div class="type-cards">
            <div class="type-card" :class="{ active: form.interviewType === 'COMPREHENSIVE' }" @click="form.interviewType = 'COMPREHENSIVE'">
              <div class="type-icon">🎯</div>
              <h4>综合模拟</h4>
              <p>全面考察，适合准备充分的同学</p>
            </div>
            <div class="type-card" :class="{ active: form.interviewType === 'BAGU' }" @click="form.interviewType = 'BAGU'">
              <div class="type-icon">📚</div>
              <h4>八股模拟</h4>
              <p>专注基础知识，巩固理论</p>
            </div>
            <div class="type-card" :class="{ active: form.interviewType === 'PROJECT' }" @click="form.interviewType = 'PROJECT'">
              <div class="type-icon">💼</div>
              <h4>项目模拟</h4>
              <p>深挖项目经验，展示实战能力</p>
            </div>
            <div class="type-card" :class="{ active: form.interviewType === 'INTERNSHIP' }" @click="form.interviewType = 'INTERNSHIP'">
              <div class="type-icon">🌱</div>
              <h4>实习模拟</h4>
              <p>适合实习生，偏基础问题</p>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>面试难度</label>
          <select v-model="form.difficulty">
            <option value="EASY">简单</option>
            <option value="MEDIUM">中等</option>
            <option value="HARD">困难</option>
          </select>
        </div>

        <div class="form-group">
          <label>面试轮次</label>
          <div class="rounds-input">
            <button class="round-btn" @click="decreaseRounds" :disabled="form.totalRounds <= 3">-</button>
            <span class="rounds-value">{{ form.totalRounds }} 轮</span>
            <button class="round-btn" @click="increaseRounds" :disabled="form.totalRounds >= 20">+</button>
          </div>
        </div>

        <!-- AI 推断的岗位类型显示 -->
        <div v-if="inferredPosition" class="inferred-position">
          <label>AI 推断岗位</label>
          <div class="position-display">
            <span class="position-tag">{{ inferredPosition }}</span>
            <button class="change-btn" @click="showPositionSelect = true">修改</button>
          </div>
        </div>

        <!-- 可选的岗位修改 -->
        <div v-if="showPositionSelect && !inferredPosition" class="form-group">
          <label>面试岗位（可选，AI 会自动推断）</label>
          <select v-model="form.positionType">
            <option value="">让 AI 自动推断</option>
            <option value="Java后端">Java后端</option>
            <option value="前端开发">前端开发</option>
            <option value="Python后端">Python后端</option>
            <option value="Go后端">Go后端</option>
            <option value="算法工程师">算法工程师</option>
            <option value="系统设计">系统设计</option>
          </select>
        </div>

        <!-- 简历上传（根据面试类型显示不同提示） -->
        <div v-if="showResumeUpload" class="form-group">
          <label>{{ resumeLabel }}</label>
          <div class="upload-area" @click="$refs.fileInput.click()">
            <input
              ref="fileInput"
              type="file"
              accept=".pdf,.doc,.docx,.txt"
              @change="handleFileSelect"
              style="display: none"
            />
            <p v-if="!form.resumeFile">{{ resumePlaceholder }}</p>
            <p v-else class="selected">{{ form.resumeFile.name }}</p>
          </div>
          <p v-if="resumeHint" class="hint">{{ resumeHint }}</p>
        </div>

        <div v-if="error" class="error">{{ error }}</div>

        <!-- 进度提示 -->
        <div v-if="loading" class="progress-box">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
          <p class="progress-text">{{ progressText }}</p>
        </div>

        <button class="submit-btn" @click="startInterview" :disabled="loading">
          {{ loading ? '准备中...' : '开始面试' }}
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { startInterview as startInterviewApi } from '@/api/interview'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  positionType: '',
  interviewType: 'COMPREHENSIVE',
  difficulty: 'MEDIUM',
  totalRounds: 5,
  resumeFile: null
})

const loading = ref(false)
const error = ref('')
const progress = ref(0)
const progressText = ref('')
const inferredPosition = ref('')
const showPositionSelect = ref(false)

// 根据面试类型决定是否显示简历上传
const showResumeUpload = computed(() => {
  // 八股模拟不需要简历
  return form.value.interviewType !== 'BAGU'
})

const resumeLabel = computed(() => {
  switch (form.value.interviewType) {
    case 'PROJECT':
      return '简历（仅提取项目经历）'
    case 'INTERNSHIP':
      return '简历（仅提取实习经历）'
    default:
      return '简历（可选，支持 PDF、Word、TXT 文件）'
  }
})

const resumePlaceholder = computed(() => {
  switch (form.value.interviewType) {
    case 'PROJECT':
      return '上传简历，面试将聚焦项目经历'
    case 'INTERNSHIP':
      return '上传简历，面试将聚焦实习经历'
    default:
      return '点击上传简历（支持 PDF、Word、TXT 格式）'
  }
})

const resumeHint = computed(() => {
  switch (form.value.interviewType) {
    case 'PROJECT':
      return '提示：面试仅考察项目经验，请确保简历包含项目经历'
    case 'INTERNSHIP':
      return '提示：面试仅考察实习经历，请确保简历包含实习经历'
    default:
      return ''
  }
})

const progressSteps = [
  { text: '正在解析简历...', duration: 2000 },
  { text: '正在推断岗位类型...', duration: 2000 },
  { text: '正在匹配知识库...', duration: 2000 },
  { text: '正在生成面试问题...', duration: 4000 },
  { text: '正在准备面试环境...', duration: 2000 }
]

function goBack() {
  router.push('/interview')
}

function handleFileSelect(e) {
  const file = e.target.files[0]
  if (file) {
    form.value.resumeFile = file
  }
}

function increaseRounds() {
  if (form.value.totalRounds < 20) {
    form.value.totalRounds++
  }
}

function decreaseRounds() {
  if (form.value.totalRounds > 3) {
    form.value.totalRounds--
  }
}

function startProgress() {
  progress.value = 0
  let currentStep = 0
  let elapsed = 0
  const totalDuration = progressSteps.reduce((sum, s) => sum + s.duration, 0)

  const interval = setInterval(() => {
    if (!loading.value) {
      clearInterval(interval)
      return
    }

    elapsed += 100
    progress.value = Math.min(95, (elapsed / totalDuration) * 100)

    // 更新当前步骤文本
    let stepTime = 0
    for (let i = 0; i < progressSteps.length; i++) {
      stepTime += progressSteps[i].duration
      if (elapsed < stepTime) {
        if (currentStep !== i) {
          currentStep = i
          progressText.value = progressSteps[i].text
        }
        break
      }
    }
  }, 100)
}

async function startInterview() {
  loading.value = true
  error.value = ''
  startProgress()

  try {
    const formData = new FormData()
    if (form.value.resumeFile) {
      formData.append('resume', form.value.resumeFile)
    }
    if (form.value.positionType) {
      formData.append('positionType', form.value.positionType)
    }
    formData.append('difficulty', form.value.difficulty)
    formData.append('totalRounds', form.value.totalRounds)
    formData.append('interviewType', form.value.interviewType)

    const res = await startInterviewApi(formData)
    progress.value = 100
    progressText.value = '准备完成！'
    setTimeout(() => {
      router.push(`/interview/${res.data.sessionId}`)
    }, 500)
  } catch (e) {
    error.value = e.message || '开始面试失败'
    loading.value = false
  }
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.start-container {
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
  display: flex;
  justify-content: center;
  padding: 50px 20px;
}

.form-card {
  background: white;
  border-radius: 10px;
  padding: 40px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.form-card h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 500;
}

.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 10px;
  padding: 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-area:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.upload-area p {
  color: #666;
}

.upload-area .selected {
  color: #667eea;
  font-weight: 500;
}

.error {
  color: #e74c3c;
  text-align: center;
  margin-bottom: 15px;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.progress-box {
  margin-bottom: 15px;
}

.progress-bar {
  height: 8px;
  background: #e8e8e8;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  color: #667eea;
  font-size: 14px;
  margin-top: 10px;
}

.inferred-position {
  margin-bottom: 25px;
}

.inferred-position label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 500;
}

.position-display {
  display: flex;
  align-items: center;
  gap: 15px;
}

.position-tag {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 5px;
  font-weight: 500;
}

.change-btn {
  padding: 8px 16px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.change-btn:hover {
  background: #e8e8e8;
}

.rounds-input {
  display: flex;
  align-items: center;
  gap: 15px;
}

.round-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 5px;
  background: #f5f5f5;
  color: #666;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.round-btn:hover:not(:disabled) {
  background: #e8e8e8;
  border-color: #667eea;
  color: #667eea;
}

.round-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.rounds-value {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  min-width: 60px;
  text-align: center;
}

.type-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.type-card {
  padding: 15px;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.type-card:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.type-card.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.type-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.type-card h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 14px;
}

.type-card p {
  margin: 0;
  color: #999;
  font-size: 12px;
}

.type-card.active h4 {
  color: #667eea;
}

.hint {
  margin-top: 8px;
  font-size: 12px;
  color: #e67e22;
}
</style>
