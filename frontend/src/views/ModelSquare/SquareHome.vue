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
              <span class="seller">👤 {{ item.nickname || item.username || '未知卖家' }}</span>
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
import { View, Star, Picture } from '@element-plus/icons-vue'
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
  background: #f5f7fa;
}

/* 搜索栏 */
.search-bar {
  padding: 16px;
  background: #fff;
  display: flex;
  gap: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.search-bar .el-input {
  flex: 1;
}

/* Tab栏 */
.tab-bar {
  padding: 12px 16px;
  background: #fff;
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 排序栏 */
.sort-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-label {
  font-size: 13px;
  color: #909399;
}

/* 内容列表 */
.content-list {
  padding: 16px;
  height: calc(100vh - 180px);
  overflow-y: auto;
}

/* PC端优化 */
@media (min-width: 769px) {
  .content-list {
    height: calc(100vh - 140px);
  }
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
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
}

.item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  margin-top: auto;
}

.manufacturer {
  width: 50px;
  font-size: 13px;
  color: #606266;
}

.seller {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  padding: 40px 0;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}

.loading-tip,
.no-more-tip {
  text-align: center;
  padding: 16px 0;
  color: #909399;
  font-size: 14px;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .search-bar {
    padding: 12px;
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

  .seller {
    font-size: 10px;
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