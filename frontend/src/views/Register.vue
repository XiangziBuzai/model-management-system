<template>
  <div class="register-container">
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
    <div class="register-wrapper">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-icon-wrapper">
            <el-icon :size="80" color="#fff" class="brand-icon"><UserFilled /></el-icon>
          </div>
          <h1 class="brand-title">加入ModelSphere</h1>
          <p class="brand-slogan">开启AI智能之旅</p>
          <div class="brand-features">
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>免费账号注册</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>即刻开始使用</span>
            </div>
            <div class="feature-item">
              <el-icon><CircleCheck /></el-icon>
              <span>专属数据空间</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="form-section">
        <div class="register-box">
          <div class="register-header">
            <h2 class="fade-in-up" style="animation-delay: 0.2s">用户注册</h2>
          </div>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form fade-in-up"
            style="animation-delay: 0.4s"
            label-position="top"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名(3-20位)"
                  prefix-icon="User"
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrapper">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码(6-20位)"
                  prefix-icon="Lock"
                  show-password
                />
              </div>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <div class="input-wrapper">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  prefix-icon="Lock"
                  show-password
                />
              </div>
            </el-form-item>

            <el-form-item prop="nickname">
              <div class="input-wrapper">
                <el-input
                  v-model="registerForm.nickname"
                  placeholder="请输入昵称"
                  prefix-icon="Avatar"
                />
              </div>
            </el-form-item>

<!--
            <el-form-item label="邮箱" prop="email">
              <div class="input-wrapper">
                <el-input
                  v-model="registerForm.email"
                  placeholder="请输入邮箱(可选)"
                  prefix-icon="Message"
                />
              </div>
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <div class="input-wrapper">
                <el-input
                  v-model="registerForm.phone"
                  placeholder="请输入手机号(可选)"
                  prefix-icon="Phone"
                />
              </div>
            </el-form-item>
-->

            <el-form-item>
              <el-button
                type="success"
                size="large"
                class="register-btn glow-effect"
                :loading="loading"
                @click="handleRegister"
              >
                <span class="btn-text">注 册</span>
              </el-button>
            </el-form-item>

            <div class="register-footer">
              <span>已有账号？</span>
              <el-link type="primary" class="login-link" @click="goToLogin">立即登录</el-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, CircleCheck } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/useAuthStore'

const router = useRouter()
const authStore = useAuthStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: ''
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

// 验证密码是否一致
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20位之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在2-20位之间', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

async function handleRegister() {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await authStore.register(registerForm)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      console.error('注册失败:', error)
    } finally {
      loading.value = false
    }
  })
}

function goToLogin() {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
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
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  top: -200px;
  right: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  bottom: -150px;
  left: -150px;
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
.register-wrapper {
  display: flex;
  width: 100%;
  max-width: 1400px;
  min-height: 550px;
  position: relative;
  z-index: 10;
  margin:100px 200px;
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
  background: linear-gradient(135deg, rgba(240, 147, 251, 0.9) 0%, rgba(245, 87, 108, 0.9) 100%);
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

/* 注册框样式优化 */
.register-box {
  width: 100%;
  max-width: 500px;
}

.register-header {
  text-align: center;
}

.register-header h2 {
  font-size: 32px;
  color: #303133;
  margin: 0 0 12px 0;
  font-weight: 700;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-header p {
  font-size: 15px;
  color: #909399;
  margin: 0;
}

.register-form {
  margin-top: 20px;
  width: 100%;
}

.register-form :deep(.el-form-item) {
  width: 100%;
}

.register-form :deep(.el-form-item__content) {
  width: 100%;
  display: flex;
  flex: 1;
  min-width: 0;
}

.register-form :deep(.el-input) {
  width: 100%;
  flex: 1;
  min-width: 0;
}

.register-form :deep(.el-input__wrapper) {
  width: 100% !important;
  flex: 1;
  min-width: 0;
  box-sizing: border-box;
}

/* 输入框包装器 - 流光边框效果 */
.input-wrapper {
  position: relative;
  border-radius: 12px;
  padding: 2px;
  background: transparent;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
}

.input-wrapper::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f093fb, #f5576c, #667eea, #764ba2);
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
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.15);
  transform: translateY(-2px);
}

.input-wrapper :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 6px 20px rgba(240, 147, 251, 0.25);
  transform: translateY(-2px) scale(1.02);
}

.input-wrapper :deep(.el-input__inner) {
  font-size: 15px;
}

/* 按钮微交互效果 */
.register-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: 12px;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.4);
}

.register-btn::before {
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

.register-btn:hover::before {
  width: 300px;
  height: 300px;
}

.register-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(103, 194, 58, 0.5);
}

.register-btn:active {
  transform: translateY(-1px);
}

.glow-effect {
  animation: button-glow 2s ease-in-out infinite;
}

@keyframes button-glow {
  0%, 100% {
    box-shadow: 0 4px 15px rgba(103, 194, 58, 0.4);
  }
  50% {
    box-shadow: 0 4px 25px rgba(103, 194, 58, 0.6), 0 0 40px rgba(103, 194, 58, 0.3);
  }
}

.btn-text {
  position: relative;
  z-index: 1;
}

.register-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
  animation: fade-in-up 0.8s ease-out 0.6s both;
}

.login-link {
  margin-left: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.login-link:hover {
  transform: translateX(3px);
  text-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
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
  .register-wrapper {
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
  .register-wrapper {
    margin: 60px 20px;
    flex-direction: column;
    max-width: 600px;
  }

  .brand-section {
    padding: 0;
    min-height: 180px;
  }

  .brand-title {
    font-size: 32px;
  }

  .brand-slogan {
    font-size: 16px;
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
    padding: 40px 30px;
  }

  .register-form :deep(.el-input__wrapper) {
    width: 100%;
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .register-container {
    padding: 0;
  }

  .register-header {
    margin: 0;
  }

  .register-wrapper {
    border-radius: 20px;
    margin: 60px 12px;
  }

  .brand-section {
    padding: 0;
    min-height: 180px;
  }

  .brand-icon {
    font-size: 50px !important;
  }

  .brand-title {
    font-size: 26px;
  }

  .brand-slogan {
    font-size: 14px;
    margin-bottom: 20px;
  }

  .brand-features {
    display: none;
  }

  .form-section {
    padding: 2px 16px;
  }

  .register-header h2 {
    font-size: 24px;
  }

  /* .register-form :deep(.el-form-item) {
    margin-bottom: 16px;
  } */

  .register-form :deep(.el-form-item__label) {
    font-size: 14px;
    padding-bottom: 6px;
  }

  .register-form :deep(.el-input__wrapper) {
    padding: 10px 14px;
  }

  .gradient-orb {
    filter: blur(60px);
    opacity: 0.5;
  }

  .input-wrapper :deep(.el-input__wrapper) {
    padding: 10px 14px;
  }
}

/* 小屏幕手机 */
@media (max-width: 480px) {
  .brand-section {
    padding: 0;
    min-height: 170px;
  }

  .brand-title {
    font-size: 24px;
  }

  .form-section {
    padding: 2px 16px;
  }

  .register-header {
    margin-bottom: -16px;
  }

  .register-header h2 {
    font-size: 24px;
  }

  .register-header p {
    font-size: 13px;
  }

  .register-form :deep(.el-form) {
    label-width: 70px !important;
  }

  .input-wrapper :deep(.el-input__wrapper) {
    padding: 10px 14px;
  }

  .register-btn {
    height: 46px;
    font-size: 15px;
  }
}
</style>
