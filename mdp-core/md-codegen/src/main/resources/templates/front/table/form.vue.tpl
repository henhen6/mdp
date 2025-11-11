<script lang="ts" setup>
import type { Emits } from '../data/form';

import { useForm } from '../data/form';

const emit = defineEmits<Emits>();

const { handleSubmit, loadFormData, EditForm } = useForm(emit);

defineExpose({
  loadFormData,
  handleSubmit,
});
</script>
<template>
  <div class="common-form">
    <EditForm />
  </div>
</template>
<style scoped lang="less">
@import '@vben/components/styles/common-form.less';
</style>
