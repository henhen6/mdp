<script lang="ts" setup>
import type { Emits, TreeActionType } from '../data/tree';

import { VbenVxeTree } from '@vben/plugins/vxe-tree';

import {
  DeleteOutlined,
  DragOutlined,
  EditOutlined,
  PlusOutlined,
} from '@ant-design/icons-vue';
import { Button } from 'ant-design-vue';

import { $t } from '#/locales';

import { useTree } from '../data/tree';
import MoveModal from './move.vue';

const emit = defineEmits<Emits>();
const {
  treeRef,
  moveRef,
  treeData,
  checkNodeKeys,
  state,
  permCode,
  fetch,
  handleAddRoot,
  handleBatchDelete,
  handleCurrentChange,
  handleDelete,
  handleAdd,
  handleEdit,
  handleMove,
} = useTree(emit);

defineExpose<TreeActionType>({ fetch });
</script>
<template>
  <div>
    <Button
      class="mr-2"
      @click="handleAddRoot()"
      v-hasAnyPermission="[permCode.add]"
    >
      {{ $t('common.title.addRoot') }}
    </Button>
    <Button
      class="mr-2"
      v-hasAnyPermission="[permCode.delete]"
      @click="handleBatchDelete()"
    >
      {{ $t('common.title.delete') }}
    </Button>
    <Button class="mr-2" @click="fetch()">
      {{ $t('common.title.redo') }}
    </Button>

    <VbenVxeTree
      ref="treeRef"
      v-model:check-node-keys="checkNodeKeys"
      :data="treeData"
      show-line
      expand-all
      show-checkbox
      :loading="state.loading"
      title-field="name"
      :node-config="{
        isHover: true,
        isCurrent: true,
      }"
      :checkbox-config="{ checkStrictly: true }"
      :header="{
        title: $t('#(packageConfig.subSystem).#(packageConfig.module).#(table.buildEntityVarName()).title.table'),
        search: true,
        toolbar: true,
      }"
      @current-change="handleCurrentChange"
    >
      <template #extra="{ node }">
        <DragOutlined
          v-hasAnyPermission="[permCode.move]"
          @click.stop.prevent="handleMove(node)"
          :title="$t('common.title.move')"
          class="mr-2 cursor-pointer"
        />
        <PlusOutlined
          v-hasAnyPermission="[permCode.add]"
          @click.stop.prevent="handleAdd(node)"
          :title="$t('common.title.add')"
          class="mr-2 cursor-pointer"
        />
        <EditOutlined
          v-hasAnyPermission="[permCode.edit]"
          @click.stop.prevent="handleEdit(node)"
          :title="$t('common.title.edit')"
          class="mr-2 cursor-pointer"
        />
        <DeleteOutlined
          v-hasAnyPermission="[permCode.delete]"
          class="cursor-pointer"
          style="color: #ed6f6f"
          @click.stop.prevent="handleDelete(node)"
          :title="$t('common.title.delete')"
        />
      </template>
    </VbenVxeTree>

    <MoveModal ref="moveRef" @success="fetch" />
  </div>
</template>
