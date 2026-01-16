import { defineStore } from 'pinia'
import { setToken, clearToken, getToken } from '@/utils/token'
import { apiLogin, apiRegister, apiLogout, type LoginReq, type RegisterReq } from '@/api/auth'
import { apiMe, type MeResp } from '@/api/user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    me: null as MeResp | null,
    inited: false,
  }),

  getters: {
    isAuthed: (s) => !!s.token,
    displayName: (s) => s.me?.nickname || s.me?.username || '用户',
  },

  actions: {
    async login(payload: LoginReq) {
      const res = await apiLogin(payload)
      const token = res.data.data.accessToken
      if (!token) throw new Error('登录返回未包含 accessToken')
      this.token = token
      setToken(token)
      await this.fetchMe()
    },

    async register(payload: RegisterReq) {
      await apiRegister(payload)
    },

    async logout() {
      try { await apiLogout() } catch {}
      this.token = ''
      this.me = null
      this.inited = false
      clearToken()
    },

    async fetchMe() {
      if (!this.token) return
      const res = await apiMe()
      this.me = res.data.data
      this.inited = true
    },
  },
})
