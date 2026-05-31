<template>
  <div class="ai-chat-wrapper">
    <div class="ai-chat-container">
      <!-- 侧边栏遮罩层（移动端） -->
      <div v-if="sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false"></div>
      
      <div :class="['chat-sidebar', { show: sidebarOpen }]">
        <div class="sidebar-header">
          <h3>AI 助手</h3>
          <el-button type="primary" size="small" @click="handleNewSession">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
        </div>
        
        <div class="session-list">
          <div 
            v-for="session in sessions" 
            :key="session.id"
            :class="['session-item', { active: currentSessionId === session.id }]"
            @click="handleSelectSession(session)"
          >
            <div class="session-info">
              <div class="session-title">{{ session.title }}</div>
              <div class="session-preview">{{ session.lastMessage || '暂无消息' }}</div>
            </div>
            <el-button 
              type="danger" 
              size="small" 
              circle 
              class="delete-btn"
              @click.stop="handleDeleteSession(session.id)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          
          <div v-if="sessions.length === 0" class="empty-sessions">
            <el-empty description="暂无对话记录" :image-size="60" />
          </div>
        </div>
      </div>
      
      <div class="chat-main">
        <div class="chat-header">
          <div class="header-left">
            <el-button circle size="small" class="mobile-menu-btn" @click="sidebarOpen = !sidebarOpen">
              <el-icon><ChatDotRound /></el-icon>
            </el-button>
            <span class="chat-title">{{ currentSession?.title || '新对话' }}</span>
          </div>
          <div class="chat-actions">
            <el-button size="small" class="mobile-new-btn" @click="handleNewSession">
              <el-icon><Plus /></el-icon>
              <span class="btn-text">新对话</span>
            </el-button>
            <el-button circle size="small" @click="$emit('close')">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
        
        <div class="chat-content">
          <div class="message-list" ref="messageListRef">
            <div v-if="messages.length === 0 && !loading" class="welcome-section">
              <div class="welcome-icon">
                <div class="icon-ring">
                  <el-icon :size="48"><Service /></el-icon>
                </div>
              </div>
              <h3>欢迎使用 AI 助手</h3>
              <p class="welcome-desc">我可以帮助你解答关于拼装模型管理、制作技巧等方面的疑问</p>
              
              <div class="quick-questions">
                <div class="questions-header">
                  <el-icon><QuestionFilled /></el-icon>
                  <span>快捷问题</span>
                </div>
                <div class="questions-grid">
                  <div 
                    v-for="(q, index) in presetQuestions" 
                    :key="index"
                    class="question-card"
                    :style="{ animationDelay: (index * 0.12) + 's' }"
                    @click="handleQuestionClick(q)"
                  >
                    <div class="question-text">{{ q.text }}</div>
                    <div class="question-glow"></div>
                  </div>
                </div>
              </div>
            </div>
            
            <div 
              v-for="(message, index) in messages" 
              :key="message.id || index"
              :class="['message-item', message.role]"
            >
              <div class="message-avatar">
                <el-icon v-if="message.role === 'user'" :size="24"><User /></el-icon>
                <el-icon v-else :size="24"><Service /></el-icon>
              </div>
              <div class="message-content">
                <div class="message-text" v-html="formatMessageContent(message.content)"></div>
                <div class="message-time" v-if="message.createdAt">
                  {{ formatTime(message.createdAt) }}
                </div>
              </div>
            </div>
            
            <div v-if="loading" class="message-item assistant loading">
              <div class="message-avatar">
                <el-icon :size="24"><Service /></el-icon>
              </div>
              <div class="message-content">
                <div class="loading-wrapper">
                  <div class="loading-dots">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                  <div class="thinking-text">
                    <span class="thinking-label">正在思考</span>
                    <span class="thinking-timer">{{ thinkingTime }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="message-input">
            <el-input
              ref="textareaRef"
              v-model="inputMessage"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 5 }"
              :class="{ 'textarea-overflow': textareaOverflow }"
              placeholder="请输入消息..."
              :disabled="loading"
              @keydown.enter.ctrl="handleSendMessage"
            />
            <el-button 
              type="primary" 
              class="cool-btn send-btn"
              :loading="loading"
              @click="handleSendMessage"
              :disabled="!inputMessage.trim() || loading"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, ChatDotRound, User, Service, Close, QuestionFilled } from '@element-plus/icons-vue'
import { 
  getChatSessions, 
  createChatSession, 
  deleteChatSession,
  getChatMessages,
  sendChatMessage
} from '../api/aiChat'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messageListRef = ref(null)
const sidebarOpen = ref(false)
const tempSessionId = ref(null)
const textareaRef = ref(null)
const textareaOverflow = ref(false)
const thinkingTime = ref('0.0s')
let thinkingTimer = null

const presetQuestions = [
  { text: '入门级模型推荐', prompt: '推荐一些适合新手的入门级拼装模型？' },
  { text: '工具使用指南', prompt: '拼装模型需要哪些工具？如何正确使用？' },
  { text: '上色技巧分享', prompt: '给拼装模型上色有什么技巧？推荐哪些颜料？' },
  { text: '胶水选择建议', prompt: '拼装模型用什么胶水比较好？不同胶水有什么区别？' },
  { text: '收纳展示技巧', prompt: '如何收纳和展示拼装模型？有什么好的建议？' },
  { text: '复杂模型制作', prompt: '制作复杂的拼装模型有什么方法和技巧？' },
]

// 处理AI回答内容，去除格式并优化分点显示
const cleanMarkdown = (text) => {
  if (!text) return text
  let result = text
    .replace(/^#{1,6}\s*/gm, '') // 标题
    .replace(/\*\*(.*?)\*\*/g, '$1') // 粗体
    .replace(/\*(.*?)\*/g, '$1') // 斜体
    .replace(/~~(.*?)~~/g, '$1') // 删除线
    .replace(/`([^`]+)`/g, '$1') // 代码
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1') // 链接
    .replace(/^>\s*/gm, '') // 引用
    .replace(/^\s*[-*_]{3,}\s*$/g, '') // 分隔线
    .replace(/<[^>]+>/g, '') // HTML标签
    .trim()
  
  // 分点换行处理：在数字序号前添加换行（除了开头）
  result = result.replace(/(?<!^|\n)(\s*)(\d+\.\s)/g, '\n$1$2')
  
  // 处理中文数字分点（一、二、三等）
  result = result.replace(/(?<!^|\n)(\s*)([一二三四五六七八九十]+[、．。])/g, '\n$1$2')
  
  // 处理字母分点（A. B. 等）
  result = result.replace(/(?<!^|\n)(\s*)([A-Za-z]\.\s)/g, '\n$1$2')
  
  // 处理无序列表
  result = result.replace(/(?<!^|\n)(\s*)([-●•*]\s)/g, '\n$1$2')
  
  // 处理冒号后的分点（如"建议：1."）
  result = result.replace(/([：:])(\s*)(\d+\.\s)/g, '$1\n$2$3')
  
  // 合并多余空行
  result = result.replace(/\n{3,}/g, '\n\n')
  
  return result.trim()
}

// 格式化消息内容用于HTML显示
const formatMessageContent = (text) => {
  const cleaned = cleanMarkdown(text)
  // 将换行转换为 <br> 标签
  return cleaned.replace(/\n/g, '<br>')
}

const handleQuestionClick = (question) => {
  inputMessage.value = question.prompt
  handleSendMessage()
}

const currentSession = computed(() => {
  return sessions.value.find(s => s.id === currentSessionId.value)
})

const loadSessions = async () => {
  try {
    const data = await getChatSessions()
    sessions.value = data || []
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

const handleNewSession = async () => {
  if (currentSessionId.value && messages.value.length === 0) {
    return
  }
  
  // 不立即创建会话，只清空状态，等第一条消息发送时再创建
  currentSessionId.value = null
  tempSessionId.value = null
  messages.value = []
  inputMessage.value = ''
  sidebarOpen.value = false
}

const handleSelectSession = async (session) => {
  if (currentSessionId.value === session.id) return
  
  if (loading.value) {
    ElMessage.warning('请等待当前消息发送完成')
    return
  }
  
  currentSessionId.value = session.id
  tempSessionId.value = null
  await loadMessages(session.id)
  sidebarOpen.value = false
}

const loadMessages = async (sessionId) => {
  try {
    const data = await getChatMessages(sessionId)
    messages.value = data || []
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('获取消息失败')
  }
}

const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const message = inputMessage.value.trim()
  inputMessage.value = ''
  loading.value = true
  
  // 启动思考计时器
  thinkingTime.value = '0.0s'
  const startTime = Date.now()
  thinkingTimer = setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000
    thinkingTime.value = elapsed.toFixed(1) + 's'
  }, 100)
  
  // 如果是临时会话（还没创建的），先创建
  if (!currentSessionId.value) {
    try {
      const session = await createChatSession()
      currentSessionId.value = session.id
      sessions.value.unshift(session)
    } catch (error) {
      ElMessage.error('创建会话失败')
      loading.value = false
      clearInterval(thinkingTimer)
      return
    }
  }
  
  messages.value.push({
    role: 'user',
    content: message,
    createdAt: new Date().toISOString()
  })
  await nextTick()
  scrollToBottom()
  
  try {
    const aiMessage = await sendChatMessage(currentSessionId.value, { content: message })
    messages.value.push({
      role: 'assistant',
      content: aiMessage.content,
      createdAt: aiMessage.createdAt
    })
    await nextTick()
    scrollToBottom()
    await loadSessions()
  } catch (error) {
    ElMessage.error('发送消息失败: ' + (error.response?.data?.msg || error.message))
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI 助手暂时无法回复。',
      createdAt: new Date().toISOString()
    })
  } finally {
    loading.value = false
    clearInterval(thinkingTimer)
  }
}

const handleDeleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteChatSession(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.value = []
    }
    
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const checkTextareaOverflow = () => {
  nextTick(() => {
    if (textareaRef.value) {
      const textarea = textareaRef.value.$el.querySelector('textarea')
      if (textarea) {
        textareaOverflow.value = textarea.scrollHeight > textarea.clientHeight
      }
    }
  })
}

watch(inputMessage, () => {
  checkTextareaOverflow()
})

onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
/* ========== 全局样式 - 深色毛玻璃主题 ========== */
.ai-chat-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
  /* 深色渐变背景 */
  /* background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%); */
  animation: wrapperFadeIn 0.8s ease-out;
}

/* 页面入场动画 */
@keyframes wrapperFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 全局滚动条美化 */
.ai-chat-wrapper ::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.ai-chat-wrapper ::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.ai-chat-wrapper ::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.6), rgba(118, 75, 162, 0.6));
  border-radius: 3px;
}

.ai-chat-wrapper ::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.8), rgba(118, 75, 162, 0.8));
}

/* 容器主样式 - 毛玻璃效果 */
.ai-chat-container {
  display: flex;
  width: 90%;
  max-width: 1000px;
  height: 70vh;
  max-height: 600px;
  /* 毛玻璃效果 */
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  /* 多层阴影 */
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  overflow: hidden;
  position: relative;
  animation: containerSlideIn 0.6s ease-out 0.2s backwards;
}

/* 容器滑入动画 */
@keyframes containerSlideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 侧边栏样式 ========== */
.chat-sidebar {
  width: 280px;
  /* 侧边栏毛玻璃 */
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  animation: sidebarFadeIn 0.5s ease-out 0.4s backwards;
}

@keyframes sidebarFadeIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.sidebar-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  /* 头部毛玻璃 */
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  /* 渐变文字 */
  background: linear-gradient(135deg, #a855f7, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 8px 8px;
}

/* ========== 会话列表项 - 磨砂卡片效果 ========== */
.session-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 6px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  /* 磨砂卡片背景 */
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  overflow: hidden;
}

/* 渐变发光边框效果 */
.session-item::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 12px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.35s ease;
}

/* hover效果 - 上浮、阴影放大、边框高亮 */
.session-item:hover {
  transform: translateY(-4px);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 
    0 12px 40px rgba(102, 126, 234, 0.2),
    0 0 20px rgba(102, 126, 234, 0.1);
}

.session-item:hover::before {
  opacity: 1;
}

/* 选中状态 - 微光标识 */
.session-item.active {
  background: rgba(102, 126, 234, 0.15);
  border-color: rgba(102, 126, 234, 0.5);
}

.session-item.active::before {
  opacity: 1;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.6), rgba(118, 75, 162, 0.6));
}

/* 选中状态微光指示器 */
.session-item.active::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 40%;
  background: linear-gradient(180deg, #a855f7, #6366f1);
  border-radius: 2px;
  box-shadow: 0 0 10px rgba(168, 85, 247, 0.6);
}

.session-info {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.session-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 3px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-preview {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-btn {
  opacity: 0;
  transition: all 0.3s ease;
  margin-left: 8px;
  position: relative;
  z-index: 1;
}

.session-item:hover .delete-btn {
  opacity: 1;
}

.empty-sessions {
  padding: 40px 20px;
  text-align: center;
}

/* ========== 主聊天区域 ========== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.03);
  animation: mainFadeIn 0.5s ease-out 0.5s backwards;
}

@keyframes mainFadeIn {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* ========== 顶部导航栏 - 毛玻璃悬浮效果 ========== */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  /* 毛玻璃悬浮效果 */
  background: rgba(15, 12, 41, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mobile-menu-btn {
  display: none;
}

.chat-title {
  font-size: 15px;
  font-weight: 600;
  /* 渐变文字 */
  background: linear-gradient(135deg, #c4b5fd, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.mobile-new-btn {
  display: none;
}

.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.02);
}

.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

/* ========== 欢迎区域样式 ========== */
.welcome-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 30px;
  text-align: center;
  height: 100%;
  animation: fadeInUp 0.6s ease-out;
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

.welcome-section .welcome-icon {
  margin-bottom: 24px;
  position: relative;
}

.welcome-section .icon-ring {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 
    0 10px 40px rgba(102, 126, 234, 0.4),
    0 0 60px rgba(102, 126, 234, 0.3);
  animation: pulse-ring 2s ease-in-out infinite;
  position: relative;
}

.icon-ring::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid rgba(102, 126, 234, 0.5);
  animation: ripple 2s ease-out infinite;
}

@keyframes pulse-ring {
  0%, 100% {
    transform: scale(1);
    box-shadow: 
      0 10px 40px rgba(102, 126, 234, 0.4),
      0 0 60px rgba(102, 126, 234, 0.3);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 
      0 15px 50px rgba(102, 126, 234, 0.5),
      0 0 80px rgba(102, 126, 234, 0.4);
  }
}

@keyframes ripple {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.welcome-section h3 {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #c4b5fd, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-section .welcome-desc {
  margin: 0 0 30px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  max-width: 400px;
}

/* ========== 快捷问题区域 ========== */
.quick-questions {
  width: 100%;
  max-width: 800px;
  margin-top: 10px;
}

.questions-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.questions-header .el-icon {
  color: #a855f7;
  font-size: 18px;
}

.questions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  animation: fadeIn 0.8s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.question-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(168, 85, 247, 0.2);
  border-radius: 16px;
  padding: 20px 14px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  animation: cardEntrance 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) backwards;
}

@keyframes cardEntrance {
  0% {
    opacity: 0;
    transform: scale(0.6) rotate(-5deg);
  }
  50% {
    transform: scale(1.05) rotate(2deg);
  }
  100% {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

.question-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.6s ease;
}

.question-card:hover::before {
  left: 100%;
}

.question-card:hover {
  transform: translateY(-8px) scale(1.03);
  background: rgba(168, 85, 247, 0.15);
  border-color: rgba(168, 85, 247, 0.5);
  box-shadow: 
    0 12px 40px rgba(168, 85, 247, 0.3),
    0 0 30px rgba(168, 85, 247, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.question-card:active {
  transform: translateY(-4px) scale(0.98);
}

.question-glow {
  position: absolute;
  bottom: -50%;
  left: 50%;
  width: 200%;
  height: 100%;
  transform: translateX(-50%);
  background: radial-gradient(ellipse at center, rgba(168, 85, 247, 0.3) 0%, transparent 70%);
  opacity: 0;
  transition: all 0.4s ease;
  animation: glowPulse 3s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% {
    opacity: 0;
    transform: translateX(-50%) scale(1);
  }
  50% {
    opacity: 0.5;
    transform: translateX(-50%) scale(1.1);
  }
}

.question-card:hover .question-glow {
  opacity: 0.6;
  bottom: -30%;
}

.question-text {
  position: relative;
  z-index: 1;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
  line-height: 1.4;
  font-weight: 500;
}

/* ========== 聊天内容区域 ========== */
.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: rgba(255, 255, 255, 0.02);
}

/* ========== 消息气泡样式 ========== */
.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: messageFadeIn 0.4s ease-out;
}

/* 消息弹出动画 */
@keyframes messageFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  /* 头像发光效果 */
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #10b981, #34d399);
  color: #fff;
}

.message-content {
  max-width: 70%;
  margin: 0 12px;
}

.message-text {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.6;
  word-wrap: break-word;
  font-size: 14px;
  position: relative;
}

/* 用户消息气泡 */
.message-item.user .message-text {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.8), rgba(139, 92, 246, 0.8));
  color: #fff;
  border-radius: 16px 16px 4px 16px;
  /* 多层柔化阴影 */
  box-shadow: 
    0 4px 20px rgba(99, 102, 241, 0.3),
    0 0 30px rgba(99, 102, 241, 0.15);
}

/* AI消息气泡 */
.message-item.assistant .message-text {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px 16px 16px 4px;
  /* 多层柔化阴影 */
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.2),
    0 0 30px rgba(168, 85, 247, 0.05);
}

.message-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 6px;
}

.message-item.user .message-time {
  text-align: right;
}

/* 加载状态 */
.loading-dots {
  display: flex;
  gap: 6px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px 16px 16px 4px;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.thinking-text {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.thinking-label {
  background: linear-gradient(135deg, #a855f7, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 500;
  animation: textPulse 1.5s ease-in-out infinite;
}

.thinking-timer {
  font-family: 'Courier New', monospace;
  color: rgba(168, 85, 247, 0.8);
  font-size: 11px;
  min-width: 35px;
}

@keyframes textPulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.loading-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #a855f7, #6366f1);
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% { 
    transform: scale(0);
    opacity: 0.5;
  }
  40% { 
    transform: scale(1);
    opacity: 1;
  }
}

/* ========== 底部输入框样式 ========== */
.message-input {
  padding: 20px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  gap: 16px;
  align-items: flex-end;
  /* 毛玻璃效果 */
  background: rgba(15, 12, 41, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  position: relative;
  z-index: 1;
}

.message-input :deep(.el-textarea) {
  flex: 1;
  max-height: 150px;
}

.message-input :deep(.el-textarea__inner) {
  resize: none;
  border-radius: 16px;
  font-size: 15px;
  /* 渐变边框 */
  border: 2px solid transparent;
  background: linear-gradient(rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.08)) padding-box,
              linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3)) border-box;
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  max-height: 150px;
  overflow-y: hidden;
}

.message-input :deep(.textarea-overflow .el-textarea__inner) {
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(102, 126, 234, 0.4) transparent;
}

.message-input :deep(.textarea-overflow .el-textarea__inner::-webkit-scrollbar) {
  width: 6px;
}

.message-input :deep(.textarea-overflow .el-textarea__inner::-webkit-scrollbar-track) {
  background: transparent;
}

.message-input :deep(.textarea-overflow .el-textarea__inner::-webkit-scrollbar-thumb) {
  background: linear-gradient(180deg, rgba(102, 126, 234, 0.6), rgba(118, 75, 162, 0.6));
  border-radius: 3px;
}

.message-input :deep(.el-textarea__inner::-webkit-scrollbar) {
  width: 0;
  height: 0;
}

/* 聚焦发光效果 */
.message-input :deep(.el-textarea__inner:focus) {
  border-color: transparent;
  background: linear-gradient(rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.1)) padding-box,
              linear-gradient(135deg, rgba(102, 126, 234, 0.6), rgba(118, 75, 162, 0.6)) border-box;
  box-shadow: 
    0 0 0 4px rgba(102, 126, 234, 0.15),
    0 4px 20px rgba(102, 126, 234, 0.2),
    0 0 40px rgba(102, 126, 234, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

/* 发送按钮 - 流光动画 */
.message-input :deep(.el-button) {
  padding: 10px 28px;
  border-radius: 16px;
  font-weight: 600;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 按钮流光扫过效果 */
.message-input :deep(.el-button)::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.message-input :deep(.el-button:hover)::before {
  left: 100%;
}

/* hover效果 */
.message-input :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 25px rgba(102, 126, 234, 0.4),
    0 0 30px rgba(102, 126, 234, 0.2);
}

/* 点击回弹动效 */
.message-input :deep(.el-button:active) {
  transform: translateY(0) scale(0.96);
}

.message-input :deep(.el-button:disabled) {
  background: rgba(255, 255, 255, 0.1);
  box-shadow: none;
}

.message-input :deep(.el-button:disabled)::before {
  display: none;
}

/* ========== 平板端适配 ========== */
@media screen and (max-width: 768px) {
  .ai-chat-wrapper {
    padding: 50px 10px;
  }

  .ai-chat-container {
    height: 80vh;
    max-height: none;
    border-radius: 16px;
  }

  .chat-sidebar {
    width: 220px;
  }

  .sidebar-header {
    padding: 12px;
  }

  .sidebar-header h3 {
    font-size: 14px;
  }

  .session-title {
    font-size: 13px;
  }

  .session-preview {
    font-size: 11px;
  }

  .message-list {
    padding: 15px;
  }

  /* 平板端欢迎区域适配 */
  .welcome-section {
    padding: 30px 20px;
  }

  .welcome-section .icon-ring {
    width: 80px;
    height: 80px;
  }

  .welcome-section h3 {
    font-size: 20px;
  }

  .welcome-section .welcome-desc {
    font-size: 13px;
    margin-bottom: 25px;
  }

  .questions-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .question-card {
    padding: 14px 10px;
    border-radius: 14px;
  }

  .question-text {
    font-size: 12px;
  }

  .message-avatar {
    width: 30px;
    height: 30px;
  }

  .message-text {
    font-size: 13px;
    padding: 10px 12px;
  }

  .message-content {
    max-width: 75%;
  }

  .empty-chat h2 {
    font-size: 18px;
  }

  .empty-chat p {
    font-size: 13px;
  }
}

/* ========== 移动端适配 ========== */
@media screen and (max-width: 480px) {
  .ai-chat-wrapper {
    padding: 50px 10px;
    align-items: flex-start;
  }

  .ai-chat-container {
    height: 75vh;
    max-height: none;
    border-radius: 16px;
    box-shadow: 
      0 8px 32px rgba(0, 0, 0, 0.4),
      0 0 60px rgba(102, 126, 234, 0.1);
    margin: 80px 10px;
  }

  .sidebar-overlay {
    position: absolute;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(8px);
    z-index: 9;
    border-radius: 16px;
  }

  .chat-sidebar {
    width: 80%;
    max-width: 280px;
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    z-index: 10;
    transform: translateX(-100%);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 16px 0 0 16px;
  }

  .chat-sidebar.show {
    transform: translateX(0);
  }

  .mobile-menu-btn {
    display: flex;
  }
  
  .mobile-new-btn {
    display: flex;
    align-items: center;
    gap: 4px;
  }
  
  .mobile-new-btn .btn-text {
    font-size: 13px;
  }

  .chat-main {
    width: 100%;
  }

  .chat-header {
    padding: 10px 15px;
  }

  .chat-title {
    font-size: 14px;
  }

  .message-list {
    padding: 10px;
  }

  .message-item {
    margin-bottom: 12px;
  }

  .message-avatar {
    width: 28px;
    height: 28px;
  }

  .message-avatar .el-icon {
    font-size: 16px !important;
  }

  .message-content {
    max-width: 80%;
    margin: 0 8px;
  }

  .message-text {
    font-size: 13px;
    padding: 10px 12px;
  }

  .message-time {
    font-size: 10px;
  }

  /* 移动端欢迎区域适配 */
  .welcome-section {
    padding: 20px 15px;
  }

  .welcome-section .welcome-icon {
    margin-bottom: 16px;
  }

  .welcome-section .icon-ring {
    width: 70px;
    height: 70px;
  }

  .welcome-section .icon-ring .el-icon {
    font-size: 36px;
  }

  .welcome-section h3 {
    font-size: 18px;
  }

  .welcome-section .welcome-desc {
    font-size: 13px;
    margin-bottom: 20px;
  }

  .quick-questions {
    margin-top: 5px;
  }

  .questions-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .question-card {
    padding: 12px 8px;
    border-radius: 12px;
  }

  .question-text {
    font-size: 12px;
  }

  .welcome-message {
    padding: 30px 15px;
  }
  
  .welcome-message h3 {
    font-size: 16px;
  }

  .welcome-message p {
    font-size: 12px;
  }

  .welcome-message .welcome-icon .el-icon {
    font-size: 50px !important;
  }

  .message-input {
    padding: 12px 15px;
  }

  .message-input :deep(.el-textarea__inner) {
    font-size: 14px;
  }

  .message-input :deep(.el-button) {
    padding: 8px 20px;
  }
}
</style>
