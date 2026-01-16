import axios, { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearToken } from '@/utils/token'

export const http = axios.create({
  baseURL: '/',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    if (res?.data && typeof res.data === 'object' && 'success' in res.data) {
      if (res.data.success === false) {
        const msg = res.data.message || '请求失败'
        ElMessage.error(msg)
        return Promise.reject(new Error(msg))
      }
    }
    return res
  },
  (err: AxiosError<any>) => {
    const status = err.response?.status
    const msg =
      err.response?.data?.message ||
      err.response?.data?.msg ||
      err.message ||
      '请求失败'

    if (status === 401) {
      clearToken()
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(err)
    }

    ElMessage.error(msg)
    return Promise.reject(err)
  }
)
