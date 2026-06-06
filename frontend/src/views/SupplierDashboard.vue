<template>
  <div class="space-y-6">
    <div class="flex flex-wrap gap-4 items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">供应商绩效看板</h2>
    </div>

    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
      <div class="flex flex-wrap gap-4 items-end">
        <div class="flex flex-col gap-1">
          <span class="text-xs text-gray-500">供应类型</span>
          <el-select v-model="filterSupplyType" placeholder="全部" clearable class="!w-44">
            <el-option label="原材料供应商" value="RAW_MATERIAL" />
            <el-option label="外包车间承包商" value="OUTSOURCED" />
          </el-select>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs text-gray-500">信用等级</span>
          <el-select v-model="filterCreditLevel" placeholder="全部" clearable class="!w-36">
            <el-option label="优秀 (90-100)" value="EXCELLENT" />
            <el-option label="良好 (70-89)" value="GOOD" />
            <el-option label="一般 (50-69)" value="AVERAGE" />
            <el-option label="警告 (30-49)" value="WARNING" />
            <el-option label="危险 (0-29)" value="DANGER" />
          </el-select>
        </div>
        <div class="flex flex-col gap-1">
          <span class="text-xs text-gray-500">当前状态</span>
          <el-select v-model="filterStatus" placeholder="全部" clearable class="!w-36">
            <el-option label="正常合作" value="NORMAL" />
            <el-option label="观察期" value="OBSERVATION" />
            <el-option label="黑名单" value="BLACKLIST" />
          </el-select>
        </div>
        <el-button type="primary" @click="fetchData" icon="Search">查询</el-button>
        <el-button @click="handleReset" icon="RefreshLeft">重置</el-button>
      </div>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white p-5 rounded-xl shadow-sm border border-blue-100 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center">
          <el-icon :size="24" class="text-blue-500"><OfficeBuilding /></el-icon>
        </div>
        <div>
          <p class="text-sm text-gray-500">合作供应商总数</p>
          <p class="text-2xl font-bold text-blue-600">{{ dashboardStats.totalSuppliers || 0 }}</p>
        </div>
      </div>
      <div class="bg-white p-5 rounded-xl shadow-sm border border-red-100 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-red-50 flex items-center justify-center">
          <el-icon :size="24" class="text-red-500"><WarningFilled /></el-icon>
        </div>
        <div>
          <p class="text-sm text-gray-500">黑名单供应商</p>
          <p class="text-2xl font-bold text-red-600">{{ dashboardStats.blacklistSuppliers || 0 }}</p>
        </div>
      </div>
      <div class="bg-white p-5 rounded-xl shadow-sm border border-yellow-100 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-yellow-50 flex items-center justify-center">
          <el-icon :size="24" class="text-yellow-500"><DataLine /></el-icon>
        </div>
        <div>
          <p class="text-sm text-gray-500">本月异常关联次数</p>
          <p class="text-2xl font-bold text-yellow-600">{{ dashboardStats.monthlyExceptionCount || 0 }}</p>
        </div>
      </div>
      <div class="bg-white p-5 rounded-xl shadow-sm border border-green-100 flex items-center gap-4">
        <div class="w-12 h-12 rounded-full bg-green-50 flex items-center justify-center">
          <el-icon :size="24" class="text-green-500"><TrendCharts /></el-icon>
        </div>
        <div>
          <p class="text-sm text-gray-500">平均信用分</p>
          <p class="text-2xl font-bold text-green-600">{{ dashboardStats.averageCreditScore || 0 }}</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">信用等级分布</h3>
        <div ref="distributionChartRef" class="w-full h-80"></div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">风险榜单（信用分最低TOP10）</h3>
        <el-table :data="riskTop10" border style="width: 100%" height="320">
          <el-table-column prop="rank" label="排名" width="70" align="center">
            <template #default="{ row }">
              <span
                class="inline-flex items-center justify-center w-6 h-6 rounded-full text-xs font-bold"
                :class="row.rank <= 3 ? 'bg-red-100 text-red-600' : 'bg-gray-100 text-gray-600'"
              >
                {{ row.rank }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="供应商" min-width="180">
            <template #default="{ row }">
              <div>
                <p class="font-medium text-gray-800 cursor-pointer hover:text-blue-600" @click="goToDetail(row.id)">
                  {{ row.supplierName }}
                </p>
                <p class="text-xs text-gray-500">{{ row.supplierCode }}</p>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="creditScore" label="信用分" width="90" align="center">
            <template #default="{ row }">
              <span class="font-bold text-red-600">{{ row.creditScore }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="recent30DaysExceptions" label="近30天异常" width="100" align="center" />
          <el-table-column prop="totalDeductScore" label="累计扣分" width="90" align="center" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { supplierApi } from '../api'
import * as echarts from 'echarts'

const router = useRouter()

const filterSupplyType = ref('')
const filterCreditLevel = ref('')
const filterStatus = ref('')

const dashboardStats = ref<any>({})
const creditDistribution = ref<any>({})
const riskTop10 = ref<any[]>([])

const distributionChartRef = ref<HTMLElement | null>(null)
let distributionChart: echarts.ECharts | null = null

const getStatusText = (status: string) => {
  if (status === 'NORMAL') return '正常合作'
  if (status === 'OBSERVATION') return '观察期'
  if (status === 'BLACKLIST') return '黑名单'
  return status
}

const getStatusTagType = (status: string) => {
  if (status === 'NORMAL') return 'success'
  if (status === 'OBSERVATION') return 'warning'
  if (status === 'BLACKLIST') return 'danger'
  return 'info'
}

const fetchData = async () => {
  try {
    const [statsRes, distRes, top10Res] = await Promise.all([
      supplierApi.getDashboardStats(),
      supplierApi.getCreditDistribution(),
      supplierApi.getRiskTop10()
    ])
    
    dashboardStats.value = (statsRes as any).data || {}
    creditDistribution.value = (distRes as any).data || {}
    riskTop10.value = (top10Res as any).data || []
    
    nextTick(() => {
      renderDistributionChart()
    })
  } catch (e) {
    console.error(e)
  }
}

const renderDistributionChart = () => {
  if (!distributionChartRef.value) return
  if (!distributionChart) {
    distributionChart = echarts.init(distributionChartRef.value)
  }

  const dist = creditDistribution.value
  const colors = ['#10b981', '#3b82f6', '#eab308', '#f97316', '#ef4444']
  const data = Object.entries(dist).map(([key, value]: [string, any], index) => ({
    name: value.name,
    value: value.count,
    itemStyle: { color: colors[index] }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}家 ({d}%)'
    },
    legend: {
      bottom: '0%',
      left: 'center'
    },
    series: [{
      name: '信用等级',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{c}家 ({d}%)'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        }
      },
      data
    }]
  }
  distributionChart.setOption(option)
}

const goToDetail = (id: number) => {
  router.push(`/suppliers/detail/${id}`)
}

const handleReset = () => {
  filterSupplyType.value = ''
  filterCreditLevel.value = ''
  filterStatus.value = ''
  fetchData()
}

const handleResize = () => {
  distributionChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  distributionChart?.dispose()
})
</script>
