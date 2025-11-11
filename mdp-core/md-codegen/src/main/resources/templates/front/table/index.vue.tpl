<script lang="ts" setup>
import { Page } from '@vben/common-ui';
import { Icon } from '@vben/components/icon';
import { TableAction } from '@vben/components/table-action';

import { Button } from 'ant-design-vue';

import { $t } from '#/locales';

import { useIndex } from './data';
import FormWrapper from './modules/wrapper.vue';

defineOptions({
  name: '#(table.getComment())',
  inheritAttrs: false,
});

const { Grid, formWrapperRef, handleRefresh, actions, dropActions } =
  useIndex();
</script>
<template>
  <Page auto-content-height>
    <FormWrapper ref="formWrapperRef" @success="handleRefresh" />
    <Grid>
      <template #toolbar-actions>
        <TableAction :actions="actions" :drop-down-actions="dropActions">
          <template #more>
            <Button class="ml-2">
              {{ $t('ui.actionTitle.moreOperation') }}
              <Icon icon="ant-design:down-outlined" />
            </Button>
          </template>
        </TableAction>
      </template>
    </Grid>
  </Page>
</template>
