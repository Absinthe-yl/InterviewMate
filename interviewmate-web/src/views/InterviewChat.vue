<template>
  <div class="chat-container">
    <header class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>模拟面试</h1>
      <div class="header-right">
        <span class="round-info">第 {{ currentRound }}/{{ totalRounds }} 轮</span>
      </div>
    </header>

    <main class="chat-main" ref="chatMain">
      <div class="messages">
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
          <div class="avatar">{{ msg.role === 'assistant' ? '🤖' : '👤' }}</div>
          <div class="content">
            <div class="text">{{ msg.content }}</div>
            <div v-if="msg.score" class="score-info">
              <span class="score">得分: {{ msg.score }}</span>
              <span class="feedback">{{ msg.feedback }}</span>
            </div>
          </div>
        </div>

        <div v-if="thinking" class="message assistant">
          <div class="avatar">🤖</div>
          <div class="content">
            <div class="text thinking">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="thinking-text">{{ thinkingText }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>

    <footer class="chat-footer">
      <div v-if="isFinished" class="finished">
        <p>面试已结束！</p>
        <button @click="viewReport">查看报告</button>
      </div>
      <div v-else class="input-area">
        <textarea
          v-model="userInput"
          placeholder="请输入您的回答..."
          @keydown.enter.ctrl="sendMessage"
          :disabled="thinking"
        ></textarea>
        <div class="btn-group">
          <button @click="sendMessage" :disabled="!userInput.trim() || thinking" class="send-btn">
            发送
          </button>
          <button @click="handleEndInterview" :disabled="thinking" class="end-btn">
            结束面试
          </button>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSession, answer, endInterview } from '@/api/interview'

const route = useRoute()
const router = useRouter()

const sessionId = ref(null)
const currentRound = ref(1)
const totalRounds = ref(5)
const messages = ref([])
const userInput = ref('')
const thinking = ref(false)
const thinkingText = ref('正在思考')
const isFinished = ref(false)
const chatMain = ref(null)

onMounted(async () => {
  sessionId.value = route.params.id
  await loadSession()
})

async function loadSession() {
  try {
    const res = await getSession(sessionId.value)
    const session = res.data

    currentRound.value = session.currentRound
    totalRounds.value = session.totalRounds
    isFinished.value = session.status === 'COMPLETED'

    // 重建消息历史
    if (session.rounds && session.rounds.length > 0) {
      for (const round of session.rounds) {
        messages.value.push({
          role: 'assistant',
          content: round.question
        })
        if (round.userAnswer) {
          messages.value.push({
            role: 'user',
            content: round.userAnswer,
            score: round.score,
            feedback: round.feedback
          })
        }
      }
    }

    scrollToBottom()
  } catch (e) {
    alert('加载面试失败')
    router.push('/interview')
  }
}

async function sendMessage() {
  if (!userInput.value.trim() || thinking.value) return

  const answerText = userInput.value.trim()
  userInput.value = ''

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: answerText
  })
  scrollToBottom()

  thinking.value = true

  // 动态更新思考提示
  const thinkingSteps = ['正在评估回答', '正在分析要点', '正在生成反馈', '正在准备下一题']
  let stepIndex = 0
  const thinkingInterval = setInterval(() => {
    stepIndex = (stepIndex + 1) % thinkingSteps.length
    thinkingText.value = thinkingSteps[stepIndex]
  }, 1500)

  try {
    const res = await answer(sessionId.value, answerText)
    const data = res.data

    clearInterval(thinkingInterval)
    thinking.value = false

    // 更新最后一条消息的评分
    const lastMsg = messages.value[messages.value.length - 1]
    lastMsg.score = data.roundScore
    lastMsg.feedback = data.roundFeedback

    currentRound.value = data.currentRound

    if (data.isFinished) {
      isFinished.value = true
    } else {
      // 添加下一题
      messages.value.push({
        role: 'assistant',
        content: data.nextQuestion
      })
    }

    scrollToBottom()
  } catch (e) {
    alert('提交回答失败: ' + (e.message || '未知错误'))
  } finally {
    thinking.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatMain.value) {
      chatMain.value.scrollTop = chatMain.value.scrollHeight
    }
  })
}

function goBack() {
  router.push('/interview')
}

function viewReport() {
  router.push(`/interview/report/${sessionId.value}`)
}

async function handleEndInterview() {
  if (!confirm('确定要提前结束面试吗？将根据已完成的轮次生成报告。')) {
    return
  }

  try {
    thinking.value = true
    thinkingText.value = '正在结束面试'
    await endInterview(sessionId.value)
    isFinished.value = true
    thinking.value = false
    alert('面试已结束')
  } catch (e) {
    alert('结束面试失败: ' + (e.message || '未知错误'))
    thinking.value = false
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.header {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  gap: 15px;
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
  font-size: 20px;
}

.round-info {
  background: rgba(255,255,255,0.2);
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 14px;
}

.chat-main {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.messages {
  max-width: 800px;
  margin: 0 auto;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message .content {
  max-width: 70%;
}

.message .text {
  background: white;
  padding: 12px 16px;
  border-radius: 10px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.message.assistant .text {
  border-bottom-left-radius: 2px;
}

.message.user .text {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 2px;
}

.thinking {
  color: #667eea;
  display: flex;
  align-items: center;
  gap: 3px;
}

.thinking .dot {
  width: 6px;
  height: 6px;
  background: #667eea;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.thinking .dot:nth-child(1) { animation-delay: -0.32s; }
.thinking .dot:nth-child(2) { animation-delay: -0.16s; }
.thinking .dot:nth-child(3) { animation-delay: 0s; }

.thinking-text {
  margin-left: 8px;
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.score-info {
  margin-top: 8px;
  font-size: 13px;
}

.score {
  background: #667eea;
  color: white;
  padding: 3px 10px;
  border-radius: 10px;
  margin-right: 10px;
}

.feedback {
  color: #666;
}

.chat-footer {
  background: white;
  padding: 15px 20px;
  border-top: 1px solid #eee;
}

.finished {
  text-align: center;
  padding: 20px;
}

.finished p {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
}

.finished button {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
}

.input-area {
  display: flex;
  gap: 10px;
  max-width: 800px;
  margin: 0 auto;
}

.input-area textarea {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  resize: none;
  height: 60px;
  font-size: 14px;
}

.input-area textarea:focus {
  outline: none;
  border-color: #667eea;
}

.btn-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.send-btn {
  padding: 12px 25px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.end-btn {
  padding: 8px 15px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 13px;
}

.end-btn:hover {
  background: #c0392b;
}

.end-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
