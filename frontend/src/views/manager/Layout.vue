<template>
  <el-container class="layout-container">
    <!-- PC端侧边栏 -->
    <el-aside width="220px" class="sidebar desktop-sidebar">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="28"><Box /></el-icon>
        </div>
        <h2>Model Share</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#fff"
        :unique-opened="true"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/models">
          <el-icon><Box /></el-icon>
          <span>模型管理</span>
        </el-menu-item>
        <el-menu-item index="/tools">
          <el-icon><Tools /></el-icon>
          <span>工具管理</span>
        </el-menu-item>
        <el-menu-item index="/import">
          <el-icon><Upload /></el-icon>
          <span>Excel 导入</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <div class="menu-toggle">
            <el-icon :size="20"><Fold /></el-icon>
          </div>
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <div class="header-right">
          <!-- 动态展示当前时间 -->
          <div class="current-time">
            <el-icon><Clock /></el-icon>
            <span>{{ currentTime }}</span>
          </div>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userInfo?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'">
              </el-avatar>
              <span class="username">{{ userInfo?.nickname || userInfo?.username || '用户' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="square">
                  <el-icon><shop /></el-icon>
                  模型广场
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
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
      
      <!-- 移动端底部导航 -->
      <div class="mobile-tabbar">
        <div 
          v-for="item in navItems" 
          :key="item.path"
          class="tab-item"
          :class="{ active: route.path === item.path }"
          @click="handleTabClick(item.path)"
        >
          <el-icon :size="20">
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Box,
  DataAnalysis,
  Tools,
  Upload,
  Fold,
  Clock,
  ArrowDown,
  User,
  SwitchButton,
  View,
  Shop
} from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/useAuthStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)

// 导航菜单配置
const navItems = [
  { path: '/dashboard', icon: 'DataAnalysis', label: '统计' },
  { path: '/models', icon: 'Box', label: '模型' },
  { path: '/tools', icon: 'Tools', label: '工具' },
  { path: '/import', icon: 'Upload', label: '导入' }
]

const currentTitle = computed(() => {
  return route.meta.title || '数据统计'
})

// 用户信息
const userInfo = computed(() => authStore.userInfo)

// 当前时间
const currentTime = ref('')
let timeInterval = null

// 格式化时间
function formatTime() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  
  // 星期几
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekDay = weekDays[now.getDay()]
  
  currentTime.value = `${year}-${month}-${day} ${hours}:${minutes}:${seconds} ${weekDay}`
}

// 启动时间更新
onMounted(() => {
  formatTime()
  timeInterval = setInterval(formatTime, 1000)
})

// 清理定时器
onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})

// 处理下拉菜单命令
async function handleCommand(command) {
  if (command === 'square') {
    // 跳转到广场页面
    router.push('/square')
  } else if (command === 'profile') {
    // 跳转到个人信息页面
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      authStore.logout()
      ElMessage.success('已退出登录')
    } catch {
      // 用户取消
    }
  }
}

// 处理移动端底部导航点击
function handleTabClick(path) {
  if (path && path !== route.path) {
    router.push(path)
  }
}

</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
  width: 100vw;
}

/* 侧边栏样式 - 炫酷效果 */
.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: #fff;
  box-shadow: 
    2px 0 20px rgba(0, 0, 0, 0.3),
    inset 0 0 60px rgba(64, 158, 255, 0.05);
  transition: width 0.3s ease;
  position: relative;
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(64, 158, 255, 0.03) 50%,
    transparent 70%
  );
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: linear-gradient(135deg, #409EFF, #79bbff, #67c23a);
  background-size: 200% 200%;
  animation: gradientShift 4s ease infinite;
  padding: 0 20px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.4);
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.logo::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  animation: glowPulse 2s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  animation: float 3s ease-in-out infinite;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
}

.logo h2 {
  color: #fff;
  font-size: 18px;
  margin: 0;
  font-weight: bold;
  text-shadow: 
    0 0 10px rgba(255, 255, 255, 0.8),
    0 2px 4px rgba(0, 0, 0, 0.2);
}

/* 菜单样式优化 - 炫酷效果 */
:deep(.el-menu) {
  border-right: none;
  background: transparent !important;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 6px 12px;
  border-radius: 12px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

:deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s ease;
}

:deep(.el-menu-item:hover::before) {
  left: 100%;
}

:deep(.el-menu-item:hover) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.2), rgba(103, 194, 58, 0.1)) !important;
  transform: translateX(8px) scale(1.02);
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.3);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #409EFF, #79bbff) !important;
  color: #fff !important;
  box-shadow: 
    0 4px 20px rgba(64, 158, 255, 0.4),
    0 0 30px rgba(64, 158, 255, 0.2);
  animation: activeGlow 2s ease-in-out infinite;
}

@keyframes activeGlow {
  0%, 100% {
    box-shadow: 
      0 4px 20px rgba(64, 158, 255, 0.4),
      0 0 30px rgba(64, 158, 255, 0.2);
  }
  50% {
    box-shadow: 
      0 4px 25px rgba(64, 158, 255, 0.5),
      0 0 40px rgba(64, 158, 255, 0.3);
  }
}

:deep(.el-menu-item .el-icon) {
  margin-right: 12px;
  font-size: 18px;
}

/* 头部样式 - 炫酷效果 */
.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 70px;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 0 40px rgba(64, 158, 255, 0.05);
  transition: all 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.menu-toggle {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  color: #606266;
}

.menu-toggle:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.breadcrumb {
  font-size: 14px;
}

:deep(.el-breadcrumb__inner) {
  color: #606266;
  font-weight: normal;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #303133;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.current-time {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(103, 194, 58, 0.05));
  border-radius: 12px;
  color: #409EFF;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(64, 158, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.current-time::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.1), transparent);
  transition: left 0.6s ease;
}

.current-time:hover::before {
  left: 100%;
}

.current-time:hover {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(103, 194, 58, 0.1));
  transform: scale(1.05);
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.3);
}

.current-time .el-icon {
  color: #409EFF;
  font-size: 18px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.username {
  font-size: 14px;
}

/* 主内容区域 - 炫酷效果 */
.main-content {
  background: linear-gradient(135deg, rgba(245, 247, 250, 0.8) 0%, rgba(232, 236, 241, 0.8) 100%);
  padding: 0;
  min-height: calc(100vh - 70px);
  overflow-x: hidden;
  position: relative;
}

/* 移动端底部导航 */
.mobile-tabbar {
  display: none;
}

/* 响应式适配 */
@media (max-width: 768px) {
  /* 隐藏PC端侧边栏 */
  .desktop-sidebar {
    display: none !important;
  }
  
  .header {
    padding: 0 12px;
    height: 50px;
  }
  
  .breadcrumb {
    display: none;
  }
  
  .menu-toggle {
    display: none;
  }
  
  .current-time {
    font-size: 11px;
    padding: 4px 8px;
  }
  
  .current-time .el-icon {
    display: none;
  }
  
  .main-content {
    min-height: calc(100vh - 50px - 50px);
    padding-bottom: 50px; /* 为底部导航留空间 */
  }
  
  /* 移动端底部导航样式 */
  .mobile-tabbar {
    display: flex;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 50px;
    background: #fff;
    border-top: 1px solid #e4e7ed;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
    z-index: 1000;
    display: flex;
    justify-content: space-around;
    align-items: center;
  }
  
  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2px;
    font-size: 12px;
    color: #909399;
    cursor: pointer;
    transition: all 0.3s ease;
    padding: 6px 0;
  }
  
  .tab-item:hover {
    color: #409EFF;
  }
  
  .tab-item.active {
    color: #409EFF;
    font-weight: 500;
  }
  
  .tab-item .el-icon {
    font-size: 20px;
  }
}

/* 小屏幕手机适配 */
@media (max-width: 480px) {
  .header {
    padding: 0 8px;
  }
  
  .current-time {
    font-size: 11px;
    padding: 4px 8px;
  }
}
</style>
