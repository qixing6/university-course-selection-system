<template>
  <div class="page">
    <el-card class="card" shadow="hover">
      <template #header>
        <div class="card-header">学生登录</div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="72px" @submit.prevent>
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="form.studentId" maxlength="10" placeholder="6-10 位数字" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="6-32 位"
            clearable
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="submit">
            登录
          </el-button>
        </el-form-item>
        <div class="footer">
          还没有账号？
          <router-link to="/register">去注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/studentAuth'
import { setToken, setProfile } from '../utils/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  studentId: '',
  password: ''
})

const rules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^[0-9]{6,10}$/, message: '学号须为 6-10 位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ]
}

async function submit() {
  try {
    await formRef.value.validate()
    loading.value = true
    const res = await login({
      studentId: form.studentId,
      password: form.password
    })
    const payload = res.data
    setToken(payload.token)
    setProfile(payload.student)
    ElMessage.success('登录成功')
    const raw = route.query.redirect
    const redirect = Array.isArray(raw) ? raw[0] : raw
    router.replace(redirect && typeof redirect === 'string' ? redirect : '/')
  } catch (_) {
    // 错误提示在请求拦截器/表单校验中统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0fe 0%, #f5f7fa 50%, #eef2ff 100%);
  padding: 24px;
}
.card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
}
.card-header {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}
.footer {
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}
.footer a {
  color: #409eff;
  text-decoration: none;
}
.footer a:hover {
  text-decoration: underline;
}
</style>
