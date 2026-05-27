import axios from 'axios'
import { ElMessage } from 'element-plus'

// 开发环境使用相对路径，通过 Vite 代理转发
// 生产环境使用环境变量中的完整 URL
const baseURL = import.meta.env.DEV 
  ? '/api' 
  : (import.meta.env.VITE_API_BASE_URL || '/api')

const request = axios.create({
  baseURL,
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token并添加到请求头
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 调试:打印请求参数
    // if (config.method === 'get' && config.params) {
    //   console.log('GET 请求参数:', config.url, config.params)
    // }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果是 blob 类型(文件下载),直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    if (res.code !== 200) {
      // Token过期或无效，跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    // 处理HTTP错误
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
