<template>
  <div class="square-detail" v-loading="loading">
    <div v-if="item" class="detail-content">
      <!-- 物品信息 -->
      <div class="item-info">
        <div v-if="item.cover" class="item-cover">
          <img :src="item.cover" :alt="item.name" @error="handleCoverError" />
        </div>
        <h1 class="item-name">{{ item.name }}</h1>
        <div class="item-price">¥{{ item.price }}</div>
        <p class="item-remark" v-html="formatRemark(item.remark)"></p>
        <div class="item-meta">
          <span class="item-user" @click="goToUserProfile">
            <el-icon><User /></el-icon>
            {{ item.nickname || '未知用户' }}
          </span>
          <span>发布时间: {{ formatDate(item.createdAt) }}</span>
        </div>
        <div class="item-stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            {{ item.viewCount || 0 }}
          </span>
          <span class="stat-item">
            <el-icon><Star /></el-icon>
            {{ item.favoriteCount || 0 }}
          </span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button 
          :type="isFavorited ? 'warning' : 'primary'" 
          size="large"
          @click="handleFavorite"
        >
          {{ isFavorited ? '取消收藏' : '收藏' }}
        </el-button>
        <el-button 
          v-if="isOwnItem"
          type="warning" 
          size="large" 
          @click="showEditDialog = true"
        >
          编辑
        </el-button>
        <el-button 
          v-if="!isOwnItem"
          type="success" 
          size="large" 
          @click="showBuyDialog = true"
        >
          立即购买
        </el-button>
        <el-button 
          v-if="!isOwnItem"
          type="info" 
          size="large" 
          @click="goToChat"
        >
          联系卖家
        </el-button>
        <el-tag v-if="isOwnItem && !isFromMyProfile" type="info" size="large">这是您发布的物品</el-tag>
      </div>
    </div>

    <!-- 购买对话框 -->
    <el-dialog 
      v-model="showBuyDialog" 
      title="确认购买" 
      :width="dialogWidth"
      class="buy-dialog"
    >
      <el-form :model="orderForm" label-width="80px">
        <el-form-item label="成交价格">
          <el-input-number v-model="orderForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" placeholder="选填" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBuyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleBuy" :loading="buying">确认购买</el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      :title="editDialogTitle" 
      width="600px"
      class="edit-dialog"
      @close="handleEditDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editFormData"
        :rules="editFormRules"
        label-width="100px"
      >
        <el-form-item v-if="itemType === 'model'" label="厂家" prop="manufacturerId">
          <el-select
            v-model="editFormData.manufacturerId"
            placeholder="请选择厂家"
            style="width: 100%"
            filterable
            allow-create
            default-first-option
          >
            <el-option
              v-for="m in manufacturerStore.manufacturers"
              :key="m.id"
              :label="m.name"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="editFormData.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="editFormData.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="封面图片">
          <div class="cover-upload">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :http-request="handleCoverUpload"
              :before-upload="beforeCoverUpload"
              accept="image/*"
            >
              <img v-if="editFormData.cover" :src="editFormData.cover" class="cover-preview" />
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <span v-if="editFormData.cover" class="cover-remove" @click="removeCover">移除</span>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <RichEditor v-model="editFormData.remark" />
        </el-form-item>
        <el-form-item label="是否售出">
          <el-radio-group v-model="editFormData.sold">
            <el-radio :value="0">未售出</el-radio>
            <el-radio :value="1">已售出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否公开">
          <el-radio-group v-model="editFormData.isPublic">
            <el-radio :value="0">私有</el-radio>
            <el-radio :value="1">公开</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, View, Star } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/useAuthStore'
import { useManufacturerStore } from '../../stores/useManufacturerStore'
import { getPublicModelById } from '../../api/square'
import { getPublicToolById } from '../../api/square'
import { checkFavorite, addFavorite, removeFavorite, createOrder } from '../../api/square'
import { updateModel } from '../../api/model'
import { updateTool } from '../../api/tool'
import { addManufacturer } from '../../api/manufacturer'
import RichEditor from '../../components/RichEditor.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const manufacturerStore = useManufacturerStore()
const loading = ref(false)
const item = ref(null)
const isFavorited = ref(false)
const showBuyDialog = ref(false)
const buying = ref(false)
const isOwnItem = ref(false)
const isFromMyProfile = ref(false)
const showEditDialog = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const orderForm = ref({
  price: 0,
  remark: ''
})

const itemType = route.params.type
const itemId = route.params.id

const editDialogTitle = computed(() => itemType === 'model' ? '编辑模型' : '编辑工具')

const editFormData = reactive({
  id: null,
  manufacturerId: null,
  name: '',
  price: 0,
  remark: '',
  cover: '',
  sold: 0,
  isPublic: 0
})

const editFormRules = {
  manufacturerId: itemType === 'model' ? [
    { required: true, message: '请选择厂家', trigger: 'change' }
  ] : [],
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ]
}

const dialogWidth = computed(() => {
  return window.innerWidth <= 768 ? '90%' : '500px'
})

const loadDetail = async () => {
  loading.value = true
  try {
    let res
    if (itemType === 'model') {
      res = await getPublicModelById(itemId)
    } else {
      res = await getPublicToolById(itemId)
    }

    item.value = res
    orderForm.value.price = res.price
    
    const authStore = useAuthStore()
    isOwnItem.value = res.userId === authStore.userInfo?.id
    isFromMyProfile.value = route.query.fromMyProfile === 'true'
    
    const favRes = await checkFavorite(itemType.toUpperCase(), itemId)
    isFavorited.value = favRes
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleFavorite = async () => {
  try {
    if (isFavorited.value) {
      await removeFavorite(itemType.toUpperCase(), itemId)
      isFavorited.value = false
      if (item.value) {
        item.value.favoriteCount = Math.max(0, (item.value.favoriteCount || 0) - 1)
      }
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite({
        itemType: itemType.toUpperCase(),
        itemId: Number(itemId)
      })
      isFavorited.value = true
      if (item.value) {
        item.value.favoriteCount = (item.value.favoriteCount || 0) + 1
      }
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleBuy = async () => {
  buying.value = true
  try {
    await createOrder({
      itemType: itemType.toUpperCase(),
      itemId: Number(itemId),
      price: orderForm.value.price,
      remark: orderForm.value.remark
    })
    ElMessage.success('订单创建成功')
    showBuyDialog.value = false
    router.push('/square/orders')
  } catch (error) {
    ElMessage.error(error.message || '购买失败')
  } finally {
    buying.value = false
  }
}

const goToChat = () => {
  if (item.value && item.value.userId) {
    router.push(`/square/chat/${item.value.userId}`)
  }
}

const goToUserProfile = () => {
  if (item.value && item.value.userId) {
    router.push(`/square/user/${item.value.userId}`)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const formatRemark = (remark) => {
  if (!remark) return '暂无描述'
  return remark.replace(/\[image:([^\]]+)\]/g, '<img src="$1" style="max-width: 100%; max-height: 200px; border-radius: 4px;"/>')
}

const handleCoverError = (e) => {
  e.target.style.display = 'none'
}

async function handleCoverUpload(options) {
  const { file, onSuccess, onError } = options
  try {
    const uploadData = new FormData()
    uploadData.append('file', file)
    
    const response = await fetch('/api/upload/cover', {
      method: 'POST',
      body: uploadData,
      headers: getUploadHeaders()
    })
    
    const result = await response.json()
    if (result.code === 200) {
      const coverUrl = result.data
      console.log('上传成功，封面URL:', coverUrl)
      editFormData.cover = coverUrl
      setTimeout(() => {
        onSuccess(coverUrl, file)
      }, 0)
      ElMessage.success('封面上传成功')
    } else {
      onError(new Error(result.message || '上传失败'))
      ElMessage.error(result.message || '上传失败')
    }
  } catch (error) {
    onError(error)
    ElMessage.error('上传失败：' + error.message)
  }
}

function beforeCoverUpload(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

function removeCover() {
  editFormData.cover = ''
}

function getUploadHeaders() {
  const token = localStorage.getItem('token')
  if (token) {
    return { 'Authorization': 'Bearer ' + token }
  }
  return {}
}

const handleEditDialogClose = () => {
  editFormRef.value?.resetFields()
  Object.assign(editFormData, {
    id: null,
    manufacturerId: null,
    name: '',
    price: 0,
    remark: '',
    cover: '',
    sold: 0,
    isPublic: 0
  })
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    editLoading.value = true
    try {
      let manufacturerId = editFormData.manufacturerId
      
      if (itemType === 'model' && typeof manufacturerId === 'string' && manufacturerId.trim()) {
        const manufacturerRes = await addManufacturer({ name: manufacturerId.trim() })
        manufacturerId = manufacturerRes.id
        await manufacturerStore.refreshManufacturers()
      }
      
      const submitData = {
        ...editFormData,
        manufacturerId: manufacturerId
      }
      
      if (itemType === 'model') {
        await updateModel(itemId, submitData)
      } else {
        await updateTool(itemId, submitData)
      }
      
      ElMessage.success('更新成功')
      showEditDialog.value = false
      loadDetail()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    } finally {
      editLoading.value = false
    }
  })
}

const initEditForm = () => {
  if (item.value) {
    Object.assign(editFormData, {
      id: item.value.id,
      manufacturerId: item.value.manufacturerId,
      name: item.value.name,
      price: item.value.price,
      remark: item.value.remark || '',
      cover: item.value.cover || '',
      sold: item.value.sold ?? 0,
      isPublic: item.value.isPublic ?? 0
    })
  }
}

watch(showEditDialog, (newVal) => {
  if (newVal) {
    initEditForm()
  }
})

onMounted(() => {
  manufacturerStore.fetchManufacturers()
  loadDetail()
})
</script>

<style scoped>
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(40px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes glowPulse {
  0%, 100% { box-shadow: 0 0 15px rgba(64, 158, 255, 0.4), 0 0 30px rgba(64, 158, 255, 0.2); }
  50% { box-shadow: 0 0 30px rgba(64, 158, 255, 0.6), 0 0 60px rgba(64, 158, 255, 0.3); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes shimmer {
  0% { background-position: -200% center; }
  100% { background-position: 200% center; }
}

@keyframes rotateGlow {
  0% { filter: hue-rotate(0deg); }
  100% { filter: hue-rotate(360deg); }
}

.square-detail {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  padding: 30px;
  position: relative;
  overflow-y: auto;
}

.square-detail::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(64, 158, 255, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(255, 119, 198, 0.12) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(103, 194, 58, 0.08) 0%, transparent 60%);
  pointer-events: none;
  z-index: 0;
}

.detail-content {
  max-width: 900px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
  height: calc(100vh - 160px);
  overflow: auto;
}

.item-info {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  animation: slideUp 0.6s ease-out;
}

.item-cover {
  width: 100%;
  max-height: 420px;
  margin-bottom: 24px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(103, 194, 58, 0.2));
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
  position: relative;
  transition: all 0.5s ease;
}

.item-cover::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 60%, rgba(0, 0, 0, 0.4) 100%);
  pointer-events: none;
  z-index: 1;
}

.item-cover:hover {
  transform: scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4), 0 0 40px rgba(64, 158, 255, 0.3);
}

.item-cover img {
  width: 100%;
  height: auto;
  max-height: 420px;
  object-fit: cover;
  display: block;
  transition: all 0.5s ease;
}

.item-cover:hover img {
  transform: scale(1.05);
  filter: brightness(1.1) saturate(1.2);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.item-name {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  margin: 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  flex: 1;
}

.favorite-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.4s ease;
  color: rgba(255, 255, 255, 0.7);
}

.favorite-btn:hover {
  background: rgba(245, 108, 108, 0.2);
  border-color: rgba(245, 108, 108, 0.5);
  transform: scale(1.15) rotate(15deg);
  color: #f56c6c;
}

.favorite-btn.is-favorited {
  background: rgba(245, 108, 108, 0.3);
  border-color: rgba(245, 108, 108, 0.6);
  color: #f56c6c;
  animation: glowPulse 2s infinite;
}

.item-price {
  font-size: 38px;
  font-weight: 900;
  background: linear-gradient(135deg, #f56c6c, #ff9a9e, #ffecd2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 20px;
  filter: drop-shadow(0 4px 8px rgba(245, 108, 108, 0.4));
  animation: shimmer 3s infinite;
  background-size: 200% auto;
}

.item-remark {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.9;
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  border-left: 4px solid rgba(64, 158, 255, 0.5);
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 32px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.7);
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03);
}

.meta-item:hover {
  background: rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.meta-item .el-icon {
  font-size: 18px;
  color: rgba(64, 158, 255, 0.8);
}

.item-stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 16px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(103, 194, 58, 0.15);
  transform: translateY(-2px);
}

.stat-item .el-icon {
  font-size: 18px;
  color: rgba(103, 194, 58, 0.8);
}

.item-user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-weight: 600;
  cursor: pointer;
  padding: 10px 20px;
  border-radius: 12px;
  background: rgba(64, 158, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.2);
  transition: all 0.4s ease;
}

.item-user:hover {
  background: rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.4);
  transform: translateX(5px);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.item-user .el-icon {
  font-size: 18px;
  animation: float 3s ease-in-out infinite;
}

.action-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  animation: slideInRight 0.6s ease-out;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 140px;
  margin-left: 0;
  border-radius: 14px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.action-buttons .el-button--primary {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.4), rgba(103, 194, 58, 0.3));
  border-color: rgba(64, 158, 255, 0.5);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.action-buttons .el-button--primary:hover {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.6), rgba(103, 194, 58, 0.4));
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
}

.action-buttons .el-button--success {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.4), rgba(64, 158, 255, 0.3));
  border-color: rgba(103, 194, 58, 0.5);
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
}

.action-buttons .el-button--success:hover {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.6), rgba(64, 158, 255, 0.4));
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(103, 194, 58, 0.4);
}

@media (max-width: 768px) {
  .square-detail {
    padding: 20px;
  }

  .item-info {
    padding: 20px;
    border-radius: 20px;
  }

  .item-cover {
    margin-bottom: 20px;
    border-radius: 12px;
  }

  .item-name {
    font-size: 22px;
  }

  .item-price {
    font-size: 30px;
  }

  .item-meta {
    gap: 16px;
  }

  .meta-item {
    padding: 6px 12px;
    font-size: 13px;
  }

  .item-stats {
    gap: 12px;
  }

  .stat-item {
    padding: 6px 12px;
    font-size: 12px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}

:deep(.buy-dialog) {
  background: rgba(15, 12, 41, 0.95) !important;
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.buy-dialog .el-dialog__header) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(103, 194, 58, 0.1));
  border-radius: 20px 20px 0 0;
  padding: 20px;
}

:deep(.buy-dialog .el-dialog__title) {
  color: #fff;
  font-weight: 700;
}

:deep(.buy-dialog .el-dialog__body) {
  padding: 24px;
}

:deep(.buy-dialog .el-dialog__footer) {
  padding: 16px 24px;
}

:deep(.buy-dialog .el-form-item__label) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.buy-dialog .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.buy-dialog .el-input__inner) {
  color: #fff;
}

:deep(.buy-dialog .el-input-number) {
  width: 100%;
}

:deep(.buy-dialog .el-input-number__decrease,
:deep(.buy-dialog .el-input-number__increase)) {
  background: rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.3);
  color: #fff;
}

@media (max-width: 768px) {
  :deep(.buy-dialog) {
    .el-dialog {
      margin: 20px auto !important;
      max-height: calc(100vh - 40px);
      overflow-y: auto;
    }

    .el-dialog__header {
      padding: 16px;
      font-size: 16px;
    }

    .el-dialog__body {
      padding: 16px;
    }

    .el-dialog__footer {
      padding: 12px 16px;
    }

    .el-form-item__label {
      font-size: 14px;
    }

    .el-input-number {
      width: 100% !important;
    }
  }
}

.cover-upload {
  position: relative;
  display: inline-block;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border-radius: 16px;
  border: 2px dashed rgba(64, 158, 255, 0.4);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: auto;
  z-index: 1;
  background: rgba(64, 158, 255, 0.05);
  transition: all 0.4s ease;
}

.avatar-uploader:hover {
  border-color: #409eff;
  background-color: rgba(64, 158, 255, 0.1);
  transform: scale(1.05);
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.3);
}

.avatar-uploader-icon {
  font-size: 32px;
  color: rgba(64, 158, 255, 0.6);
  pointer-events: none;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}

.avatar-uploader:hover .avatar-uploader-icon {
  color: #409eff;
  transform: scale(1.2);
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  pointer-events: none;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
}

.cover-remove {
  position: absolute;
  bottom: -24px;
  left: 0;
  color: #f56c6c;
  font-size: 12px;
  cursor: pointer;
}

.cover-remove:hover {
  text-decoration: underline;
}

:deep(.avatar-uploader .el-upload) {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  cursor: pointer;
  z-index: 2;
}

:deep(.avatar-uploader .el-upload input[type="file"]) {
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409EFF, #79bbff);
  padding: 20px;
}

:deep(.el-dialog__title) {
  color: white;
  font-weight: bold;
  font-size: 18px;
}

:deep(.el-dialog__close) {
  color: white;
}

:deep(.el-dialog__close:hover) {
  color: rgba(255, 255, 255, 0.8);
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 90% !important;
    margin-top: 12vh !important;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px 15px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 13px;
  }
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: none;
}

:deep(.el-input__inner) {
  color: #fff;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

:deep(.el-radio-group) {
  display: flex;
  gap: 10px;
}

:deep(.el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  border-radius: 10px;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.4), rgba(64, 158, 255, 0.2));
  border-color: rgba(64, 158, 255, 0.5);
  color: #fff;
  box-shadow: 0 0 20px rgba(64, 158, 255, 0.4);
}

:deep(.edit-dialog) {
  background: rgba(51, 52, 52, 0.95) !important;
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

:deep(.edit-dialog .el-dialog__header) {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2), rgba(103, 194, 58, 0.1));
  border-radius: 20px 20px 0 0;
  padding: 20px;
}

:deep(.edit-dialog .el-dialog__title) {
  color: #fff;
  font-weight: 700;
}

:deep(.edit-dialog .el-dialog__close) {
  color: white;
}

:deep(.edit-dialog .el-dialog__body) {
  padding: 24px;
  max-height: 50vh;
  overflow-y: auto;
}

:deep(.edit-dialog .el-form-item__label) {
  color: #fff;
  font-weight: 600;
  font-size: 14px;
}

:deep(.edit-dialog .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1) !important;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2);
}

:deep(.edit-dialog .el-input__inner) {
  color: #fff !important;
  background: transparent;
}

:deep(.edit-dialog .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

:deep(.edit-dialog .el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1) !important;
}

:deep(.edit-dialog .el-select-dropdown) {
  background: rgba(15, 12, 41, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.edit-dialog .el-select-dropdown__item) {
  color: rgba(255, 255, 255, 0.9);
}

:deep(.edit-dialog .el-select-dropdown__item:hover) {
  background: rgba(64, 158, 255, 0.2);
}

:deep(.edit-dialog .el-input-number) {
  width: 100%;
}

:deep(.edit-dialog .el-input-number__decrease),
:deep(.edit-dialog .el-input-number__increase) {
  background: rgba(64, 158, 255, 0.2);
  border-color: rgba(64, 158, 255, 0.3);
  color: #fff;
}

:deep(.edit-dialog .el-radio-group) {
  display: flex;
  gap: 16px;
}

:deep(.edit-dialog .el-radio) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.edit-dialog .el-radio__input.is-checked .el-radio__inner) {
  border-color: #409EFF;
  background: #409EFF;
}

:deep(.edit-dialog .el-radio__input.is-checked + .el-radio__label) {
  color: #fff;
}

@media (max-width: 768px) {
  :deep(.edit-dialog) {
    width: 91% !important;
    margin: 150px auto !important;
    max-height: 65vh;
  }
  
  :deep(.edit-dialog .el-dialog__body) {
    padding: 16px;
    max-height: 60vh;
  }
}
</style>
