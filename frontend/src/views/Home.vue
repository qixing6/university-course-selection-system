<template>
  <div class="layout">
    <el-header class="header">
      <span class="title">课程注册系统</span>
      <div class="actions">
        <span class="welcome" v-if="displayName">你好，{{ displayName }}</span>
        <el-button type="danger" plain size="small" @click="onLogout">退出登录</el-button>
      </div>
    </el-header>
    <el-main class="main">
      <el-card shadow="never" class="welcome-card">
        <h2>首页</h2>
        <p v-if="profile">当前登录学号：<strong>{{ profile.studentId }}</strong></p>
        <p class="hint">可在此扩展选课入口、个人中心等模块。</p>
        <el-button type="primary" @click="refreshMe" :loading="loading">刷新个人信息</el-button>
      </el-card>
    </el-main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { clearAuth, getProfile, setProfile } from '../utils/auth'
import { fetchCurrentUser, logout } from '../api/studentAuth'

const router = useRouter()
const profile = ref(getProfile())
const loading = ref(false)

const displayName = computed(() => profile.value?.name || profile.value?.studentId || '')

onMounted(async () => {
  await refreshMe()
})

async function refreshMe() {
  loading.value = true
  try {
    const res = await fetchCurrentUser()
    profile.value = res.data
    setProfile(res.data)
  } catch {
    /* 401 由拦截器处理 */
  } finally {
    loading.value = false
  }
}

async function onLogout() {
  try {
    await logout()
  } catch {
    /* 即使失败也清理本地状态 */
  }
  clearAuth()
  profile.value = null
  ElMessage.success('已退出')
  router.replace('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 24px;
}
.title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}
.actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.welcome {
  color: #4b5563;
  font-size: 14px;
}
.main {
  padding: 32px 24px;
}
.welcome-card {
  max-width: 640px;
  border-radius: 12px;
}
.welcome-card h2 {
  margin-top: 0;
  color: #1f2937;
}
.hint {
  color: #6b7280;
  margin-bottom: 20px;
}
</style>
