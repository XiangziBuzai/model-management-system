<template>
  <div class="login-container">
    <!-- 动态流体渐变背景 -->
    <div class="gradient-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <!-- 粒子效果层 -->
    <div class="particles">
      <div v-for="i in 30" :key="i" class="particle" :style="getParticleStyle(i)"></div>
    </div>

    <!-- PC端左右分栏布局 -->
    <div class="login-wrapper">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="brand-content">
            <div class="brand-icon-wrapper">
              <el-icon :size="80" color="#fff" class="brand-icon"><Box /></el-icon>
            </div>
            <h1 class="brand-title">ModelSphere</h1>
            <p class="brand-slogan">AI驱动 · 智能协作 · 无限可能</p>
          <div class="brand-features">
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>多模型统一管理</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>可视化数据分析</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>安全权限控制</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="form-section">
        <div class="login-box">
          <div class="login-header">
            <h2 class="fade-in-up" style="animation-delay: 0.2s">欢迎登录</h2>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form fade-in-up"
            style="animation-delay: 0.4s"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  prefix-icon="User"
                  size="large"
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrapper">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  prefix-icon="Lock"
                  size="large"
                  show-password
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-btn glow-effect"
                :loading="loading"
                @click="handleLogin"
              >
                <span class="btn-text">登 录</span>
              </el-button>
            </el-form-item>

            <el-form-item>
              <div class="entry-selection">
                <el-radio-group v-model="entryType" size="small">
                  <el-radio label="square">模型广场</el-radio>
                  <el-radio label="admin">个人管理后台</el-radio>
                </el-radio-group>
              </div>
            </el-form-item>

            <div class="login-footer">
              <span>还没有账号？</span>
              <el-link type="primary" class="register-link" @click="goToRegister">立即注册</el-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, inject } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Box, CircleCheck } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/useAuthStore'

const router = useRouter()
const authStore = useAuthStore()
const showWelcomeGuide = inject('showWelcomeGuide')

const loginFormRef = ref(null)
const loading = ref(false)
const entryType = ref('square')

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
      if (showWelcomeGuide) {
        setTimeout(() => showWelcomeGuide(), 100)
      }
      if (entryType.value === 'square') {
        router.push('/square/home')
      } else {
        router.push('/dashboard')
      }
    } catch (error) {
      console.error('登录失败:', error)
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
  position: relative;
  overflow: hidden;
  background: #0f0c29;
}

/* 动态流体渐变背景 */
.gradient-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float-orb 20s infinite ease-in-out;
}

.orb-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -150px;
  right: -150px;
  animation-delay: -7s;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -14s;
}

@keyframes float-orb {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(50px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-30px, 30px) scale(0.9);
  }
}

/* 粒子效果增强 */
.particles {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  top: 0;
  left: 0;
  z-index: 1;
}

.particle {
  position: absolute;
  bottom: -10px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: float-up infinite linear;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
}

@keyframes float-up {
  0% {
    transform: translateY(0) rotate(0deg) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 0.8;
  }
  100% {
    transform: translateY(-100vh) rotate(720deg) scale(0.5);
    opacity: 0;
  }
}

/* 左右分栏布局 */
.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1400px;
  min-height: 550px;
  position: relative;
  z-index: 10;
  margin: 100px 200px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: container-slide-in 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes container-slide-in {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 左侧品牌展示区 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.9) 0%, rgba(118, 75, 162, 0.9) 100%);
  backdrop-filter: blur(20px);
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: rotate-bg 30s linear infinite;
}

@keyframes rotate-bg {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

.brand-icon-wrapper {
  margin-bottom: 10px;
  animation: brand-icon-float 3s ease-in-out infinite;
}

@keyframes brand-icon-float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-15px) rotate(5deg);
  }
}

.brand-icon {
  filter: drop-shadow(0 10px 30px rgba(0, 0, 0, 0.3));
}

.brand-title {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 16px 0;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  animation: fade-in-up 0.8s ease-out 0.3s both;
}

.brand-slogan {
  font-size: 18px;
  opacity: 0.95;
  margin: 0 0 40px 0;
  letter-spacing: 2px;
  animation: fade-in-up 0.8s ease-out 0.4s both;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: fade-in-up 0.8s ease-out 0.5s both;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateX(10px);
}

.feature-item .el-icon {
  font-size: 20px;
}

/* 右侧表单区域 */
.form-section {
  flex: 1;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 登录框样式优化 */
.login-box {
  width: 100%;
  max-width: 420px;
}

.login-header {
  text-align: center;
}

.login-header h2 {
  font-size: 32px;
  color: #303133;
  margin: 0 0 12px 0;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-header p {
  font-size: 15px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-top: 20px;
}

/* 输入框包装器 - 流光边框效果 */
.input-wrapper {
  width: 100%;
  position: relative;
  border-radius: 12px;
  padding: 2px;
  background: transparent;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-wrapper::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2, #f093fb, #f5576c);
  z-index: -1;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.input-wrapper:focus-within::before {
  opacity: 1;
  animation: border-glow 2s linear infinite;
}

@keyframes border-glow {
  0%, 100% {
    filter: hue-rotate(0deg);
  }
  50% {
    filter: hue-rotate(30deg);
  }
}

.input-wrapper :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
}

.input-wrapper :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.input-wrapper :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.25);
  transform: translateY(-2px) scale(1.02);
}

.input-wrapper :deep(.el-input__inner) {
  font-size: 15px;
}

/* 按钮微交互效果 */
.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.login-btn:hover::before {
  width: 300px;
  height: 300px;
}

.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.login-btn:active {
  transform: translateY(-1px);
}

.glow-effect {
  animation: button-glow 2s ease-in-out infinite;
}

@keyframes button-glow {
  0%, 100% {
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 4px 25px rgba(102, 126, 234, 0.6), 0 0 40px rgba(102, 126, 234, 0.3);
  }
}

.btn-text {
  position: relative;
  z-index: 1;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #606266;
  animation: fade-in-up 0.8s ease-out 0.6s both;
}

.entry-selection {
  margin-left: 20px;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #606266;
}

.entry-selection span {
  white-space: nowrap;
}

.register-link {
  margin-left: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.register-link:hover {
  transform: translateX(3px);
  text-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

/* 淡入上移动画 */
.fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.4, 0, 0.2, 1) both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* PC端优化 */
@media (min-width: 1025px) {
  .login-wrapper {
    margin: 100px 200px;
  }

  .brand-section {
    padding: 80px 60px;
  }

  .brand-title {
    font-size: 48px;
  }

  .form-section {
    padding: 80px 60px;
  }
}

/* 平板端适配 */
@media (max-width: 1024px) {
  .login-wrapper {
    margin: 10px 100px;
    flex-direction: column;
    max-width: 600px;
    min-height: auto;
  }

  .brand-section {
    padding: 30px 30px;
    min-height: 180px;
  }

  .brand-title {
    font-size: 32px;
    margin-bottom: 10px;
  }

  .brand-slogan {
    font-size: 16px;
    margin-bottom: 10px;
  }

  .brand-features {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
  }

  .feature-item {
    flex: 1;
    min-width: 200px;
  }

  .form-section {
    padding: 20px;
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .login-container {
    padding: 0;
  }

  .login-wrapper {
    margin: 0 50px;
    border-radius: 20px;
  }

  .brand-section {
    padding: 20px;
    min-height: 180px;
  }

  .brand-icon-wrapper {
    margin-bottom: 0;
  }

  .brand-icon {
    font-size: 60px !important;
  }

  .brand-title {
    font-size: 28px;
  }

  .brand-slogan {
    font-size: 14px;
    margin-bottom: 0;
  }

  .brand-features {
    display: none;
  }

  .form-section {
    padding: 20px;
  }

  .login-header h2 {
    font-size: 26px;
  }

  .gradient-orb {
    filter: blur(60px);
    opacity: 0.5;
  }

  .input-wrapper :deep(.el-input__wrapper) {
    padding: 10px 14px;
    width: 350px !important;
  }
}

/* 小屏幕手机 */
@media (max-width: 480px) {
  .brand-section {
    padding: 24px 16px;
    min-height: 170px;
  }

  .brand-title {
    font-size: 24px;
  }

  .form-section {
    padding: 24px 16px;
  }

  .login-header h2 {
    font-size: 24px;
  }

  .login-header p {
    font-size: 13px;
  }

  .input-wrapper :deep(.el-input__wrapper) {
    padding: 10px 14px;
    width: 100%;
  }

  .login-btn {
    height: 46px;
    font-size: 15px;
  }
}
</style>
