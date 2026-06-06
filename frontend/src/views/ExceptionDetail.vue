<template>
  <div class="space-y-6" v-loading="store.loading">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <h2 class="text-xl font-bold text-gray-800">工单详情</h2>
        <el-tag v-if="order" :type="statusTagType(order.status)" effect="dark" size="large">{{ order.status }}</el-tag>
      </div>
      <el-button @click="router.push('/exception')" icon="ArrowLeft">返回列表</el-button>
    </div>

    <template v-if="order">
      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-base font-semibold text-gray-700 mb-4 border-b pb-2">基本信息</h3>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div>
            <p class="text-xs text-gray-400 mb-1">异常编号</p>
            <p class="font-mono font-medium text-gray-800">{{ order.orderNo }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">工单状态</p>
            <el-tag :type="statusTagType(order.status)" effect="dark" size="small">{{ order.status }}</el-tag>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">创建人</p>
            <p class="text-gray-800">{{ order.creatorName }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">创建时间</p>
            <p class="text-gray-800">{{ formatDateTime(order.createTime) }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-base font-semibold text-gray-700 mb-4 border-b pb-2">异常数据快照</h3>
        <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div>
            <p class="text-xs text-gray-400 mb-1">业务日期</p>
            <p class="text-gray-800">{{ order.recordDate }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">产品名称</p>
            <p class="text-gray-800">{{ order.productName }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">原材料理论产量</p>
            <p class="text-gray-800">{{ order.theoreticalYield }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">车间申报产量</p>
            <p class="text-gray-800">{{ order.declaredQuantity }}</p>
          </div>
          <div>
            <p class="text-xs text-gray-400 mb-1">库房实际产量</p>
            <p class="text-gray-800">{{ order.actualTotalQuantity }}</p>
          </div>
          <div class="md:col-span-3">
            <p class="text-xs text-gray-400 mb-1">异常描述</p>
            <p class="text-red-600 bg-red-50 p-3 rounded text-sm leading-relaxed">{{ order.exceptionDescription }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-base font-semibold text-gray-700 mb-4 border-b pb-2">责任归属</h3>
        <div v-if="order.responsibleSupplierId" class="mb-4 p-4 bg-blue-50 rounded-lg border border-blue-200">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600">
                已关联责任供应商：
                <span class="font-medium text-blue-700">{{ responsibleSupplierName }}</span>
              </p>
              <p class="text-sm text-gray-500 mt-1">
                责任说明：{{ order.responsibilityDescription || '无' }}
              </p>
              <p class="text-sm text-red-500 mt-1" v-if="order.deductScore">
                已扣除信用分：-{{ order.deductScore }}分
              </p>
            </div>
          </div>
        </div>
        <div v-else class="space-y-4">
          <div>
            <div class="flex items-center gap-2 mb-1">
              <label class="text-sm text-gray-500">责任供应商</label>
              <el-tag v-if="recommendedSupplier" type="success" size="small">系统推荐（基于业务记录）</el-tag>
            </div>
            <el-select
              v-model="responsibilityForm.supplierId"
              placeholder="请选择责任供应商（可选）"
              clearable
              filterable
              class="!w-full"
            >
              <el-option
                v-for="s in allSuppliers"
                :key="s.id"
                :label="`${s.supplierName} (${s.supplierCode})`"
                :value="s.id"
              />
            </el-select>
          </div>
          <div>
            <label class="text-sm text-gray-500 mb-1 block">责任说明
              <span v-if="responsibilityForm.supplierId" class="text-red-500">*</span>
            </label>
            <el-input
              v-model="responsibilityForm.responsibilityDescription"
              type="textarea"
              :rows="2"
              placeholder="请填写具体的责任原因，如：原材料纯度不达标导致实际产量低于理论值"
              maxlength="500"
              show-word-limit
            />
          </div>
          <div>
            <label class="text-sm text-gray-500 mb-2 block">扣分档位
              <span v-if="responsibilityForm.supplierId" class="text-red-500">*</span>
            </label>
            <el-radio-group v-model="responsibilityForm.deductLevel">
              <el-radio value="3">轻微异常（扣3分）<span class="text-xs text-gray-500 ml-1">误差率5%-10%</span></el-radio>
              <el-radio value="8">中度异常（扣8分）<span class="text-xs text-gray-500 ml-1">误差率10%-20%</span></el-radio>
              <el-radio value="15">严重异常（扣15分）<span class="text-xs text-gray-500 ml-1">误差率20%以上</span></el-radio>
            </el-radio-group>
          </div>
          <div class="flex gap-3 pt-2">
            <el-button type="primary" @click="handleConfirmResponsibility" :loading="submittingResponsibility">
              确认关联
            </el-button>
            <el-button @click="handleSkipResponsibility">暂不关联</el-button>
          </div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-base font-semibold text-gray-700 mb-4 border-b pb-2">异常分类</h3>
        <el-select v-model="form.exceptionType" placeholder="请选择异常分类" class="!w-72">
          <el-option label="原材料损耗超标" value="原材料损耗超标" />
          <el-option label="工人计件虚报" value="工人计件虚报" />
          <el-option label="原材料无记录但有产出" value="原材料无记录但有产出" />
          <el-option label="实际库存但无车间申报" value="实际库存但无车间申报" />
          <el-option label="数据录入错误" value="数据录入错误" />
          <el-option label="其他" value="其他" />
        </el-select>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-base font-semibold text-gray-700 mb-4 border-b pb-2">根因分析（5Why分析法）</h3>
        <div class="space-y-4">
          <div v-for="i in 5" :key="i">
            <div class="flex items-center gap-2 mb-1">
              <span class="text-xs font-medium px-2 py-0.5 rounded" :class="i === 5 ? 'bg-indigo-100 text-indigo-700' : 'bg-gray-100 text-gray-600'">
                为什么{{ i }}
              </span>
              <span class="text-xs text-gray-400">{{ whyLabels[i - 1] }}</span>
            </div>
            <el-input
              v-model="form['why' + i]"
              type="textarea"
              :rows="2"
              :placeholder="'请输入第' + i + '层原因分析'"
              :class="i === 5 ? '!border-indigo-400' : ''"
            />
          </div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-base font-semibold text-gray-700 mb-4 border-b pb-2">改善措施</h3>
        <div class="space-y-4">
          <div>
            <p class="text-xs text-gray-500 mb-1">改善措施描述</p>
            <el-input v-model="form.improvementMeasure" type="textarea" :rows="3" placeholder="请描述改善措施" />
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p class="text-xs text-gray-500 mb-1">处理负责人姓名</p>
              <el-input v-model="form.responsiblePerson" placeholder="请输入负责人姓名" />
            </div>
            <div>
              <p class="text-xs text-gray-500 mb-1">计划完成日期</p>
              <el-date-picker
                v-model="form.planCompletionDate"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                class="!w-full"
              />
            </div>
          </div>
          <div>
            <p class="text-xs text-gray-500 mb-1">验证结果</p>
            <el-input v-model="form.verificationResult" type="textarea" :rows="3" placeholder="请描述验证结果" />
          </div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <h3 class="text-base font-semibold text-gray-700 mb-4 border-b pb-2">状态管理</h3>
        <div class="flex items-center gap-4">
          <span class="text-sm text-gray-500">工单状态：</span>
          <el-select v-model="form.status" class="!w-40">
            <el-option label="待处理" value="待处理" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已关闭" value="已关闭" />
          </el-select>
        </div>
      </div>

      <div class="flex justify-end gap-4">
        <el-button @click="router.push('/exception')" icon="ArrowLeft">返回列表</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving" icon="Check">保存</el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useExceptionStore } from '../store/exceptionStore'
import { ElMessage, ElMessageBox, type FormRules } from 'element-plus'
import { supplierApi, type Supplier } from '../api'

const route = useRoute()
const router = useRouter()
const store = useExceptionStore()
const saving = ref(false)

const whyLabels = ['表面原因', '深入一层', '继续追问', '接近本质', '根本原因']

const form = ref<Record<string, string>>({
  exceptionType: '',
  why1: '',
  why2: '',
  why3: '',
  why4: '',
  why5: '',
  improvementMeasure: '',
  responsiblePerson: '',
  planCompletionDate: '',
  verificationResult: '',
  status: '待处理'
})

const order = ref<any>(null)

const allSuppliers = ref<any[]>([])
const recommendedSupplier = ref<any>(null)
const submittingResponsibility = ref(false)
const responsibilityForm = ref({
  supplierId: null as number | null,
  responsibilityDescription: '',
  deductLevel: 3
})

const responsibleSupplierName = ref('')

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

const syncFormFromOrder = () => {
  if (!store.currentOrder) return
  const o = store.currentOrder
  order.value = o
  form.value = {
    exceptionType: o.exceptionType || '',
    why1: o.why1 || '',
    why2: o.why2 || '',
    why3: o.why3 || '',
    why4: o.why4 || '',
    why5: o.why5 || '',
    improvementMeasure: o.improvementMeasure || '',
    responsiblePerson: o.responsiblePerson || '',
    planCompletionDate: o.planCompletionDate || '',
    verificationResult: o.verificationResult || '',
    status: o.status || '待处理'
  }
  
  if (o.responsibleSupplierId) {
    const supplier = allSuppliers.value.find(s => s.id === o.responsibleSupplierId)
    if (supplier) {
      responsibleSupplierName.value = `${supplier.supplierName} (${supplier.supplierCode})`
    }
  }
}

const fetchAllSuppliers = async () => {
  try {
    const res: any = await supplierApi.list({ pageSize: 1000 })
    allSuppliers.value = res.data.list || []
  } catch (e) {}
}

const fetchRecommendedSupplier = async () => {
  try {
    const res: any = await supplierApi.recommendForException(Number(route.params.id))
    if (res.data.recommendedSupplierId) {
      recommendedSupplier.value = res.data.supplier
      responsibilityForm.value.supplierId = res.data.recommendedSupplierId
    }
  } catch (e) {}
}

const handleConfirmResponsibility = async () => {
  if (!responsibilityForm.value.supplierId) {
    ElMessage.warning('请选择责任供应商')
    return
  }
  if (!responsibilityForm.value.responsibilityDescription.trim()) {
    ElMessage.warning('请填写责任说明')
    return
  }
  
  submittingResponsibility.value = true
  try {
    const deductScore = Number(responsibilityForm.value.deductLevel)
    const operatorName = localStorage.getItem('userName') || '管理员'
    
    const res: any = await supplierApi.deductScore(
      responsibilityForm.value.supplierId,
      {
        exceptionOrderId: Number(route.params.id),
        deductScore,
        responsibilityDescription: responsibilityForm.value.responsibilityDescription,
        operatorName
      }
    )
    
    if (res.code === 200) {
      const { newScore, becameBlacklist } = res.data
      let msg = `已关联责任供应商并扣除${deductScore}分，当前供应商信用分为${newScore}分`
      if (becameBlacklist) {
        msg += '，该供应商已被自动列入黑名单'
      }
      ElMessage.success(msg)
      
      await store.fetchOrderById(Number(route.params.id))
      syncFormFromOrder()
    }
  } catch (e) {
  } finally {
    submittingResponsibility.value = false
  }
}

const handleSkipResponsibility = async () => {
  try {
    await ElMessageBox({
      title: '确认跳过',
      message: '确定暂不关联责任供应商吗？后续仍可在工单处理中补充关联。',
      showCancelButton: true,
      confirmButtonText: '确认跳过',
      cancelButtonText: '返回编辑',
      type: 'info'
    })
    ElMessage.info('已跳过责任归属环节')
  } catch (e) {}
}

const handleSave = async () => {
  if (form.value.status === '已关闭' && (!form.value.verificationResult || !form.value.verificationResult.trim())) {
    ElMessage.error('关闭工单前必须填写验证结果')
    return
  }
  saving.value = true
  try {
    const res = await store.updateOrder(Number(route.params.id), form.value)
    if (res && res.code === 200) {
      ElMessage.success('保存成功')
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    saving.value = false
  }
}

watch(() => store.currentOrder, () => {
  syncFormFromOrder()
})

onMounted(async () => {
  await fetchAllSuppliers()
  await store.fetchOrderById(Number(route.params.id))
  syncFormFromOrder()
  await fetchRecommendedSupplier()
})
</script>
