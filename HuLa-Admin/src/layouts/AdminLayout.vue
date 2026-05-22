<template>
  <n-layout has-sider style="height: 100vh">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      show-trigger
      :native-scrollbar="false"
      style="background: #1f2d3d">
      <div class="sidebar-brand">
        <div class="brand-title">企业传书管理后台</div>
        <div v-if="tenantInfo" class="brand-sub">企业号：{{ tenantInfo.inviteCode }}</div>
      </div>
      <n-menu
        :value="activeMenu"
        :options="menuOptions"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        @update:value="onMenuSelect" />
      <div class="sidebar-footer">v1.14.1</div>
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="header-bar">
        <n-breadcrumb>
          <n-breadcrumb-item>首页</n-breadcrumb-item>
          <n-breadcrumb-item v-for="(c, i) in crumbs" :key="i">{{ c }}</n-breadcrumb-item>
        </n-breadcrumb>
        <n-dropdown :options="userMenu" @select="onUserMenu">
          <n-button text>admin ▾</n-button>
        </n-dropdown>
      </n-layout-header>
      <div class="tabs-bar">
        <n-tag
          v-for="tab in tabs"
          :key="tab.path"
          :type="route.path === tab.path ? 'info' : 'default'"
          closable
          @click="router.push(tab.path)"
          @close.prevent="closeTab(tab.path)">
          {{ tab.label }}
        </n-tag>
      </div>
      <n-layout-content class="main-content">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuOption } from 'naive-ui'
import { api, type TenantInfo } from '../api'

const route = useRoute()
const router = useRouter()
const tenantInfo = ref<TenantInfo | null>(null)

const tabs = ref([{ path: '/enterprise', label: '企业信息' }])

const menuOptions: MenuOption[] = [
  { label: '企业信息', key: '/enterprise' },
  {
    label: '用户管理',
    key: 'users',
    children: [
      { label: '注册用户', key: '/users/registered' },
      { label: '注销用户', key: '/users/deleted' },
      { label: '登录IP管理', key: '/users/login-ip' }
    ]
  },
  {
    label: '群组管理',
    key: 'groups',
    children: [{ label: '群聊列表', key: '/groups' }]
  },
  {
    label: '企业设置',
    key: 'settings',
    children: [
      { label: '功能配置', key: '/settings/policy' },
      { label: '修改密码', key: '/settings/password' }
    ]
  },
  {
    label: '后台设置',
    key: 'backend',
    children: [
      { label: '后台用户管理', key: '/backend/users' },
      { label: '后台角色管理', key: '/backend/roles' }
    ]
  }
]

const activeMenu = computed(() => route.path)

const crumbs = computed(() => {
  const map: Record<string, string[]> = {
    '/enterprise': ['企业信息'],
    '/users/registered': ['用户管理', '注册用户'],
    '/users/deleted': ['用户管理', '注销用户'],
    '/users/login-ip': ['用户管理', '登录IP管理'],
    '/groups': ['群组管理', '群聊列表'],
    '/settings/policy': ['企业设置', '功能配置'],
    '/settings/password': ['企业设置', '修改密码'],
    '/backend/users': ['后台设置', '后台用户管理'],
    '/backend/roles': ['后台设置', '后台角色管理']
  }
  return map[route.path] || []
})

const userMenu = [{ label: '退出登录', key: 'logout' }]

function onMenuSelect(key: string) {
  if (key.startsWith('/')) router.push(key)
}

function ensureTab(path: string, label: string) {
  if (!tabs.value.find((t) => t.path === path)) tabs.value.push({ path, label })
}

function closeTab(path: string) {
  tabs.value = tabs.value.filter((t) => t.path !== path)
  if (route.path === path && tabs.value.length) router.push(tabs.value[tabs.value.length - 1].path)
}

function onUserMenu(key: string) {
  if (key === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  }
}

watch(
  () => route.path,
  (path) => {
    const label = crumbs.value.join(' / ') || '页面'
    ensureTab(path, label.split(' / ').pop() || '页面')
  },
  { immediate: true }
)

onMounted(async () => {
  try {
    tenantInfo.value = await api<TenantInfo>('/im/admin/tenant/info')
    if (tenantInfo.value?.tenantId) {
      localStorage.setItem('tenantId', String(tenantInfo.value.tenantId))
    }
  } catch {
    tenantInfo.value = { tenantId: 1, inviteCode: 'DEFAULT', tenantName: '默认企业', registeredCount: 0, accountLimit: 0 }
  }
})
</script>

<style scoped>
.sidebar-brand {
  padding: 16px;
  color: #fff;
}
.brand-title {
  font-weight: 600;
  font-size: 14px;
}
.brand-sub {
  margin-top: 8px;
  font-size: 12px;
  opacity: 0.85;
}
.sidebar-footer {
  position: absolute;
  bottom: 12px;
  left: 16px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}
.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 48px;
}
.tabs-bar {
  padding: 8px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #e8e8e8;
}
.main-content {
  padding: 16px;
  background: #f0f2f5;
  min-height: calc(100vh - 120px);
}
</style>
