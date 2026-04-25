import api from './index'

export function searchKnowledge(keyword, categoryId, page, pageSize) {
  return api.get('/knowledge/search', {
    params: { keyword, categoryId, page, pageSize }
  })
}

export function getKnowledgeById(id) {
  return api.get(`/knowledge/${id}`)
}

export function suggestKnowledge(keyword) {
  return api.get('/knowledge/suggest', {
    params: { keyword }
  })
}

export function getKnowledgeByCategory(categoryId, page, pageSize) {
  return api.get(`/knowledge/category/${categoryId}`, {
    params: { page, pageSize }
  })
}

export function uploadKnowledge(file, categoryId, tags) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('categoryId', categoryId)
  if (tags) formData.append('tags', tags)
  return api.post('/knowledge/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadKnowledgeWithAI(file, tags) {
  const formData = new FormData()
  formData.append('file', file)
  if (tags) formData.append('tags', tags)
  return api.post('/knowledge/upload/ai', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function batchUploadKnowledge(files) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return api.post('/knowledge/upload/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function deleteKnowledge(id) {
  return api.delete(`/knowledge/${id}`)
}

export function deleteKnowledgeBatch(ids) {
  return api.delete('/knowledge/batch', { data: ids })
}

export function deleteAllKnowledge() {
  return api.delete('/knowledge/all')
}

export function getCategories() {
  return api.get('/knowledge/categories')
}