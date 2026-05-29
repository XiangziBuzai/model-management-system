import request from './request'

// 获取公开模型列表
export function getPublicModels(params) {
  return request({
    url: '/square/models',
    method: 'get',
    params
  })
}

// 获取公开工具列表
export function getPublicTools(params) {
  return request({
    url: '/square/tools',
    method: 'get',
    params
  })
}

// 获取公开模型详情
export function getPublicModelById(id) {
  return request({
    url: `/square/model/${id}`,
    method: 'get'
  })
}

// 获取公开工具详情
export function getPublicToolById(id) {
  return request({
    url: `/square/tool/${id}`,
    method: 'get'
  })
}

// 添加收藏
export function addFavorite(data) {
  return request({
    url: '/square/favorite',
    method: 'post',
    data
  })
}

// 取消收藏
export function removeFavorite(itemType, itemId) {
  return request({
    url: '/square/favorite',
    method: 'delete',
    params: { itemType, itemId }
  })
}

// 检查是否已收藏
export function checkFavorite(itemType, itemId) {
  return request({
    url: '/square/favorite/check',
    method: 'get',
    params: { itemType, itemId }
  })
}

// 获取我的收藏列表
export function getMyFavorites(params) {
  return request({
    url: '/square/favorites',
    method: 'get',
    params
  })
}

// 获取指定用户的公开收藏列表
export function getUserPublicFavorites(userId, params) {
  return request({
    url: `/square/user/${userId}/favorites`,
    method: 'get',
    params
  })
}

// 获取指定用户的公开模型列表
export function getUserPublicModels(userId, params) {
  return request({
    url: `/square/user/${userId}/models`,
    method: 'get',
    params
  })
}

// 获取指定用户的公开工具列表
export function getUserPublicTools(userId, params) {
  return request({
    url: `/square/user/${userId}/tools`,
    method: 'get',
    params
  })
}

// 发送私信
export function sendMessage(data) {
  return request({
    url: '/square/message/send',
    method: 'post',
    data
  })
}

// 获取聊天记录
export function getConversation(otherUserId, params) {
  return request({
    url: `/square/messages`,
    method: 'get',
    params: { otherUserId, ...params }
  })
}

// 标记消息为已读
export function markMessageAsRead(messageId) {
  return request({
    url: `/square/message/${messageId}/read`,
    method: 'put'
  })
}

// 标记与某用户的所有消息为已读
export function markConversationAsRead(otherUserId) {
  return request({
    url: `/square/messages/read/${otherUserId}`,
    method: 'put'
  })
}

// 获取未读消息数量
export function getUnreadCount() {
  return request({
    url: '/square/messages/unread-count',
    method: 'get'
  })
}

// 获取最近的聊天对象列表
export function getRecentConversations(params) {
  return request({
    url: '/square/conversations',
    method: 'get',
    params
  })
}

// 撤回消息
export function recallMessage(messageId) {
  return request({
    url: `/square/message/${messageId}/recall`,
    method: 'put'
  })
}

// 创建订单
export function createOrder(data) {
  return request({
    url: '/square/order',
    method: 'post',
    data
  })
}

// 获取我的购买记录
export function getMyPurchases(params) {
  return request({
    url: '/square/orders/purchases',
    method: 'get',
    params
  })
}

// 获取我的销售记录
export function getMySales(params) {
  return request({
    url: '/square/orders/sales',
    method: 'get',
    params
  })
}

// 取消订单
export function cancelOrder(orderId) {
  return request({
    url: `/square/order/${orderId}/cancel`,
    method: 'put'
  })
}

// 发货（卖家填写快递信息）
export function shipOrder(orderId, data) {
  return request({
    url: `/square/order/${orderId}/ship`,
    method: 'put',
    data
  })
}

// 确认收货（买家确认收货）
export function confirmReceipt(orderId) {
  return request({
    url: `/square/order/${orderId}/confirm`,
    method: 'put'
  })
}

// 获取价格变动提醒
export function getPriceAlerts() {
  return request({
    url: '/price-alert/alerts',
    method: 'get'
  })
}

// 标记价格提醒为已通知
export function markPriceAlertsNotified(alertIds) {
  return request({
    url: '/price-alert/mark-notified',
    method: 'post',
    data: alertIds
  })
}
