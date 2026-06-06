import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useExceptionStore = defineStore('exception', () => {
    const orderList = ref<any[]>([])
    const total = ref(0)
    const stats = ref({ thisMonth: 0, pending: 0, processing: 0, closed: 0 })
    const trend = ref<Record<string, number>>({})
    const distribution = ref<Record<string, number>>({})
    const currentOrder = ref<any>(null)
    const loading = ref(false)

    const fetchOrders = async (params: {
        status?: string
        exceptionType?: string
        productName?: string
        creatorName?: string
        startDate?: string
        endDate?: string
        page?: number
        pageSize?: number
    } = {}) => {
        loading.value = true
        try {
            const res: any = await api.get('/exception-orders', { params })
            if (res && res.code === 200) {
                orderList.value = res.data.list
                total.value = res.data.total
            }
        } finally {
            loading.value = false
        }
    }

    const fetchStats = async () => {
        const res: any = await api.get('/exception-orders/stats')
        if (res && res.code === 200) {
            stats.value = res.data
        }
    }

    const fetchTrend = async () => {
        const res: any = await api.get('/exception-orders/trend')
        if (res && res.code === 200) {
            trend.value = res.data
        }
    }

    const fetchDistribution = async () => {
        const res: any = await api.get('/exception-orders/distribution')
        if (res && res.code === 200) {
            distribution.value = res.data
        }
    }

    const fetchOrderById = async (id: number) => {
        loading.value = true
        try {
            const res: any = await api.get(`/exception-orders/${id}`)
            if (res && res.code === 200) {
                currentOrder.value = res.data
            }
        } finally {
            loading.value = false
        }
    }

    const updateOrder = async (id: number, data: any) => {
        const res: any = await api.put(`/exception-orders/${id}`, data)
        if (res && res.code === 200) {
            currentOrder.value = res.data
        }
        return res
    }

    return {
        orderList,
        total,
        stats,
        trend,
        distribution,
        currentOrder,
        loading,
        fetchOrders,
        fetchStats,
        fetchTrend,
        fetchDistribution,
        fetchOrderById,
        updateOrder
    }
})
