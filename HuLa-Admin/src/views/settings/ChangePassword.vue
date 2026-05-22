<template>
  <n-card title="修改密码">
    <p class="mb-16px">当前账号 admin</p>
    <n-form label-placement="left" label-width="100">
      <n-form-item label="旧密码" required>
        <n-input v-model:value="oldPwd" type="password" placeholder="请输入旧密码" />
      </n-form-item>
      <n-form-item label="新密码" required>
        <n-input v-model:value="newPwd" type="password" placeholder="请输入新密码" />
      </n-form-item>
      <n-form-item label="确认密码" required>
        <n-input v-model:value="confirmPwd" type="password" placeholder="请确认新密码" />
      </n-form-item>
      <n-button type="primary" @click="submit">确认更改</n-button>
    </n-form>
  </n-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useMessage } from 'naive-ui'

const message = useMessage()
const oldPwd = ref('')
const newPwd = ref('')
const confirmPwd = ref('')

function submit() {
  if (!oldPwd.value || !newPwd.value || newPwd.value !== confirmPwd.value) {
    message.warning('请填写完整且两次新密码一致')
    return
  }
  message.success('密码已更新（需对接 OAuth 改密接口）')
}
</script>
