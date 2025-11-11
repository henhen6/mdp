<script lang="ts" setup>
import { Description } from '@vben/components/description';

import { useDetail } from '../data/detail';

const { loadFormData, schemaGroup, state } = useDetail();

defineExpose({
  loadFormData,
});
</script>
<template>
  <div class="desc-wrap">
    <div v-for="schema in schemaGroup" :key="schema.field" class="desc-card">
      <Description
        :data="state.formData"
        :schema="schema.children"
        :title="schema.label"
      />
    </div>
  </div>
</template>
<style scoped lang="less">
@import '@vben/components/styles/common-detail.less';
</style>
