<template>
  <div class="login-container">
    <!-- 动态背景粒子效果 -->
    <div class="particles">
      <div v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <div class="login-box">
      <div class="login-header">
        <div class="icon-wrapper">
          <el-icon :size="48" color="#409EFF" class="floating-icon"><Box /></el-icon>
        </div>
        <h1 class="fade-in-up" style="animation-delay: 0.2s">模型管理系统</h1>
        <p class="fade-in-up" style="animation-delay: 0.3s">欢迎登录</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form fade-in-up"
        style="animation-delay: 0.4s"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="goToRegister">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Box } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/useAuthStore'

const router = useRouter()
const authStore = useAuthStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

// 生成粒子样式
const getParticleStyle = (index) => {
  const size = Math.random() * 4 + 2
  const left = Math.random() * 100
  const delay = Math.random() * 15
  const duration = Math.random() * 10 + 10
  const opacity = Math.random() * 0.5 + 0.2
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    opacity
  }
}

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await authStore.login(loginForm)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (error) {
      console.error('登录失败:', error)
      // 添加用户友好的错误提示
      ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}

function goToRegister() {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* 粒子背景动画 */
.particles {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  top: 0;
  left: 0;
}

.particle {
  position: absolute;
  bottom: -10px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  animation: float-up infinite linear;
}

@keyframes float-up {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) rotate(720deg);
    opacity: 0;
  }
}

/* 登录框样式 */
.login-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  animation: slide-in 0.6s ease-out;
  position: relative;
  z-index: 10;
}

@keyframes slide-in {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 图标包装器 */
.icon-wrapper {
  display: inline-block;
  animation: bounce-in 0.8s ease-out;
}

@keyframes bounce-in {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1);
  }
}

/* 浮动图标动画 */
.floating-icon {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* 淡入上移动画 */
.fade-in-up {
  animation: fadeInUp 0.6s ease-out both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 28px;
  color: #303133;
  margin: 16px 0 8px;
  font-weight: 600;
}

.login-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-top: 20px;
}

/* 输入框动画 */
.login-form :deep(.el-input) {
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper) {
  transition: all 0.3s ease;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
  transform: scale(1.02);
}

/* 按钮波纹效果 */
.login-form :deep(.el-button--primary) {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.login-form :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.login-form :deep(.el-button--primary:active) {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #606266;
  animation: fadeInUp 0.6s ease-out 0.5s both;
}

.login-footer .el-link {
  margin-left: 8px;
  transition: all 0.3s ease;
}

.login-footer .el-link:hover {
  transform: translateX(3px);
}

/* PC端优化 */
@media (min-width: 769px) {
  .login-box {
    width: 420px;
    padding: 40px;
  }

  .login-header h1 {
    font-size: 28px;
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .login-box {
    width: 90%;
    padding: 30px 20px;
    margin: 20px;
  }

  .login-header h1 {
    font-size: 24px;
  }

  .login-header p {
    font-size: 13px;
  }

  /* 移动端减少粒子数量以提升性能 */
  .particle:nth-child(n+11) {
    display: none;
  }
}

/* 小屏幕手机 */
@media (max-width: 480px) {
  .login-box {
    width: 95%;
    padding: 25px 15px;
  }

  .login-header h1 {
    font-size: 22px;
  }

  .icon-wrapper {
    transform: scale(0.9);
  }
}
</style>
