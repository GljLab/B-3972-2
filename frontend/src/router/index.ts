import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import DataEntry from '../views/DataEntry.vue'
import ExceptionManagement from '../views/ExceptionManagement.vue'
import ExceptionDetail from '../views/ExceptionDetail.vue'

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
        }
    ]
})

export default router
