<template>
  <div class="excel-import">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>Excel 批量导入</span>
        </div>
      </template>

      <!-- 导入类型选择 -->
      <el-form :inline="true" class="import-type-form">
        <el-form-item label="导入类型">
          <el-radio-group v-model="importType">
            <el-radio value="model">模型数据</el-radio>
            <el-radio value="tool">工具数据</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleDownloadTemplate">
            <el-icon><Download /></el-icon>
            下载导入模板
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 文件上传 -->
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 xlsx/xls 文件
          </div>
        </template>
      </el-upload>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button
          type="success"
          size="large"
          @click="handleImport"
          :loading="importLoading"
          :disabled="!selectedFile"
        >
          <el-icon><Upload /></el-icon>
          开始导入
        </el-button>
      </div>

      <!-- 导入结果 -->
      <div v-if="importResult" class="import-result">
        <el-divider />
        <h3>导入结果</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总行数">
            {{ importResult.totalRows }}
          </el-descriptions-item>
          <el-descriptions-item label="成功数">
            <span style="color: #67c23a">{{ importResult.successCount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败数">
            <span style="color: #f56c6c">{{ importResult.failCount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="耗时">
            {{ importResult.duration }}ms
          </el-descriptions-item>
        </el-descriptions>

        <!-- 错误详情 -->
        <div v-if="importResult.errors && importResult.errors.length > 0" class="error-details">
          <h4>错误详情</h4>
          <el-table :data="importResult.errors" border stripe max-height="400">
            <el-table-column prop="rowNum" label="行号" width="80" />
            <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- 导入说明 -->
    <el-card shadow="never" class="instruction-card">
      <template #header>
        <div class="card-header">
          <span>导入说明</span>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item>
          <p>1. 选择导入类型（模型数据或工具数据）</p>
        </el-timeline-item>
        <el-timeline-item>
          <p>2. 下载对应的导入模板</p>
        </el-timeline-item>
        <el-timeline-item>
          <p>3. 按照模板格式填写数据</p>
        </el-timeline-item>
        <el-timeline-item>
          <p>4. 上传填写好的 Excel 文件</p>
        </el-timeline-item>
        <el-timeline-item>
          <p>5. 点击"开始导入"按钮进行导入</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { importExcel, downloadModelTemplate, downloadToolTemplate } from '../api/excel'

const importType = ref('model')
const selectedFile = ref(null)
const uploadRef = ref(null)
const importLoading = ref(false)
const importResult = ref(null)

// 文件选择变化
function handleFileChange(file) {
  selectedFile.value = file.raw
  importResult.value = null
}

// 下载模板
async function handleDownloadTemplate() {
  try {
    let blob
    if (importType.value === 'model') {
      blob = await downloadModelTemplate()
    } else {
      blob = await downloadToolTemplate()
    }
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${importType.value === 'model' ? '模型' : '工具'}导入模板.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
  }
}

// 导入文件
async function handleImport() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  importLoading.value = true
  importResult.value = null

  try {
    const result = await importExcel(selectedFile.value, importType.value)
    importResult.value = result
    
    if (result.successCount === 0 && result.failCount === 0) {
      ElMessage.warning('文件中没有有效数据，请检查文件格式和内容')
    } else if (result.failCount === 0) {
      ElMessage.success(`导入成功！共导入 ${result.successCount} 条数据`)
    } else {
      ElMessage.warning(`导入完成！成功 ${result.successCount} 条，失败 ${result.failCount} 条`)
    }
    
    // 清空文件选择
    selectedFile.value = null
    uploadRef.value?.clearFiles()
  } catch (error) {
    console.error('导入失败:', error)
  } finally {
    importLoading.value = false
  }
}
</script>

<style scoped>
.excel-import {
  padding: 20px;
}

.card-header {
  font-weight: bold;
}

.import-type-form {
  margin-bottom: 20px;
}

.upload-demo {
  margin: 20px 0;
}

.action-buttons {
  margin: 20px 0;
  text-align: center;
}

.import-result {
  margin-top: 20px;
}

.error-details {
  margin-top: 20px;
}

.instruction-card {
  margin-top: 20px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .excel-import {
    padding: 12px;
  }
  
  /* 导入类型表单改为垂直布局 */
  :deep(.import-type-form .el-form-item) {
    margin-right: 0;
    margin-bottom: 12px;
    width: 100%;
  }
  
  :deep(.import-type-form .el-form-item__content) {
    width: 100%;
  }
  
  /* 上传区域优化 */
  .upload-demo {
    margin: 12px 0;
  }
  
  :deep(.el-upload-dragger) {
    padding: 20px 10px;
  }
  
  :deep(.el-upload__text) {
    font-size: 13px;
  }
  
  :deep(.el-upload__tip) {
    font-size: 12px;
  }
  
  /* 操作按钮 */
  .action-buttons {
    margin: 12px 0;
  }
  
  :deep(.action-buttons .el-button) {
    width: 100%;
  }
  
  /* 结果表格优化 */
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-descriptions) {
    font-size: 13px;
  }
  
  /* 时间轴优化 */
  :deep(.el-timeline-item__content p) {
    font-size: 13px;
  }
}

/* 小屏幕手机适配 */
@media (max-width: 480px) {
  .excel-import {
    padding: 8px;
  }
  
  :deep(.el-card) {
    border-radius: 8px;
  }
  
  .card-header {
    font-size: 15px;
  }
  
  :deep(.el-upload-dragger) {
    padding: 15px 8px;
  }
  
  :deep(.el-upload__text) {
    font-size: 12px;
  }
}
</style>
