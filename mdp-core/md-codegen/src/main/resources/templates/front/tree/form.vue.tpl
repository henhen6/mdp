<script lang="ts" setup>
import type { Emits, FormActionType } from '../data/form';

import { ActionEnum } from '@vben/constants';

import { Button, Card } from 'ant-design-vue';

import { $t } from '#/locales';

import { useForm } from '../data/form';

const emit = defineEmits<Emits>();
const { Form, isHorizontal, resetForm, setData, state, formReset, formSubmit } =
  useForm(emit);
defineExpose<FormActionType>({ resetForm, setData });
</script>
<template>
  <Card :title="state.title" :body-style="{ padding: '0px' }">
    <Form
      class="mx-4 mt-4"
      :layout="isHorizontal ? 'horizontal' : 'vertical'"
    />
    <template #extra>
      <div v-if="state.type !== ActionEnum.VIEW">
        <Button @click="formReset">{{ $t('common.resetText') }}</Button>
        <Button
          class="ml-2"
          type="primary"
          :loading="state.loading"
          @click="formSubmit"
        >
          {{ $t('common.saveText') }}
        </Button>
      </div>
    </template>
  </Card>
</template>
<style scoped lang="less">
@import '@vben/components/styles/common-form.less';
</style>