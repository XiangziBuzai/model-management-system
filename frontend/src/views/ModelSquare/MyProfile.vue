<template>
  <div class="my-profile">
    <div class="profile-header">
      <div class="profile-info">
        <el-avatar :size="64" :src="userAvatar" class="avatar">
          {{ nickname.charAt(nickname.length - 1) }}
        </el-avatar>
        <div class="user-details">
          <h2 class="nickname">{{ nickname }}</h2>
          <p class="username">{{ username }}</p>
        </div>
        <el-button 
          type="info" 
          plain 
          circle
          size="small"
          @click="handleShowGuide"
          class="guide-btn"
        >
          <el-icon><QuestionFilled /></el-icon>
        </el-button>
      </div>
      <div class="profile-stats">
        <div class="stat-item" @click="activeTab = 'models'">
          <span class="stat-value">{{ modelCount }}</span>
          <span class="stat-label">模型</span>
        </div>
        <div class="stat-item" @click="activeTab = 'tools'">
          <span class="stat-value">{{ toolCount }}</span>
          <span class="stat-label">工具</span>
        </div>
        <div class="stat-item" @click="activeTab = 'favorites'">
          <span class="stat-value">{{ favoriteCount }}</span>
          <span class="stat-label">收藏</span>
        </div>
      </div>
    </div>

    <div class="content-section">
      <div class="tab-bar">
        <el-radio-group v-model="activeTab" size="large">
          <el-radio-button label="models">模型</el-radio-button>
          <el-radio-button label="tools">工具</el-radio-button>
          <el-radio-button label="favorites">
            <el-icon><StarFilled /></el-icon>
            收藏
          </el-radio-button>
        </el-radio-group>
        <el-radio-group v-model="visibilityTab" size="large" @change="handleVisibilityChange" v-if="activeTab !== 'favorites'">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="public">公开</el-radio-button>
          <el-radio-button label="private">私密</el-radio-button>
        </el-radio-group>
      </div>

      <div class="content-list" v-loading="loading" ref="contentListRef" @scroll="handleScroll">
        <div v-if="activeTab === 'favorites'">
          <div v-if="favoriteList.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无收藏" />
          </div>

          <div class="card-grid">
            <div v-for="item in favoriteList" :key="item.id" class="item-card" @click="goToFavoriteDetail(item)">
              <div class="cover-wrapper">
                <div class="item-cover" v-if="item.cover">
                  <img :src="item.cover" :alt="item.itemName" class="cover-img" />
                </div>
                <div class="item-cover placeholder-cover" v-else>
                  <el-icon class="cover-icon"><Picture /></el-icon>
                </div>
                <div class="cover-meta">
                  <span class="meta-item"><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
                  <span class="meta-item"><el-icon><Star /></el-icon>{{ item.favoriteCount || 0 }}</span>
                </div>
              </div>
              
              <div class="item-info">
                <h3 class="item-name">{{ item.itemName }}</h3>
                <span class="item-price">¥{{ item.itemPrice }}</span>
                <div class="item-meta">
                  <span class="manufacturer">{{ item.sellerNickname || '' }}</span>
                  <span class="seller-status">
                    <el-tag type="warning" size="small">
                      {{ item.itemType === 'MODEL' ? '模型' : '工具' }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="loading" class="loading-tip">
            <span>加载中...</span>
          </div>
          <div v-else-if="!favoriteHasMore && favoriteList.length > 0" class="no-more-tip">
            <span>没有更多了</span>
          </div>
        </div>

        <div v-else>
          <div v-if="filteredList.length === 0 && !loading" class="empty-state">
            <el-empty description="暂无数据" />
          </div>

          <div class="card-grid">
            <div v-for="item in filteredList" :key="item.id" class="item-card" @click="goToDetail(item)">
              <div class="cover-wrapper">
                <div class="item-cover" v-if="item.cover">
                  <img :src="item.cover" :alt="item.name" class="cover-img" />
                </div>
                <div class="item-cover placeholder-cover" v-else>
                  <el-icon class="cover-icon"><Picture /></el-icon>
                </div>
                <div class="cover-meta">
                  <span class="meta-item"><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
                  <span class="meta-item"><el-icon><Star /></el-icon>{{ item.favoriteCount || 0 }}</span>
                </div>
              </div>
              
              <div class="item-info">
                <h3 class="item-name">{{ item.name }}</h3>
                <span class="item-price">¥{{ item.price }}</span>
                <div class="item-meta">
                  <span class="manufacturer">{{ item.manufacturerName || '' }}</span>
                  <span class="seller-status">
                    <el-tag :type="item.isPublic === 1 ? 'warning' : 'info'" size="small">
                      {{ item.isPublic === 1 ? '已公开' : '私有' }}
                    </el-tag>
                    <el-tag :type="item.sold === 1 ? 'success' : 'info'" size="small">
                      {{ item.sold === 1 ? '已售出' : '未售出' }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="loading" class="loading-tip">
            <span>加载中...</span>
          </div>
          <div v-else-if="!hasMore && filteredList.length > 0" class="no-more-tip">
            <span>没有更多了</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, inject } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Star, Picture, StarFilled, QuestionFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/useAuthStore'
import { getModelList } from '../../api/model'
import { getToolList } from '../../api/tool'
import { getMyFavorites } from '../../api/square'

const router = useRouter()
const authStore = useAuthStore()
const showWelcomeGuide = inject('showWelcomeGuide')
const loading = ref(false)
const activeTab = ref('models')
const visibilityTab = ref('all')
const list = ref([])
const favoriteList = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)
const favoritePageNum = ref(1)
const favoritePageSize = ref(20)
const favoriteHasMore = ref(true)
const contentListRef = ref(null)
const modelCount = ref(0)
const toolCount = ref(0)
const favoriteCount = ref(0)
let scrollTimer = null

const userInfo = authStore.userInfo || {}
const userAvatar = userInfo.avatar || ''
const nickname = userInfo.nickname || userInfo.realName || '用户'
const username = userInfo.username || ''

const filteredList = computed(() => {
  if (visibilityTab.value === 'all') {
    return list.value
  }
  const isPublic = visibilityTab.value === 'public'
  return list.value.filter(item => (item.isPublic === 1) === isPublic)
})

const loadCounts = async () => {
  try {
    const [modelResult, toolResult, favoriteResult] = await Promise.all([
      getModelList({ page: 1, size: 1 }),
      getToolList({ page: 1, size: 1 }),
      getMyFavorites({ pageNum: 1, pageSize: 1 })
    ])
    modelCount.value = modelResult.total || 0
    toolCount.value = toolResult.total || 0
    const favoriteData = favoriteResult.data || favoriteResult
    favoriteCount.value = favoriteData.total || 0
  } catch (error) {
    console.error('加载数量错误:', error)
  }
}

const loadModels = async () => {
  loading.value = true
  try {
    const params = {
      page: pageNum.value,
      size: pageSize.value
    }
    const result = await getModelList(params)
    const data = result.records || []
    
    if (pageNum.value === 1) {
      list.value = data
    } else {
      list.value.push(...data)
    }
    
    hasMore.value = list.value.length < (result.total || 0)
    pageNum.value++
  } catch (error) {
    console.error('加载模型数据错误:', error)
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadTools = async () => {
  loading.value = true
  try {
    const params = {
      page: pageNum.value,
      size: pageSize.value
    }
    const result = await getToolList(params)
    const data = result.records || []
    
    if (pageNum.value === 1) {
      list.value = data
    } else {
      list.value.push(...data)
    }
    
    hasMore.value = list.value.length < (result.total || 0)
    pageNum.value++
  } catch (error) {
    console.error('加载工具数据错误:', error)
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: favoritePageNum.value,
      pageSize: favoritePageSize.value
    }
    const res = await getMyFavorites(params)
    const data = res.data || res
    const newData = data.records || []
    const total = data.total || 0
    
    if (favoritePageNum.value === 1) {
      favoriteList.value = newData
    } else {
      favoriteList.value.push(...newData)
    }
    
    favoriteHasMore.value = favoriteList.value.length < total
    favoritePageNum.value++
  } catch (error) {
    console.error('加载收藏列表错误:', error)
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    list.value = []
    hasMore.value = true
  }

  if (!hasMore.value || loading.value) return

  if (activeTab.value === 'models') {
    await loadModels()
  } else if (activeTab.value === 'tools') {
    await loadTools()
  } else if (activeTab.value === 'favorites') {
    if (reset) {
      favoritePageNum.value = 1
      favoriteList.value = []
      favoriteHasMore.value = true
    }
    if (!favoriteHasMore.value || loading.value) return
    await loadFavorites()
  }
}

const handleVisibilityChange = () => {
  // 只需要重新过滤，不需要重新加载数据
}

const loadMore = () => {
  loadData(false)
}

const handleScroll = (e) => {
  if (scrollTimer) return
  
  scrollTimer = setTimeout(() => {
    const { scrollTop, scrollHeight, clientHeight } = e.target
    if (scrollTop + clientHeight >= scrollHeight - 200) {
      loadMore()
    }
    scrollTimer = null
  }, 200)
}

const goToDetail = (item) => {
  const type = activeTab.value === 'models' ? 'model' : 'tool'
  router.push({
    path: `/square/detail/${type}/${item.id}`,
    query: { fromMyProfile: 'true' }
  })
}

const goToFavoriteDetail = (item) => {
  const type = item.itemType === 'MODEL' ? 'model' : 'tool'
  router.push({
    path: `/square/detail/${type}/${item.itemId}`,
    query: { fromMyProfile: 'true' }
  })
}

const handleShowGuide = () => {
  if (showWelcomeGuide) {
    showWelcomeGuide()
  }
}

onMounted(() => {
  loadCounts()
  loadData(true)
})

watch(activeTab, (newTab, oldTab) => {
  if (newTab !== oldTab) {
    loadData(true)
  }
})

onUnmounted(() => {
})
</script>

<style scoped>
.my-profile {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  position: relative;
  overflow: hidden;
}

.my-profile::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 30% 0%, rgba(64, 158, 255, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 100%, rgba(255, 119, 198, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.my-profile > * {
  position: relative;
  z-index: 1;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes glowPulse {
  0%, 100% { box-shadow: 0 0 10px rgba(64, 158, 255, 0.4), 0 0 20px rgba(64, 158, 255, 0.2); }
  50% { box-shadow: 0 0 25px rgba(64, 158, 255, 0.6), 0 0 50px rgba(64, 158, 255, 0.3); }
}

.profile-header {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.3), rgba(103, 194, 58, 0.2), rgba(255, 119, 198, 0.2));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  padding: 30px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
  animation: slideUp 0.6s ease-out;
}

.profile-header::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(ellipse at center, rgba(64, 158, 255, 0.2) 0%, transparent 60%);
  animation: float 6s ease-in-out infinite;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.avatar {
  border: 3px solid rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.15);
  font-size: 28px;
  font-weight: bold;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3), 0 0 20px rgba(64, 158, 255, 0.3);
  transition: all 0.4s ease;
  animation: glowPulse 3s infinite;
}

.avatar:hover {
  transform: scale(1.1) rotate(5deg);
}

.user-details {
  flex: 1;
}

.guide-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  opacity: 0.8;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.guide-btn:hover {
  opacity: 1;
  background: rgba(64, 158, 255, 0.3);
  transform: scale(1.1) rotate(10deg);
}

.nickname {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.username {
  margin: 6px 0 0;
  font-size: 14px;
  opacity: 0.8;
}

.profile-stats {
  display: flex;
  gap: 32px;
  position: relative;
  z-index: 1;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.4);
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(135deg, #fff, #e0e0e0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 4px;
}

.content-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tab-bar {
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tab-bar :deep(.el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  min-width: 90px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.tab-bar :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.4), rgba(64, 158, 255, 0.2));
  border-color: rgba(64, 158, 255, 0.5);
  color: #fff;
  box-shadow: 0 0 20px rgba(64, 158, 255, 0.4);
}

@media (min-width: 769px) {
  .tab-bar {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}

.content-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.content-list::-webkit-scrollbar {
  width: 8px;
}

.content-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.content-list::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.5), rgba(103, 194, 58, 0.5));
  border-radius: 4px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.item-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  display: flex;
  flex-direction: column;
  animation: slideUp 0.5s ease-out;
  animation-fill-mode: backwards;
}

.item-card:nth-child(1) { animation-delay: 0.05s; }
.item-card:nth-child(2) { animation-delay: 0.1s; }
.item-card:nth-child(3) { animation-delay: 0.15s; }
.item-card:nth-child(4) { animation-delay: 0.2s; }
.item-card:nth-child(5) { animation-delay: 0.25s; }
.item-card:nth-child(6) { animation-delay: 0.3s; }

.item-card:hover {
  transform: translateY(-10px) rotateX(3deg);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 30px rgba(64, 158, 255, 0.3);
  border-color: rgba(64, 158, 255, 0.4);
}

.cover-wrapper {
  position: relative;
  width: 100%;
  padding-top: 75%;
  overflow: hidden;
}

.item-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 20px 20px 0 0;
  overflow: hidden;
  transition: all 0.5s ease;
}

.item-card:hover .item-cover {
  transform: scale(1.1);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.5s ease;
}

.item-card:hover .cover-img {
  filter: brightness(1.1) saturate(1.2);
}

.placeholder-cover {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(103, 194, 58, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  font-size: 56px;
  color: rgba(255, 255, 255, 0.4);
  filter: drop-shadow(0 0 20px rgba(64, 158, 255, 0.5));
  transition: all 0.4s ease;
}

.item-card:hover .cover-icon {
  transform: scale(1.2) rotate(10deg);
  filter: drop-shadow(0 0 30px rgba(64, 158, 255, 0.8));
}

.cover-meta {
  position: absolute;
  bottom: 10px;
  left: 10px;
  right: 10px;
  display: flex;
  justify-content: space-between;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s ease;
}

.item-card:hover .cover-meta {
  background: rgba(0, 0, 0, 0.75);
  border-color: rgba(64, 158, 255, 0.3);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #fff;
}

.item-info {
  padding: 18px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.item-card:hover .item-name {
  color: #409eff;
}

.item-price {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, #f56c6c, #ff9a9e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
}

.item-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: auto;
}

.manufacturer {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.seller-status {
  display: flex;
  gap: 8px;
}

.seller-status :deep(.el-tag) {
  border-radius: 8px;
  border: none;
  font-size: 11px;
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

.loading-tip,
.no-more-tip {
  text-align: center;
  padding: 20px 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

@media (max-width: 768px) {
  .profile-header {
    padding: 20px;
  }

  .profile-info {
    gap: 14px;
  }

  .avatar {
    width: 56px !important;
    height: 56px !important;
    font-size: 20px;
  }

  .nickname {
    font-size: 20px;
  }

  .profile-stats {
    gap: 12px;
  }

  .stat-item {
    padding: 8px 14px;
    border-radius: 12px;
  }

  .stat-value {
    font-size: 22px;
  }

  .stat-label {
    font-size: 11px;
  }

  .content-list {
    padding: 14px;
  }

  .card-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 14px;
  }

  .cover-wrapper {
    padding-top: 100%;
  }

  .cover-icon {
    font-size: 40px;
  }

  .item-info {
    padding: 14px;
  }

  .item-name {
    font-size: 14px;
  }

  .item-price {
    font-size: 18px;
  }
}
</style>
