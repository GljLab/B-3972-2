<template>
  <el-table :data="data" v-loading="loading" style="width: 100%" :row-class-name="tableRowClassName" border>
    <el-table-column prop="recordDate" label="业务日期" width="120" sortable />
    <el-table-column prop="productName" label="产品名称" width="150" sortable />
    
    <el-table-column label="原材料维度" align="center">
      <el-table-column prop="theoreticalYield" label="理论总产能" width="120" />
    </el-table-column>
    
    <el-table-column label="车间维度" align="center">
      <el-table-column prop="declaredQuantity" label="工人申报产量" width="120" />
    </el-table-column>
    
    <el-table-column label="库房发货维度" align="center">
      <el-table-column prop="actualTotalQuantity" label="实际总库存(盘点+发货)" width="180" />
    </el-table-column>
    
    <el-table-column label="核验结果分析" min-width="300">
      <template #default="{ row }">
        <div v-if="row.warnings && row.warnings.length === 0" class="flex items-center text-emerald-600 font-medium">
          <el-icon class="mr-1"><Select /></el-icon> 数据完全吻合
        </div>
        <div v-else class="flex flex-col gap-1">
          <div v-for="(warn, idx) in row.warnings" :key="idx" class="flex items-start text-xs rounded bg-red-50 text-red-700 px-2 py-1.5 border border-red-100">
            <el-icon class="mr-1 mt-0.5"><WarningFilled /></el-icon>
            <span class="leading-tight">{{ warn }}</span>
          </div>
        </div>
      </template>
    </el-table-column>

    <el-table-column label="操作" width="130" align="center" fixed="right">
      <template #default="{ row }">
        <div v-if="row.warnings && row.warnings.length > 0">
          <el-button
            v-if="!row.existingOrder"
            type="danger"
            size="small"
            @click="$emit('create-order', row)"
          >
            创建工单
          </el-button>
          <el-button
            v-else
            type="primary"
            size="small"
            link
            @click="$emit('view-order', row.existingOrder)"
          >
            查看工单
          </el-button>
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { PropType } from 'vue'

defineProps({
  data: {
    type: Array as PropType<any[]>,
    default: () => []
  },
  loading: Boolean
})

defineEmits(['create-order', 'view-order'])

const tableRowClassName = ({ row }: { row: any }) => {
  if (row.warnings && row.warnings.length > 0) {
    return 'bg-red-50/30'
  }
  return ''
}
</script>

<style scoped>
:deep(.el-table .bg-red-50\/30) {
  --el-table-tr-bg-color: #fef2f2;
}
</style>
