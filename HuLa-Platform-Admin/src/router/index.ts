import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layouts/PlatformLayout.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/Login.vue') },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: () => import('../views/Dashboard.vue') },
        { path: 'tenants', component: () => import('../views/TenantList.vue') },
        { path: 'tenants/create', component: () => import('../views/TenantCreate.vue') }
      ]
    }
  ]
})
