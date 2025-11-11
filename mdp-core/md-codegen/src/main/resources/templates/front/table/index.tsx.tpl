import type { ExtendedModalApi, VbenFormSchema } from '@vben/common-ui';

import type {
  OnActionClickFn,
  OnActionClickParams,
  VxeTableGridOptions,
} from '@vben/components/adapter';
import type { ActionItem } from '@vben/components/table-action';

import type { PageParams } from '#/api';
#for(importClass : table.buildIndexTsxImports())
#(importClass)
#end

import { ref } from 'vue';

import { ActionEnum } from '@vben/constants';

import { useVbenVxeGrid } from '@vben/components/adapter';
import { useMessage } from '@vben/components/hooks';
import { $t } from '#/locales';

/**
 * 操作列类型枚举
 */
enum ActionType {
  DELETE = 'delete',
  EDIT = 'edit',
  VIEW = 'view',
}

/**
 * 表格搜索表单 schema
 * @returns VbenFormSchema[]
 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
#for(column : table.getSortedSearchColumns())
    {
      component: '#(column.searchConfig?.getComponentType())',
      fieldName: '#(column.property)',
      label: $t('#(packageConfig.subSystem).#(packageConfig.module).#(table.buildEntityVarName()).#(column.property)'),
    },
#end
  ];
}

/**
 * 表格列配置
 * @param onActionClick 操作栏按钮点击事件
 * @returns VxeTableGridOptions<#(table.buildEntityClassName())Type.#(voClassName)>['columns']
 */
export function useColumns(
  onActionClick: OnActionClickFn<#(table.buildEntityClassName())Type.#(voClassName)>,
): VxeTableGridOptions<#(table.buildEntityClassName())Type.#(voClassName)>['columns'] {
  return [
    { type: 'checkbox', width: 50 },
    { title: $t('common.title.seq'), type: 'seq', width: 50 },
#for(column : table.getSortedListColumns())
    {
      field: '#(column.property)',
      title: $t('#(packageConfig.subSystem).#(packageConfig.module).#(table.buildEntityVarName()).#(column.property)'),
    },
#end
    {
      cellRender: {
        attrs: {
          nameField: 'name',
          nameTitle: $t('#(packageConfig.subSystem).#(packageConfig.module).#(table.buildEntityVarName()).title.table'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          { code: ActionType.VIEW, auth: permCode.view },
          { code: ActionType.EDIT, auth: permCode.edit },
          { code: ActionType.DELETE,  auth: permCode.delete },
        ],
      },
      field: 'operation',
      showOverflow: false,
      title: $t('ui.actionTitle.operation'),
      width: 150,
    },
  ];
}

export function useIndex() {
  // 表单 页面
  const formWrapperRef = ref<{
    modalApi: ExtendedModalApi;
  }>();
  const { createConfirm, createMessage } = useMessage();

  // 操作栏按钮点击事件
  function onActionClick({
    code,
    row,
  }: OnActionClickParams<#(table.buildEntityClassName())Type.#(voClassName)>) {
    const actionHandlers: Record<
      string,
      (row: #(table.buildEntityClassName())Type.#(voClassName)) => void
    > = {
      [ActionType.DELETE]: handleDelete,
      [ActionType.EDIT]: handleEdit,
      [ActionType.VIEW]: handleView,
    };

    const handler = actionHandlers[code];
    if (handler) {
      handler(row);
    } else {
      throw new Error(`未知的操作类型: ${code}`);
    }
  }

  const [Grid, gridApi] = useVbenVxeGrid({
    formOptions: {
      fieldMappingTime: [['createdAt', ['createdAt_st', 'createdAt_ed']]],
      schema: useGridFormSchema(),
      submitOnChange: true,
      collapsed: true,
      commonConfig: {
        labelWidth: 60,
      },
    },
    gridOptions: {
      columns: useColumns(onActionClick),
      height: 'auto',
      keepSource: true,
      proxyConfig: {
        ajax: {
          query: async (params, formValues) => {
            const { page, sorts, filters } = params;
            const model = { ...formValues };
            // 处理筛选条件
            filters.forEach(({ field, values }) => {
              model[field] = values.join(',');
            });

            // 组装分页请求参数
            const queryParams: PageParams<
              Partial<#(table.buildEntityClassName())Type.#(queryClassName)>
            > = {
              model,
              size: page.pageSize,
              current: page.currentPage,
            };

            // 处理排序条件
            const firstSort = sorts[0];
            if (firstSort) {
              queryParams.sort = firstSort.field;
              queryParams.order = firstSort.order;
            }
            return await #(table.buildEntityClassName())Api.page(queryParams);
          },
        },
      },
    } as VxeTableGridOptions<#(table.buildEntityClassName())Type.#(voClassName)>,
  });

  /**
   * 单行删除
   * @param row 当前行
   */
  async function handleDelete(row: #(table.buildEntityClassName())Type.#(voClassName)) {
    await #(table.buildEntityClassName())Api.remove([row.id]);
    createMessage.success($t('common.tips.deleteSuccess'));
    handleRefresh();
  }

  /**
   * 批量删除已勾选数据
   */
  async function handleBatchDelete() {
    const selectedRows = gridApi.grid.getCheckboxRecords();
    const idList = selectedRows.map((item) => item.id);

    if (idList.length <= 0) {
      createMessage.warning($t('common.tips.pleaseSelectTheData'));
      return;
    }

    createConfirm({
      iconType: 'warning',
      content: $t('common.tips.confirmDelete'),
      onOk: async () => {
        try {
          await #(table.buildEntityClassName())Api.remove(idList);
          createMessage.success($t('common.tips.deleteSuccess'));
          handleRefresh();
        } catch {}
      },
    });
  }

  /**
   * 编辑当前行
   * @param row 当前行数据
   */
  function handleEdit(row: #(table.buildEntityClassName())Type.#(voClassName)) {
    formWrapperRef.value?.modalApi
      .setData({ type: ActionEnum.EDIT, formData: row })
      .open();
  }

  /**
   * 查看当前行
   * @param row 当前行数据
   */
  function handleView(row: #(table.buildEntityClassName())Type.#(voClassName)) {
    formWrapperRef.value?.modalApi
      .setData({ type: ActionEnum.VIEW, formData: row })
      .open();
  }

  /**
   * 新增
   */
  function handleAdd() {
    formWrapperRef.value?.modalApi
      .setData({ type: ActionEnum.ADD, formData: {} })
      .open();
  }

  /**
   * 刷新表格数据
   */
  function handleRefresh() {
    gridApi.query();
  }

  /**
   * 表头固定按钮
   */
  const actions: ActionItem[] = [
    {
      label: $t('ui.actionTitle.create'),
      type: 'primary',
      onClick: handleAdd.bind(null),
      auth: permCode.add,
    },
  ];

  /**
   * 表头下拉框按钮
   */
  const dropActions: ActionItem[] = [
    {
      label: $t('ui.actionTitle.delete'),
      auth: permCode.delete,
      onClick: handleBatchDelete.bind(null),
    },
  ];

  return {
    Grid,
    formWrapperRef,
    actions,
    dropActions,
    handleRefresh,
  };
}