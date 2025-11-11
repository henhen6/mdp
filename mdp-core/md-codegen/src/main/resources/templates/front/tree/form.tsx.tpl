#for(importClass : table.buildTreeFormTsxImports())
#(importClass)
#end

export interface Emits {
  (e: 'success'): void;
}

export function useSchema(): VbenFormSchema[] {
  return [
#for(column : table.getSortedFormColumns())
#if(column.formConfig?.show == null || column.formConfig?.show)
    {
      component: '#(column.formConfig?.getComponentType())',
      fieldName: '#(column.property)',
      label: $t('#(packageConfig.subSystem).#(packageConfig.module).#(table.buildEntityVarName()).#(column.property)'),
      #if(column.formConfig?.hidden != null && column.formConfig?.hidden == false)
      dependencies: {
        show: false,
        triggerFields: ['id'],
      },
      #end
    },
#end
#end
  ];
}

const customRules: FormSchemaExt[] = [];

export interface DataType {
  parent: #(table.buildEntityClassName())Type.#(voClassName);
  record: Partial<#(table.buildEntityClassName())Type.#(voClassName)>;
  type: ActionEnum;
}
export interface FormState {
  loading: boolean;
  title: string;
  type: ActionEnum;
  parent: Partial<#(table.buildEntityClassName())Type.#(voClassName)>;
  formData: Partial<#(table.buildEntityClassName())Type.#(voClassName)>;
}

export interface FormActionType {
  setData: (data: DataType) => void;
  resetForm: () => void;
}

export function useForm(emit: Emits) {
  const state = reactive<FormState>({
    loading: false,
    title: '未选中任何数据',
    type: ActionEnum.VIEW,
    formData: {},
    parent: {},
  });
  const breakpoints = useBreakpoints(breakpointsTailwind);
  const isHorizontal = computed(() => breakpoints.greaterOrEqual('md').value);
  const { createMessage } = useMessage();
  const [Form, formApi] = useVbenForm({
    commonConfig: {
      colon: true,
      formItemClass: 'col-span-2 md:col-span-1',
    },
    schema: useSchema(),
    showDefaultActions: false,
    wrapperClass: 'grid-cols-2 gap-x-4',
  });

  async function resetForm() {
    state.title = '未选中任何资源';

    const parentComponentRef =
      formApi.getFieldComponentRef<InstanceType<typeof ApiTreeSelect>>(
        'parentId',
      );
    await parentComponentRef?.fetch();
  }

  async function setData(data: DataType) {
    await formApi.resetForm();
    state.type = data.type;
    state.loading = true;

    try {
      const { parent } = data;
      state.parent = parent;

      const vo: Partial<#(table.buildEntityClassName())Type.#(voClassName)> =
        state.type === ActionEnum.ADD
          ? { parentId: parent.id }
          : await #(table.buildEntityClassName())Api.getById(data.record.id as string);
      const record = { ...data.record, ...vo };
      state.formData = record;

      await formApi.setValues(record);

      state.title = $t(`common.title.${state.type}`);

      if (state.type !== ActionEnum.VIEW) {
        const validateApi =
          state.type === ActionEnum.EDIT
            ? #(table.buildEntityClassName())Config.Update
            : #(table.buildEntityClassName())Config.Save;
        const rules = await getValidateRuleByVben({
          Api: validateApi,
          customRules,
        });
        formApi.updateSchema(rules);
      }
    } finally {
      state.loading = false;
    }
  }

  function formReset() {
    formApi.resetForm();
  }

  async function formSubmit() {
    try {
      if (state.loading) {
        return;
      }

      state.loading = true;

      const { valid } = await formApi.validate();
      if (!valid) {
        return;
      }
      const data =
        await formApi.getValues<
          Omit<#(table.buildEntityClassName())Type.#(dtoClassName), 'children' | 'id'>
        >();

      state.type === ActionEnum.ADD
        ? await #(table.buildEntityClassName())Api.save(data)
        : await #(table.buildEntityClassName())Api.update(data);

      createMessage.success($t(`common.tips.${state.type}Success`));
      emit('success');
    } finally {
      state.loading = false;
    }
  }
  return {
    Form,
    state,
    isHorizontal,
    resetForm,
    setData,
    formReset,
    formSubmit,
  };
}
