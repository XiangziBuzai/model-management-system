<template>
  <div class="user-profile" v-loading="loading">
    <div v-if="userInfo" class="profile-header">
      <div class="profile-info">
        <el-avatar :size="64" :src="userInfo.avatar || defaultAvatar" class="avatar">
          {{ userInfo.nickname?.charAt(userInfo.nickname.length - 1) || 'U' }}
        </el-avatar>
        <div class="user-details">
          <h2 class="nickname">{{ userInfo.nickname || userInfo.username }}</h2>
          <p class="username">@{{ userInfo.username }}</p>
        </div>
      </div>
      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-value">{{ modelCount }}</span>
          <span class="stat-label">模型</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ toolCount }}</span>
          <span class="stat-label">工具</span>
        </div>
        <div class="stat-item" v-if="showFavoriteTab">
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
          <el-radio-button label="favorites" v-if="showFavoriteTab">
            <el-icon><StarFilled /></el-icon>
            收藏
          </el-radio-button>
        </el-radio-group>
        <!-- <el-radio-group v-model="visibilityTab" size="large" @change="handleVisibilityChange" v-if="activeTab !== 'favorites'">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="public">公开</el-radio-button>
        </el-radio-group> -->
      </div>

      <div class="content-list" v-loading="contentLoading" ref="contentListRef" @scroll="handleScroll">
        <div v-if="activeTab === 'favorites' && showFavoriteTab">
          <div v-if="favoriteList.length === 0 && !contentLoading" class="empty-state">
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

          <div v-if="contentLoading" class="loading-tip">
            <span>加载中...</span>
          </div>
          <div v-else-if="!favoriteHasMore && favoriteList.length > 0" class="no-more-tip">
            <span>没有更多了</span>
          </div>
        </div>

        <div v-else>
          <div v-if="filteredList.length === 0 && !contentLoading" class="empty-state">
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
                    <el-tag type="warning" size="small">
                      公开
                    </el-tag>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="contentLoading" class="loading-tip">
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
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Star, Picture, StarFilled } from '@element-plus/icons-vue'
import { getUserPublicProfile } from '../../api/user'
import { getUserPublicModels, getUserPublicTools } from '../../api/square'
import { getUserPublicFavorites } from '../../api/square'
import { useAuthStore } from '../../stores/useAuthStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const contentLoading = ref(false)
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
const userInfo = ref(null)
const showFavoriteTab = ref(false)
let scrollTimer = null

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const filteredList = computed(() => {
  if (visibilityTab.value === 'all') {
    return list.value
  }
  return list.value.filter(item => item.isPublic === 1)
})

const loadUserInfo = async () => {
  loading.value = true
  try {
    const userId = route.params.userId
    const data = await getUserPublicProfile(userId)
    userInfo.value = data
    
    showFavoriteTab.value = data.isPublicFavorite === 1
  } catch (error) {
    ElMessage.error(error.message || '加载用户信息失败')
    setTimeout(() => {
      router.back()
    }, 2000)
  } finally {
    loading.value = false
  }
}

const loadCounts = async () => {
  try {
    const userId = route.params.userId
    const [modelResult, toolResult] = await Promise.all([
      getUserPublicModels(userId, { pageNum: 1, pageSize: 1 }),
      getUserPublicTools(userId, { pageNum: 1, pageSize: 1 })
    ])
    modelCount.value = modelResult.total || 0
    toolCount.value = toolResult.total || 0
    
    if (showFavoriteTab.value) {
      try {
        const favoriteResult = await getUserPublicFavorites(userId, { pageNum: 1, pageSize: 1 })
        const favoriteData = favoriteResult.data || favoriteResult
        favoriteCount.value = favoriteData.total || 0
      } catch (error) {
        console.error('加载收藏数量失败:', error)
        favoriteCount.value = 0
      }
    }
  } catch (error) {
    console.error('加载数量错误:', error)
  }
}

const loadModels = async () => {
  contentLoading.value = true
  try {
    const userId = route.params.userId
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const result = await getUserPublicModels(userId, params)
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
    contentLoading.value = false
  }
}

const loadTools = async () => {
  contentLoading.value = true
  try {
    const userId = route.params.userId
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const result = await getUserPublicTools(userId, params)
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
    contentLoading.value = false
  }
}

const loadFavorites = async () => {
  contentLoading.value = true
  try {
    const userId = route.params.userId
    const params = {
      pageNum: favoritePageNum.value,
      pageSize: favoritePageSize.value
    }
    const res = await getUserPublicFavorites(userId, params)
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
    contentLoading.value = false
  }
}

const loadData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    list.value = []
    hasMore.value = true
  }

  if (!hasMore.value || contentLoading.value) return

  if (activeTab.value === 'models') {
    await loadModels()
  } else if (activeTab.value === 'tools') {
    await loadTools()
  }
}

const loadFavoriteData = async (reset = false) => {
  if (reset) {
    favoritePageNum.value = 1
    favoriteList.value = []
    favoriteHasMore.value = true
  }

  if (!favoriteHasMore.value || contentLoading.value) return

  await loadFavorites()
}

const handleVisibilityChange = () => {
}

const loadMore = () => {
  if (activeTab.value === 'favorites' && showFavoriteTab.value) {
    loadFavoriteData(false)
  } else {
    loadData(false)
  }
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
  router.push(`/square/detail/${type}/${item.id}`)
}

const goToFavoriteDetail = (item) => {
  const type = item.itemType === 'MODEL' ? 'model' : 'tool'
  router.push(`/square/detail/${type}/${item.itemId}`)
}

watch(activeTab, (newTab, oldTab) => {
  if (newTab !== oldTab) {
    if (newTab === 'favorites') {
      loadFavoriteData(true)
    } else {
      loadData(true)
    }
  }
})

watch(showFavoriteTab, (newVal) => {
  if (newVal && activeTab.value === 'favorites') {
    loadFavoriteData(true)
  }
})

onMounted(async () => {
  await loadUserInfo()
  if (userInfo.value) {
    await loadCounts()
    loadData(true)
  }
})

onUnmounted(() => {
})
</script>

<style scoped>
.user-profile {
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
