import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(null)

  async function login(username, password) {
    const res = await loginApi({ username, password })
    // loginApi 成功返回时，res 就是 { code: 0, message: "...", data: { token: "..." } }
    if (res && res.data && res.data.token) {
      token.value = res.data.token
      setToken(res.data.token)
    }
    return res
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      console.error('Logout error:', e)
    }
    token.value = null
    userInfo.value = null
    removeToken()
  }

  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getCurrentUser()
      userInfo.value = res.data
    } catch (e) {
      logout()
    }
  }

  return {
    token,
    userInfo,
    login,
    logout,
    fetchUserInfo
  }
})
