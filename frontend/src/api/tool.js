import request from './request'

// 分页查询工具列表
export function getToolList(params) {
  return request({
    url: '/tools',
    method: 'get',
    params
  })
}

// 新增工具
export function addTool(data) {
  return request({
    url: '/tools',
    method: 'post',
    data
  })
}

// 更新工具
export function updateTool(id, data) {
  return request({
    url: `/tools/${id}`,
    method: 'put',
    data
  })
}

// 删除工具
export function deleteTool(id) {
  return request({
    url: `/tools/${id}`,
    method: 'delete'
  })
}

// 获取单个工具详情
export function getToolById(id) {
  return request({
    url: `/tools/${id}`,
    method: 'get'
  })
}

// 批量设置工具为公开
export function batchSetToolsPublic(ids) {
  return request({
    url: '/tools/batch/public',
    method: 'put',
    data: ids
  })
}

// 批量设置工具为私有
export function batchSetToolsPrivate(ids) {
  return request({
    url: '/tools/batch/private',
    method: 'put',
    data: ids
  })
}

// 设置所有工具为公开
export function setAllToolsPublic() {
  return request({
    url: '/tools/all/public',
    method: 'put'
  })
}

// 设置所有工具为私有
export function setAllToolsPrivate() {
  return request({
    url: '/tools/all/private',
    method: 'put'
  })
}

// 上传封面
export function uploadCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/cover',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
