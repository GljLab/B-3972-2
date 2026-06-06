import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import DataEntry from '../views/DataEntry.vue'
import ExceptionManagement from '../views/ExceptionManagement.vue'
import ExceptionDetail from '../views/ExceptionDetail.vue'
import SupplierList from '../views/SupplierList.vue'
import SupplierDetail from '../views/SupplierDetail.vue'
import SupplierDashboard from '../views/SupplierDashboard.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/dashboard'
        },
        {
            path: '/dashboard',
            name: 'Dashboard',
            component: Dashboard
        },
        {
            path: '/entry',
            name: 'DataEntry',
            component: DataEntry
        },
        {
            path: '/exception',
            name: 'ExceptionManagement',
            component: ExceptionManagement
        },
        {
            path: '/exception/detail/:id',
            name: 'ExceptionDetail',
            component: ExceptionDetail
        },
        {
            path: '/suppliers',
            name: 'SupplierList',
            component: SupplierList
        },
        {
            path: '/suppliers/detail/:id',
            name: 'SupplierDetail',
            component: SupplierDetail
        },
        {
            path: '/supplier-dashboard',
            name: 'SupplierDashboard',
            component: SupplierDashboard
        }
    ]
})

export default router
