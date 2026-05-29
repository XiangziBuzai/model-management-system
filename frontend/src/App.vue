<template>
  <CoolParticleBackground />
  <div class="app-content">
    <router-view />
  </div>
  <PriceAlertModal />
  <WelcomeGuide ref="welcomeGuideRef" />
</template>

<script setup>
import { ref, provide } from 'vue'
import PriceAlertModal from './components/PriceAlertModal.vue'
import WelcomeGuide from './components/WelcomeGuide.vue'
import CoolParticleBackground from './components/CoolParticleBackground.vue'

const welcomeGuideRef = ref(null)

function showWelcomeGuide() {
  if (welcomeGuideRef.value && welcomeGuideRef.value.showWelcomeGuide) {
    welcomeGuideRef.value.showWelcomeGuide()
  }
}

provide('showWelcomeGuide', showWelcomeGuide)
</script>

<style>
.app-content {
  position: relative;
  z-index: 1;
}
</style>
