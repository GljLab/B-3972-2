<template>
  <div class="space-y-6">
    <div class="flex flex-wrap gap-4 items-center justify-between">
      <h2 class="text-xl font-bold text-gray-800">供应商档案管理</h2>
      <div class="flex gap-3">
        <el-button type="primary" @click="handleExport" icon="Download">导出供应商数据</el-button>
        <el-button type="success" @click="showAddDialog = true" icon="Plus">新增供应商</el-button>
      </div>
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
        <div class="flex flex-col gap-1">
          <span class="text-xs text-gray-500">搜索</span>
          <el-input v-model="filterKeyword" placeholder="供应商名称/编码" clearable class="!w-60" prefix-icon="Search" />
        </div>
        <el-button type="primary" @click="handleSearch" icon="Search">查询</el-button>
        <el-button @click="handleReset" icon="RefreshLeft">重置</el-button>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div
        v-for="supplier in supplierList"
        :key="supplier.id"
        class="relative rounded-xl shadow-sm border overflow-hidden transition-all hover:shadow-md"
        :class="getCardBgClass(supplier.status)"
      >
        <div v-if="supplier.status === 'BLACKLIST'" class="absolute top-3 right-3">
          <el-tag type="danger" effect="dark" size="small">黑名单</el-tag>
        </div>
        <div v-else-if="supplier.status === 'OBSERVATION'" class="absolute top-3 right-3">
          <el-tag type="warning" effect="dark" size="small">信用恢复</el-tag>
        </div>

        <div class="p-5">
          <div class="flex items-start justify-between mb-3">
            <div>
              <h3 class="font-semibold text-gray-800 text-base">{{ supplier.supplierName }}</h3>
              <p class="text-xs text-gray-500 mt-1">{{ supplier.supplierCode }}</p>
            </div>
            <el-tag :type="getSupplyTypeTagType(supplier.supplyType)" size="small">
              {{ getSupplyTypeText(supplier.supplyType) }}
            </el-tag>
          </div>

          <div class="mb-3">
            <div class="flex justify-between text-xs mb-1">
              <span class="text-gray-500">信用分</span>
              <span class="font-semibold" :class="getScoreTextClass(supplier.creditScore)">{{ supplier.creditScore }}分</span>
            </div>
            <el-progress
              :percentage="supplier.creditScore || 0"
              :color="getScoreColor(supplier.creditScore)"
              :stroke-width="8"
              :show-text="false"
            />
          </div>

          <div class="flex items-center justify-between text-sm">
            <span class="text-gray-500">
              <el-icon class="mr-1"><Warning /></el-icon>
              关联异常 {{ supplier.exceptionCount || 0 }} 次
            </span>
            <el-tag :type="getStatusTagType(supplier.status)" size="small">
              {{ getStatusText(supplier.status) }}
            </el-tag>
          </div>

          <div class="border-t mt-4 pt-4 flex gap-2">
            <el-button size="small" type="primary" link @click="viewDetail(supplier)">查看详情</el-button>
            <el-button size="small" type="primary" link @click="handleEdit(supplier)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(supplier)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-end" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[8, 16, 24, 40]"
        layout="total, sizes, prev, pager, next"
        @current-change="handleSearch"
        @size-change="handleSearch"
      />
    </div>

    <el-empty v-if="supplierList.length === 0 && !loading" description="暂无供应商数据" />

    <el-dialog v-model="showAddDialog" title="新增供应商" width="600px" @close="resetForm">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="formData.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="formData.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="供应类型" prop="supplyType">
          <el-select v-model="formData.supplyType" placeholder="请选择供应类型" class="w-full">
            <el-option label="原材料供应商" value="RAW_MATERIAL" />
            <el-option label="外包车间承包商" value="OUTSOURCED" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作日期" prop="cooperationStartDate">
          <el-date-picker
            v-model="formData.cooperationStartDate"
            type="date"
            placeholder="选择合作起始日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { supplierApi, type Supplier } from '../api'
import * as XLSX from 'xlsx'

const router = useRouter()
const supplierList = ref<any[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)

const filterSupplyType = ref('')
const filterCreditLevel = ref('')
const filterStatus = ref('')
const filterKeyword = ref('')

const showAddDialog = ref(false)
const formRef = ref<FormInstance>()
const formData = ref<Partial<Supplier>>({
  supplierName: '',
  contactName: '',
  contactPhone: '',
  supplyType: '',
  cooperationStartDate: '',
  remark: ''
})
const editingId = ref<number | null>(null)

const formRules: FormRules = {
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  supplyType: [{ required: true, message: '请选择供应类型', trigger: 'change' }]
}

const getCardBgClass = (status: string) => {
  if (status === 'BLACKLIST') return 'bg-red-50 border-red-200'
  return 'bg-white border-gray-100'
}

const getSupplyTypeText = (type: string) => {
  if (type === 'RAW_MATERIAL') return '原材料供应商'
  if (type === 'OUTSOURCED') return '外包车间承包商'
  return type
}

const getSupplyTypeTagType = (type: string) => {
  if (type === 'RAW_MATERIAL') return 'primary'
  if (type === 'OUTSOURCED') return 'success'
  return 'info'
}

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

const getScoreTextClass = (score: number) => {
  if (score >= 90) return 'text-green-600'
  if (score >= 70) return 'text-blue-600'
  if (score >= 50) return 'text-yellow-600'
  if (score >= 30) return 'text-orange-600'
  return 'text-red-600'
}

const getScoreColor = (score: number) => {
  if (score >= 90) return '#10b981'
  if (score >= 70) return '#3b82f6'
  if (score >= 50) return '#eab308'
  if (score >= 30) return '#f97316'
  return '#ef4444'
}

const fetchSuppliers = async () => {
  loading.value = true
  try {
    const res: any = await supplierApi.list({
      supplyType: filterSupplyType.value || undefined,
      creditLevel: filterCreditLevel.value || undefined,
      status: filterStatus.value || undefined,
      keyword: filterKeyword.value || undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    supplierList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchSuppliers()
}

const handleReset = () => {
  filterSupplyType.value = ''
  filterCreditLevel.value = ''
  filterStatus.value = ''
  filterKeyword.value = ''
  currentPage.value = 1
  fetchSuppliers()
}

const viewDetail = (supplier: any) => {
  router.push(`/suppliers/detail/${supplier.id}`)
}

const handleEdit = (supplier: any) => {
  editingId.value = supplier.id
  formData.value = { ...supplier }
  showAddDialog.value = true
}

const handleDelete = async (supplier: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除供应商「${supplier.supplierName}」吗？`, '确认删除', {
      type: 'warning'
    })
    await supplierApi.delete(supplier.id)
    ElMessage.success('删除成功')
    fetchSuppliers()
  } catch (e) {}
}

const resetForm = () => {
  formData.value = {
    supplierName: '',
    contactName: '',
    contactPhone: '',
    supplyType: '',
    cooperationStartDate: '',
    remark: ''
  }
  editingId.value = null
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (editingId.value) {
          await supplierApi.update(editingId.value, formData.value)
          ElMessage.success('更新成功')
        } else {
          await supplierApi.create(formData.value)
          ElMessage.success('创建成功')
        }
        showAddDialog.value = false
        fetchSuppliers()
      } catch (e) {}
    }
  })
}

const handleExport = async () => {
  try {
    const res: any = await supplierApi.list({ pageSize: 1000 })
    const list = res.data.list || []
    
    const exportData = list.map((item: any) => ({
      '供应商编码': item.supplierCode,
      '供应商名称': item.supplierName,
      '供应类型': getSupplyTypeText(item.supplyType),
      '联系人': item.contactName || '',
      '联系电话': item.contactPhone || '',
      '当前信用分': item.creditScore,
      '信用等级': getCreditLevelText(item.creditScore),
      '当前状态': getStatusText(item.status),
      '累计异常次数': item.exceptionCount || 0,
      '合作起始日期': item.cooperationStartDate || ''
    }))

    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '供应商数据')
    
    const now = new Date()
    const timestamp = now.getFullYear().toString() +
      (now.getMonth() + 1).toString().padStart(2, '0') +
      now.getDate().toString().padStart(2, '0') + '_' +
      now.getHours().toString().padStart(2, '0') +
      now.getMinutes().toString().padStart(2, '0') +
      now.getSeconds().toString().padStart(2, '0')
    
    XLSX.writeFile(wb, `供应商数据_${timestamp}.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
  }
}

const getCreditLevelText = (score: number) => {
  if (score >= 90) return '优秀'
  if (score >= 70) return '良好'
  if (score >= 50) return '一般'
  if (score >= 30) return '警告'
  return '危险'
}

onMounted(() => {
  fetchSuppliers()
})
</script>
