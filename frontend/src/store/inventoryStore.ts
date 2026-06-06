import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useInventoryStore = defineStore('inventory', () => {
    const reportData = ref([])
    const loading = ref(false)

    const fetchReport = async (startDate: string, endDate: string, productName: string, tolerance: number = 0.05) => {
        loading.value = true
        try {
            const params: any = { tolerance }
            if (startDate) params.startDate = startDate
            if (endDate) params.endDate = endDate
            if (productName) params.productName = productName

            const res: any = await api.get('/analysis/cross-check', { params })
            if (res && res.code === 200) {
                reportData.value = res.data
            }
        } finally {
            loading.value = false
        }
    }

    return {
        reportData,
        loading,
        fetchReport
    }
})
