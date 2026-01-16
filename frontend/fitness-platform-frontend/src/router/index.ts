import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/LoginPage.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/auth/RegisterPage.vue'),
    meta: { title: '注册' },
  },

  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/pages/dashboard/DashboardPage.vue') },

      { path: 'workout', redirect: '/workout/submit' },
      { path: 'workout/submit', name: 'WorkoutSubmit', component: () => import('@/pages/workout/WorkoutSubmitPage.vue') },
      { path: 'workout/history', name: 'WorkoutHistory', component: () => import('@/pages/workout/WorkoutHistoryPage.vue') },

      { path: 'diet', redirect: '/diet/submit' },
      { path: 'diet/submit', name: 'DietSubmit', component: () => import('@/pages/diet/DietSubmitPage.vue') },
      { path: 'diet/history', name: 'DietHistory', component: () => import('@/pages/diet/DietHistoryPage.vue') },

      { path: 'report/weekly', name: 'ReportWeekly', component: () => import('@/pages/report/ReportWeeklyPage.vue') },

      { path: 'plan', redirect: '/plan/today' },
      { path: 'plan/today', name: 'PlanToday', component: () => import('@/pages/plan/PlanTodayPage.vue') },
      { path: 'plan/calendar', name: 'PlanCalendar', component: () => import('@/pages/plan/PlanCalendarPage.vue') },

      { path: 'body/trend', name: 'BodyTrend', component: () => import('@/pages/body/BodyTrendPage.vue') },

      { path: 'settings/profile', name: 'Settings', component: () => import('@/pages/settings/SettingsPage.vue') },
    ],
  },

  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  const title = to.meta?.title as string | undefined
  if (title) document.title = `${title} - 智能健身助手`
})

export default router
