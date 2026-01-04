<script lang="ts" setup>
import { useDetail } from '../data/detail';

const { loadFormData, DetailForm } = useDetail();

defineExpose({
  loadFormData,
});
</script>
<template>
  <div class="common-form">
    <DetailForm />
  </div>
</template>
<style scoped lang="less">
@import '@vben/components/styles/common-form.less';
</style>
