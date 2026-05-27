import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi } from '../api/auth'
import router from '../router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 登录
  async function login(loginData) {
    try {
      const result = await loginApi(loginData)
      // 从正确的数据结构中提取token和userInfo
      token.value = result.token || result.data?.token
      userInfo.value = result.userInfo || result.data?.userInfo
      
      // 保存到localStorage
      localStorage.setItem('token', token.value)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      
      return result
    } catch (error) {
      throw error
    }
  }

  // 注册
  async function register(registerData) {
    try {
      const result = await registerApi(registerData)
      return result
    } catch (error) {
      throw error
    }
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  // 检查是否已登录
  function isAuthenticated() {
    return !!token.value
  }

  return {
    token,
    userInfo,
    login,
    register,
    logout,
    isAuthenticated
  }
})
