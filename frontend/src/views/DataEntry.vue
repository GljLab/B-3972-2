<template>
  <div class="space-y-6">
    <!-- 顶部控制栏 -->
    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex justify-between items-center">
      <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2">
        <el-icon><EditPen /></el-icon> 多维度数据录入
      </h2>
      <el-dropdown @command="downloadTemplate">
        <el-button color="#4f46e5" plain icon="Download">
          获取批量导入模板<el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="raw" icon="Box">原材料记录模板</el-dropdown-item>
            <el-dropdown-item command="workshop" icon="UserFilled">车间生产计件模板</el-dropdown-item>
            <el-dropdown-item command="warehouse" icon="HomeFilled">库房盘点与发货模板</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 主表单区域 -->
    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
    <!-- 原材料录入 -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="bg-indigo-50 border-b border-indigo-100 px-6 py-4 flex items-center gap-3">
        <div class="bg-indigo-600 text-white p-2 rounded-lg"><el-icon><Box /></el-icon></div>
        <h3 class="text-lg font-bold text-indigo-900">原材料记录</h3>
      </div>
      <div class="p-6">
        <el-form :model="rawForm" label-position="top" label-width="120px" class="space-y-4">
          <div class="flex gap-4">
             <el-form-item label="原材料流水ID" class="flex-1">
               <el-input v-model="rawForm.materialId" placeholder="非必填：原材批次ID" />
             </el-form-item>
             <el-form-item label="产品名称/规格" class="flex-1">
               <el-input v-model="rawForm.productName" placeholder="如：产品A" />
             </el-form-item>
          </div>
          <el-form-item label="理论产出数量 (基于BOM)">
            <el-input-number v-model="rawForm.theoreticalYield" :min="0" class="!w-full" controls-position="right" />
          </el-form-item>
          <el-form-item label="生产领料日期">
            <el-date-picker v-model="rawForm.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" class="!w-full" />
          </el-form-item>
          <div class="pt-4 flex gap-3">
            <el-button color="#4f46e5" class="flex-1 font-medium" :loading="btnLoading1" @click="submitRaw">保存原材料记录</el-button>
            <el-upload
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              accept=".xlsx,.xls"
              :on-change="onRawImportChange"
            >
              <el-button plain icon="Upload" :loading="importLoading.raw">批量导入</el-button>
            </el-upload>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 车间录入 -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="bg-emerald-50 border-b border-emerald-100 px-6 py-4 flex items-center gap-3">
        <div class="bg-emerald-500 text-white p-2 rounded-lg"><el-icon><UserFilled /></el-icon></div>
        <h3 class="text-lg font-bold text-emerald-900">车间生产计件</h3>
      </div>
      <div class="p-6">
        <el-form :model="workForm" label-position="top" label-width="120px" class="space-y-4">
          <div class="flex gap-4">
            <el-form-item label="车间编号" class="flex-1">
              <el-input v-model="workForm.workshopId" placeholder="如：W_01" />
            </el-form-item>
            <el-form-item label="工人编号" class="flex-1">
              <el-input v-model="workForm.workerId" placeholder="如：W_001" />
            </el-form-item>
          </div>
          <el-form-item label="生产产品名称">
            <el-input v-model="workForm.productName" placeholder="如：产品A" />
          </el-form-item>
          <el-form-item label="工人申报总数">
            <el-input-number v-model="workForm.declaredQuantity" :min="0" class="!w-full" controls-position="right" />
          </el-form-item>
          <el-form-item label="计件日期">
            <el-date-picker v-model="workForm.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" class="!w-full" />
          </el-form-item>
          <div class="pt-4 flex gap-3">
            <el-button color="#10b981" class="flex-1 font-medium" :loading="btnLoading2" @click="submitWork">录入计件数据</el-button>
            <el-upload
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              accept=".xlsx,.xls"
              :on-change="onWorkshopImportChange"
            >
              <el-button plain icon="Upload" :loading="importLoading.workshop">批量导入</el-button>
            </el-upload>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 库房录入 -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <div class="bg-amber-50 border-b border-amber-100 px-6 py-4 flex items-center gap-3">
        <div class="bg-amber-500 text-white p-2 rounded-lg"><el-icon><HomeFilled /></el-icon></div>
        <h3 class="text-lg font-bold text-amber-900">库房盘点 / 发货</h3>
      </div>
      <div class="p-6">
        <el-form :model="warehouseForm" label-position="top" label-width="120px" class="space-y-4">
          <div class="flex gap-4">
            <el-form-item label="系统批次号" class="flex-1">
              <el-input v-model="warehouseForm.batchNo" placeholder="如：BATCH_1" />
            </el-form-item>
            <el-form-item label="产品名称" class="flex-1">
              <el-input v-model="warehouseForm.productName" placeholder="如：产品A" />
            </el-form-item>
          </div>
          
          <div class="flex gap-4 bg-gray-50 p-3 rounded-lg border border-gray-200">
             <el-form-item label="库房实盘数量" class="flex-1 !mb-0">
               <el-input-number v-model="warehouseForm.inventoryQuantity" :min="0" class="!w-full" controls-position="right" />
             </el-form-item>
             <el-form-item label="当日发货数量" class="flex-1 !mb-0">
               <el-input-number v-model="warehouseForm.shippedQuantity" :min="0" class="!w-full" controls-position="right" />
             </el-form-item>
          </div>
          <div class="text-xs text-amber-600 font-medium px-1">
            <el-icon class="mr-1 relative top-0.5"><InfoFilled/></el-icon>实际库存总量由系统自动计算: 盘点({{warehouseForm.inventoryQuantity || 0}}) + 发货({{warehouseForm.shippedQuantity || 0}})
          </div>
           
          <el-form-item label="对口甲方/客户 (选填)">
            <el-input v-model="warehouseForm.clientInfo" placeholder="发货对象" />
          </el-form-item>
          <el-form-item label="业务登记日期">
            <el-date-picker v-model="warehouseForm.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" class="!w-full" />
          </el-form-item>
          <div class="pt-4 flex gap-3">
            <el-button color="#f59e0b" class="flex-1 font-medium" :loading="btnLoading3" @click="submitWarehouse">登记库房数据</el-button>
            <el-upload
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              accept=".xlsx,.xls"
              :on-change="onWarehouseImportChange"
            >
              <el-button plain icon="Upload" :loading="importLoading.warehouse">批量导入</el-button>
            </el-upload>
          </div>
        </el-form>
      </div>
    </div>
  </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadProps } from 'element-plus'
import * as XLSX from 'xlsx'
import api from '../api'

type ImportType = 'raw' | 'workshop' | 'warehouse'
type TemplateMeta = { name: string; headers: string[] }

const TEMPLATE_META: Record<ImportType, TemplateMeta> = {
  raw: {
    name: '原材料记录',
    headers: ['原材料流水ID', '产品名称/规格', '理论产出数量', '生产领料日期(YYYY-MM-DD)']
  },
  workshop: {
    name: '车间生产计件',
    headers: ['车间编号', '工人编号', '产品名称', '工人申报总数', '计件日期(YYYY-MM-DD)']
  },
  warehouse: {
    name: '库房盘点与发货',
    headers: ['系统批次号', '产品名称', '库房实盘数量', '当日发货数量', '对口甲方/客户(选填)', '业务登记日期(YYYY-MM-DD)']
  }
}

const BATCH_ENDPOINT_MAP: Record<ImportType, string> = {
  raw: '/records/raw-materials/batch',
  workshop: '/records/workshop/batch',
  warehouse: '/records/warehouse/batch'
}

const getToday = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const rawForm = reactive({ materialId: '', productName: '', theoreticalYield: 0, recordDate: getToday() })
const workForm = reactive({ workshopId: '', workerId: '', productName: '', declaredQuantity: 0, recordDate: getToday() })
const warehouseForm = reactive({ batchNo: '', productName: '', inventoryQuantity: 0, shippedQuantity: 0, clientInfo: '', recordDate: getToday() })

const btnLoading1 = ref(false)
const btnLoading2 = ref(false)
const btnLoading3 = ref(false)
const importLoading = reactive<Record<ImportType, boolean>>({
  raw: false,
  workshop: false,
  warehouse: false
})

const toText = (value: unknown): string => String(value ?? '').trim()

const toNumber = (value: unknown): number | null => {
  if (value === null || value === undefined || value === '') return null
  const normalized = toText(value).replace(/,/g, '')
  const num = Number(normalized)
  return Number.isFinite(num) ? num : null
}

const formatDate = (date: Date): string => {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const toDate = (value: unknown): string | null => {
  if (value === null || value === undefined || value === '') return null

  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return formatDate(value)
  }

  if (typeof value === 'number') {
    const parsed = XLSX.SSF.parse_date_code(value)
    if (parsed) {
      return `${parsed.y}-${String(parsed.m).padStart(2, '0')}-${String(parsed.d).padStart(2, '0')}`
    }
    return null
  }

  const normalized = toText(value).replace(/\//g, '-')
  if (!normalized) return null

  if (/^\d{4}-\d{1,2}-\d{1,2}$/.test(normalized)) {
    const [year, month, day] = normalized.split('-').map(Number)
    const candidate = new Date(year, month - 1, day)
    if (candidate.getFullYear() === year && candidate.getMonth() === month - 1 && candidate.getDate() === day) {
      return formatDate(candidate)
    }
  }

  const parsed = new Date(normalized)
  if (!Number.isNaN(parsed.getTime())) {
    return formatDate(parsed)
  }

  return null
}

const isRowEmpty = (row: unknown[]): boolean => row.every((cell) => toText(cell) === '')

const buildRawPayload = (row: unknown[]) => {
  const productName = toText(row[1])
  const theoreticalYield = toNumber(row[2])
  const recordDate = toDate(row[3])

  if (!productName || theoreticalYield === null || recordDate === null) return null
  return {
    materialId: toText(row[0]),
    productName,
    theoreticalYield,
    recordDate
  }
}

const buildWorkshopPayload = (row: unknown[]) => {
  const workshopId = toText(row[0])
  const workerId = toText(row[1])
  const productName = toText(row[2])
  const declaredQuantity = toNumber(row[3])
  const recordDate = toDate(row[4])

  if (!workshopId || !workerId || !productName || declaredQuantity === null || recordDate === null) return null
  return {
    workshopId,
    workerId,
    productName,
    declaredQuantity,
    recordDate
  }
}

const buildWarehousePayload = (row: unknown[]) => {
  const batchNo = toText(row[0])
  const productName = toText(row[1])
  const inventoryQuantity = toNumber(row[2])
  const shippedQuantity = toNumber(row[3])
  const clientInfo = toText(row[4])
  const recordDate = toDate(row[5])

  if (!batchNo || !productName || inventoryQuantity === null || shippedQuantity === null || recordDate === null) return null
  return {
    batchNo,
    productName,
    inventoryQuantity,
    shippedQuantity,
    clientInfo,
    recordDate
  }
}

const parseRows = (type: ImportType, rows: unknown[][]) => {
  const invalidRows: number[] = []
  const payloads: Record<string, unknown>[] = []

  rows.forEach((row, index) => {
    if (isRowEmpty(row)) return

    let payload: Record<string, unknown> | null = null
    if (type === 'raw') payload = buildRawPayload(row)
    if (type === 'workshop') payload = buildWorkshopPayload(row)
    if (type === 'warehouse') payload = buildWarehousePayload(row)

    if (payload) {
      payloads.push(payload)
      return
    }
    invalidRows.push(index + 2) // +1 for 0-index, +1 for header row
  })

  return { payloads, invalidRows }
}

const importExcelFile = async (file: File, type: ImportType) => {
  if (importLoading[type]) return

  importLoading[type] = true
  try {
    const arrayBuffer = await file.arrayBuffer()
    const workbook = XLSX.read(arrayBuffer, { type: 'array', cellDates: true })
    const sheetName = workbook.SheetNames[0]
    if (!sheetName) {
      ElMessage.error('文件中未找到可读取的工作表')
      return
    }

    const rows = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName], { header: 1, defval: '' }) as unknown[][]
    if (rows.length <= 1) {
      ElMessage.warning('导入文件没有可提交的数据行')
      return
    }

    const dataRows = rows.slice(1)
    const { payloads, invalidRows } = parseRows(type, dataRows)

    if (!payloads.length) {
      ElMessage.error('导入失败：未识别到有效数据，请检查模板列顺序和日期格式')
      return
    }

    await api.post(BATCH_ENDPOINT_MAP[type], payloads)

    if (invalidRows.length > 0) {
      ElMessage.warning(`导入完成：成功 ${payloads.length} 条，失败 ${invalidRows.length} 行（${invalidRows.join(', ')}）`)
      return
    }

    ElMessage.success(`批量导入成功，共 ${payloads.length} 条`)
  } catch (error: any) {
    // Axios errors already show a global message in interceptor.
    if (!error?.response) {
      ElMessage.error('导入失败：文件解析异常，请确认 Excel 格式是否正确')
    }
  } finally {
    importLoading[type] = false
  }
}

const onRawImportChange: UploadProps['onChange'] = (uploadFile) => {
  if (!uploadFile.raw) {
    ElMessage.error('无法读取上传文件')
    return
  }
  void importExcelFile(uploadFile.raw, 'raw')
}

const onWorkshopImportChange: UploadProps['onChange'] = (uploadFile) => {
  if (!uploadFile.raw) {
    ElMessage.error('无法读取上传文件')
    return
  }
  void importExcelFile(uploadFile.raw, 'workshop')
}

const onWarehouseImportChange: UploadProps['onChange'] = (uploadFile) => {
  if (!uploadFile.raw) {
    ElMessage.error('无法读取上传文件')
    return
  }
  void importExcelFile(uploadFile.raw, 'warehouse')
}

const submitRaw = async () => {
  if (!rawForm.productName) return ElMessage.warning('请填写产品名称')
  btnLoading1.value = true
  try {
    await api.post('/records/raw-materials', rawForm)
    ElMessage.success('录入原材料数据成功')
    rawForm.theoreticalYield = 0
  } finally {
    btnLoading1.value = false
  }
}

const submitWork = async () => {
  if (!workForm.productName || !workForm.workerId) return ElMessage.warning('请填写产品及工人编号')
  btnLoading2.value = true
  try {
    await api.post('/records/workshop', workForm)
    ElMessage.success('录入车间计件成功')
    workForm.declaredQuantity = 0
  } finally {
    btnLoading2.value = false
  }
}

const downloadTemplate = (command: ImportType) => {
  const target = TEMPLATE_META[command]
  const wb = XLSX.utils.book_new()
  const ws = XLSX.utils.aoa_to_sheet([target.headers])
  XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
  XLSX.writeFile(wb, `${target.name}导入模板.xlsx`)

  ElMessage.success(`《${target.name}导入模板.xlsx》已开始下载...`)
}

const submitWarehouse = async () => {
  if (!warehouseForm.productName || !warehouseForm.batchNo) return ElMessage.warning('批次和产品名称必填')
  btnLoading3.value = true
  try {
    await api.post('/records/warehouse', warehouseForm)
    ElMessage.success('登记库房核销成功')
    warehouseForm.inventoryQuantity = 0
    warehouseForm.shippedQuantity = 0
  } finally {
    btnLoading3.value = false
  }
}
</script>
