<template>
  <div class="detail-container">
    <header class="header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>InterviewMate</h1>
      <div class="header-right">
        <button v-if="isAdmin" class="delete-btn" @click="handleDelete">删除</button>
        <button @click="handleLogout">退出</button>
      </div>
    </header>

    <main class="content" v-if="knowledge">
      <div class="title-section">
        <h1>{{ knowledge.title }}</h1>
        <div class="meta">
          <span v-if="knowledge.categoryName" class="category">{{ knowledge.categoryName }}</span>
          <span class="views">{{ knowledge.viewCount }} 次浏览</span>
          <span v-if="knowledge.tags" class="tags">{{ knowledge.tags }}</span>
        </div>
      </div>

      <article class="markdown-body" v-html="renderedContent"></article>
    </main>

    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getKnowledgeById, deleteKnowledge } from '@/api/knowledge'
import { marked } from 'marked'
import hljs from 'highlight.js'

// 配置 marked
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

const knowledge = ref(null)

const renderedContent = computed(() => {
  if (!knowledge.value?.content) return ''
  let content = knowledge.value.content

  // 按行处理，过滤掉不需要的行
  const lines = content.split('\n')
  const filteredLines = lines.filter(line => {
    const trimmed = line.trim()
    // 过滤掉更新时间行
    if (/^更新[：:]/.test(trimmed)) return false
    // 过滤掉原文链接行
    if (/^原文[：:]/.test(trimmed)) return false
    // 过滤掉包含 yuque.com 的行
    if (trimmed.includes('yuque.com')) return false
    // 过滤掉单独的 URL 行
    if (/^https?:\/\//.test(trimmed)) return false
    return true
  })

  return marked.parse(filteredLines.join('\n'))
})

onMounted(async () => {
  await userStore.fetchUserInfo()
  await loadKnowledge()
})

async function loadKnowledge() {
  const id = route.params.id
  try {
    const res = await getKnowledgeById(id)
    knowledge.value = res.data
  } catch (e) {
    alert('加载失败')
    router.push('/knowledge')
  }
}

function goBack() {
  router.push('/knowledge')
}

async function handleDelete() {
  if (!confirm('确定删除此知识点？')) return

  try {
    await deleteKnowledge(knowledge.value.id)
    router.push('/knowledge')
  } catch (e) {
    alert('删除失败')
  }
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.detail-container {
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

.header-right {
  display: flex;
  gap: 10px;
}

.header-right button {
  padding: 8px 16px;
  background: white;
  color: #667eea;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.delete-btn {
  background: #ff4d4f !important;
  color: white !important;
}

.content {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
  background: white;
  margin-top: 20px;
  border-radius: 10px;
}

.title-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.title-section h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 15px;
}

.meta {
  display: flex;
  gap: 15px;
  font-size: 14px;
  color: #999;
}

.category {
  background: #e8f4ff;
  color: #1890ff;
  padding: 3px 10px;
  border-radius: 3px;
}

.tags {
  color: #666;
}

.markdown-body {
  line-height: 1.8;
  color: #333;
}

.markdown-body :deep(h1) {
  font-size: 24px;
  margin: 30px 0 15px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.markdown-body :deep(h2) {
  font-size: 20px;
  margin: 25px 0 12px;
  color: #333;
}

.markdown-body :deep(h3) {
  font-size: 18px;
  margin: 20px 0 10px;
  color: #333;
}

.markdown-body :deep(h4) {
  font-size: 16px;
  margin: 15px 0 8px;
  color: #333;
}

.markdown-body :deep(p) {
  margin: 10px 0;
}

.markdown-body :deep(pre) {
  background: #282c34;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
  margin: 15px 0;
}

.markdown-body :deep(code) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
}

.markdown-body :deep(pre code) {
  color: #abb2bf;
  background: none;
  padding: 0;
  font-size: 14px;
}

.markdown-body :deep(p code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 3px;
  color: #e96900;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #667eea;
  padding-left: 15px;
  margin: 15px 0;
  color: #666;
  background: #f9f9f9;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 10px 0;
  padding-left: 20px;
}

.markdown-body :deep(li) {
  margin: 5px 0;
}

.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 15px 0;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
}

.markdown-body :deep(th) {
  background: #f6f8fa;
}

.markdown-body :deep(img) {
  max-width: 100%;
}

.markdown-body :deep(a) {
  color: #667eea;
}

.loading {
  text-align: center;
  padding: 100px;
  color: #999;
}
</style>