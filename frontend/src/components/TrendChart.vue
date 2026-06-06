<template>
  <div ref="chartRef" class="w-full h-64"></div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, markRaw } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{ data: any[] }>()
const chartRef = ref<HTMLElement | null>(null)
let myChart: echarts.ECharts | null = null

const updateChart = () => {
  if (!myChart) return

  // Sort data by date ascending for line chart
  const sorted = [...props.data].sort((a,b) => new Date(a.recordDate).getTime() - new Date(b.recordDate).getTime())
  
  const dates = sorted.map(d => `${d.recordDate}(${d.productName})`)
  const theo = sorted.map(d => d.theoreticalYield)
  const decl = sorted.map(d => d.declaredQuantity)
  const act = sorted.map(d => d.actualTotalQuantity)

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['理论总产能', '车间申报产量', '实际总库存'] },
    grid: { left: '3%', right: '12%', bottom: '5%', containLabel: true },
    xAxis: { 
      type: 'category', 
      boundaryGap: false, 
      data: dates,
      axisLabel: {
        interval: 0,
        rotate: 30,
        formatter: (val: string) => val.replace('(', '\n(')
      }
    },
    yAxis: { type: 'value' },
    series: [
      { name: '理论总产能', type: 'line', data: theo, smooth: true, itemStyle: { color: '#818cf8' } },
      { name: '车间申报产量', type: 'line', data: decl, smooth: true, itemStyle: { color: '#34d399' } },
      { name: '实际总库存', type: 'line', data: act, smooth: true, itemStyle: { color: '#fbbf24' } }
    ]
  }

  myChart.setOption(option)
}

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

onMounted(() => {
  if (chartRef.value) {
    myChart = markRaw(echarts.init(chartRef.value))
    updateChart()
  }
  window.addEventListener('resize', handleResize)
})

const handleResize = () => myChart?.resize()

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  myChart?.dispose()
})
</script>
