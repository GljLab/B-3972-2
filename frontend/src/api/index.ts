let backendUrl = 'http://localhost:8080/api'
if (import.meta.env.PROD) {
    backendUrl = '/api'
}

import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
    baseURL: backendUrl,
    timeout: 10000
})

api.interceptors.response.use(
    (response) => {
        return response.data
    },
    (error) => {
        ElMessage.error(error.response?.data?.msg || '网络请求失败，请稍后再试')
        return Promise.reject(error)
    }
)

export interface Supplier {
    id?: number
    supplierCode?: string
    supplierName: string
    contactName?: string
    contactPhone?: string
    supplyType: string
    cooperationStartDate?: string
    creditScore?: number
    status?: string
    remark?: string
    createTime?: string
    updateTime?: string
    exceptionCount?: number
}

export interface SupplierScoreRecord {
    id?: number
    supplierId: number
    exceptionOrderId?: number
    exceptionOrderNo?: string
    deductScore: number
    scoreAfter: number
    responsibilityDescription?: string
    operatorName: string
    createTime?: string
}

export const supplierApi = {
    create: (data: Partial<Supplier>) => api.post('/suppliers', data),
    getById: (id: number) => api.get(`/suppliers/${id}`),
    update: (id: number, data: Partial<Supplier>) => api.put(`/suppliers/${id}`, data),
    delete: (id: number) => api.delete(`/suppliers/${id}`),
    list: (params?: {
        supplyType?: string
        creditLevel?: string
        status?: string
        keyword?: string
        page?: number
        pageSize?: number
    }) => api.get('/suppliers', { params }),
    listByType: (supplyType: string) => api.get(`/suppliers/by-type/${supplyType}`),
    deductScore: (id: number, data: {
        exceptionOrderId?: number
        deductScore: number
        responsibilityDescription: string
        operatorName: string
    }) => api.post(`/suppliers/${id}/deduct-score`, data),
    applyRecovery: (id: number, data: {
        recoveryReason: string
        certificationMaterial?: string
        approverName: string
    }) => api.post(`/suppliers/${id}/apply-recovery`, data),
    getScoreRecords: (id: number) => api.get(`/suppliers/${id}/score-records`),
    getExceptionOrders: (id: number, status?: string) => api.get(`/suppliers/${id}/exception-orders`, { params: { status } }),
    getCreditTrend: (id: number) => api.get(`/suppliers/${id}/credit-trend`),
    getDashboardStats: () => api.get('/suppliers/dashboard/stats'),
    getCreditDistribution: () => api.get('/suppliers/dashboard/credit-distribution'),
    getRiskTop10: () => api.get('/suppliers/dashboard/risk-top10'),
    recommendForException: (exceptionOrderId: number) => api.get(`/suppliers/recommend-for-exception/${exceptionOrderId}`)
}

export default api
