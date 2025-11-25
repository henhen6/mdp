<script lang="ts" setup>
import type { Emits } from '../data/move';

import { VbenVxeTree } from '@vben/plugins/vxe-tree';

import { Button } from 'ant-design-vue';

import { useMove } from '../data/move';
import { $t } from '#/locales';

defineOptions({
  name: '移动',
  inheritAttrs: false,
});

const emit = defineEmits<Emits>();
const {
  treeData,
  treeRef,
  state,
  handleMoveToRoot,
  handleCurrentMethod,
  Modal,
  modalApi,
} = useMove(emit);

defineExpose(modalApi);
</script>

<template>
    <Modal
      class="w-[50%]"
      :title="$t('common.title.moveLevel')"
      :confirm-text="$t('common.title.moveSelect')"
    >
    <template #center-footer>
      <Button @click="handleMoveToRoot">
        {{ $t('common.title.moveRoot') }}
      </Button>
    </template>

    <VbenVxeTree
      ref="treeRef"
      :data="treeData"
      show-line
      expand-all
      :loading="state.loading"
      title-field="name"
      :node-config="{
        isHover: true,
        isCurrent: true,
        currentMethod: handleCurrentMethod,
      }"
      :checkbox-config="{ checkStrictly: true }"
      :header="{
        title: $t('common.title.moveTips', { name: state.current?.name }),
        search: true,
        toolbar: true,
      }"
    />
  </Modal>
</template>
