#for(importClass : table.buildTreeMoveTsxImports())
#(importClass)
#end

export interface Emits {
  (e: 'success'): void;
}

interface MoveState {
  loading: boolean;
  current: #(table.buildEntityClassName())Type.#(voClassName);
}
export function useMove(emit: Emits) {
  const treeRef = ref();

  // 树结构数据
  const treeData = ref<#(table.buildEntityClassName())Type.#(voClassName)[]>([]);
  // 表单临时状态值
  const state = reactive<MoveState>({
    loading: false,
    current: {} as #(table.buildEntityClassName())Type.#(voClassName),
  });
  const { createMessage, createConfirm } = useMessage();

  function getTree() {
    const treeR = unref(treeRef);
    if (!treeR) {
      throw new Error('树结构加载失败,请刷新页面');
    }
    return treeR.getTreeInstance();
  }

  const [Modal, modalApi] = useVbenModal({
    async onOpened() {
      const data = modalApi.getData();
      state.current = data?.current;

      getTree().clearCurrentNode();

      try {
        state.loading = true;
        treeData.value = await #(table.buildEntityClassName())Api.tree({});
      } finally {
        state.loading = false;
      }
    },
    async onConfirm() {
      await handleSubmit(false);
    },
  });

  async function handleMoveToRoot() {
    await handleSubmit(true);
  }
  async function handleSubmit(isMoveRoot = false) {
    modalApi.setState({ loading: true, confirmLoading: true });
    try {
      const id = state.current?.id;
      if (!id) {
        createMessage.error('请选择需要移动的节点');
      }

      const parent = getTree().getCurrentNode();
      let title = `确定要将【${state.current?.name}】移动到【根节点】`;
      // 父节点id
      let parentId = '';
      if (!isMoveRoot) {
        parentId = parent?.id;
        if (!parentId) {
          createMessage.error('尚未选择任何节点');
          return;
        }

        title = `确定要将【${state.current?.name}】移动成为【${parent.name}】的子节点吗？`;
      }

      createConfirm({
        iconType: 'warning',
        content: title,
        onOk: async () => {
          try {
            await #(table.buildEntityClassName())Api.move(state.current.id, parentId);
            createMessage.success('移动成功');
            modalApi.close();
            emit('success');
          } catch {}
        },
      });
    } finally {
      modalApi.setState({ loading: false, confirmLoading: false });
    }
  }

  function handleCurrentMethod({ node }: { node: #(table.buildEntityClassName())Type.#(voClassName) }) {
    if (node.id === state.current?.parentId) {
      createMessage.error('不能移动到他的父节点');
      return false;
    }

    if (node.id === state.current?.id) {
      createMessage.error('不能移动成为自己的子节点');
      return false;
    }

    if (node.treePath?.includes(`/${state.current?.id}/`)) {
      createMessage.error('不能移动到他的子孙节点');
      return false;
    }

    return true;
  }
  return {
    treeRef,
    getTree,
    state,
    treeData,
    handleMoveToRoot,
    handleCurrentMethod,
    Modal,
    modalApi,
  };
}
