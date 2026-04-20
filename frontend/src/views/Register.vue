<template>
  <div class="page">
    <el-card class="card" shadow="hover">
      <template #header>
        <div class="card-header">学生注册</div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="72px" @submit.prevent>
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="form.studentId" maxlength="10" placeholder="6-10 位数字" clearable />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" maxlength="64" placeholder="真实姓名" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="6-32 位"
            clearable
          />
        </el-form-item>
        <el-form-item label="确认" prop="confirm">
          <el-input
            v-model="form.confirm"
            type="password"
            show-password
            placeholder="再次输入密码"
            clearable
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="submit">
            注册
          </el-button>
        </el-form-item>
        <div class="footer">
          已有账号？
          <router-link to="/login">去登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/studentAuth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  studentId: '',
  name: '',
  password: '',
  confirm: ''
})

const validateConfirm = (_rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { pattern: /^[0-9]{6,10}$/, message: '学号须为 6-10 位数字', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 64, message: '姓名不能超过 64 字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function submit() {
  try {
    await formRef.value.validate()
    loading.value = true
    await register({
      studentId: form.studentId,
      name: form.name,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
  background: linear-gradient(135deg, #ecfdf5 0%, #f5f7fa 50%, #e0f2fe 100%);
  padding: 24px;
}
.card {
  width: 100%;
  max-width: 420px;
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
