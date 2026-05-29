import request from './request'

// 获取当前用户信息
export function getUserProfile() {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

// 上传头像
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取指定用户公开信息
export function getUserPublicProfile(userId) {
  return request({
    url: `/user/public/${userId}`,
    method: 'get'
  })
}
