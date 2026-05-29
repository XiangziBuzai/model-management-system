<template>
  <el-dialog
    v-model="visible"
    title="欢迎使用Model Share"
    :width="dialogWidth"
    :close-on-click-modal="false"
    :show-close="true"
    class="welcome-guide-dialog"
  >
    <div class="welcome-content">
      <div class="welcome-header">
        <el-icon class="welcome-icon" :size="48"><Monitor /></el-icon>
        <h2>欢迎 {{ userInfo?.nickname || userInfo?.username || '用户' }}</h2>
        <p class="welcome-subtitle">让我们快速了解系统的核心功能</p>
      </div>

      <el-divider />

      <div class="feature-list">
        <div class="feature-section">
          <h3><el-icon><DataAnalysis /></el-icon> 管理后台功能</h3>
          <el-space direction="vertical" :size="12" class="feature-items">
            <el-card shadow="hover" class="feature-card">
              <div class="feature-item">
                <el-icon :size="20" color="#409EFF"><Box /></el-icon>
                <div class="feature-text">
                  <strong>模型管理</strong>
                  <p>管理和展示各类AI模型，支持模型的增删改查操作</p>
                </div>
              </div>
            </el-card>
            
            <el-card shadow="hover" class="feature-card">
              <div class="feature-item">
                <el-icon :size="20" color="#67C23A"><Tools /></el-icon>
                <div class="feature-text">
                  <strong>工具管理</strong>
                  <p>管理系统提供的各种工具和服务，便于用户使用</p>
                </div>
              </div>
            </el-card>
            
            <el-card shadow="hover" class="feature-card">
              <div class="feature-item">
                <el-icon :size="20" color="#E6A23C"><Upload /></el-icon>
                <div class="feature-text">
                  <strong>Excel 导入</strong>
                  <p>支持批量导入模型和工具数据，快速完成初始化</p>
                </div>
              </div>
            </el-card>
            
            <el-card shadow="hover" class="feature-card">
              <div class="feature-item">
                <el-icon :size="20" color="#F56C6C"><DataAnalysis /></el-icon>
                <div class="feature-text">
                  <strong>数据统计</strong>
                  <p>查看系统使用数据、订单统计和活跃度分析</p>
                </div>
              </div>
            </el-card>
          </el-space>
        </div>

        <el-divider content-position="center">
          <el-icon><ArrowDown /></el-icon>
        </el-divider>

        <div class="feature-section">
          <h3><el-icon><Shop /></el-icon> 模型广场功能</h3>
          <el-space direction="vertical" :size="12" class="feature-items">
            <el-card shadow="hover" class="feature-card">
              <div class="feature-item">
                <el-icon :size="20" color="#409EFF"><HomeFilled /></el-icon>
                <div class="feature-text">
                  <strong>浏览模型</strong>
                  <p>在模型广场浏览和搜索各种AI模型及工具</p>
                </div>
              </div>
            </el-card>
            
            <el-card shadow="hover" class="feature-card">
              <div class="feature-item">
                <el-icon :size="20" color="#67C23A"><ShoppingCart /></el-icon>
                <div class="feature-text">
                  <strong>订单管理</strong>
                  <p>查看和管理您的模型购买订单记录</p>
                </div>
              </div>
            </el-card>
            
            <el-card shadow="hover" class="feature-card">
              <div class="feature-item">
                <el-icon :size="20" color="#E6A23C"><ChatDotRound /></el-icon>
                <div class="feature-text">
                  <strong>消息通知</strong>
                  <p>接收系统通知、订单状态更新和价格提醒</p>
                </div>
              </div>
            </el-card>
            
            <el-card shadow="hover" class="feature-card">
              <div class="feature-item">
                <el-icon :size="20" color="#F56C6C"><User /></el-icon>
                <div class="feature-text">
                  <strong>个人信息</strong>
                  <p>管理个人资料、修改密码和收藏列表</p>
                </div>
              </div>
            </el-card>
          </el-space>
        </div>
      </div>

      <el-divider />

      <div class="welcome-tips">
        <el-alert
          title="使用提示"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <ul class="tips-list">
              <li>系统支持 PC 端和移动端自适应访问</li>
              <li>点击右上角用户名可快速切换管理后台和模型广场</li>
              <li>您可以随时在个人信息页面重新查看本指南</li>
            </ul>
          </template>
        </el-alert>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-checkbox v-model="notShowAgain" size="large">
          <span class="checkbox-label">不再提示</span>
        </el-checkbox>
        <div class="button-group">
          <el-button @click="showGuideLater">稍后再说</el-button>
          <el-button type="primary" @click="closeGuide">知道了，开始使用</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '../stores/useAuthStore'
import {
  Monitor,
  Box,
  Tools,
  Upload,
  DataAnalysis,
  HomeFilled,
  ShoppingCart,
  ChatDotRound,
  User,
  Shop,
  ArrowDown
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const visible = ref(false)
const notShowAgain = ref(false)
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)

const userInfo = computed(() => authStore.userInfo)

const dialogWidth = computed(() => {
  if (windowWidth.value <= 480) {
    return '92vw'
  } else if (windowWidth.value <= 768) {
    return '85vw'
  } else {
    return '600px'
  }
})

function handleResize() {
  windowWidth.value = window.innerWidth
}

const STORAGE_KEY = 'welcome_guide_dismissed'

function closeGuide() {
  if (notShowAgain.value) {
    localStorage.setItem(STORAGE_KEY, 'true')
  }
  visible.value = false
}

function showGuideLater() {
  visible.value = false
}

function showWelcomeGuide() {
  notShowAgain.value = false
  visible.value = true
}

defineExpose({
  showWelcomeGuide
})
</script>

<style scoped>
.welcome-content {
  max-height: 65vh;
  overflow-y: auto;
  padding: 0 8px;
}

.welcome-content::-webkit-scrollbar {
  width: 6px;
}

.welcome-content::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.welcome-content::-webkit-scrollbar-track {
  background-color: transparent;
}

.welcome-header {
  text-align: center;
  padding: 12px 0;
}

.welcome-icon {
  color: #409EFF;
  margin-bottom: 12px;
}

.welcome-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
}

.welcome-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.feature-list {
  padding: 0 8px;
}

.feature-section {
  margin-bottom: 16px;
}

.feature-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.feature-items {
  width: 100%;
}

.feature-card {
  border-radius: 8px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.feature-item .el-icon {
  margin-top: 2px;
  flex-shrink: 0;
}

.feature-text {
  flex: 1;
}

.feature-text strong {
  display: block;
  color: #303133;
  font-size: 14px;
  margin-bottom: 4px;
}

.feature-text p {
  margin: 0;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}

.welcome-tips {
  margin-top: 16px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.tips-list li {
  margin-bottom: 4px;
}

.tips-list li:last-child {
  margin-bottom: 0;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.checkbox-label {
  font-size: 14px;
  color: #606266;
}

.button-group {
  display: flex;
  gap: 12px;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409EFF, #79bbff);
  margin: 0;
  padding: 20px;
  border-radius: 8px 8px 0 0;
}

:deep(.el-dialog__title) {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

:deep(.el-dialog__body) {
  padding: 20px 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e4e7ed;
}

@media (max-width: 768px) {
  .welcome-content {
    max-height: 55vh;
    padding: 0 4px;
  }

  .welcome-header {
    padding: 8px 0;
  }

  .welcome-header h2 {
    font-size: 18px;
  }

  .welcome-subtitle {
    font-size: 13px;
  }

  .welcome-icon {
    margin-bottom: 8px;
  }

  .feature-list {
    padding: 0 4px;
  }

  .feature-section {
    margin-bottom: 12px;
  }

  .feature-section h3 {
    font-size: 15px;
    gap: 6px;
    margin-bottom: 10px;
  }

  .feature-items {
    gap: 8px;
  }

  .feature-card {
    padding: 12px !important;
  }

  .feature-item {
    gap: 10px;
  }

  .feature-item .el-icon {
    margin-top: 1px;
  }

  .feature-text strong {
    font-size: 13px;
    margin-bottom: 3px;
  }

  .feature-text p {
    font-size: 12px;
    line-height: 1.4;
  }

  .welcome-tips {
    margin-top: 12px;
  }

  .tips-list {
    font-size: 12px;
    padding-left: 16px;
    line-height: 1.6;
  }

  .dialog-footer {
    flex-direction: column;
    gap: 12px;
  }

  .button-group {
    width: 100%;
    display: flex;
    gap: 10px;
  }

  .button-group .el-button {
    flex: 1;
    padding: 10px 16px;
    font-size: 14px;
  }

  .checkbox-label {
    font-size: 13px;
  }

  :deep(.el-dialog__header) {
    padding: 14px 16px;
  }

  :deep(.el-dialog__title) {
    font-size: 16px;
  }

  :deep(.el-dialog__body) {
    padding: 16px;
  }

  :deep(.el-dialog__footer) {
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .welcome-content {
    max-height: 50vh;
  }

  .welcome-header h2 {
    font-size: 16px;
  }

  .welcome-subtitle {
    font-size: 12px;
  }

  .feature-section h3 {
    font-size: 14px;
  }

  .feature-card {
    padding: 10px !important;
  }

  .feature-text strong {
    font-size: 12px;
  }

  .feature-text p {
    font-size: 11px;
  }

  .tips-list {
    font-size: 11px;
  }

  .button-group .el-button {
    padding: 8px 12px;
    font-size: 13px;
  }

  :deep(.el-dialog__header) {
    padding: 12px;
  }

  :deep(.el-dialog__body) {
    padding: 12px;
  }

  :deep(.el-dialog__footer) {
    padding: 10px 12px;
  }
}
</style>
