<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="profileForm.nickname"
                placeholder="请输入昵称"
                maxlength="50"
              />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="profileForm.email"
                placeholder="请输入邮箱"
              />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="profileForm.phone"
                placeholder="请输入手机号"
                maxlength="11"
              />
            </el-form-item>

            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-avatar :size="100" :src="profileForm.avatar || defaultAvatar" />
                <div class="avatar-actions">
                  <el-upload
                    ref="avatarUploadRef"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleAvatarChange"
                    accept="image/*"
                  >
                    <el-button type="primary">
                      <el-icon><Upload /></el-icon>
                      选择图片
                    </el-button>
                  </el-upload>
                  <el-button 
                    v-if="profileForm.avatar && profileForm.avatar !== defaultAvatar" 
                    type="danger" 
                    @click="handleRemoveAvatar"
                  >
                    删除头像
                  </el-button>
                </div>
              </div>
              <div class="avatar-tip">支持JPG、PNG、GIF、WEBP格式，大小不超过5MB</div>
            </el-form-item>

            <el-form-item label="注册时间">
              <el-input v-model="profileForm.createdAt" disabled />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="updateLoading">
                保存修改
              </el-button>
              <el-button @click="loadProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入旧密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码(6-20位)"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, changePassword, uploadAvatar } from '../api/user'
import { useAuthStore } from '../stores/useAuthStore'

const authStore = useAuthStore()

const activeTab = ref('basic')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const avatarUploadRef = ref(null)
const updateLoading = ref(false)
const passwordLoading = ref(false)
const uploadLoading = ref(false)

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const profileForm = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  createdAt: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 加载用户信息
async function loadProfile() {
  try {
    const data = await getUserProfile()
    Object.assign(profileForm, {
      id: data.id,
      username: data.username,
      nickname: data.nickname || '',
      email: data.email || '',
      phone: data.phone || '',
      avatar: data.avatar || '',
      createdAt: data.createdAt ? new Date(data.createdAt).toLocaleString('zh-CN') : ''
    })
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

// 更新用户信息
async function handleUpdateProfile() {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return

    updateLoading.value = true
    try {
      const data = await updateUserProfile({
        nickname: profileForm.nickname,
        email: profileForm.email,
        phone: profileForm.phone,
        avatar: profileForm.avatar
      })

      // 更新本地存储的用户信息
      if (authStore.userInfo) {
        authStore.userInfo.nickname = data.nickname
        authStore.userInfo.email = data.email
        authStore.userInfo.phone = data.phone
        authStore.userInfo.avatar = data.avatar
        localStorage.setItem('userInfo', JSON.stringify(authStore.userInfo))
      }

      ElMessage.success('更新成功')
      await loadProfile()
    } catch (error) {
      console.error('更新失败:', error)
    } finally {
      updateLoading.value = false
    }
  })
}

// 修改密码
async function handleChangePassword() {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      await changePassword(passwordForm)
      ElMessage.success('密码修改成功，请重新登录')
      
      // 清空表单
      resetPasswordForm()
      
      // 延迟退出登录
      setTimeout(() => {
        authStore.logout()
      }, 1500)
    } catch (error) {
      console.error('修改密码失败:', error)
    } finally {
      passwordLoading.value = false
    }
  })
}

// 重置密码表单
function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

// 更换头像（文件选择后自动上传）
async function handleAvatarChange(file) {
  const rawFile = file.raw
  
  if (!rawFile) return

  // 验证文件类型
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }

  // 验证文件大小（5MB）
  if (rawFile.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  uploadLoading.value = true
  try {
    // 上传文件
    const avatarUrl = await uploadAvatar(rawFile)
    
    // 更新表单中的头像URL
    profileForm.avatar = avatarUrl
    
    ElMessage.success('头像上传成功，请点击“保存修改”按钮保存')
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    uploadLoading.value = false
  }
}

// 删除头像
function handleRemoveAvatar() {
  ElMessageBox.confirm('确定要删除头像吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    profileForm.avatar = ''
    ElMessage.success('头像已删除，请点击“保存修改”按钮保存')
  }).catch(() => {
    // 用户取消
  })
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.profile-tabs {
  margin-top: 20px;
}

.profile-form {
  max-width: 600px;
  margin-top: 20px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .profile-container {
    padding: 12px;
  }
  
  .profile-card {
    border-radius: 12px;
  }
  
  .card-header {
    font-size: 16px;
  }
  
  .profile-form {
    max-width: 100%;
  }
  
  /* 头像上传改为垂直布局 */
  .avatar-upload {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .avatar-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .avatar-actions .el-button {
    width: 100%;
  }
  
  /* 表单按钮组 */
  :deep(.el-form-item:last-child .el-button) {
    width: 100%;
    margin-bottom: 8px;
  }
}

/* 小屏幕手机适配 */
@media (max-width: 480px) {
  .profile-container {
    padding: 8px;
  }
  
  .profile-card {
    border-radius: 8px;
  }
  
  .card-header {
    font-size: 15px;
  }
  
  :deep(.el-avatar) {
    width: 80px !important;
    height: 80px !important;
  }
}
</style>
