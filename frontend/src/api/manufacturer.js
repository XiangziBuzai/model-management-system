import request from './request'

// 获取厂家列表（用于下拉框）
export function getManufacturerList() {
  return request({
    url: '/manufacturers',
    method: 'get'
  })
}

// 新增厂家
export function addManufacturer(data) {
  return request({
    url: '/manufacturers',
    method: 'post',
    data
  })
}

// 更新厂家
export function updateManufacturer(id, data) {
  return request({
    url: `/manufacturers/${id}`,
    method: 'put',
    data
  })
}

// 删除厂家
export function deleteManufacturer(id) {
  return request({
    url: `/manufacturers/${id}`,
    method: 'delete'
  })
}
