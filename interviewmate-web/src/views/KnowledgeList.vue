<template>
  <div class="knowledge-container">
    <header class="header">
      <h1>InterviewMate 知识库</h1>
      <div class="header-right">
        <button v-if="isAdmin" class="upload-btn" @click="showUpload = true">上传文档</button>
        <button @click="handleLogout">退出</button>
      </div>
    </header>

    <div class="main-content">
      <!-- 左侧分类 -->
      <aside class="sidebar">
        <h3>分类</h3>
        <ul class="category-list">
          <li :class="{ active: !selectedCategory }" @click="selectCategory(null)">全部</li>
          <li
            v-for="cat in categories"
            :key="cat.id"
            :class="{ active: selectedCategory === cat.id }"
            @click="selectCategory(cat.id)"
          >
            {{ cat.name }} ({{ cat.knowledgeCount }})
          </li>
        </ul>
      </aside>

      <!-- 右侧内容 -->
      <main class="content">
        <!-- 搜索框 -->
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索知识点..."
            @input="handleSearch"
            @keyup.enter="search"
          />
          <button @click="search">搜索</button>
        </div>

        <!-- 搜索建议 -->
        <div v-if="suggestions.length > 0" class="suggestions">
          <span
            v-for="s in suggestions"
            :key="s"
            class="suggestion-item"
            @click="selectSuggestion(s)"
          >{{ s }}</span>
        </div>

        <!-- 批量操作栏 (仅管理员可见) -->
        <div v-if="isAdmin && list.length > 0" class="batch-actions">
          <label class="select-all">
            <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
            全选当前页
          </label>
          <span class="selected-count" v-if="selectedIds.length > 0">
            已选择 {{ selectedIds.length }} 项
          </span>
          <button
            v-if="selectedIds.length > 0"
            class="delete-btn"
            @click="handleBatchDelete"
          >
            批量删除
          </button>
          <button class="clear-all-btn" @click="handleClearAll">
            清空知识库
          </button>
        </div>

        <!-- 知识点列表 -->
        <div class="knowledge-list">
          <div v-if="list.length === 0" class="empty">暂无数据</div>
          <div
            v-for="item in list"
            :key="item.id"
            class="knowledge-item"
            :class="{ selected: selectedIds.includes(item.id) }"
          >
            <input
              v-if="isAdmin"
              type="checkbox"
              class="item-checkbox"
              :checked="selectedIds.includes(item.id)"
              @change="toggleSelect(item.id)"
              @click.stop
            />
            <div class="item-content" @click="goToDetail(item.id)">
              <h3>{{ item.title }}</h3>
              <p class="summary">{{ item.summary }}</p>
              <div class="meta">
                <span v-if="item.categoryName" class="category">{{ item.categoryName }}</span>
                <span class="views">{{ item.viewCount }} 次浏览</span>
              </div>
            </div>
            <button v-if="isAdmin" class="item-delete" @click.stop="handleDelete(item.id)">删除</button>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination">
          <button :disabled="page === 1" @click="changePage(page - 1)">上一页</button>
          <span>{{ page }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
        </div>
      </main>
    </div>

    <!-- 上传弹窗 -->
    <div v-if="showUpload" class="modal-overlay" @click.self="closeUpload">
      <div class="modal">
        <h3>上传文档</h3>

        <!-- 上传方式选择 -->
        <div class="upload-tabs">
          <button :class="{ active: uploadMode === 'ai' }" @click="uploadMode = 'ai'">AI智能分类</button>
          <button :class="{ active: uploadMode === 'manual' }" @click="uploadMode = 'manual'">手动选择分类</button>
        </div>

        <!-- 文件上传区域 -->
        <div class="upload-area"
             @dragover.prevent="dragover = true"
             @dragleave="dragover = false"
             @drop.prevent="handleDrop"
             :class="{ dragover }">
          <input
            ref="fileInput"
            type="file"
            accept=".md"
            multiple
            webkitdirectory
            @change="handleFileSelect"
            style="display: none"
          />
          <p v-if="selectedFiles.length === 0">
            拖拽文件/文件夹到此处，或
            <span class="click-here" @click="$refs.fileInput.click()">点击选择</span>
          </p>
          <div v-else class="selected-files">
            <p>已选择 {{ selectedFiles.length }} 个文件：</p>
            <ul>
              <li v-for="(f, i) in selectedFiles.slice(0, 5)" :key="i">{{ f.name }}</li>
              <li v-if="selectedFiles.length > 5">... 还有 {{ selectedFiles.length - 5 }} 个文件</li>
            </ul>
            <button class="clear-btn" @click="selectedFiles = []">清除选择</button>
          </div>
        </div>

        <!-- 手动分类模式 -->
        <div v-if="uploadMode === 'manual'" class="form-group">
          <label>分类</label>
          <select v-model="uploadForm.categoryId">
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>

        <div class="form-group">
          <label>标签 (可选，逗号分隔)</label>
          <input v-model="uploadForm.tags" type="text" placeholder="Redis,性能" />
        </div>

        <!-- 上传进度 -->
        <div v-if="uploading" class="upload-progress">
          <p>正在上传... {{ uploadProgress.current }} / {{ uploadProgress.total }}</p>
          <p class="upload-status">成功: {{ uploadResults.success.length }} | 失败: {{ uploadResults.failed.length }}</p>
          <div class="progress-bar">
            <div class="progress" :style="{ width: uploadProgress.percent + '%' }"></div>
          </div>
          <button class="cancel-btn" @click="cancelUpload">中断上传</button>
        </div>

        <div class="modal-actions">
          <button @click="closeUpload" :disabled="uploading">取消</button>
          <button class="primary" @click="handleUpload" :disabled="selectedFiles.length === 0 || uploading">
            {{ uploading ? '上传中...' : '上传' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { searchKnowledge, suggestKnowledge, uploadKnowledge, uploadKnowledgeWithAI, batchUploadKnowledge, deleteKnowledge, deleteKnowledgeBatch, deleteAllKnowledge, getCategories } from '@/api/knowledge'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

const keyword = ref('')
const selectedCategory = ref(null)
const categories = ref([])
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const suggestions = ref([])
const selectedIds = ref([])
const selectAll = ref(false)

const showUpload = ref(false)
const uploadMode = ref('ai')
const selectedFiles = ref([])
const dragover = ref(false)
const uploading = ref(false)
const uploadCancelled = ref(false)
const uploadProgress = ref({ current: 0, total: 0, percent: 0 })
const uploadResults = ref({ success: [], failed: [] })
const uploadForm = ref({
  categoryId: null,
  tags: ''
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

onMounted(async () => {
  await userStore.fetchUserInfo()
  await loadCategories()
  await search()
})

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

async function search() {
  try {
    const res = await searchKnowledge(keyword.value, selectedCategory.value, page.value, pageSize.value)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
    suggestions.value = []
    selectedIds.value = []
    selectAll.value = false
  } catch (e) {
    console.error('搜索失败', e)
  }
}

let searchTimer = null
async function handleSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    if (keyword.value.length >= 2) {
      try {
        const res = await suggestKnowledge(keyword.value)
        suggestions.value = res.data || []
      } catch (e) {
        suggestions.value = []
      }
    } else {
      suggestions.value = []
    }
  }, 300)
}

function selectSuggestion(s) {
  keyword.value = s
  suggestions.value = []
  page.value = 1
  search()
}

function selectCategory(catId) {
  selectedCategory.value = catId
  page.value = 1
  search()
}

function changePage(newPage) {
  page.value = newPage
  search()
}

function goToDetail(id) {
  router.push(`/knowledge/${id}`)
}

function toggleSelectAll() {
  if (selectAll.value) {
    selectedIds.value = list.value.map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

function toggleSelect(id) {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
  selectAll.value = selectedIds.value.length === list.value.length
}

async function handleDelete(id) {
  if (!confirm('确定删除此知识点？')) return
  try {
    await deleteKnowledge(id)
    search()
  } catch (e) {
    alert('删除失败')
  }
}

async function handleBatchDelete() {
  if (!confirm(`确定删除选中的 ${selectedIds.value.length} 个知识点？`)) return
  try {
    await deleteKnowledgeBatch(selectedIds.value)
    search()
  } catch (e) {
    alert('批量删除失败')
  }
}

async function handleClearAll() {
  if (!confirm('确定清空整个知识库？此操作不可恢复！')) return
  if (!confirm('再次确认：真的要清空所有知识点吗？')) return
  try {
    await deleteAllKnowledge()
    alert('知识库已清空')
    search()
  } catch (e) {
    alert('清空失败')
  }
}

function handleFileSelect(e) {
  const files = Array.from(e.target.files)
    .filter(f => f.name.endsWith('.md') && f.size > 0) // 过滤空文件
  selectedFiles.value = files
}

function handleDrop(e) {
  dragover.value = false
  const droppedFiles = Array.from(e.dataTransfer.files)
    .filter(f => f.name.endsWith('.md') && f.size > 0) // 过滤空文件
  selectedFiles.value = droppedFiles
}

function closeUpload() {
  showUpload.value = false
  selectedFiles.value = []
  uploadForm.value = { categoryId: null, tags: '' }
  uploadProgress.value = { current: 0, total: 0, percent: 0 }
  uploadResults.value = { success: [], failed: [] }
  uploadCancelled.value = false
}

async function handleUpload() {
  if (selectedFiles.value.length === 0) return

  uploading.value = true
  uploadCancelled.value = false
  uploadProgress.value = { current: 0, total: selectedFiles.value.length, percent: 0 }
  uploadResults.value = { success: [], failed: [] }

  const concurrency = 2 // 并发数（降低避免AI API限流）
  const delayBetweenRequests = 800 // 每个请求间隔ms
  const files = [...selectedFiles.value]
  let completed = 0

  // 延迟函数
  const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

  // 上传单个文件
  const uploadFile = async (file) => {
    try {
      let res
      if (uploadMode.value === 'ai') {
        res = await uploadKnowledgeWithAI(file, uploadForm.value.tags)
      } else {
        if (!uploadForm.value.categoryId) {
          return { name: file.name, error: '请选择分类' }
        }
        res = await uploadKnowledge(file, uploadForm.value.categoryId, uploadForm.value.tags)
      }
      if (res.data) {
        return { success: true, name: file.name }
      }
    } catch (e) {
      const errMsg = e.response?.data?.message || e.message || '未知错误'
      return { name: file.name, error: errMsg }
    }
    return null
  }

  try {
    // 使用并发池
    const executing = new Set()

    for (const file of files) {
      if (uploadCancelled.value) break

      const promise = uploadFile(file).then(async result => {
        executing.delete(promise)
        completed++
        uploadProgress.value.current = completed
        uploadProgress.value.percent = Math.round(completed / files.length * 100)

        if (result) {
          if (result.success) {
            uploadResults.value.success.push(result.name)
          } else {
            uploadResults.value.failed.push(result)
          }
        }

        // 每个请求完成后等待一段时间，避免AI API限流
        if (!uploadCancelled.value && completed < files.length) {
          await delay(delayBetweenRequests)
        }
      })

      executing.add(promise)

      // 控制并发数
      if (executing.size >= concurrency) {
        await Promise.race(executing)
      }
    }

    // 等待所有剩余请求完成
    await Promise.all(executing)

    // 显示结果
    if (uploadCancelled.value) {
      alert('上传已中断')
    }

    let msg = `上传完成！\n成功: ${uploadResults.value.success.length} 个`
    if (uploadResults.value.failed.length > 0) {
      msg += `\n失败: ${uploadResults.value.failed.length} 个`
      if (uploadResults.value.failed.length <= 10) {
        msg += '\n\n失败文件:\n' + uploadResults.value.failed.map(f => `${f.name}: ${f.error}`).join('\n')
      }
    }
    alert(msg)
    closeUpload()
    search()
  } catch (e) {
    alert('上传失败: ' + (e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

function cancelUpload() {
  uploadCancelled.value = true
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.knowledge-container {
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

.upload-btn {
  background: rgba(255,255,255,0.2) !important;
  color: white !important;
}

.main-content {
  display: flex;
  padding: 20px 40px;
  gap: 20px;
}

.sidebar {
  width: 200px;
  background: white;
  border-radius: 10px;
  padding: 20px;
  height: fit-content;
}

.sidebar h3 {
  margin-bottom: 15px;
  color: #333;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-list li {
  padding: 10px;
  cursor: pointer;
  border-radius: 5px;
  margin-bottom: 5px;
  color: #666;
}

.category-list li:hover {
  background: #f0f0f0;
}

.category-list li.active {
  background: #667eea;
  color: white;
}

.content {
  flex: 1;
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.search-box input {
  flex: 1;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
}

.search-box input:focus {
  outline: none;
  border-color: #667eea;
}

.search-box button {
  padding: 12px 25px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.suggestion-item {
  padding: 5px 12px;
  background: #e8e8e8;
  border-radius: 15px;
  font-size: 12px;
  cursor: pointer;
}

.suggestion-item:hover {
  background: #667eea;
  color: white;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  padding: 10px 15px;
  background: white;
  border-radius: 5px;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.selected-count {
  color: #667eea;
  font-size: 14px;
}

.delete-btn {
  padding: 6px 12px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.clear-all-btn {
  padding: 6px 12px;
  background: #ff7875;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  margin-left: auto;
}

.knowledge-list {
  background: white;
  border-radius: 10px;
}

.empty {
  text-align: center;
  padding: 60px;
  color: #999;
}

.knowledge-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.knowledge-item:last-child {
  border-bottom: none;
}

.knowledge-item:hover {
  background: #fafafa;
}

.knowledge-item.selected {
  background: #f0f7ff;
}

.item-checkbox {
  margin-right: 15px;
}

.item-content {
  flex: 1;
  cursor: pointer;
}

.item-content h3 {
  color: #333;
  margin-bottom: 5px;
  font-size: 16px;
}

.item-content .summary {
  color: #666;
  font-size: 13px;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-content .meta {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: #999;
}

.item-content .category {
  background: #e8f4ff;
  color: #1890ff;
  padding: 2px 8px;
  border-radius: 3px;
}

.item-delete {
  padding: 5px 10px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.pagination button {
  padding: 8px 16px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 5px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 30px;
  border-radius: 10px;
  width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal h3 {
  margin-bottom: 20px;
}

.upload-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.upload-tabs button {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 5px;
  cursor: pointer;
}

.upload-tabs button.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 10px;
  padding: 30px 20px;
  text-align: center;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.upload-area.dragover {
  border-color: #667eea;
  background: #f8f9ff;
}

.upload-area p {
  color: #666;
}

.click-here {
  color: #667eea;
  cursor: pointer;
  text-decoration: underline;
}

.selected-files {
  text-align: left;
}

.selected-files ul {
  margin: 10px 0 0 20px;
  color: #666;
  font-size: 12px;
  max-height: 100px;
  overflow-y: auto;
}

.clear-btn {
  margin-top: 10px;
  padding: 5px 10px;
  background: #f0f0f0;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #666;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.upload-progress {
  margin-bottom: 15px;
}

.upload-progress p {
  margin-bottom: 5px;
  color: #667eea;
}

.upload-status {
  font-size: 12px;
  color: #666;
}

.cancel-btn {
  margin-top: 10px;
  padding: 8px 20px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.progress-bar {
  height: 6px;
  background: #e8e8e8;
  border-radius: 3px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: #667eea;
  transition: width 0.3s;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.modal-actions button {
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 5px;
  cursor: pointer;
}

.modal-actions button.primary {
  background: #667eea;
  color: white;
  border: none;
}

.modal-actions button.primary:disabled {
  background: #ccc;
}
</style>