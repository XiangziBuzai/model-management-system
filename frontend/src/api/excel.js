import request from './request'

// 导入 Excel 文件
export function importExcel(file, type) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/import/excel',
    method: 'post',
    params: { type },
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载模型导入模板
export function downloadModelTemplate() {
  return request({
    url: '/import/template/model',
    method: 'get',
    responseType: 'blob'
  })
}

// 下载工具导入模板
export function downloadToolTemplate() {
  return request({
    url: '/import/template/tool',
    method: 'get',
    responseType: 'blob'
  })
}
