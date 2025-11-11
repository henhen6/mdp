<script lang="ts" setup>
import type { Emits } from '../data/move';

import { VbenVxeTree } from '@vben/plugins/vxe-tree';

import { Button } from 'ant-design-vue';

import { useMove } from '../data/move';

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
  <Modal class="w-[50%]" title="调整层级关系" confirm-text="移动到选中节点">
    <template #center-footer>
      <Button @click="handleMoveToRoot"> 移动到根节点</Button>
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
        title: `您正在移动【${state.current?.name}】`,
        search: true,
        toolbar: true,
      }"
    />
  </Modal>
</template>
