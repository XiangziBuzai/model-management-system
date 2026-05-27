import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getManufacturerList } from '../api/manufacturer'

export const useManufacturerStore = defineStore('manufacturer', () => {
  const manufacturers = ref([])
  const loading = ref(false)

  // 获取厂家列表并缓存
  async function fetchManufacturers() {
    if (manufacturers.value.length > 0) {
      return manufacturers.value
    }
    
    loading.value = true
    try {
      const data = await getManufacturerList()
      manufacturers.value = data || []
      return manufacturers.value
    } catch (error) {
      console.error('获取厂家列表失败:', error)
      return []
    } finally {
      loading.value = false
    }
  }

  // 刷新厂家列表
  async function refreshManufacturers() {
    manufacturers.value = []
    return fetchManufacturers()
  }

  return {
    manufacturers,
    loading,
    fetchManufacturers,
    refreshManufacturers
  }
})
