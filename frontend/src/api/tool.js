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
