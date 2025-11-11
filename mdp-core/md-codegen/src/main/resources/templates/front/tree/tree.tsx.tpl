#for(importClass : table.buildTreeTreeTsxImports())
#(importClass)
#end

interface TreeState {
  loading: boolean;
}

export interface TreeActionType {
  fetch: () => void;
}

export interface Emits {
  (
    e: 'add' | 'edit' | 'select',
    parent: #(table.buildEntityClassName())Type.#(voClassName),
    current: Partial<#(table.buildEntityClassName())Type.#(voClassName)>,
  ): void;
  (e: 'change', id: string, name: string): void;
}

export function useTree(emit: Emits) {
  // 树组件
  const treeRef = ref();
  // 移动组件
  const moveRef = ref();
  // 树数据
  const treeData = ref<#(table.buildEntityClassName())Type.#(voClassName)[]>([]);
  // 勾选中的数据
  const checkNodeKeys = ref<VxeTreePropTypes.CheckNodeKeys>([]);
  // 表单临时状态值
  const state = reactive<TreeState>({
    loading: false,
  });

  const { createMessage, createConfirm } = useMessage();

  function getTree() {
    const treeR = unref(treeRef);
    if (!treeR) {
      throw new Error('树结构加载失败,请刷新页面');
    }
    return treeR.getTreeInstance();
  }

  onMounted(async () => {
    await fetch();
  });

  async function fetch() {
    try {
      state.loading = true;
      treeData.value = await #(table.buildEntityClassName())Api.tree({});

      setTimeout(() => {
        getTree().clearCheckboxNode();
        checkNodeKeys.value = [];
      }, 0);
    } finally {
      state.loading = false;
    }
  }

  // 执行批量删除
  async function batchDelete(ids: string[]) {
    createConfirm({
      iconType: 'warning',
      content: '选中节点及其子结点将被永久删除, 是否确定删除？',
      onOk: async () => {
        try {
          await #(table.buildEntityClassName())Api.remove(ids);
          await fetch();
          createMessage.success($t('common.tips.deleteSuccess'));
        } catch {}
      },
    });
  }

  // 点击树外面的 批量删除
  function handleBatchDelete() {
    const checked = getTree().getCheckboxNodeIds();

    if (!checked || checked.length <= 0) {
      createMessage.warn($t('common.tips.pleaseSelectTheData'));
      return;
    }
    batchDelete(checked as string[]);
  }

  // 选中某行
  function handleCurrentChange({ node }: { node: #(table.buildEntityClassName())Type.#(voClassName) }) {
    const current = findNodeByKey(node.id, treeData.value);
    const parent = findNodeByKey(node?.parentId, treeData.value);

    emit('select', parent, current);
  }

  function handleMove(node: #(table.buildEntityClassName())Type.#(voClassName)) {
    moveRef.value.open();
    moveRef.value.setData({
      current: node,
    });
  }

  function handleAddRoot() {
    const parent = {} as #(table.buildEntityClassName())Type.#(voClassName);
    emit('add', parent, {});
  }
  function handleAdd(node: #(table.buildEntityClassName())Type.#(voClassName)) {
    const parent = findNodeByKey(node.id, treeData.value);
    getTree().setCurrentNodeId(node.id);
    emit('add', parent, {});
  }
  function handleEdit(node: #(table.buildEntityClassName())Type.#(voClassName)) {
    const parent = findNodeByKey(node.parentId, treeData.value);
    const current = findNodeByKey(node.id, treeData.value);
    getTree().setCurrentNodeId(node.id);
    emit('edit', parent, current);
  }
  function handleDelete(node: #(table.buildEntityClassName())Type.#(voClassName)) {
    batchDelete([node.id as string]);
  }

  return {
    treeRef,
    moveRef,
    treeData,
    checkNodeKeys,
    state,
    permCode,
    fetch,
    handleAdd,
    handleBatchDelete,
    handleCurrentChange,
    handleAddRoot,
    handleDelete,
    handleEdit,
    handleMove,
  };
}
