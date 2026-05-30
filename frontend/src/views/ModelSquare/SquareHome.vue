<template>
  <div class="square-home">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索模型或工具..."
        prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- Tab切换和排序 -->
    <div class="tab-bar">
      <el-radio-group v-model="activeTab" size="large" @change="handleTabChange">
        <el-radio-button label="models">模型</el-radio-button>
        <el-radio-button label="tools">工具</el-radio-button>
      </el-radio-group>
      
      <!-- 排序选项 -->
      <div class="sort-bar">
        <span class="sort-label">排序：</span>
        <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
          <el-radio-button label="newest">最新</el-radio-button>
          <el-radio-button label="hot">最热</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 列表内容 -->
    <div class="content-list" v-loading="loading" ref="contentListRef" @scroll="handleScroll">
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无数据" />
      </div>

      <div class="card-grid">
        <div v-for="item in list" :key="item.id" class="item-card" @click="goToDetail(item)">
          <div class="cover-wrapper">
            <div class="item-cover" v-if="item.cover">
              <img :src="item.cover" :alt="item.name" class="cover-img" />
            </div>
            <div class="item-cover placeholder-cover" v-else>
              <el-icon class="cover-icon"><Picture /></el-icon>
            </div>
            <div class="cover-meta">
              <span v-if="item.viewCount !== undefined" class="meta-item"><el-icon><View /></el-icon>{{ item.viewCount }}</span>
              <span v-if="item.favoriteCount !== undefined" class="meta-item"><el-icon><Star /></el-icon>{{ item.favoriteCount }}</span>
            </div>
          </div>
          
          <div class="item-info">
            <div class="item-meta">
              <h3 class="item-name">{{ item.name }}</h3>
              <span class="manufacturer" v-if="item.type === 'model'">{{ item.manufacturerName || '未知厂家' }}</span>
            </div>
            <div class="item-meta">
              <span class="seller"><el-icon><User /></el-icon> {{ item.nickname || item.username || '未知卖家' }}</span>
              <span class="item-price">¥{{ item.price }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态提示 -->
      <div v-if="loading" class="loading-tip">
        <span>加载中...</span>
      </div>
      <div v-else-if="!hasMore && list.length > 0" class="no-more-tip">
        <span>没有更多了</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicModels, getPublicTools } from '../../api/square'
import { View, Star, Picture, User } from '@element-plus/icons-vue'
import { useSquareStore } from '../../stores/useSquareStore'

const router = useRouter()
const squareStore = useSquareStore()
const loading = ref(false)
const keyword = ref('')
const activeTab = ref('models')
const sortBy = ref('newest')
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)
const contentListRef = ref(null)
let scrollTimer = null

// 加载数据
const loadData = async (reset = false) => {
  if (reset && reset !== 'keep-page') {
    pageNum.value = 1
    list.value = []
    hasMore.value = true
  }

  if (!hasMore.value || loading.value) return

  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      sortBy: sortBy.value
    }

    let res
    if (activeTab.value === 'models') {
      res = await getPublicModels(params)
    } else {
      res = await getPublicTools(params)
    }

    const data = res.data || res
    const records = data.records || []
    const total = data.total || 0
    
    if (reset && reset !== 'keep-page') {
      list.value = records
    } else {
      list.value.push(...records)
    }

    hasMore.value = list.value.length < total
    pageNum.value++
  } catch (error) {
    console.error('加载数据错误:', error)
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 从详情页返回时加载到指定页码
const loadToSavedPage = async (targetPage) => {
  loading.value = true
  list.value = []
  hasMore.value = true
  pageNum.value = 1
  
  let total = 0
  
  const requests = []
  for (let i = 1; i <= targetPage && i <= 10; i++) {
    const params = {
      pageNum: i,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      sortBy: sortBy.value
    }
    requests.push(
      activeTab.value === 'models' ? getPublicModels(params) : getPublicTools(params)
    )
  }
  
  try {
    const results = await Promise.all(requests)
    results.forEach(res => {
      const data = res.data || res
      const records = data.records || []
      total = data.total || total
      list.value.push(...records)
    })
    pageNum.value = targetPage + 1
    hasMore.value = list.value.length < total
  } catch (error) {
    console.error('加载数据错误:', error)
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  loadData(true)
}

// Tab切换
const handleTabChange = () => {
  loadData(true)
}

// 排序切换
const handleSortChange = () => {
  loadData(true)
}

// 加载更多
const loadMore = () => {
  loadData(false)
}

// 滚动处理（带防抖）
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

// 跳转到详情页
const goToDetail = (item) => {
  const scrollTop = contentListRef.value?.scrollTop || 0
  squareStore.saveListState(activeTab.value, sortBy.value, keyword.value, pageNum.value, pageSize.value, scrollTop)
  const type = activeTab.value === 'models' ? 'model' : 'tool'
  router.push(`/square/detail/${type}/${item.id}`)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 格式化备注（转换图片标记为实际图片，限制显示长度）
const formatRemark = (remark) => {
  if (!remark) return '暂无备注'
  
  const maxLength = 15
  let text = remark
  
  const textOnly = remark.replace(/\[image:[^\]]+\]/g, '[图片]')
  
  if (textOnly.length > maxLength) {
    let charsRemaining = maxLength
    let result = ''
    let i = 0
    
    while (i < text.length && charsRemaining > 0) {
      if (text.slice(i).startsWith('[image:')) {
        const endIndex = text.indexOf(']', i)
        if (endIndex !== -1) {
          const imgTag = text.slice(i, endIndex + 1)
          result += imgTag
          i = endIndex + 1
          charsRemaining -= 4
        } else {
          break
        }
      } else {
        result += text[i]
        i++
        charsRemaining--
      }
    }
    
    text = result + '...'
  }
  
  return text.replace(/\[image:([^\]]+)\]/g, '<img src="$1" style="max-width: 100%; max-height: 150px; border-radius: 4px;"/>')
}

let pendingScrollTop = 0

onMounted(() => {
  if (squareStore.getIsFromDetail()) {
    const savedState = squareStore.restoreListState()
    activeTab.value = savedState.activeTab
    sortBy.value = savedState.sortBy
    keyword.value = savedState.keyword
    pageNum.value = savedState.pageNum
    pageSize.value = savedState.pageSize
    pendingScrollTop = savedState.scrollTop || 0
    
    const restoreScroll = () => {
      if (contentListRef.value && pendingScrollTop > 0) {
        contentListRef.value.scrollTop = pendingScrollTop
      }
    }
    
    const loadAndRestore = async () => {
      if (pageNum.value > 1) {
        await loadToSavedPage(pageNum.value - 1)
      } else {
        await loadData(true)
      }
      
      setTimeout(() => {
        restoreScroll()
        setTimeout(() => {
          restoreScroll()
        }, 50)
      }, 100)
    }
    loadAndRestore()
  } else {
    squareStore.resetListState()
    activeTab.value = 'models'
    sortBy.value = 'newest'
    keyword.value = ''
    pageNum.value = 1
    pageSize.value = 10
    loadData(true)
  }
})

onUnmounted(() => {
})

watch(activeTab, () => {
})

watch(sortBy, () => {
})

watch(list, () => {
  if (pendingScrollTop > 0 && contentListRef.value && list.value.length > 0) {
    setTimeout(() => {
      contentListRef.value.scrollTop = pendingScrollTop
    }, 50)
  }
}, { deep: true })
</script>

<style scoped>
.square-home {
  min-height: 100%;
  background: linear-gradient(180deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  position: relative;
  overflow: hidden;
}

.square-home::before {
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

.square-home > * {
  position: relative;
  z-index: 1;
}

@keyframes gradientFlow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes floatCard {
  0%, 100% { transform: translateY(0px) rotateX(0deg); }
  50% { transform: translateY(-5px) rotateX(2deg); }
}

@keyframes glowPulse {
  0%, 100% { box-shadow: 0 0 5px rgba(64, 158, 255, 0.3), 0 0 10px rgba(64, 158, 255, 0.2); }
  50% { box-shadow: 0 0 20px rgba(64, 158, 255, 0.6), 0 0 40px rgba(64, 158, 255, 0.3); }
}

@keyframes shimmerEffect {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@keyframes borderGlow {
  0%, 100% { border-color: rgba(64, 158, 255, 0.3); }
  50% { border-color: rgba(64, 158, 255, 0.8); }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-bar {
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  gap: 16px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  overflow: hidden;
}

.search-bar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.5), transparent);
}

.search-bar .el-input {
  flex: 1;
}

.search-bar .el-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 25px;
  box-shadow: none;
  transition: all 0.4s ease;
  padding: 8px 20px;
}

.search-bar .el-input :deep(.el-input__wrapper:hover),
.search-bar .el-input :deep(.el-input__wrapper:focus) {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(64, 158, 255, 0.5);
  box-shadow: 0 0 20px rgba(64, 158, 255, 0.3);
}

.search-bar .el-input :deep(.el-input__inner) {
  color: #fff;
}

.search-bar .el-input :deep(.el-input__inner)::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.search-bar .el-button {
  background: linear-gradient(135deg, #409eff, #67c23a);
  border: none;
  border-radius: 25px;
  padding: 12px 28px;
  margin-top: 8px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.search-bar .el-button:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.6);
}

.tab-bar {
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  margin-top: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.tab-bar :deep(.el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.tab-bar :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.4), rgba(64, 158, 255, 0.2));
  border-color: rgba(64, 158, 255, 0.5);
  color: #fff;
  box-shadow: 0 0 20px rgba(64, 158, 255, 0.4);
}

.sort-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sort-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.sort-bar :deep(.el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  font-size: 12px;
  padding: 8px 16px;
}

.sort-bar :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.4), rgba(103, 194, 58, 0.2));
  border-color: rgba(103, 194, 58, 0.5);
  color: #fff;
  box-shadow: 0 0 15px rgba(103, 194, 58, 0.4);
}

.content-list {
  padding: 20px;
  height: calc(100vh - 180px);
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

.content-list::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.7), rgba(103, 194, 58, 0.7));
}

@media (min-width: 769px) {
  .content-list {
    height: calc(100vh - 170px);
  }
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
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  transform-style: preserve-3d;
  perspective: 1000px;
  animation: scaleIn 0.5s ease-out;
  animation-fill-mode: backwards;
}

.item-card:nth-child(1) { animation-delay: 0.05s; }
.item-card:nth-child(2) { animation-delay: 0.1s; }
.item-card:nth-child(3) { animation-delay: 0.15s; }
.item-card:nth-child(4) { animation-delay: 0.2s; }
.item-card:nth-child(5) { animation-delay: 0.25s; }
.item-card:nth-child(6) { animation-delay: 0.3s; }

.item-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, transparent 50%, rgba(255, 119, 198, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
  z-index: 1;
}

.item-card:hover::before {
  opacity: 1;
}

.item-card:hover {
  transform: translateY(-12px) rotateX(5deg) rotateY(-5deg);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.4),
    0 0 30px rgba(64, 158, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.4);
}

.item-card:active {
  transform: translateY(-8px) scale(0.98);
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
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.3);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #fff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
}

.meta-item .el-icon {
  transition: all 0.3s ease;
}

.item-card:hover .meta-item .el-icon {
  transform: scale(1.2);
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.8));
}

.item-info {
  padding: 18px;
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 2;
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
  transition: all 0.3s ease;
}

.item-card:hover .item-name {
  color: #409eff;
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.5);
}

.item-price {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, #f56c6c, #ff9a9e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none;
  filter: drop-shadow(0 2px 4px rgba(245, 108, 108, 0.3));
  transition: all 0.4s ease;
}

.item-card:hover .item-price {
  transform: scale(1.05);
  filter: drop-shadow(0 4px 8px rgba(245, 108, 108, 0.5));
}

.item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: auto;
}

.manufacturer {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.1);
  padding: 4px 10px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.item-card:hover .manufacturer {
  background: rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.3);
  color: #409eff;
}

.seller {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}

.item-card:hover .seller {
  color: rgba(255, 255, 255, 0.8);
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

.loading-tip,
.no-more-tip {
  text-align: center;
  padding: 20px 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  position: relative;
}

.loading-tip::before,
.no-more-tip::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 20%;
  right: 20%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.3), transparent);
}

@media (max-width: 768px) {
  .search-bar {
    /* padding: 12px; */
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    gap: 10px;
  }

  .search-bar .el-input :deep(.el-input__wrapper) {
    padding: 2px 12px;
    font-size: 13px;
  }

  .search-bar .el-button {
    padding: 10px 20px;
    font-size: 13px;
    margin-top: 2px;
  }

  .content-list {
    padding: 12px;
  }

  .tab-bar {
    gap: 10px;
    padding: 10px 12px;
    /* flex-direction: column;
    align-items: flex-start; */
  }

  .tab-bar :deep(.el-radio-button__inner) {
    font-size: 12px;
    padding: 6px 14px;
  }

  .sort-bar {
    gap: 8px;
  }

  .sort-label {
    font-size: 12px;
  }

  .sort-bar :deep(.el-radio-button__inner) {
    font-size: 11px;
    padding: 4px 12px;
  }

  .card-grid {
    grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
    gap: 12px;
  }

  .cover-wrapper {
    padding-top: 100%;
  }

  .cover-icon {
    font-size: 36px;
  }

  .item-info {
    padding: 12px;
  }

  .item-name {
    font-size: 13px;
  }

  .item-price {
    font-size: 16px;
  }

  .manufacturer {
    font-size: 10px;
  }

  .seller {
    font-size: 11px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .card-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 18px;
  }
}

@media (min-width: 1025px) {
  .card-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 24px;
  }
}
</style>