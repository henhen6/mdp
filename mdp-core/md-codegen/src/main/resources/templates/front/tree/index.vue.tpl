<script lang="ts" setup>
import { reactive } from 'vue';

import { ColPage } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Tooltip } from 'ant-design-vue';

import { $t } from '#/locales';

import { useIndex } from './data';
import Form from './modules/form.vue';
import Tree from './modules/tree.vue';

defineOptions({
  name: '#(table.getComment())',
  inheritAttrs: false,
});

const props = reactive({
  leftCollapsedWidth: 5,
  // 左侧最大宽度百分比
  leftMaxWidth: 50,
  // 左侧最小宽度百分比
  leftMinWidth: 10,
  leftWidth: 30,
  rightWidth: 70,
  // 左侧可折叠
  leftCollapsible: true,
  // 显示拖动手柄
  splitHandle: true,
  // 可拖动调整宽度
  resizable: true,
  // 显示拖动分隔线
  splitLine: true,
});

const {
  treeRef,
  formRef,
  handleEditSuccess,
  handleTreeSelect,
  handleTreeAdd,
  handleTreeEdit,
} = useIndex();
</script>
<template>
  <ColPage v-bind="props" auto-content-height>
    <template #left="{ isCollapsed, expand }">
      <div v-if="isCollapsed" @click="expand">
        <Tooltip :title="$t('common.title.expand')">
          <Button shape="circle" type="primary">
            <template #icon>
              <IconifyIcon class="text-2xl" icon="bi:arrow-right" />
            </template>
          </Button>
        </Tooltip>
      </div>
      <div
        v-else
        class="mr-2 h-full overflow-auto rounded-[var(--radius)] border border-border bg-card p-2"
      >
        <Tree
          ref="treeRef"
          @select="handleTreeSelect"
          @add="handleTreeAdd"
          @edit="handleTreeEdit"
        />
      </div>
    </template>
    <Form ref="formRef" @success="handleEditSuccess" />
  </ColPage>
</template>
