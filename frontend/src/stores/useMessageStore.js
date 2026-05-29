import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '../api/square'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)

  async function fetchUnreadCount() {
    try {
      const res = await getUnreadCount()
      unreadCount.value = Number(res.data || res || 0)
    } catch {
      // 静默失败
    }
  }

  function decrementUnreadCount(count = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - count)
  }

  function clearUnreadCount() {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    fetchUnreadCount,
    decrementUnreadCount,
    clearUnreadCount
  }
})