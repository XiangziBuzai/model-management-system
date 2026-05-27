<template>
  <el-container class="layout-container">
    <!-- PC端侧边栏 -->
    <el-aside width="220px" class="sidebar desktop-sidebar">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="28"><Box /></el-icon>
        </div>
        <h2>模型管理系统</h2>
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
import { Box, DataAnalysis, Tools, Upload, Fold, Clock, ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/useAuthStore'

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
  if (command === 'profile') {
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
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg, #304156 0%, #2b3a4c 100%);
  color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  transition: width 0.3s ease;
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: linear-gradient(135deg, #409EFF, #79bbff);
  padding: 0 20px;
  position: relative;
  overflow: hidden;
}

.logo::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.logo h2 {
  color: #fff;
  font-size: 18px;
  margin: 0;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 菜单样式优化 */
:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(64, 158, 255, 0.15) !important;
  transform: translateX(4px);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #409EFF, #79bbff) !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

:deep(.el-menu-item .el-icon) {
  margin-right: 12px;
  font-size: 18px;
}

/* 头部样式 */
.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 70px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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
  padding: 8px 16px;
  background: linear-gradient(135deg, #f5f7fa, #e8ecf1);
  border-radius: 8px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.current-time:hover {
  background: linear-gradient(135deg, #e8ecf1, #dce1e6);
  color: #409EFF;
}

.current-time .el-icon {
  color: #409EFF;
  font-size: 16px;
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

/* 主内容区域 */
.main-content {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 0;
  min-height: calc(100vh - 70px);
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
  
  .username {
    display: none;
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
