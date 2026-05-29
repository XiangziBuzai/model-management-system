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
  background: #f5f7fa;
}

.profile-header {
  background: linear-gradient(135deg, #409eff, #79bbff);
  padding: 24px;
  color: #fff;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.avatar {
  border: 3px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.2);
  font-size: 24px;
  font-weight: bold;
}

.user-details {
  flex: 1;
}

.guide-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  opacity: 0.8;
}

.guide-btn:hover {
  opacity: 1;
}

.nickname {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.username {
  margin: 4px 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.profile-stats {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.content-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tab-bar {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  padding: 16px;
  overflow-y: auto;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.item-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.cover-wrapper {
  position: relative;
  width: 100%;
  padding-top: 75%;
}

.item-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 12px 12px 0 0;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-cover {
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  font-size: 48px;
  color: #c0c4cc;
}

.cover-meta {
  position: absolute;
  bottom: 8px;
  left: 8px;
  right: 8px;
  display: flex;
  justify-content: space-between;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 6px;
  padding: 6px 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #fff;
}

.item-info {
  padding: 14px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
  margin-bottom: 10px;
}

.item-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: auto;
}

.manufacturer {
  font-size: 13px;
  color: #606266;
}

.seller-status {
  display: flex;
  gap: 6px;
}

.empty-state {
  padding: 40px 0;
}

.loading-tip,
.no-more-tip {
  text-align: center;
  padding: 16px 0;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 768px) {
  .profile-header {
    padding: 16px;
  }

  .profile-info {
    gap: 12px;
  }

  .avatar {
    width: 48px !important;
    height: 48px !important;
    font-size: 18px;
  }

  .nickname {
    font-size: 18px;
  }

  .profile-stats {
    gap: 16px;
  }

  .stat-value {
    font-size: 20px;
  }

  .content-list {
    padding: 12px;
  }

  .card-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }

  .cover-wrapper {
    padding-top: 100%;
  }

  .cover-icon {
    font-size: 32px;
  }

  .item-info {
    padding: 10px;
  }

  .item-name {
    font-size: 13px;
  }

  .item-price {
    font-size: 15px;
  }

  .manufacturer {
    font-size: 11px;
  }

  .seller-status :deep(.el-tag) {
    font-size: 10px;
    padding: 0 4px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .card-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 14px;
  }
}

@media (min-width: 1025px) {
  .card-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 18px;
  }
}
</style>
