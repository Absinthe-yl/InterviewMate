import api from './index'

export function getPrompts() {
  return api.get('/admin/prompts')
}

export function getPrompt(key) {
  return api.get(`/admin/prompts/${key}`)
}

export function updatePrompt(key, data) {
  return api.put(`/admin/prompts/${key}`, data)
}
