<template>
  <el-dialog
    v-model="visible"
    title="价格变动提醒"
    :width="dialogWidth"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="alerts.length === 0" class="empty-state">
      <el-empty description="暂无价格变动提醒" />
    </div>
    
    <div v-else class="alert-list">
      <div 
        v-for="alert in alerts" 
        :key="alert.id" 
        class="alert-item"
      >
        <div class="alert-info">
          <div class="item-name">{{ alert.itemName || '未知物品' }}</div>
          <div class="price-info">
            <span class="label">原价：</span>
            <span class="price old-price">¥{{ formatPrice(alert.lastPrice) }}</span>
            <span class="label ml-2">现价：</span>
            <span :class="['price', getPriceClass(alert)]">¥{{ formatPrice(alert.currentPrice) }}</span>
            <span :class="['change', getPriceClass(alert)]">
              {{ alert.priceChange >= 0 ? '↑' : '↓' }}
              {{ Math.abs(alert.priceChangePercent).toFixed(2) }}%
            </span>
          </div>
          <div class="item-type">{{ alert.itemType === 'MODEL' ? '模型' : '工具' }}</div>
        </div>
        <div v-if="alert.itemImage" class="item-image">
          <img :src="alert.itemImage" :alt="alert.itemName" class="preview-img" />
        </div>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">知道了</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getPriceAlerts, markPriceAlertsNotified } from '../api/square'

const visible = ref(false)
const alerts = ref([])
const dialogWidth = ref('400px')
let checkInterval = null

const formatPrice = (price) => {
  if (!price) return '0.00'
  return typeof price === 'number' ? price.toFixed(2) : price.toString()
}

const getPriceClass = (alert) => {
  if (!alert.priceChange) return 'normal-price'
  return alert.priceChange >= 0 ? 'price-up' : 'price-down'
}

const isLoggedIn = () => {
  return localStorage.getItem('token') !== null
}

const loadPriceAlerts = async () => {
  // 如果用户未登录，不发起请求
  if (!isLoggedIn()) {
    return
  }
  
  try {
    const res = await getPriceAlerts()
    const data = res.data || res
    if (data.alerts && data.alerts.length > 0) {
      alerts.value = data.alerts
      visible.value = true
    }
  } catch (error) {
    console.error('加载价格提醒失败:', error)
  }
}

const handleClose = async () => {
  visible.value = false
  // 标记为已通知
  if (alerts.value.length > 0) {
    const alertIds = alerts.value.map(a => a.id)
    try {
      await markPriceAlertsNotified(alertIds)
      alerts.value = []
    } catch (error) {
      console.error('标记已通知失败:', error)
    }
  }
}

const startCheckTimer = () => {
  // 页面加载时立即检查一次
  loadPriceAlerts()
  
  // 每5分钟检查一次
  checkInterval = setInterval(() => {
    loadPriceAlerts()
  }, 5 * 60 * 1000)
}

const stopCheckTimer = () => {
  if (checkInterval) {
    clearInterval(checkInterval)
    checkInterval = null
  }
}

onMounted(() => {
  startCheckTimer()
})

onUnmounted(() => {
  stopCheckTimer()
})
</script>

<style scoped>
.alert-list {
  max-height: 400px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.alert-info {
  flex: 1;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.price-info {
  font-size: 14px;
  margin-bottom: 4px;
}

.label {
  color: #909399;
}

.price {
  font-weight: 600;
}

.old-price {
  color: #909399;
  text-decoration: line-through;
}

.normal-price {
  color: #606266;
}

.price-up {
  color: #f56c6c;
}

.price-down {
  color: #67c23a;
}

.change {
  margin-left: 8px;
  font-weight: 600;
}

.item-type {
  font-size: 12px;
  color: #909399;
}

.item-image {
  margin-left: 16px;
}

.preview-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.empty-state {
  padding: 40px 0;
}

.ml-2 {
  margin-left: 8px;
}
</style>
