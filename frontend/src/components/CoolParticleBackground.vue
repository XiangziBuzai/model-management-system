<template>
  <div class="particle-container">
    <canvas ref="canvasRef" class="particle-canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref(null)
let animationId = null
let particles = []
let ctx = null
let canvas = null

class Particle {
  constructor(width, height) {
    this.x = Math.random() * width
    this.y = Math.random() * height
    this.size = Math.random() * 5 + 1
    this.speedX = (Math.random() - 0.5) * 2
    this.speedY = (Math.random() - 0.5) * 2
    this.color = this.getRandomColor()
    this.alpha = Math.random() * 0.5 + 0.2
  }

  getRandomColor() {
    const colors = [
      'rgba(64, 158, 255, ',
      'rgba(103, 194, 58, ',
      'rgba(245, 108, 108, ',
      'rgba(230, 162, 60, ',
      'rgba(156, 39, 176, '
    ]
    return colors[Math.floor(Math.random() * colors.length)]
  }

  update(width, height) {
    this.x += this.speedX
    this.y += this.speedY

    if (this.x > width) this.x = 0
    if (this.x < 0) this.x = width
    if (this.y > height) this.y = 0
    if (this.y < 0) this.y = height
  }

  draw(ctx) {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fillStyle = this.color + this.alpha + ')'
    ctx.fill()
  }
}

function connectParticles() {
  for (let a = 0; a < particles.length; a++) {
    for (let b = a; b < particles.length; b++) {
      const dx = particles[a].x - particles[b].x
      const dy = particles[a].y - particles[b].y
      const distance = Math.sqrt(dx * dx + dy * dy)

      if (distance < 120) {
        ctx.beginPath()
        ctx.strokeStyle = `rgba(64, 158, 255, ${0.2 - distance / 600})`
        ctx.lineWidth = 1
        ctx.moveTo(particles[a].x, particles[a].y)
        ctx.lineTo(particles[b].x, particles[b].y)
        ctx.stroke()
      }
    }
  }
}

function animate() {
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  particles.forEach(particle => {
    particle.update(canvas.width, canvas.height)
    particle.draw(ctx)
  })

  connectParticles()
  animationId = requestAnimationFrame(animate)
}

function init() {
  canvas = canvasRef.value
  ctx = canvas.getContext('2d')
  
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  const particleCount = Math.floor((canvas.width * canvas.height) / 15000)
  particles = []
  for (let i = 0; i < particleCount; i++) {
    particles.push(new Particle(canvas.width, canvas.height))
  }

  animate()
}

function handleResize() {
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
}

onMounted(() => {
  init()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.particle-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.particle-canvas {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
