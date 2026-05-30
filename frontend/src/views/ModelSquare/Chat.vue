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
        :autosize="{ minRows: 1, maxRows: 100 }"
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
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

.chat::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(64, 158, 255, 0.1) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(255, 119, 198, 0.08) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.chat > * {
  position: relative;
  z-index: 1;
}

@keyframes floatIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bubblePop {
  0% {
    opacity: 0;
    transform: scale(0.8);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes pulseRing {
  0% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(64, 158, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0);
  }
}

.chat-header {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
  animation: floatIn 0.5s ease-out;
}

.chat-header .el-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #fff;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.chat-header .el-button:hover {
  background: rgba(64, 158, 255, 0.3);
  border-color: rgba(64, 158, 255, 0.5);
  transform: scale(1.05);
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.avatar-wrapper {
  flex-shrink: 0;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(64, 158, 255, 0.4);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3), 0 0 20px rgba(64, 158, 255, 0.2);
  transition: all 0.3s ease;
  animation: pulseRing 3s infinite;
}

.avatar:hover {
  transform: scale(1.1) rotate(5deg);
  border-color: rgba(64, 158, 255, 0.8);
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-list::-webkit-scrollbar {
  width: 8px;
}

.message-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.message-list::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.5), rgba(103, 194, 58, 0.5));
  border-radius: 4px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  animation: bubblePop 0.4s ease-out;
}

.my-message {
  flex-direction: row;
  justify-content: flex-end;
  animation: slideInRight 0.4s ease-out;
}

.other-message {
  animation: slideInLeft 0.4s ease-out;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 75%;
}

.sender-name {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  padding-left: 6px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.my-sender-name {
  text-align: right;
  padding-left: 0;
  padding-right: 6px;
}

.message-bubble {
  position: relative;
  padding: 14px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  min-width: 60px;
  transition: all 0.3s ease;
}

.message-bubble:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
}

.other-message .message-bubble {
  border-bottom-left-radius: 6px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.3), rgba(64, 158, 255, 0.1));
}

.other-message .message-bubble::before {
  content: '';
  position: absolute;
  left: -8px;
  top: 12px;
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-right: 10px solid rgba(64, 158, 255, 0.3);
}

.my-message .message-bubble {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.6), rgba(103, 194, 58, 0.4));
  color: #fff;
  border-bottom-right-radius: 6px;
  border: 1px solid rgba(64, 158, 255, 0.4);
}

.my-message .message-bubble::before {
  content: '';
  position: absolute;
  right: -8px;
  top: 12px;
  width: 0;
  height: 0;
  border-top: 8px solid transparent;
  border-bottom: 8px solid transparent;
  border-left: 10px solid rgba(64, 158, 255, 0.6);
}

.message-text {
  font-size: 15px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.recall-edit-btn {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 8px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  display: inline-block;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.recall-edit-btn:hover {
  background: rgba(64, 158, 255, 0.3);
  border-color: rgba(64, 158, 255, 0.5);
  color: #fff;
}

.recall-edit-btn.disabled {
  color: rgba(255, 255, 255, 0.3);
  cursor: not-allowed;
}

.recall-edit-btn.disabled:hover {
  background: rgba(255, 255, 255, 0.1);
}

.context-menu {
  position: fixed;
  background: rgba(30, 30, 50, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 20px rgba(64, 158, 255, 0.2);
  padding: 8px 0;
  min-width: 140px;
  z-index: 9999;
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: bubblePop 0.3s ease-out;
}

.menu-item {
  padding: 10px 20px;
  font-size: 14px;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.menu-item:hover {
  background: rgba(64, 158, 255, 0.3);
  color: #fff;
}

.time-separator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 16px 0;
  gap: 12px;
}

.time-separator::before,
.time-separator::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(64, 158, 255, 0.3), transparent);
}

.time-separator span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  padding: 6px 16px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  white-space: nowrap;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.input-area {
  background: rgba(20, 20, 40, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 16px 16px 120px;
  display: flex;
  gap: 14px;
  align-items: flex-end;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
  position: relative;
}

.input-area::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.5), transparent);
}

.input-area .el-textarea {
  flex: 1;
}

.input-area .el-textarea :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  color: #fff;
  padding: 12px 16px;
  resize: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
  max-height: 120px;
  overflow-y: auto;
}

.input-area .el-textarea :deep(.el-textarea__inner)::-webkit-scrollbar {
  width: 6px;
}

.input-area .el-textarea :deep(.el-textarea__inner)::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.input-area .el-textarea :deep(.el-textarea__inner)::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.5), rgba(103, 194, 58, 0.5));
  border-radius: 3px;
}

.input-area .el-textarea :deep(.el-textarea__inner)::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.7), rgba(103, 194, 58, 0.7));
}

.input-area .el-textarea :deep(.el-textarea__inner:focus) {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(64, 158, 255, 0.5);
  box-shadow: 0 0 20px rgba(64, 158, 255, 0.3);
}

.input-area .el-textarea :deep(.el-textarea__inner)::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.input-area .el-button {
  margin-bottom: 6px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  border: none;
  border-radius: 16px;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.input-area .el-button:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.6);
}
</style>