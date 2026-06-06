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

export default api
