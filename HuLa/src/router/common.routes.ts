import type { RouteRecordRaw } from 'vue-router'
import { lazyView } from '@/router/lazy'

/** Shared routes used on desktop (and Tauri multi-window); not registered on mobile APK. */
export const commonRoutes: Array<RouteRecordRaw> = [
  {
    path: '/manageGroupMember',
    name: 'manageGroupMember',
    component: lazyView(() => import('@/views/ManageGroupMember.vue'))
  },
  {
    path: '/login',
    name: 'login',
    component: lazyView(() => import('@/views/loginWindow/Login.vue'))
  },
  {
    path: '/splashscreen',
    name: 'desktopSplashscreen',
    component: lazyView(() => import('#/views/Splashscreen.vue'))
  },
  {
    path: '/register',
    name: 'register',
    component: lazyView(() => import('@/views/registerWindow/index.vue'))
  },
  {
    path: '/forgetPassword',
    name: 'forgetPassword',
    component: lazyView(() => import('@/views/forgetPasswordWindow/index.vue'))
  },
  {
    path: '/qrCode',
    name: 'qrCode',
    component: lazyView(() => import('@/views/loginWindow/QRCode.vue'))
  },
  {
    path: '/network',
    name: 'network',
    component: lazyView(() => import('@/views/loginWindow/Network.vue'))
  },
  {
    path: '/tray',
    name: 'tray',
    component: lazyView(() => import('@/views/Tray.vue'))
  },
  {
    path: '/notify',
    name: 'notify',
    component: lazyView(() => import('@/views/Notify.vue'))
  },
  {
    path: '/update',
    name: 'update',
    component: lazyView(() => import('@/views/Update.vue'))
  },
  {
    path: '/checkupdate',
    name: 'checkupdate',
    component: lazyView(() => import('@/views/CheckUpdate.vue'))
  },
  {
    path: '/capture',
    name: 'capture',
    component: lazyView(() => import('@/views/Capture.vue'))
  },
  {
    path: '/imageViewer',
    name: 'imageViewer',
    component: lazyView(() => import('@/views/imageViewerWindow/index.vue'))
  },
  {
    path: '/videoViewer',
    name: 'videoViewer',
    component: lazyView(() => import('@/views/videoViewerWindow/index.vue'))
  },
  {
    path: '/modal-serviceAgreement',
    name: 'modal-serviceAgreement',
    component: lazyView(() => import('@/views/agreementWindow/Server.vue'))
  },
  {
    path: '/modal-privacyAgreement',
    name: 'modal-privacyAgreement',
    component: lazyView(() => import('@/views/agreementWindow/Privacy.vue'))
  },
  {
    path: '/modal-remoteLogin',
    name: 'modal-remoteLogin',
    component: lazyView(() => import('@/views/loginWindow/RemoteLoginModal.vue'))
  }
]
