<template>
  <router-view />
  <PriceAlertModal />
  <WelcomeGuide ref="welcomeGuideRef" />
</template>

<script setup>
import { ref, provide } from 'vue'
import PriceAlertModal from './components/PriceAlertModal.vue'
import WelcomeGuide from './components/WelcomeGuide.vue'

const welcomeGuideRef = ref(null)

function showWelcomeGuide() {
  if (welcomeGuideRef.value && welcomeGuideRef.value.showWelcomeGuide) {
    welcomeGuideRef.value.showWelcomeGuide()
  }
}

provide('showWelcomeGuide', showWelcomeGuide)
</script>
