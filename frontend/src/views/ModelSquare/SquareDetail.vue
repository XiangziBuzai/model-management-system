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
          v-if="isFromMyProfile"
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
.square-detail {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 16px;
}

.detail-content {
  max-width: 800px;
  margin: 0 auto;
}

.item-info {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.item-cover {
  width: 100%;
  max-height: 400px;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.item-cover img {
  width: 100%;
  height: auto;
  max-height: 400px;
  object-fit: contain;
  display: block;
}

.item-name {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 16px 0;
}

.item-price {
  font-size: 32px;
  font-weight: 700;
  color: #f56c6c;
  margin-bottom: 16px;
}

.item-remark {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  margin-bottom: 16px;
}

.item-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #909399;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.item-stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #606266;
  margin-top: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-item .el-icon {
  font-size: 16px;
  color: #909399;
}

.item-user {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
  font-weight: 500;
  cursor: pointer;
}

.item-user:hover {
  text-decoration: underline;
}

.item-user .el-icon {
  font-size: 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 120px;
  margin-left: 0;
}

@media (max-width: 768px) {
  .square-detail {
    padding: 12px;
  }

  .item-info {
    padding: 16px;
  }

  .item-cover {
    margin-bottom: 16px;
    border-radius: 6px;
  }

  .item-name {
    font-size: 20px;
  }

  .item-price {
    font-size: 28px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}

/* 对话框移动端优化 */
:deep(.buy-dialog) {
  @media (max-width: 768px) {
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
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: auto;
  z-index: 1;
}

.avatar-uploader:hover {
  border-color: #409eff;
  background-color: #fafafa;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #c0c4cc;
  pointer-events: none;
  position: relative;
  z-index: 1;
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
    margin-top: 5vh !important;
  }
  
  :deep(.el-dialog__body) {
    padding: 20px 15px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 13px;
  }
}
</style>
