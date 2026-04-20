import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearAuth, getToken } from './auth'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (err) => Promise.reject(err)
)

request.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body == null) {
      return res
    }
    const ok = body.code === 200 || body.code === 0 || body.success === true
    if (!ok) {
      const msg = body.message || body.msg || '操作失败，请稍后重试'
      ElMessage.error(msg)
      return Promise.reject(new Error(msg))
    }
    return body
  },
  (err) => {
    const status = err.response?.status
    if (status === 401) {
      ElMessage.warning('登录已失效，请重新登录')
      clearAuth()
      router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    } else {
      const backendMsg = err.response?.data?.message || err.response?.data?.msg
      const msg = backendMsg
        ? backendMsg
        : err.response
          ? '服务暂时不可用，请稍后重试'
          : '网络异常，请检查网络后重试'
      ElMessage.error(msg)
    }
    return Promise.reject(err)
  }
)

export default request
