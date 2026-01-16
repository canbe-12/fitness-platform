import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'

import LoginPage from '@/pages/auth/LoginPage.vue'
import RegisterPage from '@/pages/auth/RegisterPage.vue'

import DashboardPage from '@/pages/dashboard/DashboardPage.vue'

import DietSubmitPage from '@/pages/diet/DietSubmitPage.vue'
import DietHistoryPage from '@/pages/diet/DietHistoryPage.vue'

import WorkoutSubmitPage from '@/pages/workout/WorkoutSubmitPage.vue'
import WorkoutHistoryPage from '@/pages/workout/WorkoutHistoryPage.vue'

import PlanTodayPage from '@/pages/plan/PlanTodayPage.vue'
import PlanCalendarPage from '@/pages/plan/PlanCalendarPage.vue'

import BodyTrendPage from '@/pages/body/BodyTrendPage.vue'
import ReportWeeklyPage from '@/pages/report/ReportWeeklyPage.vue'
import SettingsPage from '@/pages/settings/SettingsPage.vue'

import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },

    { path: '/login', name: 'Login', component: LoginPage },
    { path: '/register', name: 'Register', component: RegisterPage },

    {
      path: '/',
      component: AppLayout,
      children: [
        { path: 'dashboard', name: 'Dashboard', component: DashboardPage },

        { path: 'workout/submit', name: 'WorkoutSubmit', component: WorkoutSubmitPage },
        { path: 'workout/history', name: 'WorkoutHistory', component: WorkoutHistoryPage },

        { path: 'diet/submit', name: 'DietSubmit', component: DietSubmitPage },
        { path: 'diet/history', name: 'DietHistory', component: DietHistoryPage },

        { path: 'plan/today', name: 'PlanToday', component: PlanTodayPage },
        { path: 'plan/calendar', name: 'PlanCalendar', component: PlanCalendarPage },

        { path: 'body/trend', name: 'BodyTrend', component: BodyTrendPage },

        { path: 'report/weekly', name: 'ReportWeekly', component: ReportWeeklyPage },

        { path: 'settings', name: 'Settings', component: SettingsPage },

        ],
    },

    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  const publicPages = ['/login', '/register']
  if (publicPages.includes(to.path)) return true
  if (!auth.token) return '/login'
  return true
})

export default router
