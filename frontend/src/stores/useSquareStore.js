import { defineStore } from 'pinia'
import { ref } from 'vue'

const STORAGE_KEY = 'square_list_state'

export const useSquareStore = defineStore('square', () => {
  const activeTab = ref('models')
  const sortBy = ref('newest')
  const keyword = ref('')
  const pageNum = ref(1)
  const pageSize = ref(10)
  const scrollTop = ref(0)
  const isFromDetail = ref(false)
  const selectedManufacturers = ref([])

  function saveListState(tab, sort, kw, page, size, scroll, manufacturers) {
    activeTab.value = tab
    sortBy.value = sort
    keyword.value = kw
    pageNum.value = page
    pageSize.value = size
    scrollTop.value = scroll
    selectedManufacturers.value = manufacturers
    isFromDetail.value = true
    
    const state = {
      activeTab: tab,
      sortBy: sort,
      keyword: kw,
      pageNum: page,
      pageSize: size,
      scrollTop: scroll,
      selectedManufacturers: manufacturers,
      isFromDetail: true
    }
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  }

  function restoreListState() {
    const state = {
      activeTab: activeTab.value,
      sortBy: sortBy.value,
      keyword: keyword.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      scrollTop: scrollTop.value,
      selectedManufacturers: selectedManufacturers.value
    }
    isFromDetail.value = false
    sessionStorage.removeItem(STORAGE_KEY)
    return state
  }

  function resetListState() {
    activeTab.value = 'models'
    sortBy.value = 'newest'
    keyword.value = ''
    pageNum.value = 1
    pageSize.value = 10
    scrollTop.value = 0
    selectedManufacturers.value = []
    isFromDetail.value = false
    sessionStorage.removeItem(STORAGE_KEY)
  }

  function getIsFromDetail() {
    if (!isFromDetail.value) {
      const saved = sessionStorage.getItem(STORAGE_KEY)
      if (saved) {
        try {
          const state = JSON.parse(saved)
          activeTab.value = state.activeTab
          sortBy.value = state.sortBy
          keyword.value = state.keyword
          pageNum.value = state.pageNum
          pageSize.value = state.pageSize
          scrollTop.value = state.scrollTop || 0
          selectedManufacturers.value = Array.isArray(state.selectedManufacturers) ? state.selectedManufacturers : []
          isFromDetail.value = true
        } catch (e) {
          console.error('Failed to parse saved state:', e)
        }
      }
    }
    return isFromDetail.value
  }

  return {
    activeTab,
    sortBy,
    scrollTop,
    keyword,
    pageNum,
    pageSize,
    isFromDetail,
    selectedManufacturers,
    saveListState,
    restoreListState,
    resetListState,
    getIsFromDetail
  }
})