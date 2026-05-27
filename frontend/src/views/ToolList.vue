<template>
  <div class="tool-list-container">
    <!-- 页面标题 -->
    <!-- <div class="page-header">
      <h2 class="page-title">
        <el-icon><Tools /></el-icon>
        工具管理
      </h2>
      <p class="page-subtitle">管理系统中的所有工具数据</p>
    </div> -->

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="hover">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="工具名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入工具名称"
            clearable
            style="width: 180px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="价格范围">
          <el-input-number
            v-model="queryParams.minPrice"
            :min="0"
            :precision="2"
            placeholder="最低价"
            style="width: 130px"
          />
          <span style="margin: 0 8px">至</span>
          <el-input-number
            v-model="queryParams.maxPrice"
            :min="0"
            :precision="2"
            placeholder="最高价"
            style="width: 130px"
          />
        </el-form-item>
        <el-form-item label="是否售出">
          <el-select
            v-model="queryParams.sold"
            placeholder="请选择"
            clearable
            style="width: 140px"
          >
            <el-option label="未售出" :value="0" />
            <el-option label="已售出" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
          <el-button type="warning" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
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
        class="desktop-table"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="工具名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="tool-name-wrapper">
              <el-icon class="tool-icon"><Tools /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
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
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 移动端显示卡片列表 -->
      <div class="mobile-cards" v-loading="loading">
        <div v-for="item in tableData" :key="item.id" class="tool-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="tool-icon"><Tools /></el-icon>
              <span class="tool-name">{{ item.name }}</span>
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
              <span class="label">价格：</span>
              <span class="value price-text">¥{{ item.price }}</span>
            </div>
            <div class="card-item" v-if="item.remark">
              <span class="label">备注：</span>
              <span class="value">{{ item.remark }}</span>
            </div>
          </div>
          <div class="card-actions">
            <el-button type="primary" size="small" @click="handleEdit(item)">
              <el-icon><Edit /></el-icon>
              <span>编辑</span>
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(item)">
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
        <el-form-item label="工具名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入工具名称" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="formData.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
        <el-form-item label="是否售出" prop="sold">
          <el-radio-group v-model="formData.sold">
            <el-radio :value="0">未售出</el-radio>
            <el-radio :value="1">已售出</el-radio>
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
import { getToolList, addTool, updateTool, deleteTool } from '../api/tool'
import * as XLSX from 'xlsx'
import { Tools, Search, Refresh, Plus, Download, Edit, Delete } from '@element-plus/icons-vue'

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  minPrice: null,
  maxPrice: null,
  sold: null
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const submitLoading = ref(false)
const formData = reactive({
  id: null,
  name: '',
  price: 0,
  remark: '',
  sold: 0
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入工具名称', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ]
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const result = await getToolList(queryParams)
    tableData.value = result.records || []
    total.value = result.total || 0
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
    name: '',
    minPrice: null,
    maxPrice: null,
    sold: null
  })
  loadData()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增工具'
  dialogVisible.value = true
}

// 编辑
function handleEdit(row) {
  dialogTitle.value = '编辑工具'
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    price: row.price,
    remark: row.remark,
    sold: row.sold ?? 0
  })
  dialogVisible.value = true
}

// 删除
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该工具吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteTool(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (formData.id) {
        await updateTool(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        await addTool(formData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('提交失败:', error)
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
    name: '',
    price: 0,
    remark: '',
    sold: 0
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
      
      const result = await getToolList(params)
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
      '工具名称': item.name,
      '价格': item.price,
      '是否售出': item.sold === 1 ? '已售出' : '未售出',
      '备注': item.remark || ''
    }))

    // 创建工作簿
    const ws = XLSX.utils.json_to_sheet(exportData)
    
    // 设置列宽
    ws['!cols'] = [
      { wch: 8 },   // ID
      { wch: 25 },  // 工具名称
      { wch: 12 },  // 价格
      { wch: 12 },  // 是否售出
      { wch: 30 },  // 备注
    ]

    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '工具列表')

    // 生成文件名(带时间戳和数据量)
    const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-')
    const fileName = `工具列表_共${allData.length}条_${timestamp}.xlsx`

    // 导出文件
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success(`成功导出 ${allData.length} 条数据`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败,请重试')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* .tool-list-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  min-height: calc(100vh - 60px);
} */

/* 页面标题样式 */
.page-header {
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, #67C23A, #95d475);
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title .el-icon {
  color: #67C23A;
  font-size: 28px;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 搜索卡片样式 */
.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

/* 表格卡片样式 */
.table-card {
  border-radius: 12px;
  border: none;
}

/* 工具名称样式 */
.tool-name-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tool-icon {
  color: #67C23A;
  font-size: 16px;
}

/* 价格文本样式 */
.price-text {
  font-weight: bold;
  color: #F56C6C;
  font-size: 15px;
}

/* 分页样式 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 移动端卡片列表 */
.mobile-cards {
  display: none;
}

.tool-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.tool-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.tool-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.tool-card .card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.tool-card .tool-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-card .card-body {
  margin-bottom: 12px;
}

.tool-card .card-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.tool-card .card-item .label {
  color: #909399;
  min-width: 50px;
}

.tool-card .card-item .value {
  color: #606266;
  flex: 1;
}

.tool-card .card-actions {
  display: flex;
  gap: 8px;
}

.tool-card .card-actions .el-button {
  flex: 1;
}

/* 表单对话框优化 */
:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #67C23A, #95d475);
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
  .tool-list-container {
    padding: 12px;
  }
  
  .page-header {
    padding: 16px;
  }
  
  .page-title {
    font-size: 20px;
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
  
  /* 按钮组改为垂直排列 */
  :deep(.el-form-item__content) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
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
  }
  
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  :deep(.el-pagination .el-pagination__sizes),
  :deep(.el-pagination .el-pagination__jump) {
    width: 100%;
    text-align: center;
    margin-top: 8px;
  }
}

/* 小屏幕手机适配 */
@media (max-width: 480px) {
  .tool-list-container {
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
</style>
