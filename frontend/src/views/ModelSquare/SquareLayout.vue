<template>
  <div class="square-layout">
    <!-- PC端顶部导航栏 -->
    <div class="top-nav">
      <div class="nav-content">
        <div class="nav-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="nav-right">
          <router-link to="/square/home" class="nav-link">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/square/orders" class="nav-link">
            <el-icon><List /></el-icon>
            <span>订单</span>
          </router-link>
          <router-link to="/square/messages" class="nav-link">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-icon><ChatDotRound /></el-icon>
            </el-badge>
            <span>消息</span>
          </router-link>
          <router-link to="/square/profile" class="nav-link">
            <el-icon><UserFilled /></el-icon>
            <span>我</span>
          </router-link>
          <!-- PC端用户信息下拉菜单 -->
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-info-pc">
              <el-avatar :size="32" :src="userAvatar" class="user-avatar">
                {{ nickname.charAt(nickname.length - 1) }}
              </el-avatar>
              <span class="user-name">{{ nickname }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="admin">
                  <el-icon><Setting /></el-icon>
                  管理后台
                </el-dropdown-item>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 移动端顶部导航栏 -->
    <div class="mobile-header">
      <div class="mobile-header-content">
        <span class="mobile-page-title">{{ pageTitle }}</span>
        <!-- 移动端用户信息下拉菜单 -->
        <el-dropdown trigger="click" @command="handleUserCommand">
          <div class="user-info-mobile">
            <el-avatar :size="28" :src="userAvatar" class="user-avatar">
              {{ nickname.charAt(nickname.length - 1) }}
            </el-avatar>
            <span class="user-name">{{ nickname }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="admin">
                <el-icon><Setting /></el-icon>
                管理后台
              </el-dropdown-item>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人信息
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <router-view />
    </div>

    <!-- 底部导航栏（仅移动端显示） -->
    <div class="bottom-nav">
      <router-link to="/square/home" class="nav-item" active-class="active">
        <span class="icon-wrapper"><el-icon><HomeFilled /></el-icon></span>
        <span>首页</span>
      </router-link>
      <router-link to="/square/orders" class="nav-item" active-class="active">
        <span class="icon-wrapper"><el-icon><List /></el-icon></span>
        <span>订单</span>
      </router-link>
      <router-link to="/square/messages" class="nav-item" active-class="active">
        <span class="icon-wrapper">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <el-icon><ChatDotRound /></el-icon>
          </el-badge>
        </span>
        <span>消息</span>
      </router-link>
      <router-link to="/square/profile" class="nav-item" active-class="active">
        <span class="icon-wrapper"><el-icon><UserFilled /></el-icon></span>
        <span>我</span>
      </router-link>
    </div>
    
    <!-- AI 助手入口（聊天页面不显示） -->
    <div v-if="!isChatPage" class="ai-assistant-entry" @click="showAiAssistant = true">
      <el-icon :size="28"><Service /></el-icon>
    </div>
    
    <!-- AI 助手全屏覆盖层 -->
    <Teleport to="body">
      <div v-if="showAiAssistant" class="ai-fullscreen-overlay" @click.self="showAiAssistant = false">
        <div class="ai-fullscreen-container">
          <AiChatAssistant @close="showAiAssistant = false" />
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled,
  ChatDotRound,
  List,
  ArrowDown,
  User,
  UserFilled,
  Setting,
  SwitchButton,
  Service
} from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/useAuthStore'
import { useMessageStore } from '../../stores/useMessageStore'
import { useSquareStore } from '../../stores/useSquareStore'
import AiChatAssistant from '../../components/AiChatAssistant.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const messageStore = useMessageStore()
const squareStore = useSquareStore()

const pageTitle = computed(() => {
  return route.meta.title || '模型广场'
})

const showAiAssistant = ref(false)
const isMobile = ref(false)

const userInfo = computed(() => authStore.userInfo || {})
const userAvatar = computed(() => userInfo.value.avatar || '')
const userName = computed(() => userInfo.value.username || userInfo.value.realName || '用户')
const nickname = computed(() => userInfo.value.nickname || userInfo.value.realName || '用户')

const unreadCount = computed(() => messageStore.unreadCount)

// 判断当前是否在聊天页面
const isChatPage = computed(() => route.name === 'Chat')

let unreadTimer = null

onMounted(() => {
  messageStore.fetchUnreadCount()
  unreadTimer = setInterval(() => messageStore.fetchUnreadCount(), 30000)
  
  // 检测是否为移动端
  isMobile.value = window.innerWidth <= 768
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth <= 768
  })
})

onUnmounted(() => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
})

const goToAdmin = () => {
  router.push('/dashboard')
}

const goToProfile = () => {
  router.push('/profile')
}

// 处理用户下拉菜单命令
const handleUserCommand = (command) => {
  if (command === 'admin') {
    goToAdmin()
  } else if (command === 'profile') {
    goToProfile()
  } else if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}

watch(() => route.path, (newPath, oldPath) => {
  const isLeavingHome = oldPath?.includes('/square/home') && !newPath.includes('/square/home')
  if (isLeavingHome && !newPath.includes('/square/detail/')) {
    squareStore.resetListState()
  }
})
</script>

<style scoped>
.square-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.square-layout::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(255, 119, 198, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 40% 50%, rgba(66, 129, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 0 5px rgba(64, 158, 255, 0.5), 0 0 10px rgba(64, 158, 255, 0.3); }
  50% { box-shadow: 0 0 20px rgba(64, 158, 255, 0.8), 0 0 40px rgba(64, 158, 255, 0.4); }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@keyframes neon-flicker {
  0%, 100% { opacity: 1; }
  92% { opacity: 1; }
  93% { opacity: 0.8; }
  94% { opacity: 1; }
  96% { opacity: 0.9; }
  97% { opacity: 1; }
}

@keyframes ripple {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

.top-nav {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  display: none;
  flex-shrink: 0;
}

.nav-content {
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 14px;
  color: #606266;
}

.back-btn:hover {
  color: #409eff;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
  animation: neon-flicker 3s infinite;
}

.nav-right {
  display: flex;
  gap: 24px;
  align-items: center;
}

.user-info-pc {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(64, 158, 255, 0.05));
  border: 1px solid rgba(64, 158, 255, 0.3);
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.user-info-pc::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.user-info-pc:hover::before {
  left: 100%;
}

.user-info-pc:hover {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.4), rgba(64, 158, 255, 0.2));
  border-color: rgba(64, 158, 255, 0.6);
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4), 0 0 15px rgba(64, 158, 255, 0.2);
}

.user-info-mobile {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3), 0 0 10px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4), 0 0 20px rgba(64, 158, 255, 0.5);
}

.user-name {
  font-size: 14px;
  color: #fff;
  font-weight: 500;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  margin-left: 5px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  padding: 10px 16px;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #409eff, #67c23a, #409eff);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.nav-link:hover::after,
.nav-link.router-link-active::after {
  width: 80%;
}

.nav-link:hover {
  color: #fff;
  background: rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.nav-link.router-link-active {
  color: #fff;
  background: rgba(64, 158, 255, 0.2);
}

.nav-link .el-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.nav-link:hover .el-icon {
  transform: scale(1.2);
  filter: drop-shadow(0 0 8px rgba(64, 158, 255, 0.8));
}

.mobile-header {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  display: none;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 999;
}

.mobile-header-content {
  height: 50px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.mobile-back-btn {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.mobile-back-btn:hover {
  color: #409EFF;
}

.mobile-page-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  flex: 1;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

.main-content {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: rgba(20, 20, 40, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 1000;
  box-shadow: 0 -8px 30px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  padding: 0 8px;
  padding-bottom: calc(env(safe-area-inset-bottom) + 4px);
}

.bottom-nav::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.5), transparent);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: 10px;
  transition: all 0.3s ease;
  flex: 0 0 25%;
  height: 100%;
  max-width: 70px;
  position: relative;
}

.nav-item .icon-wrapper {
  position: relative;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-item .el-icon {
  font-size: 24px;
  width: 24px;
  height: 24px;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.nav-item.active {
  color: #409eff;
}

.nav-item.active .el-icon {
  transform: translateY(-2px) scale(1.1);
  filter: drop-shadow(0 4px 8px rgba(64, 158, 255, 0.5)) drop-shadow(0 0 15px rgba(64, 158, 255, 0.4));
}

.nav-item:hover {
  color: #fff;
}

.nav-item:hover .el-icon {
  transform: scale(1.1);
}

.nav-item:active {
  transform: scale(0.95);
}

.nav-item .el-badge {
  position: absolute !important;
  top: 3px !important;
  right: 1px !important;
  transform: none !important;
  font-size: 9px !important;
  min-width: 14px !important;
  height: 14px !important;
  line-height: 14px !important;
  padding: 0 3px !important;
}

.nav-item .el-badge :deep(.el-badge__content) {
    font-size: 9px;
    min-width: 14px;
    height: 14px;
    line-height: 14px;
    padding: 0 3px;
    background: linear-gradient(135deg, #f56c6c, #ff9a9e);
    box-shadow: 0 2px 8px rgba(245, 108, 108, 0.5);
  }

/* AI 助手入口按钮 */
.ai-assistant-entry {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  z-index: 999;
  animation: float 3s ease-in-out infinite;
}

.ai-assistant-entry:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 30px rgba(102, 126, 234, 0.6);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* AI 助手全屏覆盖层样式 */
.ai-fullscreen-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.ai-fullscreen-container {
  width: 95%;
  max-width: 1100px;
  height: 85vh;
  animation: scaleIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@media (max-width: 768px) {
  .ai-fullscreen-container {
    width: 100%;
    height: 100vh;
    max-width: none;
  }
}

@media (min-width: 769px) {
  .top-nav {
    display: block;
    animation: slideDown 0.5s ease-out;
  }

  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .mobile-header {
    display: none;
  }

  .bottom-nav {
    display: none;
  }

  .main-content {
    padding-bottom: 0;
  }
}

@media (max-width: 768px) {
  .mobile-header {
    display: block;
    animation: slideDown 0.5s ease-out;
  }

  .main-content {
    padding-bottom: 65px;
  }

  .nav-item.active::after {
    content: '';
    position: absolute;
    top: -1px;
    left: 50%;
    transform: translateX(-50%);
    width: 30px;
    height: 3px;
    background: linear-gradient(90deg, #409eff, #67c23a);
    border-radius: 0 0 3px 3px;
    box-shadow: 0 0 10px rgba(64, 158, 255, 0.6);
  }
}
</style>
