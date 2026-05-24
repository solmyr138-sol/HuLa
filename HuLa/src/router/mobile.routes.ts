import type { RouteRecordRaw } from 'vue-router'
import { lazyView } from '@/router/lazy'

import ChatRoomLayout from '#/layout/chat-room/ChatRoomLayout.vue'
import FriendsLayout from '#/layout/friends/FriendsLayout.vue'
import MobileHome from '#/layout/index.vue'
import NoticeLayout from '#/layout/chat-room/NoticeLayout.vue'
import MobileLogin from '#/login.vue'
import Splashscreen from '#/views/Splashscreen.vue'

/** Critical path: sync import for first paint / auth gate */
export const mobileRoutes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'mobileRoot',
    redirect: '/mobile/login'
  },
  {
    path: '/mobile/login',
    name: 'mobileLogin',
    component: MobileLogin
  },
  {
    path: '/mobile/splashscreen',
    name: 'splashscreen',
    component: Splashscreen
  },
  {
    path: '/mobile/MobileForgetPassword',
    name: 'mobileForgetPassword',
    component: lazyView(() => import('#/views/MobileForgetPassword.vue'))
  },
  {
    path: '/mobile/serviceAgreement',
    name: 'mobileServiceAgreement',
    component: lazyView(() => import('#/views/MobileServiceAgreement.vue'))
  },
  {
    path: '/mobile/privacyAgreement',
    name: 'mobilePrivacyAgreement',
    component: lazyView(() => import('#/views/MobilePrivacyAgreement.vue'))
  },
  {
    path: '/mobile/syncData',
    name: 'mobileSyncData',
    component: lazyView(() => import('#/views/SyncData.vue'))
  },
  {
    path: '/mobile/chatRoom',
    name: 'mobileChatRoom',
    component: ChatRoomLayout,
    children: [
      {
        path: '',
        name: 'mobileChatRoomDefault',
        redirect: '/mobile/chatRoom/chatMain'
      },
      {
        path: 'chatMain/:uid?',
        name: 'mobileChatMain',
        component: lazyView(() => import('#/views/chat-room/MobileChatMain.vue')),
        props: true,
        meta: { keepAlive: true }
      },
      {
        path: 'setting',
        name: 'mobileChatSetting',
        component: lazyView(() => import('#/views/chat-room/ChatSetting.vue'))
      },
      {
        path: 'searchContent',
        name: 'mobileSearchChatContent',
        component: lazyView(() => import('#/views/chat-room/SearchChatContent.vue'))
      },
      {
        path: 'mediaViewer',
        name: 'mobileMediaViewer',
        component: lazyView(() => import('#/views/chat-room/MediaViewer.vue'))
      },
      {
        path: 'groupChatMember',
        name: 'mobileGroupChatMember',
        component: lazyView(() => import('#/views/chat-room/GroupChatMember.vue')),
        meta: { keepAlive: true }
      },
      {
        path: 'inviteGroupMember',
        name: 'mobileInviteGroupMember',
        component: lazyView(() => import('#/views/chat-room/MobileInviteGroupMember.vue'))
      },
      {
        path: 'notice',
        name: 'mobileChatNotice',
        component: NoticeLayout,
        children: [
          {
            path: '',
            name: 'mobileChatNoticeList',
            component: lazyView(() => import('#/views/chat-room/notice/NoticeList.vue'))
          },
          {
            path: 'add',
            name: 'mobileChatNoticeAdd',
            component: lazyView(() => import('#/views/chat-room/notice/NoticeEdit.vue'))
          },
          {
            path: 'edit/:id',
            name: 'mobileChatNoticeEdit',
            component: lazyView(() => import('#/views/chat-room/notice/NoticeEdit.vue'))
          },
          {
            path: 'detail/:id',
            name: 'mobileChatNoticeDetail',
            component: lazyView(() => import('#/views/chat-room/notice/NoticeDetail.vue'))
          }
        ]
      }
    ]
  },
  {
    path: '/mobile/home',
    name: 'mobileHome',
    component: MobileHome,
    children: [
      {
        path: '',
        name: 'mobileHomeDefault',
        redirect: '/mobile/message'
      },
      {
        path: '/mobile/message',
        name: 'mobileMessage',
        component: lazyView(() => import('#/views/message/index.vue'))
      },
      {
        path: '/mobile/friends',
        name: 'mobileFriends',
        component: lazyView(() => import('#/views/friends/index.vue'))
      },
      {
        path: '/mobile/community',
        name: 'mobileCommunity',
        component: lazyView(() => import('#/views/community/index.vue'))
      },
      {
        path: '/mobile/my',
        name: 'mobileMy',
        component: lazyView(() => import('#/views/my/MobileMyHome.vue'))
      }
    ]
  },
  {
    path: '/mobile/mobileMy',
    name: 'mobileMyLayout',
    component: lazyView(() => import('#/layout/my/MyLayout.vue')),
    children: [
      {
        path: '',
        name: 'mobileMyDefault',
        redirect: '/mobile/mobileMy/editProfile'
      },
      {
        path: 'editProfile',
        name: 'mobileEditProfile',
        component: lazyView(() => import('#/views/my/EditProfile.vue'))
      },
      {
        path: 'myMessages',
        name: 'mobileMyMessages',
        component: lazyView(() => import('#/views/my/MyMessages.vue'))
      },
      {
        path: 'editBio',
        name: 'mobileEditBio',
        component: lazyView(() => import('#/views/my/EditBio.vue'))
      },
      {
        path: 'editBirthday',
        name: 'mobileEditBirthday',
        component: lazyView(() => import('#/views/my/EditBirthday.vue'))
      },
      {
        path: 'publishCommunity',
        name: 'mobilePublishCommunity',
        component: lazyView(() => import('#/views/my/PublishCommunity.vue'))
      },
      {
        path: 'settings',
        name: 'MobileSettings',
        component: lazyView(() => import('#/views/my/MobileSettings.vue'))
      },
      {
        path: 'scanQRCode',
        name: 'mobileQRCode',
        component: lazyView(() => import('#/views/my/MobileQRCode.vue'))
      },
      {
        path: 'share',
        name: 'mobileShare',
        component: lazyView(() => import('#/views/my/Share.vue'))
      },
      {
        path: 'SimpleBio',
        name: 'mobileSimpleBio',
        component: lazyView(() => import('#/views/my/SimpleBio.vue'))
      },
      {
        path: 'aiAssistant',
        name: 'mobileAiAssistant',
        component: lazyView(() => import('#/views/my/AiAssistant.vue'))
      },
      {
        path: 'myAlbum',
        name: 'mobileMyAlbum',
        component: lazyView(() => import('#/views/my/MyAlbum.vue'))
      }
    ]
  },
  {
    path: '/mobile/mobileFriends',
    name: 'mobileFriendsLayout',
    component: FriendsLayout,
    children: [
      {
        path: '',
        name: 'mobileFriendsDefault',
        redirect: '/mobile/mobileFriends/addFriends'
      },
      {
        path: 'addFriends',
        name: 'mobileAddFriends',
        component: lazyView(() => import('#/views/friends/AddFriends.vue'))
      },
      {
        path: 'startGroupChat',
        name: 'mobileStartGroupChat',
        component: lazyView(() => import('#/views/friends/StartGroupChat.vue'))
      },
      {
        path: 'confirmAddFriend',
        name: 'mobileConfirmAddFriend',
        component: lazyView(() => import('#/views/friends/ConfirmAddFriend.vue'))
      },
      {
        path: 'confirmAddGroup',
        name: 'mobileConfirmAddGroup',
        component: lazyView(() => import('#/views/friends/ConfirmAddGroup.vue'))
      },
      {
        path: 'friendInfo/:uid',
        name: 'mobileFriendInfo',
        component: lazyView(() => import('#/views/friends/FriendInfo.vue'))
      }
    ]
  },
  {
    path: '/mobile/confirmQRLogin/:ip/:expireTime/:deviceType/:locPlace/:qrId',
    name: 'mobileConfirmQRLogin',
    component: lazyView(() => import('#/views/ConfirmQRLogin.vue')),
    props: true
  },
  {
    path: '/mobile/myQRCode',
    name: 'mobileMyQRCode',
    component: FriendsLayout,
    children: [
      {
        path: '',
        name: 'mobileMyQRCodePage',
        component: lazyView(() => import('#/views/MyQRCode.vue'))
      }
    ]
  },
  {
    path: '/mobile/rtcCall',
    name: 'rtcCall',
    component: lazyView(() => import('#/views/rtcCall/index.vue'))
  },
  {
    path: '/mobile/dynamic/:id',
    name: 'mobileDynamicDetail',
    component: lazyView(() => import('#/views/community/DynamicDetailPage.vue')),
    props: true
  }
]
