import request from './request'

// 分页查询模型列表
export function getModelList(params) {
  return request({
    url: '/models',
    method: 'get',
    params
  })
}

// 获取单个模型详情
export function getModelById(id) {
  return request({
    url: `/models/${id}`,
    method: 'get'
  })
}

// 新增模型
export function addModel(data) {
  return request({
    url: '/models',
    method: 'post',
    data
  })
}

// 更新模型
export function updateModel(id, data) {
  return request({
    url: `/models/${id}`,
    method: 'put',
    data
  })
}

// 删除模型
export function deleteModel(id) {
  return request({
    url: `/models/${id}`,
    method: 'delete'
  })
}

// 批量删除模型
export function batchDeleteModels(ids) {
  return request({
    url: '/models/batch',
    method: 'delete',
    data: ids
  })
}

// 批量设置模型为公开
export function batchSetModelsPublic(ids) {
  return request({
    url: '/models/batch/public',
    method: 'put',
    data: ids
  })
}

// 批量设置模型为私有
export function batchSetModelsPrivate(ids) {
  return request({
    url: '/models/batch/private',
    method: 'put',
    data: ids
  })
}

// 设置所有模型为公开
export function setAllModelsPublic() {
  return request({
    url: '/models/all/public',
    method: 'put'
  })
}

// 设置所有模型为私有
export function setAllModelsPrivate() {
  return request({
    url: '/models/all/private',
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
