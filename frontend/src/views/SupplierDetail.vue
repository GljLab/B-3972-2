<template>
  <div class="space-y-6">
    <div class="flex flex-wrap gap-4 items-center justify-between">
      <div class="flex items-center gap-3">
        <el-button @click="goBack" icon="ArrowLeft">返回列表</el-button>
        <h2 class="text-xl font-bold text-gray-800">供应商详情</h2>
      </div>
      <div class="flex gap-3">
        <el-button @click="generateReport" icon="Document">生成绩效报告</el-button>
        <el-button
          v-if="supplier?.status === 'BLACKLIST'"
          type="warning"
          @click="showRecoveryDialog = true"
          icon="RefreshRight"
        >
          申请信用恢复
        </el-button>
      </div>
    </div>

    <div
      v-if="supplier?.status === 'BLACKLIST'"
      class="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3"
    >
      <el-icon :size="24" class="text-red-500"><WarningFilled /></el-icon>
      <div>
        <p class="text-red-700 font-medium">
          该供应商已被列入黑名单（信用分：{{ supplier.creditScore }}分），建议暂停合作或加强监控
        </p>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100">
      <el-tabs v-model="activeTab" class="supplier-tabs">
        <el-tab-pane label="基本信息" name="basic">
          <div class="p-6">
            <el-form :model="editForm" label-width="120px" v-if="!isEditing">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="供应商名称">
                    <span class="text-gray-800">{{ supplier?.supplierName }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="供应商编码">
                    <span class="text-gray-800">{{ supplier?.supplierCode }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="供应类型">
                    <el-tag :type="getSupplyTypeTagType(supplier?.supplyType)">
                      {{ getSupplyTypeText(supplier?.supplyType) }}
                    </el-tag>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="当前状态">
                    <el-tag :type="getStatusTagType(supplier?.status)">
                      {{ getStatusText(supplier?.status) }}
                    </el-tag>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系人">
                    <span class="text-gray-800">{{ supplier?.contactName || '-' }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话">
                    <span class="text-gray-800">{{ supplier?.contactPhone || '-' }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="合作起始日期">
                    <span class="text-gray-800">{{ supplier?.cooperationStartDate || '-' }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="当前信用分">
                    <span class="font-bold text-lg" :class="getScoreTextClass(supplier?.creditScore)">
                      {{ supplier?.creditScore }}分
                    </span>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="备注">
                    <span class="text-gray-800">{{ supplier?.remark || '-' }}</span>
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="flex justify-center mt-6">
                <el-button type="primary" @click="startEdit">编辑信息</el-button>
              </div>
            </el-form>

            <el-form :model="editForm" :rules="formRules" ref="formRef" label-width="120px" v-else>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="供应商名称" prop="supplierName">
                    <el-input v-model="editForm.supplierName" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="供应商编码">
                    <el-input :model-value="supplier?.supplierCode" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="供应类型" prop="supplyType">
                    <el-select v-model="editForm.supplyType" class="w-full">
                      <el-option label="原材料供应商" value="RAW_MATERIAL" />
                      <el-option label="外包车间承包商" value="OUTSOURCED" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="当前状态">
                    <el-tag :type="getStatusTagType(supplier?.status)">
                      {{ getStatusText(supplier?.status) }}
                    </el-tag>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系人" prop="contactName">
                    <el-input v-model="editForm.contactName" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="contactPhone">
                    <el-input v-model="editForm.contactPhone" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="合作起始日期" prop="cooperationStartDate">
                    <el-date-picker
                      v-model="editForm.cooperationStartDate"
                      type="date"
                      format="YYYY-MM-DD"
                      value-format="YYYY-MM-DD"
                      class="w-full"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="当前信用分">
                    <el-input :model-value="supplier?.creditScore" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="备注">
                    <el-input v-model="editForm.remark" type="textarea" :rows="3" />
                  </el-form-item>
                </el-col>
              </el-row>
              <div class="flex justify-center gap-4 mt-6">
                <el-button @click="cancelEdit">取消</el-button>
                <el-button type="primary" @click="saveEdit">保存</el-button>
              </div>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="扣分历史" name="score">
          <div class="p-6">
            <div v-if="scoreRecords.length === 0" class="text-center py-12">
              <el-empty description="该供应商暂无扣分记录，表现优秀" />
            </div>
            <div v-else class="relative">
              <div class="absolute left-8 top-0 bottom-0 w-0.5 bg-gray-200"></div>
              <div class="space-y-6">
                <div v-for="(record, index) in scoreRecords" :key="record.id" class="relative flex gap-6">
                  <div
                    class="w-16 h-16 rounded-full flex items-center justify-center z-10 flex-shrink-0"
                    :class="index === 0 ? 'bg-red-100' : 'bg-gray-100'"
                  >
                    <el-icon :size="20" :class="index === 0 ? 'text-red-500' : 'text-gray-500'">
                      <Warning />
                    </el-icon>
                  </div>
                  <div class="flex-1 bg-gray-50 rounded-xl p-4 border border-gray-100">
                    <div class="flex justify-between items-start mb-2">
                      <div>
                        <p class="text-sm text-gray-500">{{ formatDateTime(record.createTime) }}</p>
                        <p class="text-base font-medium text-gray-800 mt-1">
                          {{ record.responsibilityDescription }}
                        </p>
                      </div>
                      <span class="text-red-500 font-bold text-lg">-{{ record.deductScore }}分</span>
                    </div>
                    <div class="flex flex-wrap gap-4 text-sm text-gray-600">
                      <span v-if="record.exceptionOrderNo">
                        关联工单：
                        <el-button
                          type="primary"
                          link
                          size="small"
                          @click="goToException(record.exceptionOrderId)"
                        >
                          {{ record.exceptionOrderNo }}
                        </el-button>
                      </span>
                      <span>操作人：{{ record.operatorName }}</span>
                      <span>扣分后余额：<strong>{{ record.scoreAfter }}分</strong></span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="关联异常" name="exception">
          <div class="p-6">
            <div class="flex justify-end mb-4">
              <el-select v-model="exceptionStatusFilter" placeholder="按状态筛选" clearable @change="fetchExceptionOrders">
                <el-option label="全部" value="ALL" />
                <el-option label="待处理" value="待处理" />
                <el-option label="处理中" value="处理中" />
                <el-option label="已关闭" value="已关闭" />
              </el-select>
            </div>
            <el-table :data="exceptionOrders" border style="width: 100%">
              <el-table-column prop="orderNo" label="工单编号" width="180" />
              <el-table-column prop="productName" label="产品名称" width="130" />
              <el-table-column prop="recordDate" label="业务日期" width="120" />
              <el-table-column prop="exceptionType" label="异常类型" width="150">
                <template #default="{ row }">
                  {{ row.exceptionType || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="工单状态" width="110" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusTagTypeForException(row.status)" size="small">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="170">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="goToException(row.id)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="信用趋势" name="trend">
          <div class="p-6">
            <div v-if="creditTrend.length === 0" class="text-center py-12">
              <el-empty description="暂无足够历史数据生成趋势图" />
            </div>
            <div v-else>
              <div ref="trendChartRef" class="w-full h-96"></div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="showRecoveryDialog" title="申请信用恢复" width="500px">
      <el-form :model="recoveryForm" :rules="recoveryRules" ref="recoveryFormRef" label-width="100px">
        <el-form-item label="恢复理由" prop="recoveryReason">
          <el-input
            v-model="recoveryForm.recoveryReason"
            type="textarea"
            :rows="4"
            placeholder="请说明供应商采取了哪些整改措施"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-input
            v-model="recoveryForm.certificationMaterial"
            placeholder="如质检报告编号、第三方认证文件等"
          />
        </el-form-item>
        <el-form-item label="审批人" prop="approverName">
          <el-input v-model="recoveryForm.approverName" placeholder="请输入审批人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecoveryDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRecovery">确认恢复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { supplierApi, type Supplier } from '../api'
import * as echarts from 'echarts'
import { jsPDF } from 'jspdf'

const route = useRoute()
const router = useRouter()
const supplierId = Number(route.params.id)

const supplier = ref<Supplier | null>(null)
const scoreRecords = ref<any[]>([])
const exceptionOrders = ref<any[]>([])
const creditTrend = ref<any[]>([])
const exceptionStatusFilter = ref('ALL')
const activeTab = ref('basic')

const isEditing = ref(false)
const editForm = ref<Partial<Supplier>>({})
const formRef = ref<FormInstance>()
const formRules: FormRules = {
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  supplyType: [{ required: true, message: '请选择供应类型', trigger: 'change' }]
}

const showRecoveryDialog = ref(false)
const recoveryForm = ref({
  recoveryReason: '',
  certificationMaterial: '',
  approverName: ''
})
const recoveryFormRef = ref<FormInstance>()
const recoveryRules: FormRules = {
  recoveryReason: [{ required: true, message: '请输入恢复理由', trigger: 'blur' }],
  approverName: [{ required: true, message: '请输入审批人姓名', trigger: 'blur' }]
}

const trendChartRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null

const getSupplyTypeText = (type?: string) => {
  if (type === 'RAW_MATERIAL') return '原材料供应商'
  if (type === 'OUTSOURCED') return '外包车间承包商'
  return type || '-'
}

const getSupplyTypeTagType = (type?: string) => {
  if (type === 'RAW_MATERIAL') return 'primary'
  if (type === 'OUTSOURCED') return 'success'
  return 'info'
}

const getStatusText = (status?: string) => {
  if (status === 'NORMAL') return '正常合作'
  if (status === 'OBSERVATION') return '观察期'
  if (status === 'BLACKLIST') return '黑名单'
  return status || '-'
}

const getStatusTagType = (status?: string) => {
  if (status === 'NORMAL') return 'success'
  if (status === 'OBSERVATION') return 'warning'
  if (status === 'BLACKLIST') return 'danger'
  return 'info'
}

const getScoreTextClass = (score?: number) => {
  if (!score) return 'text-gray-600'
  if (score >= 90) return 'text-green-600'
  if (score >= 70) return 'text-blue-600'
  if (score >= 50) return 'text-yellow-600'
  if (score >= 30) return 'text-orange-600'
  return 'text-red-600'
}

const getStatusTagTypeForException = (status: string) => {
  if (status === '待处理') return 'danger'
  if (status === '处理中') return 'warning'
  if (status === '已关闭') return 'success'
  return 'info'
}

const formatDateTime = (dt: string) => {
  if (!dt) return ''
  return dt.replace('T', ' ').substring(0, 16)
}

const fetchSupplier = async () => {
  try {
    const res: any = await supplierApi.getById(supplierId)
    supplier.value = res.data.supplier
  } catch (e) {}
}

const fetchScoreRecords = async () => {
  try {
    const res: any = await supplierApi.getScoreRecords(supplierId)
    scoreRecords.value = res.data || []
  } catch (e) {}
}

const fetchExceptionOrders = async () => {
  try {
    const res: any = await supplierApi.getExceptionOrders(supplierId, exceptionStatusFilter.value)
    exceptionOrders.value = res.data || []
  } catch (e) {}
}

const fetchCreditTrend = async () => {
  try {
    const res: any = await supplierApi.getCreditTrend(supplierId)
    creditTrend.value = res.data || []
    nextTick(() => {
      renderTrendChart()
    })
  } catch (e) {}
}

const goBack = () => {
  router.push('/suppliers')
}

const goToException = (id: number) => {
  router.push(`/exception/detail/${id}`)
}

const startEdit = () => {
  editForm.value = { ...supplier.value }
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  formRef.value?.resetFields()
}

const saveEdit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await supplierApi.update(supplierId, editForm.value)
        ElMessage.success('更新成功')
        isEditing.value = false
        fetchSupplier()
      } catch (e) {}
    }
  })
}

const submitRecovery = async () => {
  if (!recoveryFormRef.value) return
  await recoveryFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await supplierApi.applyRecovery(supplierId, recoveryForm.value)
        ElMessage.success('信用恢复申请成功，供应商已进入观察期')
        showRecoveryDialog.value = false
        fetchSupplier()
      } catch (e) {}
    }
  })
}

const renderTrendChart = () => {
  if (!trendChartRef.value || creditTrend.value.length === 0) return
  
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const months = creditTrend.value.map((item: any) => item.month)
  const scores = creditTrend.value.map((item: any) => item.score)
  
  const markPoints: any[] = []
  creditTrend.value.forEach((item: any, index: number) => {
    if (item.events && item.events.length > 0) {
      item.events.forEach((event: any) => {
        markPoints.push({
          name: event.description,
          coord: [item.month, event.scoreAfter || item.score],
          value: `-${event.deductScore}分`,
          itemStyle: { color: '#ef4444' }
        })
      })
    }
  })

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        const item = creditTrend.value[data.dataIndex]
        let html = `<div><strong>${item.month}</strong></div>`
        html += `<div>信用分：${data.value}分</div>`
        if (item.events && item.events.length > 0) {
          item.events.forEach((event: any) => {
            html += `<div style="color:#ef4444">● -${event.deductScore}分：${event.description}</div>`
          })
        }
        return html
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      interval: 20
    },
    series: [{
      name: '信用分',
      type: 'line',
      data: scores,
      smooth: true,
      areaStyle: { color: 'rgba(59,130,246,0.1)' },
      itemStyle: { color: '#3b82f6' },
      lineStyle: { width: 3 },
      markLine: {
        silent: true,
        data: [{
          yAxis: 30,
          lineStyle: { color: '#ef4444', type: 'dashed' },
          label: { formatter: '黑名单警戒线', color: '#ef4444' }
        }]
      }
    }]
  }
  trendChart.setOption(option)
}

const generateReport = async () => {
  try {
    const doc = new jsPDF()
    const pageWidth = doc.internal.pageSize.width
    
    doc.setFontSize(20)
    doc.text('供应商绩效分析报告', pageWidth / 2, 30, { align: 'center' })
    
    doc.setFontSize(14)
    doc.text(`${supplier.value?.supplierName} (${supplier.value?.supplierCode})`, pageWidth / 2, 50, { align: 'center' })
    
    const now = new Date()
    doc.setFontSize(10)
    doc.text(`报告生成时间：${now.toLocaleString()}`, pageWidth / 2, 65, { align: 'center' })
    
    doc.setFontSize(14)
    doc.text('一、供应商基本信息', 20, 90)
    
    doc.setFontSize(11)
    const basicInfo = [
      ['供应商名称', supplier.value?.supplierName || ''],
      ['供应商编码', supplier.value?.supplierCode || ''],
      ['供应类型', getSupplyTypeText(supplier.value?.supplyType)],
      ['联系人', supplier.value?.contactName || '-'],
      ['联系电话', supplier.value?.contactPhone || '-'],
      ['合作起始日期', supplier.value?.cooperationStartDate || '-'],
      ['当前信用分', `${supplier.value?.creditScore}分`],
      ['当前状态', getStatusText(supplier.value?.status)]
    ]
    
    let yPos = 105
    basicInfo.forEach(([label, value]) => {
      doc.text(label, 25, yPos)
      doc.text(String(value), 80, yPos)
      yPos += 10
    })
    
    ElMessage.success('报告已生成，正在下载...')
    doc.save(`供应商绩效报告_${supplier.value?.supplierName}_${now.getFullYear()}${(now.getMonth()+1).toString().padStart(2,'0')}${now.getDate().toString().padStart(2,'0')}.pdf`)
  } catch (e) {
    console.error(e)
    ElMessage.error('报告生成失败')
  }
}

const handleResize = () => {
  trendChart?.resize()
}

watch(activeTab, (tab) => {
  if (tab === 'trend' && creditTrend.value.length > 0) {
    nextTick(() => {
      renderTrendChart()
    })
  }
})

onMounted(async () => {
  await Promise.all([
    fetchSupplier(),
    fetchScoreRecords(),
    fetchExceptionOrders(),
    fetchCreditTrend()
  ])
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
})
</script>

<style scoped>
.supplier-tabs :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 20px;
  border-bottom: 1px solid #e5e7eb;
}
</style>
