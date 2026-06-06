<template>
  <div class="space-y-6">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white p-5 rounded-xl shadow-sm border border-blue-100 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center">
          <el-icon :size="24" class="text-blue-500"><DataLine /></el-icon>
        </div>
        <div>
          <p class="text-sm text-gray-500">本月新增异常</p>
          <p class="text-2xl font-bold text-blue-600">{{ store.stats.thisMonth }}</p>
        </div>
      </div>
      <div class="bg-white p-5 rounded-xl shadow-sm border border-red-100 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-red-50 flex items-center justify-center">
          <el-icon :size="24" class="text-red-500"><WarningFilled /></el-icon>
        </div>
        <div>
          <p class="text-sm text-gray-500">待处理工单</p>
          <p class="text-2xl font-bold text-red-600">{{ store.stats.pending }}</p>
        </div>
      </div>
      <div class="bg-white p-5 rounded-xl shadow-sm border border-yellow-100 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-yellow-50 flex items-center justify-center">
          <el-icon :size="24" class="text-yellow-500"><Clock /></el-icon>
        </div>
        <div>
          <p class="text-sm text-gray-500">处理中工单</p>
          <p class="text-2xl font-bold text-yellow-600">{{ store.stats.processing }}</p>
        </div>
      </div>
      <div class="bg-white p-5 rounded-xl shadow-sm border border-green-100 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-green-50 flex items-center justify-center">
          <el-icon :size="24" class="text-green-500"><CircleCheckFilled /></el-icon>
        </div>
        <div>
          <p class="text-sm text-gray-500">已关闭工单</p>
          <p class="text-2xl font-bold text-green-600">{{ store.stats.closed }}</p>
        </div>
      </div>
    </div>

    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
      <div class="flex flex-wrap gap-4 items-end">
        <div class="flex flex-col gap-1">
          <span class="text-xs text-gray-500">工单状态</span>
          <el-select v-model="filterStatus" placeholder="全部" clearable class="!w-36">
            <el-option label="待处理" value="待处理" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已关闭" value="已关闭" />
          </el-select>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs text-gray-500">异常类型</span>
          <el-select v-model="filterExceptionType" placeholder="全部" clearable class="!w-44">
            <el-option label="原材料损耗超标" value="原材料损耗超标" />
            <el-option label="工人计件虚报" value="工人计件虚报" />
            <el-option label="数据录入错误" value="数据录入错误" />
            <el-option label="其他" value="其他" />
          </el-select>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs text-gray-500">产品名称</span>
          <el-input v-model="filterProductName" placeholder="搜索产品名称" clearable class="!w-40" />
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs text-gray-500">日期范围</span>
          <el-date-picker
            v-model="filterDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="!w-64"
          />
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs text-gray-500">创建人</span>
          <el-input v-model="filterCreatorName" placeholder="搜索创建人" clearable class="!w-36" />
        </div>
        <el-button type="primary" @click="handleSearch" icon="Search">查询</el-button>
        <el-button @click="handleReset" icon="RefreshLeft">重置</el-button>
      </div>
    </div>

    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
      <el-table :data="store.orderList" v-loading="store.loading" border style="width: 100%">
        <el-table-column prop="orderNo" label="异常编号" width="180" />
        <el-table-column prop="productName" label="产品名称" width="130" />
        <el-table-column prop="recordDate" label="业务日期" width="120" />
        <el-table-column prop="exceptionType" label="异常类型" width="150">
          <template #default="{ row }">
            <span>{{ row.exceptionType || '未分类' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="工单状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="dark" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="store.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handleSearch"
          @size-change="handleSearch"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">异常频次趋势（最近30天）</h3>
        <div ref="trendChartRef" class="w-full h-72"></div>
      </div>
      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">异常类型分布</h3>
        <div ref="distChartRef" class="w-full h-72"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useExceptionStore } from '../store/exceptionStore'
import * as echarts from 'echarts'

const router = useRouter()
const store = useExceptionStore()

const filterStatus = ref('')
const filterExceptionType = ref('')
const filterProductName = ref('')
const filterDateRange = ref<[string, string] | null>(null)
const filterCreatorName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const trendChartRef = ref<HTMLElement | null>(null)
const distChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let distChart: echarts.ECharts | null = null

const statusTagType = (status: string) => {
  if (status === '待处理') return 'danger'
  if (status === '处理中') return 'warning'
  if (status === '已关闭') return 'success'
  return 'info'
}

const formatDateTime = (dt: string) => {
  if (!dt) return ''
  return dt.replace('T', ' ').substring(0, 19)
}

const handleSearch = () => {
  store.fetchOrders({
    status: filterStatus.value || undefined,
    exceptionType: filterExceptionType.value || undefined,
    productName: filterProductName.value || undefined,
    creatorName: filterCreatorName.value || undefined,
    startDate: filterDateRange.value?.[0] || undefined,
    endDate: filterDateRange.value?.[1] || undefined,
    page: currentPage.value,
    pageSize: pageSize.value
  })
}

const handleReset = () => {
  filterStatus.value = ''
  filterExceptionType.value = ''
  filterProductName.value = ''
  filterDateRange.value = null
  filterCreatorName.value = ''
  currentPage.value = 1
  handleSearch()
}

const viewDetail = (row: any) => {
  router.push(`/exception/detail/${row.id}`)
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const trendData = store.trend
  const dates = Object.keys(trendData).sort()
  const values = dates.map(d => trendData[d])

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: { rotate: 45, fontSize: 10 }
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '新增工单',
      type: 'line',
      data: values,
      smooth: true,
      areaStyle: { color: 'rgba(99,102,241,0.1)' },
      itemStyle: { color: '#6366f1' },
      lineStyle: { width: 2 }
    }]
  }
  trendChart.setOption(option)
}

const renderDistChart = () => {
  if (!distChartRef.value) return
  if (!distChart) {
    distChart = echarts.init(distChartRef.value)
  }

  const distData = store.distribution
  const data = Object.entries(distData).map(([name, value]) => ({ name, value }))
  const colors = ['#6366f1', '#f43f5e', '#f59e0b', '#10b981', '#8b5cf6', '#ec4899']

  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', left: 'center' },
    color: colors,
    series: [{
      name: '异常类型',
      type: 'pie',
      radius: ['35%', '65%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' }
      },
      data
    }]
  }
  distChart.setOption(option)
}

const handleResize = () => {
  trendChart?.resize()
  distChart?.resize()
}

watch([() => store.trend, () => store.distribution], () => {
  nextTick(() => {
    renderTrendChart()
    renderDistChart()
  })
}, { deep: true })

onMounted(async () => {
  await Promise.all([
    store.fetchStats(),
    store.fetchOrders({ page: 1, pageSize: 10 }),
    store.fetchTrend(),
    store.fetchDistribution()
  ])
  nextTick(() => {
    renderTrendChart()
    renderDistChart()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  distChart?.dispose()
})
</script>
