import request from './request'

export function getChatSessions() {
  return request({
    url: '/ai-chat/sessions',
    method: 'get'
  })
}

export function createChatSession() {
  return request({
    url: '/ai-chat/sessions',
    method: 'post'
  })
}

export function deleteChatSession(id) {
  return request({
    url: `/ai-chat/sessions/${id}`,
    method: 'delete'
  })
}

export function getChatMessages(sessionId) {
  return request({
    url: `/ai-chat/sessions/${sessionId}/messages`,
    method: 'get'
  })
}

export function sendChatMessage(sessionId, data) {
  return request({
    url: `/ai-chat/sessions/${sessionId}/messages`,
    method: 'post',
    data
  })
}
