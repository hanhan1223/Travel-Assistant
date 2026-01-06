<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { 
  getOverviewAPI, 
  getTrafficTrendAPI, 
  getHotProjectsAPI, 
  getInterestDistributionAPI 
} from '@/api/statistics'
import { 
  User, 
  ChatLineRound, 
  Position, 
  View 
} from '@element-plus/icons-vue'

const loading = ref(false)
const statsData = ref({
  totalUsers: 0,
  dau: 0,
  totalConversations: 0,
  totalMessages: 0 // 注意：文档中overview返回的是 totalMessages 而不是 totalRecommendations
})

const lineChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()

let lineChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

// 初始化折线图
const initLineChart = (data: any[]) => {
  if (!lineChartRef.value) return
  lineChart = echarts.init(lineChartRef.value)
  
  const option = {
    title: { text: '近七日流量趋势', left: 'left' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['浏览量(PV)', '访客数(UV)'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.map(i => i.date) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '浏览量(PV)',
        type: 'line',
        smooth: true,
        data: data.map(i => i.pv),
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.5)' },
            { offset: 1, color: 'rgba(64,158,255,0.01)' }
          ])
        }
      },
      {
        name: '访客数(UV)',
        type: 'line',
        smooth: true,
        data: data.map(i => i.uv),
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
  lineChart.setOption(option)
}

// 初始化柱状图
const initBarChart = (data: any[]) => {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)

  const option = {
    title: { text: '热门非遗项目 Top5', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(i => i.name) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '咨询次数',
        type: 'bar',
        data: data.map(i => i.visitCount),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        barWidth: '40%'
      }
    ]
  }
  barChart.setOption(option)
}

// 初始化饼图
const initPieChart = (data: any[]) => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)

  const option = {
    title: { text: '用户兴趣类别分布', left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '兴趣占比',
        type: 'pie',
        radius: '50%',
        data: data.map(i => ({ value: i.count, name: i.category })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  pieChart.setOption(option)
}

const fetchData = async () => {
  loading.value = true
  try {
    // 并行请求所有接口
    const [overviewRes, trafficRes, hotRes, interestRes] = await Promise.all([
      getOverviewAPI(),
      getTrafficTrendAPI(),
      getHotProjectsAPI(),
      getInterestDistributionAPI()
    ])

    statsData.value = overviewRes.data
    
    await nextTick()
    initLineChart(trafficRes.data)
    initBarChart(hotRes.data)
    initPieChart(interestRes.data)
  } catch (error) {
    console.error('获取统计数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  lineChart?.resize()
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <el-row :gutter="20" class="card-row">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>总用户数</span>
            <el-icon class="icon-user"><User /></el-icon>
          </div>
          <div class="card-value">{{ statsData.totalUsers.toLocaleString() }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>今日活跃 (DAU)</span>
            <el-icon class="icon-active"><View /></el-icon>
          </div>
          <div class="card-value">{{ statsData.dau.toLocaleString() }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>累计对话数</span>
            <el-icon class="icon-chat"><ChatLineRound /></el-icon>
          </div>
          <div class="card-value">{{ statsData.totalConversations.toLocaleString() }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="card-header">
            <span>累计消息数</span>
            <el-icon class="icon-rec"><Position /></el-icon>
          </div>
          <div class="card-value">{{ statsData.totalMessages.toLocaleString() }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <div ref="lineChartRef" style="width: 100%; height: 350px;"></div>
    </el-card>

    <el-row :gutter="20" class="bottom-row">
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="barChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div ref="pieChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-container { padding: 20px; background-color: #f0f2f5; min-height: calc(100vh - 84px); }
.card-row { margin-bottom: 20px; }
.data-card { height: 100px; display: flex; flex-direction: column; justify-content: center; }
.card-header { display: flex; justify-content: space-between; align-items: center; color: #909399; font-size: 14px; }
.card-value { font-size: 24px; font-weight: bold; margin-top: 10px; color: #303133; }
.icon-user { color: #409EFF; font-size: 20px; }
.icon-active { color: #67C23A; font-size: 20px; }
.icon-chat { color: #E6A23C; font-size: 20px; }
.icon-rec { color: #F56C6C; font-size: 20px; }
.chart-card { margin-bottom: 20px; }
</style>