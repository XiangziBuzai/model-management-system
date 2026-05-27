<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="12" :md="6">
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
      <el-col :xs="24" :sm="12" :md="6">
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
      <el-col :xs="24" :sm="12" :md="6">
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
      <el-col :xs="24" :sm="12" :md="6">
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
  
  const option = {
    color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#1abc9c', '#3498db'],
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      textStyle: {
        color: '#303133'
      }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
      itemWidth: 14,
      itemHeight: 14,
      textStyle: {
        fontSize: 13
      }
    },
    series: [
      {
        name: '模型数量',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}\n{c}',
          fontSize: 13,
          fontWeight: 'bold'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
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
        color: '#303133'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
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
        fontSize: 12,
        color: '#606266',
        rotate: 30
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
        fontSize: 12,
        color: '#606266'
      }
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        barWidth: '50%',
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
  window.addEventListener('resize', () => {
    manufacturerChart?.resize()
    priceChart?.resize()
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
  border-radius: 12px;
  border: none;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
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
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
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
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
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
  
  .stat-value {
    font-size: 24px;
  }
  
  .chart-container {
    height: 300px;
  }
}
</style>
