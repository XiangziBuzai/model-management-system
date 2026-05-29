<template>
  <div class="chat">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
      <div class="chat-title">
        <div class="avatar-wrapper">
          <img :src="otherUserAvatar || defaultAvatar" alt="头像" class="chat-avatar" />
        </div>
        <span>{{ otherUserName || '用户' }}</span>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-list" ref="messageListRef">
      <!-- 顶部加载提示 -->
      <div v-if="loadingMore" class="loading-more">
        <el-spinner size="small" />
        <span>加载更多...</span>
      </div>
      
      <div v-for="(msg, index) in messages" :key="msg.id">
        <!-- 时间分隔线：在每段对话的第一条消息上方显示 -->
        <div v-if="shouldShowTimeSeparator(index)" class="time-separator">
          <span>{{ formatTimeSeparator(msg.createdAt) }}</span>
        </div>
        
        <div :class="['message-item', isMyMessage(msg) ? 'my-message' : 'other-message']">
          <!-- 对方消息：头像在左 -->
          <template v-if="!isMyMessage(msg)">
            <div class="avatar-wrapper">
              <img :src="getAvatar(msg)" alt="头像" class="avatar" />
            </div>
            <div class="message-content-wrapper">
              <div v-if="!isSameSender(index, msg.senderId)" class="sender-name">{{ getSenderName(msg) }}</div>
              <div class="message-bubble">
                <!-- 对方撤回消息显示 -->
                <template v-if="msg.isRecalled === true || msg.isRecalled === 1">
                  <div class="message-text">对方已撤回一条消息</div>
                </template>
                <!-- 普通消息 -->
                <template v-else>
                  <div class="message-text">{{ msg.content }}</div>
                </template>
              </div>
            </div>
          </template>
          
          <!-- 自己消息：头像在右 -->
          <template v-else>
            <div class="message-content-wrapper">
              <div v-if="!isSameSender(index, msg.senderId)" class="sender-name my-sender-name">{{ getSenderName(msg) }}</div>
              <div class="message-bubble" @contextmenu.prevent="showContextMenu($event, msg)">
                <!-- 撤回消息显示 -->
                <template v-if="msg.isRecalled === true || msg.isRecalled === 1">
                  <div class="message-text">你已撤回了一条消息</div>
                  <!-- 显示重新编辑按钮（仅在两分钟以内） -->
                  <div v-if="canEditRecalled(msg)" class="recall-edit-btn" @click.stop="editRecalledMessage(msg)">重新编辑</div>
                </template>
                <!-- 普通消息 -->
                <template v-else>
                  <div class="message-text">{{ msg.content }}</div>
                </template>
              </div>
              <!-- 右键菜单 -->
              <div v-if="contextMenu.visible && contextMenu.msgId === msg.id" 
                   class="context-menu" 
                   :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }">
                <div v-if="canRecall(msg)" class="menu-item" @click="recallMessage(msg)">撤回</div>
              </div>
            </div>
            <div class="avatar-wrapper">
              <img :src="getAvatar(msg)" alt="我的头像" class="avatar" />
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 输入框 -->
    <div class="input-area">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="2"
        placeholder="请输入消息..."
        @keyup.enter.ctrl="sendMessage"
      />
      <el-button type="primary" @click="sendMessage" :loading="sending">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getConversation, sendMessage as sendMsg, markConversationAsRead, recallMessage as recallMsgApi } from '../../api/square'
import { useMessageStore } from '../../stores/useMessageStore'

const route = useRoute()
const router = useRouter()
const messageStore = useMessageStore()
const otherUserId = Number(route.params.userId)

const userInfoStr = localStorage.getItem('userInfo')
const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null
const currentUserId = ref(userInfo?.id || null)
const currentUserAvatar = ref(userInfo?.avatar || '')

const otherUserName = ref('')
const otherUserAvatar = ref('')

const loading = ref(false)
const loadingMore = ref(false)
const sending = ref(false)
const messages = ref([])
const inputMessage = ref('')
const messageListRef = ref(null)
const pageNum = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Ccircle cx="50" cy="50" r="45" fill="%23e8e8e8"/%3E%3Ccircle cx="50" cy="40" r="15" fill="%23c4c4c4"/%3E%3Ccircle cx="50" cy="55" r="18" fill="%23dcdcdc"/%3E%3C/svg%3E'

// 右键菜单状态
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  msgId: null
})

function isMyMessage(msg) {
  if (!msg) return false
  if (!msg.senderId && msg.senderId !== 0) return false
  if (!currentUserId.value && currentUserId.value !== 0) return false
  return Number(msg.senderId) === Number(currentUserId.value)
}

function getAvatar(msg) {
  if (!msg) return defaultAvatar
  if (isMyMessage(msg)) {
    return currentUserAvatar.value || defaultAvatar
  }
  return msg.senderAvatar || defaultAvatar
}

function getSenderName(msg) {
  if (!msg) return '未知'
  if (isMyMessage(msg)) {
    return '我'
  }
  return msg.senderNickname || msg.senderUsername || `用户${msg.senderId}`
}

function isSameSender(index, senderId) {
  if (index === 0) return false
  const prevMsg = messages.value[index - 1]
  if (!prevMsg || !prevMsg.senderId || !senderId) return false
  return Number(prevMsg.senderId) === Number(senderId)
}

// 判断是否需要显示时间分隔线（当前消息与上一条消息间隔超过5分钟时显示）
function shouldShowTimeSeparator(index) {
  if (index === 0) return true
  
  const currentMsg = messages.value[index]
  const prevMsg = messages.value[index - 1]
  
  if (!currentMsg?.createdAt || !prevMsg?.createdAt) return true
  
  const currentTime = new Date(currentMsg.createdAt).getTime()
  const prevTime = new Date(prevMsg.createdAt).getTime()
  
  // 间隔超过5分钟（300000毫秒）则显示时间分隔线
  return (currentTime - prevTime) > 300000
}

// 判断消息是否可以撤回（两分钟以内且是自己发送的消息）
function canRecall(msg) {
  if (!msg || !isMyMessage(msg)) return false
  
  // 判断是否已撤回（兼容布尔值和数字类型）
  const isRecalled = msg.isRecalled === true || msg.isRecalled === 1
  if (isRecalled) return false
  
  const msgTime = new Date(msg.createdAt).getTime()
  const now = Date.now()
  // 两分钟以内（120000毫秒）可以撤回
  return (now - msgTime) <= 120000
}

// 判断撤回的消息是否可以编辑（两分钟以内）
function canEditRecalled(msg) {
  if (!msg) return false
  
  // 必须是自己撤回的消息
  if (!isMyMessage(msg)) return false
  
  // 必须是已撤回状态
  const isRecalled = msg.isRecalled === true || msg.isRecalled === 1
  if (!isRecalled) return false
  
  const msgTime = new Date(msg.createdAt).getTime()
  const now = Date.now()
  // 两分钟以内（120000毫秒）可以编辑
  return (now - msgTime) <= 120000
}

// 显示右键菜单
function showContextMenu(event, msg) {
  // 只有自己的消息且可以撤回时才显示菜单
  if (!isMyMessage(msg)) return
  
  // 如果消息超过两分钟不可撤回，则不显示右键菜单
  const msgTime = new Date(msg.createdAt).getTime()
  const now = Date.now()
  if ((now - msgTime) > 120000) return
  
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    msgId: msg.id
  }
  
  // 点击其他地方关闭菜单
  document.addEventListener('click', hideContextMenu)
}

// 隐藏右键菜单
function hideContextMenu() {
  contextMenu.value = {
    visible: false,
    x: 0,
    y: 0,
    msgId: null
  }
  document.removeEventListener('click', hideContextMenu)
}

// 撤回消息
const recallMessage = async (msg) => {
  hideContextMenu()
  
  if (!canRecall(msg)) {
    ElMessage.warning('消息超过两分钟，无法撤回')
    return
  }
  
  try {
    // 调用撤回接口
    await recallMsgApi(msg.id)
    
    // 更新本地消息状态
    const msgIndex = messages.value.findIndex(m => m.id === msg.id)
    if (msgIndex !== -1) {
      messages.value[msgIndex] = {
        ...messages.value[msgIndex],
        content: '',
        isRecalled: true
      }
    }
    
    ElMessage.success('撤回成功')
  } catch (error) {
    console.error('撤回消息错误:', error)
    ElMessage.error(error.message || '撤回失败')
  }
}

// 编辑撤回的消息并重新发送
const editRecalledMessage = async (msg) => {
  hideContextMenu()
  
  // 获取原始内容
  const originalContent = msg.originalContent || msg.content
  const content = originalContent.replace(/【已撤回】/g, '').trim()
  
  // 如果没有原始内容，提示用户
  if (!content) {
    ElMessage.warning('该消息无法编辑')
    return
  }
  
  // 追加内容到输入框（每点一次重新编辑就追加一次）
  if (inputMessage.value) {
    inputMessage.value += content
  } else {
    inputMessage.value = content
  }
}

function updateOtherUserInfo() {
  if (messages.value.length === 0) return
  const otherMsg = messages.value.find(msg => !isMyMessage(msg))
  if (otherMsg) {
    otherUserName.value = otherMsg.senderNickname || otherMsg.senderUsername || `用户${otherMsg.senderId}`
    otherUserAvatar.value = otherMsg.senderAvatar || defaultAvatar
  }
}

const loadData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    messages.value = []
    hasMore.value = true
  }

  if (!hasMore.value || loading.value || loadingMore.value) return

  const isInitialLoad = reset
  const isLoadingMore = !reset
  
  if (isLoadingMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  // 记录加载前的滚动高度（用于保持视觉位置）
  const scrollHeightBefore = messageListRef.value?.scrollHeight || 0

  try {
    const res = await getConversation(otherUserId, {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })

    const data = res.data || res
    const newData = data.records || []
    const total = data.total || 0

    // 后端按时间倒序返回（最新的在前），需要反转成正序（旧的在前）
    const sortedData = [...newData].reverse()

    if (reset) {
      // 首次加载：最新消息在数组末尾
      messages.value = sortedData
    } else {
      // 加载更多：将更早的消息插入到数组开头
      messages.value.unshift(...sortedData)
      
      // 等待DOM更新后调整滚动位置，保持视觉连续性
      await nextTick()
      if (messageListRef.value && scrollHeightBefore > 0) {
        const addedHeight = messageListRef.value.scrollHeight - scrollHeightBefore
        messageListRef.value.scrollTop = addedHeight
      }
    }

    hasMore.value = messages.value.length < total
    pageNum.value++

    updateOtherUserInfo()

    if (isInitialLoad) {
      markConversationAsRead(otherUserId).then(() => {
        messageStore.fetchUnreadCount()
      }).catch(() => {})
    }

    if (isInitialLoad) {
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载聊天记录错误:', error)
    if (isInitialLoad) {
      ElMessage.error(error.message || '加载失败')
    }
  } finally {
    if (isLoadingMore) {
      loadingMore.value = false
    } else {
      loading.value = false
    }
  }
}

const handleScroll = () => {
  const container = messageListRef.value
  if (!container || loadingMore.value || !hasMore.value) return
  
  // 当滚动到距离顶部50px以内时触发加载
  const scrollTop = container.scrollTop
  if (scrollTop <= 50) {
    loadData(false)
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  const messageContent = inputMessage.value.trim()

  sending.value = true
  try {
    const res = await sendMsg({
      receiverId: Number(otherUserId),
      content: messageContent
    })
    
    inputMessage.value = ''
    
    // 响应拦截器已处理，res 直接是后端返回的 Message 对象
    const newMsg = res
    
    console.log('后端返回的消息:', newMsg) // 调试日志
    
    const messageData = {
      id: newMsg?.id, // 必须使用后端返回的真实数据库ID
      senderId: Number(newMsg?.senderId) || Number(currentUserId.value),
      receiverId: Number(newMsg?.receiverId) || Number(otherUserId),
      content: newMsg?.content || messageContent,
      originalContent: newMsg?.originalContent || newMsg?.content || messageContent,
      isRead: newMsg?.isRead || 0,
      isRecalled: newMsg?.isRecalled || 0,
      senderNickname: '',
      senderAvatar: currentUserAvatar.value,
      receiverNickname: otherUserName.value || '用户',
      createdAt: newMsg?.createdAt || new Date().toISOString()
    }
    
    messages.value = [...messages.value, messageData]
    scrollToBottom()
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const goBack = () => {
  router.back()
}

// 时间分隔线格式化：根据时间跨度显示不同格式
const formatTimeSeparator = (dateStr) => {
  if (!dateStr) return ''
  
  const msgDate = new Date(dateStr)
  const now = new Date()
  
  const msgYear = msgDate.getFullYear()
  const msgMonth = msgDate.getMonth()
  const msgDay = msgDate.getDate()
  const msgHours = msgDate.getHours().toString().padStart(2, '0')
  const msgMinutes = msgDate.getMinutes().toString().padStart(2, '0')
  
  const nowYear = now.getFullYear()
  const nowMonth = now.getMonth()
  const nowDay = now.getDate()
  
  // 判断是否是同一天
  if (msgYear === nowYear && msgMonth === nowMonth && msgDay === nowDay) {
    // 当天：只显示时分
    return `${msgHours}:${msgMinutes}`
  }
  
  // 判断是否是同一年
  if (msgYear !== nowYear) {
    // 非同一年：显示年月日时分
    const month = (msgMonth + 1).toString().padStart(2, '0')
    const day = msgDay.toString().padStart(2, '0')
    return `${msgYear}-${month}-${day} ${msgHours}:${msgMinutes}`
  }
  
  // 判断是否是同一周内（但不是当天）
  const nowDayOfWeek = now.getDay() || 7 // 周日为7
  const msgDayOfWeek = msgDate.getDay() || 7
  
  // 计算本周一的日期
  const mondayOfThisWeek = new Date(now)
  mondayOfThisWeek.setDate(nowDay - nowDayOfWeek + 1)
  
  // 计算消息日期是本周还是上周
  const msgTime = msgDate.getTime()
  const mondayTime = mondayOfThisWeek.getTime()
  
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  if (msgTime >= mondayTime) {
    // 同一周内（非当天）：显示周几+时分
    return `${weekDays[msgDate.getDay()]} ${msgHours}:${msgMinutes}`
  } else {
    // 不同周但同年：显示月日时分
    const month = (msgMonth + 1).toString().padStart(2, '0')
    const day = msgDay.toString().padStart(2, '0')
    return `${month}-${day} ${msgHours}:${msgMinutes}`
  }
}

onMounted(() => {
  loadData(true)
  // 添加滚动事件监听
  if (messageListRef.value) {
    messageListRef.value.addEventListener('scroll', handleScroll, { passive: true })
  }
})

onUnmounted(() => {
  // 移除滚动事件监听
  if (messageListRef.value) {
    messageListRef.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.chat {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #e8e8e8;
}

.chat-header {
  background: #fff;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.avatar-wrapper {
  flex-shrink: 0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background: #f5f7fa;
}

.chat-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  background: #f5f7fa;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.my-message {
  flex-direction: row;
  justify-content: flex-end;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.sender-name {
  font-size: 12px;
  color: #909399;
  padding-left: 4px;
}

.my-sender-name {
  text-align: right;
  padding-left: 0;
  padding-right: 4px;
}

.message-bubble {
  position: relative;
  padding: 10px 14px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
  min-width: 60px;
}

.other-message .message-bubble {
  border-bottom-left-radius: 6px;
}

.my-message .message-bubble {
  background: #07c160;
  color: #fff;
  border-bottom-right-radius: 6px;
}

.message-text {
  font-size: 15px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

/* 重新编辑按钮 */
.recall-edit-btn {
  font-size: 13px;
  color: #576b95;
  margin-top: 6px;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  display: inline-block;
  transition: background-color 0.2s;
}

.recall-edit-btn:hover {
  background-color: rgba(0, 0, 0, 0.08);
}

.recall-edit-btn.disabled {
  color: #909399;
  cursor: not-allowed;
}

.recall-edit-btn.disabled:hover {
  background-color: transparent;
}

/* 右键菜单 */
.context-menu {
  position: fixed;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  min-width: 120px;
  z-index: 9999;
  border: 1px solid #e4e7ed;
}

.menu-item {
  padding: 8px 16px;
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  transition: background-color 0.2s;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

/* 时间分隔线样式 */
.time-separator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 12px 0;
  gap: 8px;
}

.time-separator::before,
.time-separator::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, transparent, #d9d9d9, transparent);
}

.time-separator span {
  font-size: 12px;
  color: #909399;
  background: #e8e8e8;
  padding: 4px 12px;
  border-radius: 12px;
  white-space: nowrap;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 0;
  color: #909399;
  font-size: 14px;
}

.input-area {
  background: #fff;
  padding: 12px 12px 120px;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  border-top: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.input-area .el-textarea {
  flex: 1;
}

.input-area .el-button {
  height: 40px;
  padding: 20px;
}

@media (max-width: 768px) {
  .message-list {
    padding: 10px;
    gap: 12px;
  }

  .message-content-wrapper {
    max-width: 80%;
  }

  .avatar {
    width: 36px;
    height: 36px;
  }

  .message-bubble {
    padding: 8px 12px;
  }

  .message-text {
    font-size: 14px;
  }
}
</style>