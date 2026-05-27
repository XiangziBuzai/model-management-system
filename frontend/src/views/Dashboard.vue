<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="8" class="stats-cards">
      <el-col :xs="6" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card model-card">
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon :size="32"><Box /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">模型总数</div>
              <div class="stat-value">{{ overview.modelCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="6" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card tool-card">
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon :size="32"><Tools /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">工具总数</div>
              <div class="stat-value">{{ overview.toolCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="6" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card price-card">
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon :size="32"><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">模型总金额</div>
              <div class="stat-value">¥{{ overview.modelTotalPrice || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="6" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card total-price-card">
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon :size="32"><Coin /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">工具总金额</div>
              <div class="stat-value">¥{{ overview.toolTotalPrice || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><PieChart /></el-icon> 厂家模型占比</span>
            </div>
          </template>
          <div ref="manufacturerChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Histogram /></el-icon> 价格区间分布</span>
            </div>
          </template>
          <div ref="priceChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getManufacturerStats, getPriceDistribution } from '../api/statistics'
import { Box, Tools, Money, Coin, PieChart, Histogram } from '@element-plus/icons-vue'

const overview = ref({})
const manufacturerChartRef = ref(null)
const priceChartRef = ref(null)

let manufacturerChart = null
let priceChart = null

// 加载统计数据
async function loadStatistics() {
  try {
    const [overviewData, manufacturerData, priceData] = await Promise.all([
      getOverview(),
      getManufacturerStats(),
      getPriceDistribution()
    ])
    
    overview.value = overviewData || {}
    overview.value.totalPrice = overviewData?.modelTotalPrice + overviewData?.toolTotalPrice
    
    // 渲染厂家占比饼图
    renderManufacturerChart(manufacturerData)
    
    // 渲染价格分布柱状图
    renderPriceChart(priceData)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 渲染厂家占比饼图
function renderManufacturerChart(data) {
  if (!manufacturerChartRef.value) return
  
  if (!manufacturerChart) {
    manufacturerChart = echarts.init(manufacturerChartRef.value)
  }
  
  // 检测是否为移动端
  const isMobile = window.innerWidth <= 768
  
  const option = {
    color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#1abc9c', '#3498db'],
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: {
        color: '#303133',
        fontSize: isMobile ? 12 : 14
      }
    },
    legend: {
      orient: isMobile ? 'horizontal' : 'vertical',
      left: isMobile ? 'center' : 'left',
      top: isMobile ? 'bottom' : 'middle',
      bottom: isMobile ? 10 : undefined,
      itemWidth: isMobile ? 10 : 14,
      itemHeight: isMobile ? 10 : 14,
      textStyle: {
        fontSize: isMobile ? 11 : 13
      },
      pageTextStyle: {
        fontSize: isMobile ? 10 : 12
      }
    },
    series: [
      {
        name: '模型数量',
        type: 'pie',
        radius: isMobile ? ['35%', '60%'] : ['40%', '70%'],
        center: isMobile ? ['50%', '45%'] : ['60%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: isMobile ? 6 : 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: !isMobile,
          position: 'outside',
          formatter: '{b}\n{c}',
          fontSize: isMobile ? 11 : 13,
          fontWeight: 'bold'
        },
        labelLine: {
          show: !isMobile
        },
        emphasis: {
          label: {
            show: true,
            fontSize: isMobile ? 13 : 16,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 15,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        data: data?.map(item => ({
          name: item.manufacturerName,
          value: item.modelCount
        })) || []
      }
    ]
  }
  
  manufacturerChart.setOption(option)
}

// 渲染价格分布柱状图
function renderPriceChart(data) {
  if (!priceChartRef.value) return
  
  if (!priceChart) {
    priceChart = echarts.init(priceChartRef.value)
  }
  
  // 检测是否为移动端
  const isMobile = window.innerWidth <= 768
  
  // 后端返回的是 Map<String, Long>，需要转换为数组
  const chartData = data ? Object.entries(data).map(([range, count]) => ({
    priceRange: range,
    count: count
  })) : []
  
  const option = {
    color: ['#409EFF'],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: {
        color: '#303133',
        fontSize: isMobile ? 12 : 14
      }
    },
    grid: {
      left: isMobile ? '2%' : '3%',
      right: isMobile ? '2%' : '4%',
      bottom: isMobile ? '15%' : '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: chartData.map(item => item.priceRange),
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      axisLabel: {
        fontSize: isMobile ? 10 : 12,
        color: '#606266',
        rotate: isMobile ? 45 : 30,
        interval: isMobile ? 0 : 'auto'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#f0f2f5'
        }
      },
      axisLabel: {
        fontSize: isMobile ? 10 : 12,
        color: '#606266'
      }
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        barWidth: isMobile ? '60%' : '50%',
        data: chartData.map(item => item.count),
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#79bbff' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#66b1ff' },
              { offset: 1, color: '#a0cfff' }
            ])
          }
        }
      }
    ]
  }
  
  priceChart.setOption(option)
}

onMounted(() => {
  loadStatistics()
  
  // 响应式调整图表大小
  let resizeTimer = null
  window.addEventListener('resize', () => {
    // 使用防抖优化性能
    clearTimeout(resizeTimer)
    resizeTimer = setTimeout(() => {
      manufacturerChart?.resize()
      priceChart?.resize()
      
      // 重新渲染图表以适应新的屏幕尺寸
      if (manufacturerChart && priceChart) {
        loadStatistics()
      }
    }, 200)
  })
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 60px);
}

/* 统计卡片样式 */
.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 10px;
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
  margin-bottom: 8px;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
}

.model-card::before {
  background: linear-gradient(90deg, #409EFF, #79bbff);
}

.tool-card::before {
  background: linear-gradient(90deg, #67C23A, #95d475);
}

.price-card::before {
  background: linear-gradient(90deg, #E6A23C, #eebe77);
}

.total-price-card::before {
  background: linear-gradient(90deg, #F56C6C, #f89898);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px 4px;
  text-align: center;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.model-card .stat-icon {
  background: linear-gradient(135deg, #409EFF, #79bbff);
}

.tool-card .stat-icon {
  background: linear-gradient(135deg, #67C23A, #95d475);
}

.price-card .stat-icon {
  background: linear-gradient(135deg, #E6A23C, #eebe77);
}

.total-price-card .stat-icon {
  background: linear-gradient(135deg, #F56C6C, #f89898);
}

.stat-content {
  flex: 1;
  width: 100%;
  min-width: 0;
}

.stat-label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
  font-weight: 500;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

/* 图表区域样式 */
.charts {
  margin-top: 24px;
}

.chart-card {
  border-radius: 12px;
  border: none;
  height: 100%;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header .el-icon {
  color: #409EFF;
}

.chart-container {
  height: 400px;
  width: 100%;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .dashboard {
    padding: 12px;
  }
  
  .stats-cards {
    margin-bottom: 12px;
  }
  
  .stat-card {
    margin-bottom: 8px;
  }
  
  .stat-item {
    padding: 8px 2px;
    gap: 3px;
  }
  
  .stat-value {
    font-size: 16px;
  }
  
  .stat-label {
    font-size: 10px;
    margin-bottom: 3px;
  }
  
  .stat-icon {
    width: 28px;
    height: 28px;
    border-radius: 6px;
  }
  
  .stat-icon .el-icon {
    font-size: 16px !important;
  }
  
  .charts {
    margin-top: 12px;
  }
  
  .chart-container {
    height: 300px;
  }
  
  .card-header {
    font-size: 14px;
  }
}

/* 小屏幕手机适配 */
@media (max-width: 480px) {
  .dashboard {
    padding: 8px;
  }
  
  .stats-cards {
    margin-bottom: 10px;
  }
  
  .stat-card {
    margin-bottom: 6px;
    border-radius: 8px;
  }
  
  .stat-item {
    padding: 6px 2px;
    gap: 2px;
  }
  
  .stat-value {
    font-size: 14px;
  }
  
  .stat-label {
    font-size: 9px;
    margin-bottom: 2px;
  }
  
  .stat-icon {
    width: 24px;
    height: 24px;
    border-radius: 5px;
  }
  
  .stat-icon .el-icon {
    font-size: 14px !important;
  }
  
  .chart-container {
    height: 250px;
  }
  
  .card-header {
    font-size: 13px;
  }
}
</style>
