import { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard',
  },

  {
    path: '/dashboard',
    meta: { auth: true },
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        name: 'dashboard',
        path: '/dashboard',
        component: () => import('src/pages/DashBoard/DashBoard.vue'),
      },
    ],
  },

  {
    path: '/devices/list',
    meta: { auth: true },
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        name: 'DevicesList',
        path: '',
        component: () => import('pages/DevicesList.vue'),
      },
    ],
  },

  {
    path: '/devices/detail/:DeviceId',
    meta: { auth: true },
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        name: 'DeviceDetail',
        path: '',
        component: () => import('src/pages/DeviceDetail/DeviceDetail.vue'),
      },
    ],
  },

  {
    path: '/devices/logs/:DeviceId',
    meta: { auth: true },
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        name: 'DeviceLogs',
        path: '',
        component: () => import('src/pages/DeviceDetail/DeviceLogs.vue'),
      },
    ],
  },

  {
    path: '/login',
    meta: { auth: false },
    component: () => import('layouts/EmptyLayout.vue'),
    children: [{ path: '', component: () => import('pages/LoginPage.vue') }],
  },
  {
    path: '/register',
    meta: { auth: false },
    component: () => import('layouts/EmptyLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('src/pages/Register/RegisterPage.vue'),
      },
    ],
  },
  {
    path: '/confirm/:Hash',
    meta: { auth: false },
    component: () => import('layouts/EmptyLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('src/pages/Register/ConfirmRegister.vue'),
      },
    ],
  },
  {
    path: '/recovery',
    meta: { auth: false },
    component: () => import('layouts/EmptyLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('src/pages/Register/RecoveryPage.vue'),
      },
    ],
  },

  {
    path: '/userprofile',
    meta: { auth: true },
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        name: 'UserProfile',
        path: '',
        component: () => import('pages/UserProfile.vue'),
      },
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    meta: { auth: false },
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
