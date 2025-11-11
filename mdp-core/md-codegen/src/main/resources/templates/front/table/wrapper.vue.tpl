<script lang="ts" setup>
import type { FormState } from '../data/form';

import { reactive, ref, toRaw } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { ActionEnum } from '@vben/constants';

import { $t } from '#/locales';

import DetailForm from './detail.vue';
import EditForm from './form.vue';

const emit = defineEmits<Emits>();

interface Emits {
  (e: 'success'): void;
}

const formRef = ref();
const detailRef = ref();
const state = reactive<FormState>({
  type: ActionEnum.ADD,
  formData: {},
});

const [Modal, modalApi] = useVbenModal({
  footer: state.type !== ActionEnum.VIEW,
  async onOpened() {
    state.type = modalApi.getData()?.type;
    state.formData = modalApi.getData()?.formData ?? {};
    if (state.type === ActionEnum.VIEW) {
      detailRef.value.loadFormData(toRaw(state));
    } else {
      formRef.value.loadFormData(toRaw(state));
    }
  },
  onConfirm() {
    formRef.value.handleSubmit(modalApi);
  },
});

defineExpose({ modalApi });
</script>
<template>
  <Modal :title="$t(`common.title.${state.type}`)" class="w-[60%]">
    <DetailForm v-show="state.type === ActionEnum.VIEW" ref="detailRef" />
    <EditForm
      v-show="state.type !== ActionEnum.VIEW"
      ref="formRef"
      @success="emit('success')"
    />
  </Modal>
</template>
