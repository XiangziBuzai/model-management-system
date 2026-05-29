<template>
  <div class="rich-editor-wrapper">
    <div class="editor-toolbar">
      <el-button
        type="text"
        size="small"
        @click="triggerImageUpload"
        class="upload-btn"
      >
        <el-icon><Picture /></el-icon>
        插入图片
      </el-button>
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        class="file-input"
        @change="handleImageUpload"
      />
    </div>
    <!-- 使用 contenteditable 的 div 作为编辑区域 -->
    <div
      ref="editorRef"
      class="editor-content"
      contenteditable="true"
      :data-placeholder="placeholder"
      @input="handleInput"
    ></div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { Picture } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  }
})

const emit = defineEmits(['update:modelValue'])

const fileInputRef = ref(null)
const editorRef = ref(null)

// 将存储格式转换为 DOM
const storageToDom = (storageContent) => {
  if (!editorRef.value) return
  
  if (!storageContent) {
    editorRef.value.innerHTML = ''
    return
  }
  
  // 将 [image:base64] 标记转换为 img 标签
  let html = storageContent.replace(/\[image:([^\]]+)\]/g, '<img src="$1" class="editor-image" contenteditable="false"/>')
  
  // 将换行符转换为 <br>
  html = html.replace(/\n/g, '<br>')
  
  editorRef.value.innerHTML = html
}

// 将 DOM 转换为存储格式
const domToStorage = () => {
  if (!editorRef.value) return ''
  
  let content = editorRef.value.innerHTML
  
  // 将 img 标签转换为 [image:base64] 标记
  const imgRegex = /<img[^>]*src="([^"]+)"[^>]*>/g
  content = content.replace(imgRegex, '[image:$1]')
  
  // 将 <br> 转换为换行符
  content = content.replace(/<br\s*\/?>/gi, '\n')
  
  // 移除其他 HTML 标签，只保留纯文本和图片标记
  content = content.replace(/<[^>]+>/g, '')
  
  return content
}

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  // 防止循环更新
  const currentContent = domToStorage()
  if (newValue !== currentContent) {
    storageToDom(newValue)
  }
}, { immediate: true })

// 处理输入
const handleInput = () => {
  const content = domToStorage()
  emit('update:modelValue', content)
}

// 触发图片上传
const triggerImageUpload = () => {
  // 先保存当前选区
  saveSelection()
  fileInputRef.value?.click()
}

// 保存当前光标位置
const saveSelection = () => {
  const selection = window.getSelection()
  if (selection.rangeCount > 0) {
    window.currentRange = selection.getRangeAt(0)
  }
}

// 恢复光标位置
const restoreSelection = (range) => {
  const selection = window.getSelection()
  selection.removeAllRanges()
  selection.addRange(range)
}

// 处理图片上传
const handleImageUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  
  try {
    const compressedBase64 = await compressImage(file)
    
    // 在光标位置插入图片
    insertImageAtCursor(compressedBase64)
    
    // 重置 input
    event.target.value = ''
  } catch (error) {
    console.error('图片上传失败:', error)
  }
}

// 在光标位置插入图片
const insertImageAtCursor = (base64) => {
  const selection = window.getSelection()
  
  // 如果没有选区或选区不在编辑区域内，尝试恢复之前保存的选区
  if (selection.rangeCount === 0 || !editorRef.value.contains(selection.getRangeAt(0).commonAncestorContainer)) {
    if (window.currentRange && editorRef.value.contains(window.currentRange.commonAncestorContainer)) {
      restoreSelection(window.currentRange)
    } else {
      // 如果没有有效的选区，在末尾插入
      const range = document.createRange()
      range.selectNodeContents(editorRef.value)
      range.collapse(false)
      selection.removeAllRanges()
      selection.addRange(range)
    }
  }
  
  const range = selection.getRangeAt(0)
  
  // 创建图片元素
  const img = document.createElement('img')
  img.src = base64
  img.className = 'editor-image'
  img.contentEditable = 'false'
  // 设置内联样式确保图片尺寸生效
  img.style.maxWidth = '150px'
  img.style.maxHeight = '100px'
  img.style.width = 'auto'
  img.style.height = 'auto'
  img.style.borderRadius = '4px'
  img.style.margin = '4px 0'
  img.style.objectFit = 'contain'
  
  // 如果选区有内容，先删除
  if (range.deleteContents) {
    range.deleteContents()
  }
  
  // 插入图片
  range.insertNode(img)
  
  // 将光标移到图片后面
  range.collapse(false)
  selection.removeAllRanges()
  selection.addRange(range)
  
  // 触发 input 事件
  handleInput()
}

// 压缩图片
const compressImage = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const maxWidth = 800
        const maxHeight = 600
        
        let width = img.width
        let height = img.height
        
        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }
        if (height > maxHeight) {
          width = (width * maxHeight) / height
          height = maxHeight
        }
        
        const canvas = document.createElement('canvas')
        canvas.width = width
        canvas.height = height
        
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, width, height)
        
        const base64 = canvas.toDataURL('image/jpeg', 0.7)
        resolve(base64)
      }
      img.onerror = reject
      img.src = e.target.result
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

// 初始化编辑器
nextTick(() => {
  if (editorRef.value) {
    storageToDom(props.modelValue)
  }
})
</script>

<style scoped>
.rich-editor-wrapper {
  width: 100%;
  min-width: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  box-sizing: border-box;
}

.editor-toolbar {
  border-bottom: 1px solid #e4e7ed;
  padding: 10px 12px;
  background: #fafafa;
  width: 100%;
  box-sizing: border-box;
  display: flex;
}

.upload-btn {
  padding: 4px 16px;
  color: #409eff;
  font-size: 14px;
  flex: 1;
}

.upload-btn:hover {
  background: #ecf5ff;
}

.file-input {
  display: none;
}

.editor-content {
  width: 100%;
  min-height: 200px;
  padding: 12px;
  border: none;
  resize: vertical;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.6;
  box-sizing: border-box;
  outline: none;
  min-height: 200px;
}

.editor-content:empty:before {
  content: attr(data-placeholder);
  color: #909399;
  pointer-events: none;
}

.rich-editor-wrapper :deep(.editor-image) {
  max-width: 150px !important;
  max-height: 100px !important;
  width: auto !important;
  height: auto !important;
  border-radius: 4px;
  margin: 4px 0;
  cursor: default;
  object-fit: contain;
}

.rich-editor-wrapper :deep(.editor-image:hover) {
  outline: 2px solid #409eff !important;
}
</style>