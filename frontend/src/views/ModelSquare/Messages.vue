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
  background: #f5f7fa;
  padding: 16px;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.conversation-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.conversation-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar {
  flex-shrink: 0;
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
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
}

.message-preview {
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.message-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unread-badge {
  flex-shrink: 0;
  margin-left: 8px;
}

.empty-state {
  padding: 40px 0;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .messages {
    padding: 12px;
  }
}
</style>