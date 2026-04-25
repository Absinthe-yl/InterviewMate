import axios from 'axios'
import { getToken, removeToken } from '@/utils/auth'

const api = axios.create({
  baseURL: '/api',
  timeout: 120000,  // 2分钟超时，AI分类需要较长时间
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    const res = response.data
    // 业务错误
    if (res.code !== 0) {
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      removeToken()
      window.location.href = '/login'
    }
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export default api