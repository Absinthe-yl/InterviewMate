import api from './index'

export function register(data) {
  return api.post('/auth/register', data)
}

export function login(data) {
  return api.post('/auth/login', data)
}

export function logout() {
  return api.post('/auth/logout')
}

export function getCurrentUser() {
  return api.get('/user/me')
}

// 管理员接口
export function getPendingUsers() {
  return api.get('/admin/users/pending')
}

export function getAllUsers() {
  return api.get('/admin/users')
}

export function approveUser(id) {
  return api.put(`/admin/users/${id}/approve`)
}

export function rejectUser(id) {
  return api.put(`/admin/users/${id}/reject`)
}