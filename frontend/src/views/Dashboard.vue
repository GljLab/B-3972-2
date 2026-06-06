<template>
  <div ref="reportContainerRef" class="space-y-6">
    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4 justify-between items-center">
      <div class="flex flex-wrap gap-4 items-center">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          class="!w-64"
        />
        <el-input
          v-model="productName"
          placeholder="搜索产品名称"
          clearable
          class="!w-48"
          prefix-icon="Search"
        />
        <div class="flex items-center gap-2">
           <span class="text-sm text-gray-500 font-medium">误差阈值:</span>
           <el-input-number v-model="tolerance" :step="0.01" :min="0.01" :max="0.5" class="!w-32" />
        </div>
        <el-button type="primary" @click="loadData" :loading="store.loading" icon="Refresh">执行核验</el-button>
      </div>
      <div>
        <el-button color="#4f46e5" plain icon="Download" :loading="exportingPdf" @click="exportReport">导出分析报告 (PDF)</el-button>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">三方数据趋势对比</h3>
        <TrendChart :data="store.reportData" />
      </div>
      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col justify-center items-center">
        <h3 class="text-lg font-semibold text-gray-800 mb-4 w-full">异常风险构成</h3>
        <div id="pie-chart" class="w-full h-64"></div>
      </div>
    </div>

    <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
      <h3 class="text-lg font-semibold text-gray-800 mb-4">横向比对详情</h3>
      <CrossCheckTable :data="enrichedReportData" :loading="store.loading" @create-order="handleCreateOrder" @view-order="handleViewOrder" />
    </div>

    <el-dialog v-model="createDialogVisible" title="创建异常工单" width="500px" :close-on-click-modal="false">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="业务日期">
          <span>{{ createForm.recordDate }}</span>
        </el-form-item>
        <el-form-item label="产品名称">
          <span>{{ createForm.productName }}</span>
        </el-form-item>
        <el-form-item label="异常描述">
          <span class="text-red-600 text-sm">{{ createForm.exceptionDescription }}</span>
        </el-form-item>
        <el-form-item label="创建人姓名" required>
          <el-input v-model="createForm.creatorName" placeholder="请输入创建人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateOrder" :loading="creatingOrder">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="successDialogVisible" title="工单创建成功" width="400px">
      <div class="text-center py-4">
        <el-icon :size="48" class="text-green-500 mb-2"><CircleCheckFilled /></el-icon>
        <p class="text-lg font-medium mb-1">工单已成功创建</p>
        <p class="text-gray-500">编号：{{ createdOrderNo }}</p>
      </div>
      <template #footer>
        <el-button @click="successDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToOrderDetail">立即查看</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick, computed } from 'vue'
import { useInventoryStore } from '../store/inventoryStore'
import CrossCheckTable from '../components/CrossCheckTable.vue'
import TrendChart from '../components/TrendChart.vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const store = useInventoryStore()

const dateRange = ref<[string, string] | null>(null)
const productName = ref('')
const tolerance = ref(0.05)
const exportingPdf = ref(false)
const reportContainerRef = ref<HTMLElement | null>(null)

const createDialogVisible = ref(false)
const successDialogVisible = ref(false)
const creatingOrder = ref(false)
const createdOrderId = ref<number | null>(null)
const createdOrderNo = ref('')

const createForm = ref({
  productName: '',
  recordDate: '',
  theoreticalYield: 0,
  declaredQuantity: 0,
  actualTotalQuantity: 0,
  exceptionDescription: '',
  creatorName: ''
})

const existingOrders = ref<Record<string, any>>({})

const enrichedReportData = computed(() => {
  return store.reportData.map((row: any) => {
    const key = `${row.recordDate}_${row.productName}`
    return {
      ...row,
      existingOrder: existingOrders.value[key] || null
    }
  })
})

const checkExistingOrders = async () => {
  const orders: Record<string, any> = {}
  for (const item of store.reportData as any[]) {
    if (item.warnings && item.warnings.length > 0) {
      try {
        const res: any = await api.get('/exception-orders/check', {
          params: { recordDate: item.recordDate, productName: item.productName }
        })
        if (res.code === 200 && res.data) {
          const key = `${item.recordDate}_${item.productName}`
          orders[key] = res.data
        }
      } catch (e) {
        // ignore
      }
    }
  }
  existingOrders.value = orders
}

const loadData = () => {
  const start = dateRange.value?.[0] || ''
  const end = dateRange.value?.[1] || ''
  store.fetchReport(start, end, productName.value, tolerance.value)
}

const handleCreateOrder = (row: any) => {
  createForm.value = {
    productName: row.productName,
    recordDate: row.recordDate,
    theoreticalYield: row.theoreticalYield,
    declaredQuantity: row.declaredQuantity,
    actualTotalQuantity: row.actualTotalQuantity,
    exceptionDescription: row.warnings.join('; '),
    creatorName: ''
  }
  createDialogVisible.value = true
}

const submitCreateOrder = async () => {
  if (!createForm.value.creatorName.trim()) {
    ElMessage.warning('请输入创建人姓名')
    return
  }
  creatingOrder.value = true
  try {
    const res: any = await api.post('/exception-orders', createForm.value)
    if (res.code === 200) {
      createdOrderId.value = res.data.id
      createdOrderNo.value = res.data.orderNo
      createDialogVisible.value = false
      successDialogVisible.value = true
      await checkExistingOrders()
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    creatingOrder.value = false
  }
}

const handleViewOrder = (order: any) => {
  router.push(`/exception/detail/${order.id}`)
}

const goToOrderDetail = () => {
  successDialogVisible.value = false
  if (createdOrderId.value) {
    router.push(`/exception/detail/${createdOrderId.value}`)
  }
}

const exportReport = async () => {
  if (!store.reportData.length) {
    ElMessage.warning('暂无可导出的分析结果，请先执行核验')
    return
  }
  if (!reportContainerRef.value || exportingPdf.value) return

  exportingPdf.value = true
  try {
    const [{ default: html2canvas }, { jsPDF }] = await Promise.all([import('html2canvas'), import('jspdf')])
    const canvas = await html2canvas(reportContainerRef.value, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#f9fafb'
    })

    const pdf = new jsPDF('p', 'mm', 'a4')
    const margin = 8
    const pageWidth = pdf.internal.pageSize.getWidth()
    const pageHeight = pdf.internal.pageSize.getHeight()
    const imgWidth = pageWidth - margin * 2
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    const pageContentHeight = pageHeight - margin * 2

    const imgData = canvas.toDataURL('image/png')
    let heightLeft = imgHeight
    let position = margin

    pdf.addImage(imgData, 'PNG', margin, position, imgWidth, imgHeight, undefined, 'FAST')
    heightLeft -= pageContentHeight

    while (heightLeft > 0) {
      pdf.addPage()
      position = margin - (imgHeight - heightLeft)
      pdf.addImage(imgData, 'PNG', margin, position, imgWidth, imgHeight, undefined, 'FAST')
      heightLeft -= pageContentHeight
    }

    const now = new Date()
    const stamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}-${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`
    pdf.save(`cross-check-report-${stamp}.pdf`)
    ElMessage.success('分析报告 PDF 已导出')
  } catch (error) {
    console.error(error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exportingPdf.value = false
  }
}

const renderPie = () => {
    const chartDom = document.getElementById('pie-chart')
    if(!chartDom) return
    const myChart = echarts.init(chartDom)
    
    let w1 = 0, w2 = 0
    store.reportData.forEach((d: any) => {
       if(d.warnings.some((w:string) => w.includes('理论产能'))) w1++
       if(d.warnings.some((w:string) => w.includes('工资核发'))) w2++
    })

    const option = {
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center' },
      color: ['#fbbf24', '#f87171', '#34d399'],
      series: [
        {
          name: '异常分布',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false, position: 'center' },
          data: [
            { value: w1, name: '材料损耗异常' },
            { value: w2, name: '计件虚报异常' },
            { value: store.reportData.length - w1 - w2, name: '正常流转' }
          ]
        }
      ]
    }
    myChart.setOption(option)
}

watch(() => store.reportData, () => {
    nextTick(() => {
        renderPie()
        checkExistingOrders()
    })
}, { deep: true })

onMounted(() => {
  loadData()
})
</script>
