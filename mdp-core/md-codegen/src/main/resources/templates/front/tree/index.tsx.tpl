#for(importClass : table.buildTreeIndexTsxImports())
#(importClass)
#end

export function useIndex() {
  const formRef = ref<FormActionType>();
  const treeRef = ref<TreeActionType>();
  function getFormRef(): FormActionType | undefined {
    return unref(formRef);
  }
  function getTreeRef(): TreeActionType | undefined {
    return unref(treeRef);
  }

  function handleEditSuccess() {
    getTreeRef()?.fetch();
  }

  // 选中树的节点
  function handleTreeSelect(
    parent: #(table.buildEntityClassName())Type.#(voClassName),
    record: Partial<#(table.buildEntityClassName())Type.#(voClassName)>,
  ) {
    getFormRef()?.setData({
      type: ActionEnum.VIEW,
      parent,
      record,
    });
  }

  // 编辑
  function handleTreeEdit(
    parent: #(table.buildEntityClassName())Type.#(voClassName),
    record: Partial<#(table.buildEntityClassName())Type.#(voClassName)>,
  ) {
    getFormRef()?.setData({
      type: ActionEnum.EDIT,
      parent,
      record,
    });
  }

  // 点击树的新增按钮
  function handleTreeAdd(
    parent: #(table.buildEntityClassName())Type.#(voClassName),
    record: Partial<#(table.buildEntityClassName())Type.#(voClassName)>,
  ) {
    getFormRef()?.setData({
      type: ActionEnum.ADD,
      parent,
      record,
    });
  }

  return {
    treeRef,
    formRef,
    handleEditSuccess,
    handleTreeSelect,
    handleTreeAdd,
    handleTreeEdit,
  };
}
