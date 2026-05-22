import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/enterprise' },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: 'enterprise', component: () => import('../views/enterprise/Info.vue') },
        {
          path: 'users/registered',
          component: () => import('../views/user/UserTable.vue'),
          props: { title: '注册用户', deletedOnly: false }
        },
        {
          path: 'users/deleted',
          component: () => import('../views/user/UserTable.vue'),
          props: { title: '注销用户', deletedOnly: true }
        },
        { path: 'users/login-ip', component: () => import('../views/user/LoginIp.vue') },
        { path: 'groups', component: () => import('../views/groups/List.vue') },
        { path: 'settings/policy', component: () => import('../views/settings/Policy.vue') },
        { path: 'settings/password', component: () => import('../views/settings/ChangePassword.vue') },
        { path: 'backend/users', component: () => import('../views/backend/Users.vue') },
        { path: 'backend/roles', component: () => import('../views/backend/Roles.vue') }
      ]
    },
    { path: '/login', component: () => import('../views/Login.vue') }
  ]
})
