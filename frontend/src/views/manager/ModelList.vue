<template>
  <div class="model-list-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label-width="70" label="厂家">
          <el-select
            v-model="queryParams.manufacturerName"
            placeholder="请选择厂家"
            clearable
            class="responsive-select"
          >
            <el-option
              v-for="item in manufacturerStore.manufacturers"
              :key="item.id"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label-width="70" label="模型名称">
          <el-input
            v-model="queryParams.modelName"
            placeholder="请输入模型名称"
            clearable
            class="responsive-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label-width="70" label="价格范围">
          <el-input-number
            v-model="queryParams.minPrice"
            :min="0"
            :precision="2"
            placeholder="最低价"
            style="margin-right: 2px"
            class="responsive-price-input"
          />
<!--          <span style="margin: 0 8px">至</span>-->
          <el-input-number
            v-model="queryParams.maxPrice"
            :min="0"
            :precision="2"
            placeholder="最高价"
            class="responsive-price-input"
          />
        </el-form-item>
        <el-form-item label-width="70" label="是否售出">
          <el-select
            v-model="queryParams.sold"
            placeholder="请选择"
            clearable
            class="responsive-select"
          >
            <el-option label="未售出" :value="0" />
            <el-option label="已售出" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label-width="70" label="是否公开">
          <el-select
            v-model="queryParams.isPublic"
            placeholder="公开的模型将出现在广场中"
            clearable
            class="responsive-select"
          >
            <el-option label="私有" :value="0" />
            <el-option label="公开" :value="1" />
          </el-select>
        </el-form-item>
          <div class="public-switch-container">
            <span class="switch-label">全部公开:</span>
            <el-switch 
              v-model="allPublic" 
              @change="handleAllPublicChange"
              active-text="公开"
              inactive-text="私有"
            />
          </div>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" class="cool-button">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset" class="cool-button">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleAdd" class="cool-button">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
          <el-button type="warning" @click="handleExport" class="cool-button">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedIds.length === 0" class="batch-delete-btn cool-button">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="hover">
      <!-- PC端显示表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        highlight-current-row
        @selection-change="handleSelectionChange"
        class="desktop-table"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="manufacturerName" label="厂家" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="manufacturer-text">{{ row.manufacturerName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="封面" width="100" align="center">
          <template #default="{ row }">
            <img 
              v-if="row.cover" 
              :src="row.cover" 
              class="cover-thumb" 
              :alt="row.name"
            />
            <el-icon v-else class="cover-placeholder"><Picture /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="模型名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="model-name-wrapper">
              <el-icon class="model-icon"><Box /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200">
          <template #default="{ row }">
            <div class="remark-cell" v-html="formatRemark(row.remark)"></div>
          </template>
        </el-table-column>
        <el-table-column prop="sold" label="是否售出" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.sold === 1 ? 'success' : 'info'"
              effect="dark"
              size="small"
            >
              {{ row.sold === 1 ? '已售出' : '未售出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isPublic" label="是否公开" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.isPublic === 1 ? 'warning' : 'info'"
              effect="dark"
              size="small"
            >
              {{ row.isPublic === 1 ? '公开' : '私有' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" size="small" @click="handleEdit(row)" class="cool-button">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)" class="cool-button">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 移动端显示卡片列表 -->
      <div class="mobile-cards" v-loading="loading">
        <div v-for="item in tableData" :key="item.id" class="model-card">
          <div class="card-header">
            <div class="card-cover">
              <img v-if="item.cover" :src="item.cover" class="cover-img" :alt="item.name" />
              <el-icon v-else class="cover-icon"><Picture /></el-icon>
            </div>
            <div class="card-title">
              <el-icon class="model-icon"><Box /></el-icon>
              <span class="model-name">{{ item.name }}</span>
            </div>
            <el-tag 
              :type="item.sold === 1 ? 'success' : 'info'"
              effect="dark"
              size="small"
            >
              {{ item.sold === 1 ? '已售出' : '未售出' }}
            </el-tag>
          </div>
          <div class="card-body">
            <div class="card-item">
              <span class="label">厂家：</span>
              <span class="value manufacturer-text">{{ item.manufacturerName }}</span>
            </div>
            <div class="card-item">
              <span class="label">价格：</span>
              <span class="value price-text">¥{{ item.price }}</span>
            </div>
            <div class="card-item" v-if="item.remark">
              <span class="label">备注：</span>
              <span class="value remark-content" v-html="formatRemark(item.remark)"></span>
            </div>
            <div class="card-item">
              <span class="label">公开：</span>
              <el-tag 
                :type="item.isPublic === 1 ? 'warning' : 'info'"
                effect="dark"
                size="small"
              >
                {{ item.isPublic === 1 ? '公开' : '私有' }}
              </el-tag>
            </div>
          </div>
          <div class="card-actions">
            <el-button type="primary" size="small" @click="handleEdit(item)" class="cool-button">
              <el-icon><Edit /></el-icon>
              <span>编辑</span>
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(item)" class="cool-button">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </el-button>
          </div>
        </div>
        
        <!-- 空状态 -->
        <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="厂家" prop="manufacturerId">
          <el-select
            v-model="formData.manufacturerId"
            placeholder="请选择厂家"
            style="width: 100%"
            filterable
            allow-create
            default-first-option
          >
            <el-option
              v-for="item in manufacturerStore.manufacturers"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="formData.price"
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
              <img v-if="formData.cover" :src="formData.cover" class="cover-preview" />
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <span v-if="formData.cover" class="cover-remove" @click="removeCover">移除</span>
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <RichEditor v-model="formData.remark" />
        </el-form-item>
        <el-form-item label="是否售出" prop="sold">
          <el-radio-group v-model="formData.sold">
            <el-radio :value="0">未售出</el-radio>
            <el-radio :value="1">已售出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否公开" prop="isPublic">
          <el-radio-group v-model="formData.isPublic">
            <el-radio :value="0">私有</el-radio>
            <el-radio :value="1">公开</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getModelList, addModel, updateModel, deleteModel, batchDeleteModels, batchSetModelsPublic, batchSetModelsPrivate, setAllModelsPublic, setAllModelsPrivate } from '../../api/model';
import { useManufacturerStore } from '../../stores/useManufacturerStore'
import { addManufacturer } from '../../api/manufacturer'
import * as XLSX from 'xlsx'
import { Box, Search, Refresh, Plus, Download, Delete, Edit, Picture } from '@element-plus/icons-vue'
import RichEditor from '../../components/RichEditor.vue'

const manufacturerStore = useManufacturerStore()

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  manufacturerName: '',
  modelName: '',
  minPrice: null,
  maxPrice: null,
  sold: null,
  isPublic: null
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const allPublic = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({
  id: null,
  manufacturerId: null,
  name: '',
  price: 0,
  remark: '',
  cover: '',
  sold: 0,
  isPublic: 0
})

// 表单验证规则
const formRules = {
  manufacturerId: [
    { required: true, message: '请选择厂家', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入模型名称', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ]
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const result = await getModelList(queryParams)
    tableData.value = result.records || []
    total.value = result.total || 0
    
    // 检查是否所有模型都是公开的（仅在不分页或当前显示所有数据时有效）
    // 这里简化处理,如果有数据且所有数据都是公开的,则设为true
    if (tableData.value.length > 0) {
      const allPublicData = tableData.value.every(item => item.isPublic === 1);
      const allPrivateData = tableData.value.every(item => item.isPublic === 0);
      // 如果数据不全是公开也不全是私有,则保持原样
      if (allPublicData) {
        allPublic.value = true;
      } else if (allPrivateData) {
        allPublic.value = false;
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  queryParams.page = 1
  loadData()
}

// 重置
function handleReset() {
  Object.assign(queryParams, {
    page: 1,
    size: 20,
    manufacturerName: '',
    modelName: '',
    minPrice: null,
    maxPrice: null,
    sold: null,
    isPublic: null
  })
  loadData()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增模型'
  dialogVisible.value = true
}

// 编辑
function handleEdit(row) {
  dialogTitle.value = '编辑模型'
  Object.assign(formData, {
    id: row.id,
    manufacturerId: row.manufacturerId,
    name: row.name,
    price: row.price,
    remark: row.remark,
    cover: row.cover || '',
    sold: row.sold ?? 0,
    isPublic: row.isPublic ?? 0
  })
  dialogVisible.value = true
}

// 封面上传处理
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
      formData.cover = coverUrl
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

// 封面上传前校验
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

// 移除封面
function removeCover() {
  formData.cover = ''
}

// 获取上传请求头
function getUploadHeaders() {
  const token = localStorage.getItem('token')
  if (token) {
    return { 'Authorization': 'Bearer ' + token }
  }
  return {}
}

// 删除
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该模型吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteModel(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 批量删除
async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await batchDeleteModels(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 设置所有模型的公开/私有状态
async function handleAllPublicChange(value) {
  try {
    if (value) {
      await ElMessageBox.confirm(
        '确定要将所有模型设为公开吗？',
        '全部设为公开',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      await setAllModelsPublic()
      ElMessage.success('成功将所有模型设为公开')
    } else {
      await ElMessageBox.confirm(
        '确定要将所有模型设为私有吗？',
        '全部设为私有',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      await setAllModelsPrivate()
      ElMessage.success('成功将所有模型设为私有')
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置公开/私有失败:', error)
      // 恢复开关状态
      allPublic.value = !value
    } else {
      // 用户取消操作,恢复开关状态
      allPublic.value = !value
    }
  }
}

// 选择变化
function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      // 检查厂家ID是否为数字,如果不是说明是手动输入的新厂家名称
      let manufacturerId = formData.manufacturerId
      
      // 如果 manufacturerId 不是数字(是字符串),说明用户手动输入了厂家名称
      if (typeof manufacturerId === 'string' && manufacturerId.trim()) {
        // 先创建厂家
        const manufacturerRes = await addManufacturer({ name: manufacturerId.trim() })
        manufacturerId = manufacturerRes.id
        
        // 立即刷新厂家列表,确保下拉框显示最新数据
        await manufacturerStore.refreshManufacturers()
        
        // ElMessage.success(`厂家 "${manufacturerRes.name}" 创建成功`)
      }
      
      // 创建或更新模型
      const modelData = {
        ...formData,
        manufacturerId: manufacturerId
      }
      
      if (modelData.id) {
        await updateModel(modelData.id, modelData)
        ElMessage.success('更新成功')
      } else {
        await addModel(modelData)
        ElMessage.success('新增成功')
      }
      
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 关闭对话框
function handleDialogClose() {
  formRef.value?.resetFields()
  Object.assign(formData, {
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

// 导出数据
async function handleExport() {
  if (total.value === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }

  try {
    // 显示加载提示
    const loadingMsg = ElMessage({
      message: `正在获取全部 ${total.value} 条数据,请稍候...`,
      type: 'info',
      duration: 0
    })

    // 获取所有数据(不分页)
    const allData = []
    const pageSize = 1000 // 每页获取1000条
    const totalPages = Math.ceil(total.value / pageSize)

    for (let page = 1; page <= totalPages; page++) {
      const params = {
        ...queryParams,
        page: page,
        size: pageSize
      }
      
      const result = await getModelList(params)
      if (result.records && result.records.length > 0) {
        allData.push(...result.records)
      }
    }

    loadingMsg.close()

    if (allData.length === 0) {
      ElMessage.warning('没有数据可导出')
      return
    }

    // 准备导出数据
    const exportData = allData.map(item => ({
      'ID': item.id,
      '厂家': item.manufacturerName,
      '模型名称': item.name,
      '价格': item.price,
      '是否售出': item.sold === 1 ? '已售出' : '未售出',
    }))

    // 创建工作簿
    const ws = XLSX.utils.json_to_sheet(exportData)
    
    // 设置列宽
    ws['!cols'] = [
      { wch: 8 },   // ID
      { wch: 15 },  // 厂家
      { wch: 25 },  // 模型名称
      { wch: 12 },  // 价格
      { wch: 12 },  // 是否售出
      { wch: 30 },  // 备注
      { wch: 20 },  // 创建时间
      { wch: 20 }   // 更新时间
    ]

    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '模型列表')

    // 生成文件名(带时间戳和数据量)
    const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-')
    const fileName = `模型列表_共${allData.length}条_${timestamp}.xlsx`

    // 导出文件
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success(`成功导出 ${allData.length} 条数据`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败,请重试')
  }
}

// 格式化备注（转换图片标记为实际图片，限制显示长度）
const formatRemark = (remark) => {
  if (!remark) return ''
  
  const maxLength = 15 // 最大显示字符数
  let text = remark
  
  // 先提取文本内容（移除图片标记）用于长度计算
  const textOnly = remark.replace(/\[image:[^\]]+\]/g, '[图片]')
  
  // 如果文本过长，进行截断
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
  
  // 将 [image:base64] 标记转换为 img 标签
  return text.replace(/\[image:([^\]]+)\]/g, '<img src="$1" style="max-width: 100%; max-height: 80px; border-radius: 4px;"/>')
}

onMounted(() => {
  manufacturerStore.fetchManufacturers()
  loadData()
})
</script>

<style scoped>
/* 搜索卡片样式 - 炫酷效果 */
.search-card {
  margin: 20px;
  border-radius: 12px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 10px 40px rgba(64, 158, 255, 0.2),
    0 0 30px rgba(64, 158, 255, 0.1);
}

/* 表格卡片样式 - 炫酷效果 */
.table-card {
  margin:0 20px;
  border-radius: 12px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 厂家文本样式 */
.manufacturer-text {
  font-weight: 500;
  color: #409EFF;
}

/* 模型名称样式 */
.model-name-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-icon {
  color: #409EFF;
  font-size: 16px;
}

/* 价格文本样式 */
.price-text {
  font-weight: bold;
  color: #F56C6C;
  font-size: 15px;
}

/* 响应式输入框和选择框 */
.responsive-input,
.responsive-select {
  width: 180px;
}

.responsive-price-input {
  width: 130px;
}

@media (max-width: 768px) {
  .responsive-input,
  .responsive-select {
    width: 100%;
  }
  
  .responsive-price-input {
    width: 48%;
  }
}

/* 分页样式 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 表格行炫酷效果 */
:deep(.el-table__row) {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

:deep(.el-table__row:hover) {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.1), rgba(103, 194, 58, 0.05)) !important;
  transform: scale(1.01);
}

/* 移动端卡片列表 - 炫酷效果 */
.mobile-cards {
  display: none;
}

.model-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 0 30px rgba(64, 158, 255, 0.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.model-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.1), transparent);
  transition: left 0.6s ease;
}

.model-card:hover::before {
  left: 100%;
}

.model-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 
    0 20px 40px rgba(64, 158, 255, 0.2),
    0 0 40px rgba(64, 158, 255, 0.15);
}

.model-card .card-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  gap: 12px;
}

.model-card .card-cover {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.model-card .cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.model-card .cover-icon {
  font-size: 20px;
  color: #c0c4cc;
}

.model-card .card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.model-card .model-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-card .card-body {
  margin-bottom: 12px;
}

.model-card .card-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.model-card .card-item .label {
  color: #909399;
  min-width: 50px;
}

.model-card .card-item .value {
  color: #606266;
  flex: 1;
}

.model-card .card-actions {
  display: flex;
  gap: 8px;
}

.model-card .card-actions .el-button {
  flex: 1;
}

/* 表单对话框优化 */
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

/* 对话框移动端适配 */
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
  
  :deep(.el-input__inner),
  :deep(.el-textarea__inner) {
    font-size: 14px;
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .model-list-container {
    padding: 12px;
  }
  
  /* 搜索表单改为垂直布局 */
  :deep(.el-form--inline .el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
    width: 100%;
  }
  
  :deep(.el-form--inline .el-form-item > .el-input),
  :deep(.el-form--inline .el-form-item > .el-select),
  :deep(.el-form--inline .el-form-item > .el-input-number) {
    width: 100% !important;
  }
  
  /* 价格范围输入框特殊处理 */
  :deep(.el-form-item__content) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  /* 按钮组改为垂直排列 */
  :deep(.el-form--inline .el-form-item:last-child .el-form-item__content) {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    width: 100%;
  }
  
  :deep(.el-form--inline .el-form-item:last-child .el-button) {
    width: 100%;
    margin: 0;
  }
  
  /* 移动端隐藏批量删除按钮 */
  .batch-delete-btn {
    display: none !important;
  }
  
  /* 隐藏PC端表格，显示移动端卡片 */
  .desktop-table {
    display: none;
  }
  
  .mobile-cards {
    display: block;
  }
  
  /* 分页优化 */
  .pagination-wrapper {
    justify-content: center;
    padding: 16px 0;
  }
  
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
  }
  
  :deep(.el-pagination .el-pagination__total) {
    order: -1;
    width: 100%;
    text-align: center;
    margin-bottom: 8px;
    font-size: 13px;
  }
  
  :deep(.el-pagination .btn-prev),
  :deep(.el-pagination .btn-next),
  :deep(.el-pagination .el-pager li) {
    min-width: 32px;
    height: 32px;
    line-height: 32px;
    font-size: 13px;
  }
  
  :deep(.el-pagination .el-pagination__sizes) {
    width: 100%;
    text-align: center;
    margin-top: 8px;
    order: 1;
  }
  
  :deep(.el-pagination .el-pagination__jump) {
    width: 100%;
    text-align: center;
    margin-top: 8px;
    order: 2;
  }
}

/* 小屏幕手机适配 */
@media (max-width: 480px) {
  .model-list-container {
    padding: 8px;
  }
  
  .search-card,
  .table-card {
    border-radius: 8px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  .price-text {
    font-size: 13px;
  }
}

/* 封面上传样式 */
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

/* 公开开关容器样式 */
.public-switch-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.switch-label {
  font-size: 14px;
  color: #606266;
}


/* 确保 el-upload 组件可点击 */
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

/* 封面缩略图样式 */
.cover-thumb {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}

.cover-placeholder {
  font-size: 24px;
  color: #c0c4cc;
}

/* 备注单元格样式 */
.remark-cell {
  max-height: 100px;
  overflow-y: auto;
  line-height: 1.6;
}

.remark-cell img {
  max-width: 100%;
  max-height: 80px;
  border-radius: 4px;
  margin: 4px 0;
}
</style>
