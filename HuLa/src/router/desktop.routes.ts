import type { RouteRecordRaw } from 'vue-router'
import { lazyView } from '@/router/lazy'

export const desktopRoutes: Array<RouteRecordRaw> = [
  {
    path: '/home',
    name: 'home',
    component: lazyView(() => import('@/layout/index.vue')),
    children: [
      {
        path: '/message',
        name: 'message',
        component: lazyView(() => import('@/views/homeWindow/message/index.vue'))
      },
      {
        path: '/friendsList',
        name: 'friendsList',
        component: lazyView(() => import('@/views/homeWindow/FriendsList.vue'))
      },
      {
        path: '/searchDetails',
        name: 'searchDetails',
        component: lazyView(() => import('@/views/homeWindow/SearchDetails.vue'))
      }
    ]
  },
  {
    path: '/robot',
    name: 'robot',
    component: lazyView(() => import('@/plugins/robot/index.vue')),
    children: [
      {
        path: '/welcome',
        name: 'welcome',
        component: lazyView(() => import('@/plugins/robot/views/Welcome.vue'))
      },
      {
        path: '/chat',
        name: 'chat',
        component: lazyView(() => import('@/plugins/robot/views/Chat.vue'))
      },
      {
        path: '/chatSettings',
        name: 'chatSettings',
        component: lazyView(() => import('@/plugins/robot/views/chatSettings/index.vue'))
      },
      {
        path: '/imageGeneration',
        name: 'imageGeneration',
        component: lazyView(() => import('@/plugins/robot/views/ImageGeneration.vue'))
      },
      {
        path: '/videoGeneration',
        name: 'videoGeneration',
        component: lazyView(() => import('@/plugins/robot/views/VideoGeneration.vue'))
      }
    ]
  },
  {
    path: '/mail',
    name: 'mail',
    component: lazyView(() => import('@/views/mailWindow/index.vue'))
  },
  {
    path: '/fileManager',
    name: 'fileManager',
    component: lazyView(() => import('@/views/fileManagerWindow/index.vue'))
  },
  {
    path: '/dynamic',
    name: 'dynamic',
    component: lazyView(() => import('@/plugins/dynamic/index.vue'))
  },
  {
    path: '/dynamic/:id',
    name: 'dynamicDetailWithId',
    component: lazyView(() => import('@/plugins/dynamic/detail.vue'))
  },
  {
    path: '/dynamicDetail',
    name: 'dynamicDetail',
    component: lazyView(() => import('@/plugins/dynamic/detail.vue'))
  },
  {
    path: '/onlineStatus',
    name: 'onlineStatus',
    component: lazyView(() => import('@/views/onlineStatusWindow/index.vue'))
  },
  {
    path: '/about',
    name: 'about',
    component: lazyView(() => import('@/views/aboutWindow/index.vue'))
  },
  {
    path: '/alone',
    name: 'alone',
    component: lazyView(() => import('@/views/homeWindow/message/Alone.vue'))
  },
  {
    path: '/sharedScreen',
    name: 'sharedScreen',
    component: lazyView(() => import('@/views/homeWindow/SharedScreen.vue'))
  },
  {
    path: '/modal-invite',
    name: 'modal-invite',
    component: lazyView(() => import('@/views/modalWindow/index.vue'))
  },
  {
    path: '/settings',
    name: 'settings',
    component: lazyView(() => import('@/views/moreWindow/settings/index.vue')),
    children: [
      {
        path: '/general',
        name: 'general',
        component: lazyView(() => import('@/views/moreWindow/settings/General.vue'))
      },
      {
        path: '/loginSetting',
        name: 'loginSetting',
        component: lazyView(() => import('@/views/moreWindow/settings/LoginSetting.vue'))
      },
      {
        path: '/notification',
        name: 'notification',
        component: lazyView(() => import('@/views/moreWindow/settings/Notification.vue'))
      },
      {
        path: '/versatile',
        name: 'versatile',
        component: lazyView(() => import('@/views/moreWindow/settings/Versatile.vue'))
      },
      {
        path: '/manageStore',
        name: 'manageStore',
        component: lazyView(() => import('@/views/moreWindow/settings/ManageStore.vue'))
      },
      {
        path: '/shortcut',
        name: 'shortcut',
        component: lazyView(() => import('@/views/moreWindow/settings/Shortcut.vue'))
      }
    ]
  },
  {
    path: '/announList/:roomId/:type',
    name: 'announList',
    component: lazyView(() => import('@/views/announWindow/index.vue'))
  },
  {
    path: '/previewFile',
    name: 'previewFile',
    component: lazyView(() => import('@/views/previewFileWindow/index.vue'))
  },
  {
    path: '/chat-history',
    name: 'chat-history',
    component: lazyView(() => import('@/views/chatHistory/index.vue'))
  },
  {
    path: '/rtcCall',
    name: 'rtcCall',
    component: lazyView(() => import('@/views/callWindow/index.vue'))
  },
  {
    path: '/multiMsg',
    name: 'multiMsg',
    component: lazyView(() => import('@/views/multiMsgWindow/index.vue'))
  },
  {
    path: '/searchFriend',
    name: 'searchFriend',
    component: lazyView(() => import('@/views/friendWindow/SearchFriend.vue'))
  },
  {
    path: '/addFriendVerify',
    name: 'addFriendVerify',
    component: lazyView(() => import('@/views/friendWindow/AddFriendVerify.vue'))
  },
  {
    path: '/addGroupVerify',
    name: 'addGroupVerify',
    component: lazyView(() => import('@/views/friendWindow/AddGroupVerify.vue'))
  }
]
