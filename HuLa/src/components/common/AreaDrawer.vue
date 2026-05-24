<template>
  <NDrawer
    v-model:show="showModel"
    class="rounded-t-20px! overflow-hidden"
    position="bottom"
    round
    placement="bottom"
    default-height="350px">
    <VantArea
      :area-list="areaList"
      @scroll-into="onScrollInto"
      @cancel="onCancel"
      @change="onChange"
      @confirm="onConfirm"
      v-model="valueModel">
      <template #cancel>
        <n-button strong secondary round>
          {{ t('components.common.cancel') }}
        </n-button>
      </template>

      <template #confirm>
        <n-button strong secondary round type="success">
          {{ t('components.common.confirm') }}
        </n-button>
      </template>
    </VantArea>
  </NDrawer>
</template>

<script setup lang="ts">
import { invoke } from '@tauri-apps/api/core'
import { areaList as defaultAreaList } from '@vant/area-data'
import { NDrawer } from 'naive-ui'
import { AreaList, Area as VantArea } from 'vant'
import { useI18n } from 'vue-i18n'

const showModel = defineModel<boolean>('show')
const valueModel = defineModel<string>('value')

const { areaList = defaultAreaList } = defineProps<{
  areaList?: AreaList
}>()

const emit = defineEmits<{
  confirm: [payload: { selectedOptions?: Array<{ text?: string; name?: string }> }]
  cancel: []
  change: [payload: unknown]
}>()

const { t } = useI18n()

const onScrollInto = () => {
  invoke('trigger_haptic_feedback').catch(() => undefined)
}

const onConfirm = (payload: { selectedOptions?: Array<{ text?: string; name?: string }> }) => {
  emit('confirm', payload)
  showModel.value = false
}

const onCancel = () => {
  emit('cancel')
  showModel.value = false
}

const onChange = (payload: unknown) => {
  emit('change', payload)
}
</script>
