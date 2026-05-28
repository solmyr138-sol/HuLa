<template>
  <n-modal v-model:show="visible" preset="card" title="编辑用户" style="width: 520px" @after-leave="onClose">
    <n-form v-if="user" label-placement="left" label-width="96">
      <n-form-item label="账号">
        <n-input :value="user.account" disabled />
      </n-form-item>
      <n-form-item label="昵称">
        <n-input v-model:value="form.nickname" maxlength="32" />
      </n-form-item>
      <n-form-item label="手机号">
        <n-input v-model:value="form.email" placeholder="绑定手机号" />
      </n-form-item>
      <n-form-item label="状态">
        <n-select v-model:value="form.state" :options="stateOptions" />
      </n-form-item>
      <n-divider>登录密码（企业快书客户端）</n-divider>
      <n-form-item label="新密码">
        <n-input v-model:value="form.password" type="password" show-password-on="click" placeholder="留空则不修改" />
      </n-form-item>
      <n-form-item label="确认密码">
        <n-input v-model:value="form.confirmPassword" type="password" show-password-on="click" />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="visible = false">取消</n-button>
        <n-button type="primary" :loading="saving" @click="save">保存</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { resetImUserPassword, updateImUser, type ImUserRow } from '../api'

const props = defineProps<{
  show: boolean
  user: ImUserRow | null
}>()

const emit = defineEmits<{
  'update:show': [boolean]
  saved: []
}>()

const message = useMessage()
const visible = ref(false)
const saving = ref(false)

const stateOptions = [
  { label: '正常', value: 0 },
  { label: '封禁', value: 1 }
]

const form = reactive({
  nickname: '',
  email: '',
  state: 0,
  password: '',
  confirmPassword: ''
})

watch(
  () => props.show,
  (v) => {
    visible.value = v
    if (v && props.user) {
      form.nickname = props.user.nickname || ''
      form.email = props.user.mobile || ''
      form.state = props.user.state === 1 ? 1 : 0
      form.password = ''
      form.confirmPassword = ''
    }
  },
  { immediate: true }
)

watch(visible, (v) => emit('update:show', v))

function onClose() {
  form.password = ''
  form.confirmPassword = ''
}

async function save() {
  if (!props.user) return
  if (form.password && form.password !== form.confirmPassword) {
    message.warning('两次密码不一致')
    return
  }
  saving.value = true
  try {
    await updateImUser({
      uid: props.user.uid,
      nickname: form.nickname.trim(),
      email: form.email.trim(),
      state: form.state
    })
    if (form.password) {
      await resetImUserPassword({
        uid: props.user.uid,
        password: form.password,
        confirmPassword: form.confirmPassword
      })
    }
    message.success('已保存')
    visible.value = false
    emit('saved')
  } catch (e) {
    message.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    saving.value = false
  }
}
</script>
