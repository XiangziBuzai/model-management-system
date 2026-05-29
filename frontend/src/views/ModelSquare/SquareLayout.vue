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
            <el-icon><ShoppingCart /></el-icon>
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
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </router-link>
      <router-link to="/square/orders" class="nav-item" active-class="active">
        <el-icon><ShoppingCart /></el-icon>
        <span>订单</span>
      </router-link>
      <router-link to="/square/messages" class="nav-item" active-class="active">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <el-icon><ChatDotRound /></el-icon>
        </el-badge>
        <span>消息</span>
      </router-link>
      <router-link to="/square/profile" class="nav-item" active-class="active">
        <el-icon><UserFilled /></el-icon>
        <span>我</span>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  HomeFilled,
  ChatDotRound,
  ShoppingCart,
  ArrowDown,
  User,
  UserFilled,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/useAuthStore'
import { useMessageStore } from '../../stores/useMessageStore'
import { useSquareStore } from '../../stores/useSquareStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const messageStore = useMessageStore()
const squareStore = useSquareStore()

const pageTitle = computed(() => {
  return route.meta.title || '模型广场'
})

const userInfo = computed(() => authStore.userInfo || {})
const userAvatar = computed(() => userInfo.value.avatar || '')
const userName = computed(() => userInfo.value.username || userInfo.value.realName || '用户')
const nickname = computed(() => userInfo.value.nickname || userInfo.value.realName || '用户')

const unreadCount = computed(() => messageStore.unreadCount)

let unreadTimer = null

onMounted(() => {
  messageStore.fetchUnreadCount()
  unreadTimer = setInterval(() => messageStore.fetchUnreadCount(), 30000)
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
  background: #f5f7fa;
  height: 100vh;
  overflow: hidden;
}

/* PC端顶部导航栏 */
.top-nav {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: none; /* 移动端隐藏 */
  flex-shrink: 0;
}

.nav-content {
  max-width: 1200px;
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
  color: #303133;
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
  padding: 6px 12px;
  background: #f5f7fa;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.user-info-pc:hover {
  background: #ecf5ff;
}

.user-info-mobile {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.user-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
  padding: 8px 12px;
  border-radius: 6px;
}

.nav-link:hover {
  color: #409eff;
  background: #ecf5ff;
}

.nav-link.router-link-active {
  color: #409eff;
  background: #ecf5ff;
}

/* 移动端顶部导航栏 */
.mobile-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: none; /* PC端隐藏 */
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
  color: #303133;
  flex: 1;
}

.main-content {
  flex: 1;
  overflow: hidden;
}

/* 底部导航栏 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-around;
  align-items: center;
  z-index: 1000;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  text-decoration: none;
  font-size: 12px;
  transition: all 0.3s ease;
  flex: 1;
  height: 100%;
}

.nav-item .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.nav-item.active {
  color: #409eff;
}

.nav-item:hover {
  color: #409eff;
}

/* PC端优化 */
@media (min-width: 769px) {
  .top-nav {
    display: block; /* PC端显示顶部导航 */
  }

  .mobile-header {
    display: none; /* PC端隐藏移动端头部 */
  }

  .bottom-nav {
    display: none; /* PC端隐藏底部导航 */
  }

  .main-content {
    padding-bottom: 0;
  }
}

/* 移动端优化 */
@media (max-width: 768px) {
  .mobile-header {
    display: block; /* 移动端显示顶部导航 */
  }

  .main-content {
    padding-bottom: 60px; /* 确保移动端有底部导航空间 */
  }
}
</style>
