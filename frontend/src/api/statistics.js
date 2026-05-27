import request from './request'

// 获取统计概览数据
export function getOverview() {
  return request({
    url: '/statistics/overview',
    method: 'get'
  })
}

// 获取厂家统计数据
export function getManufacturerStats() {
  return request({
    url: '/statistics/manufacturer',
    method: 'get'
  })
}

// 获取价格区间分布数据
export function getPriceDistribution() {
  return request({
    url: '/statistics/price-distribution',
    method: 'get'
  })
}
