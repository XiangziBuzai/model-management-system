<template>
  <div class="my-orders">
    <!-- Tab切换 -->
    <div class="tab-bar">
      <el-radio-group v-model="activeTab" size="large">
        <el-radio-button label="purchases">购买记录</el-radio-button>
        <el-radio-button label="sales">销售记录</el-radio-button>
      </el-radio-group>
    </div>

    <div class="order-list" v-loading="loading">
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无订单" />
      </div>

      <div v-for="order in list" :key="order.id" class="order-card">
        <div class="order-header">
          <span class="order-no">订单号: {{ order.orderNo }}</span>
          <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
        </div>
        
        <div class="order-body">
          <div class="item-info">
            <span class="item-type">{{ order.itemType === 'MODEL' ? '模型' : '工具' }}</span>
            <h3 class="item-name">{{ order.itemName }}</h3>
          </div>
          <div class="order-price">¥{{ order.price }}</div>
          <div class="order-time">下单时间: {{ formatDate(order.createdAt) }}</div>
          <div v-if="order.remark" class="order-remark">备注: {{ order.remark }}</div>
          
          <!-- 物流信息 -->
          <div v-if="order.trackingNumber" class="shipping-info">
            <div class="shipping-item">
              <span class="label">快递公司:</span>
              <span class="value">{{ order.expressCompany || '未填写' }}</span>
            </div>
            <div class="shipping-item">
              <span class="label">快递单号:</span>
              <span class="value">{{ order.trackingNumber }}</span>
            </div>
          </div>
        </div>

        <div class="order-actions">
          <!-- 卖家操作：待发货状态显示去发货按钮 -->
          <el-button 
            v-if="activeTab === 'sales' && order.status === 'PENDING'" 
            type="primary" 
            size="small" 
            @click="handleShip(order)"
          >
            去发货
          </el-button>
          
          <!-- 买家操作：已发货状态显示确认收货按钮 -->
          <el-button 
            v-if="activeTab === 'purchases' && order.status === 'SHIPPED'" 
            type="success" 
            size="small" 
            @click="handleConfirmReceipt(order.id)"
          >
            确认收货
          </el-button>
          
          <!-- 取消订单：只有待发货状态可以取消 -->
          <el-button 
            v-if="order.status === 'PENDING'" 
            type="danger" 
            size="small" 
            @click="handleCancel(order.id)"
          >
            取消订单
          </el-button>
        </div>
      </div>

      <div v-if="hasMore" class="load-more">
        <el-button @click="loadMore" :loading="loading">加载更多</el-button>
      </div>
    </div>

    <!-- 发货弹窗 -->
    <el-dialog 
      v-model="showShipDialog" 
      title="填写发货信息" 
      :width="dialogWidth"
      class="ship-dialog"
    >
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="快递单号" required>
          <el-input 
            v-model="shipForm.trackingNumber" 
            placeholder="请输入快递单号" 
            @input="handleTrackingNumberInput"
          />
        </el-form-item>
        <el-form-item label="快递公司">
          <el-select 
            v-model="shipForm.expressCompany" 
            placeholder="请选择或手动输入快递公司" 
            filterable
            allow-create
            default-first-option
            style="width: 100%"
          >
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="京东物流" value="京东物流" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="邮政EMS" value="邮政EMS" />
            <el-option label="德邦快递" value="德邦快递" />
            <el-option label="极兔速递" value="极兔速递" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showShipDialog = false">取消</el-button>
        <el-button type="primary" @click="submitShip" :loading="shipping">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyPurchases, getMySales, cancelOrder, shipOrder, confirmReceipt } from '../../api/square'

const loading = ref(false)
const activeTab = ref('purchases')
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

// 发货弹窗相关
const showShipDialog = ref(false)
const shipping = ref(false)
const currentOrderId = ref(null)
const shipForm = ref({
  trackingNumber: '',
  expressCompany: ''
})

// 计算对话框宽度
const dialogWidth = computed(() => {
  return window.innerWidth < 768 ? '90%' : '500px'
})

const loadData = async (reset = false) => {
  if (reset) {
    pageNum.value = 1
    list.value = []
    hasMore.value = true
  }

  if (!hasMore.value || loading.value) return

  loading.value = true
  try {
    let res
    if (activeTab.value === 'purchases') {
      res = await getMyPurchases({
        pageNum: pageNum.value,
        pageSize: pageSize.value
      })
    } else {
      res = await getMySales({
        pageNum: pageNum.value,
        pageSize: pageSize.value
      })
    }

    // 安全地获取数据
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
    console.error('加载订单列表错误:', error)
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrder(orderId)
    ElMessage.success('订单已取消')
    loadData(true)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 打开发货弹窗
const handleShip = (order) => {
  currentOrderId.value = order.id
  shipForm.value = {
    trackingNumber: '',
    expressCompany: ''
  }
  showShipDialog.value = true
}

// 快递单号输入处理（自动识别快递公司）
const handleTrackingNumberInput = (value) => {
  // 简单的快递单号识别规则
  if (!value) {
    shipForm.value.expressCompany = ''
    return
  }
  
  // 根据单号前缀识别快递公司
  const prefixes = {
    'SF': '顺丰速运',
    'JD': '京东物流',
    'YT': '圆通速递',
    'ZT': '中通快递',
    'ST': '申通快递',
    'YD': '韵达快递',
    'DB': '德邦快递',
    'JT': '极兔速递'
  }
  
  const upperValue = value.toUpperCase()
  for (const [prefix, company] of Object.entries(prefixes)) {
    if (upperValue.startsWith(prefix)) {
      shipForm.value.expressCompany = company
      break
    }
  }
}

// 提交发货信息
const submitShip = async () => {
  if (!shipForm.value.trackingNumber || !shipForm.value.trackingNumber.trim()) {
    ElMessage.warning('请输入快递单号')
    return
  }
  
  shipping.value = true
  try {
    await shipOrder(currentOrderId.value, shipForm.value)
    ElMessage.success('发货成功')
    showShipDialog.value = false
    loadData(true)
  } catch (error) {
    ElMessage.error(error.message || '发货失败')
  } finally {
    shipping.value = false
  }
}

// 确认收货
const handleConfirmReceipt = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认已收到货物吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    await confirmReceipt(orderId)
    ElMessage.success('确认收货成功')
    loadData(true)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const getStatusType = (status) => {
  const map = {
    'PENDING': '',
    'SHIPPED': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待发货',
    'SHIPPED': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const loadMore = () => {
  loadData(false)
}

watch(activeTab, () => {
  loadData(true)
})

onMounted(() => {
  loadData(true)
})
</script>

<style scoped>
.my-orders {
  min-height: 100vh;
  background: linear-gradient(180deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.my-orders::before {
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

.my-orders > * {
  position: relative;
  z-index: 1;
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

@keyframes pulseGlow {
  0%, 100% {
    box-shadow: 0 0 5px rgba(64, 158, 255, 0.3);
  }
  50% {
    box-shadow: 0 0 20px rgba(64, 158, 255, 0.6);
  }
}

.tab-bar {
  margin-bottom: 20px;
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

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: slideUp 0.5s ease-out;
  animation-fill-mode: backwards;
}

.order-card:nth-child(1) { animation-delay: 0.05s; }
.order-card:nth-child(2) { animation-delay: 0.1s; }
.order-card:nth-child(3) { animation-delay: 0.15s; }
.order-card:nth-child(4) { animation-delay: 0.2s; }
.order-card:nth-child(5) { animation-delay: 0.25s; }

.order-card:hover {
  transform: translateY(-5px) scale(1.01);
  box-shadow: 0 15px 45px rgba(0, 0, 0, 0.3), 0 0 25px rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.3);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.order-no {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  font-family: monospace;
  letter-spacing: 1px;
}

.order-header :deep(.el-tag) {
  border-radius: 12px;
  padding: 4px 12px;
  border: none;
  font-weight: 500;
}

.order-body {
  margin-bottom: 16px;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
}

.item-type {
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: #fff;
  padding: 6px 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 10px rgba(64, 158, 255, 0.3);
}

.item-name {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.order-price {
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(135deg, #f56c6c, #ff9a9e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
  filter: drop-shadow(0 2px 4px rgba(245, 108, 108, 0.3));
}

.order-time, .order-remark {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 6px;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  gap: 12px;
}

.order-actions .el-button {
  border-radius: 16px;
  padding: 10px 20px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.order-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.order-actions .el-button--primary {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.4), rgba(64, 158, 255, 0.2));
  border-color: rgba(64, 158, 255, 0.5);
}

.order-actions .el-button--primary:hover {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.6), rgba(64, 158, 255, 0.4));
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.4);
}

.order-actions .el-button--success {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.4), rgba(103, 194, 58, 0.2));
  border-color: rgba(103, 194, 58, 0.5);
}

.order-actions .el-button--success:hover {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.6), rgba(103, 194, 58, 0.4));
  box-shadow: 0 4px 20px rgba(103, 194, 58, 0.4);
}

.order-actions .el-button--danger {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.4), rgba(245, 108, 108, 0.2));
  border-color: rgba(245, 108, 108, 0.5);
}

.order-actions .el-button--danger:hover {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.6), rgba(245, 108, 108, 0.4));
  box-shadow: 0 4px 20px rgba(245, 108, 108, 0.4);
}

.empty-state {
  padding: 60px 0;
}

.empty-state :deep(.el-empty__description) {
  color: rgba(255, 255, 255, 0.6);
}

.load-more {
  text-align: center;
  padding: 24px 0;
}

.shipping-info {
  margin-top: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.shipping-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
}

.shipping-item:last-child {
  margin-bottom: 0;
}

.shipping-item .label {
  color: rgba(255, 255, 255, 0.5);
  min-width: 90px;
}

.shipping-item .value {
  color: #fff;
  font-weight: 600;
}

@media (max-width: 768px) {
  .my-orders {
    padding: 14px;
  }

  .order-card {
    padding: 16px;
    border-radius: 16px;
  }

  .order-price {
    font-size: 20px;
  }

  .order-actions {
    flex-direction: column;
    gap: 8px;
  }

  .order-actions .el-button {
    width: 100%;
  }
}
</style>
