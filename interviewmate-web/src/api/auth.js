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
