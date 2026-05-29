<template>
  <div class="messages">
    <div class="conversation-list" v-loading="loading">
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无消息" />
      </div>

      <div v-for="item in list" :key="item.id" class="conversation-item" @click="goToChat(item)">
        <div class="avatar">
          <el-avatar :src="getOtherAvatar(item) || defaultAvatar" :size="50">
            {{ getOtherName(item)?.charAt(0) || '用户' }}
          </el-avatar>
        </div>
        <div class="content">
          <div class="header">
            <span class="nickname">{{ getOtherName(item) || '未知用户' }}</span>
            <span class="time">{{ formatDate(item.createdAt) }}</span>
          </div>
          <div class="message-row">
            <div class="message-preview">{{ getMessagePreview(item) }}</div>
            <el-badge v-if="item.unreadCount && item.unreadCount > 0" :value="item.unreadCount" class="unread-badge" />
          </div>
        </div>
      </div>

      <div v-if="hasMore" class="load-more">
        <el-button @click="loadMore" :loading="loading">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRecentConversations } from '../../api/square'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Ccircle cx="50" cy="50" r="45" fill="%23e8e8e8"/%3E%3Ccircle cx="50" cy="40" r="15" fill="%23c4c4c4"/%3E%3Ccircle cx="50" cy="55" r="18" fill="%23dcdcdc"/%3E%3C/svg%3E'

// 获取当前用户ID
function getCurrentUserId() {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return null
  try {
    const userInfo = JSON.parse(userInfoStr)
    return userInfo?.id
  } catch {
    return null
  }
}

// 获取聊天对象的ID
function getOtherUserId(item) {
  const currentUserId = getCurrentUserId()
  if (Number(item.senderId) === currentUserId) {
    return item.receiverId
  }
  return item.senderId
}

// 获取聊天对象的名字
function getOtherName(item) {
  const currentUserId = getCurrentUserId()
  if (Number(item.senderId) === currentUserId) {
    return item.receiverNickname || `用户${item.receiverId}`
  }
  return item.senderNickname || `用户${item.senderId}`
}

// 获取聊天对象的头像
function getOtherAvatar(item) {
  const currentUserId = getCurrentUserId()
  if (Number(item.senderId) === currentUserId) {
    return item.receiverAvatar || ''
  }
  return item.senderAvatar || ''
}

// 获取消息预览（处理撤回消息）
function getMessagePreview(item) {
  // 判断是否已撤回（兼容布尔值和数字类型）
  const isRecalled = item.isRecalled === true || item.isRecalled === 1
  if (isRecalled) {
    const currentUserId = getCurrentUserId()
    // 判断是自己撤回还是对方撤回
    if (Number(item.senderId) === currentUserId) {
      return '你已撤回一条消息'
    } else {
      return '对方已撤回一条消息'
    }
  }
  return item.content || ''
}

const loadData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    list.value = []
    hasMore.value = true
  }

  if (!hasMore.value || loading.value) return

  loading.value = true
  try {
    const res = await getRecentConversations({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })

    const data = res.data || res
    const newData = data.records || []
    const total = data.total || 0

    if (reset) {
      list.value = newData
    } else {
      list.value.push(...newData)
    }

    hasMore.value = list.value.length < total
    pageNum.value++
  } catch (error) {
    console.error('加载消息列表错误:', error)
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const goToChat = (item) => {
  const userId = getOtherUserId(item)
  router.push(`/square/chat/${userId}`)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString('zh-CN')
}

const loadMore = () => {
  loadData(false)
}

onMounted(() => {
  loadData(true)
})
</script>

<style scoped>
.messages {
  min-height: 100vh;
  background: linear-gradient(180deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.messages::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(120, 119, 198, 0.2) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(255, 119, 198, 0.15) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.messages > * {
  position: relative;
  z-index: 1;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes pulseGlow {
  0%, 100% {
    box-shadow: 0 0 5px rgba(64, 158, 255, 0.3), 0 0 10px rgba(64, 158, 255, 0.2);
  }
  50% {
    box-shadow: 0 0 15px rgba(64, 158, 255, 0.5), 0 0 30px rgba(64, 158, 255, 0.3);
  }
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.conversation-item {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 18px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
  animation: slideIn 0.4s ease-out;
  animation-fill-mode: backwards;
}

.conversation-item:nth-child(1) { animation-delay: 0.05s; }
.conversation-item:nth-child(2) { animation-delay: 0.1s; }
.conversation-item:nth-child(3) { animation-delay: 0.15s; }
.conversation-item:nth-child(4) { animation-delay: 0.2s; }
.conversation-item:nth-child(5) { animation-delay: 0.25s; }

.conversation-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, transparent 50%, rgba(255, 119, 198, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.conversation-item:hover::before {
  opacity: 1;
}

.conversation-item:hover {
  transform: translateX(8px) scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3), 0 0 20px rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.3);
}

.avatar {
  flex-shrink: 0;
}

.avatar :deep(.el-avatar) {
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(64, 158, 255, 0.4);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3), 0 0 15px rgba(64, 158, 255, 0.2);
  transition: all 0.3s ease;
  font-weight: 600;
  color: #fff;
}

.conversation-item:hover .avatar :deep(.el-avatar) {
  transform: scale(1.1) rotate(5deg);
  border-color: rgba(64, 158, 255, 0.8);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4), 0 0 25px rgba(64, 158, 255, 0.4);
}

.content {
  flex: 1;
  min-width: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.nickname {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  transition: color 0.3s ease;
}

.conversation-item:hover .nickname {
  color: #409eff;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

.time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.message-preview {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
  transition: color 0.3s ease;
}

.conversation-item:hover .message-preview {
  color: rgba(255, 255, 255, 0.9);
}

.message-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unread-badge {
  flex-shrink: 0;
  margin-left: 10px;
}

.unread-badge :deep(.el-badge__content) {
  background: linear-gradient(135deg, #f56c6c, #ff9a9e);
  border: none;
  box-shadow: 0 2px 10px rgba(245, 108, 108, 0.4);
  animation: pulseGlow 2s infinite;
}

.empty-state {
  padding: 60px 0;
}

.empty-state :deep(.el-empty__description) {
  color: rgba(255, 255, 255, 0.6);
}

.empty-state :deep(.el-empty__image) {
  filter: drop-shadow(0 0 20px rgba(64, 158, 255, 0.3));
}

.load-more {
  text-align: center;
  padding: 24px 0;
}

.load-more .el-button {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.7);
  border-radius: 20px;
  padding: 12px 30px;
  transition: all 0.3s ease;
}

.load-more .el-button:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.5);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

@media (max-width: 768px) {
  .messages {
    padding: 14px;
  }

  .conversation-item {
    padding: 14px;
    border-radius: 16px;
  }

  .nickname {
    font-size: 15px;
  }

  .message-preview {
    font-size: 13px;
  }
}
</style>