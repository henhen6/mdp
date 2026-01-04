import type { ExtendedModalApi } from '@vben/common-ui';
import type { AxiosRequestConfig } from '@vben/request';

import type { VbenFormSchema } from '@vben/components/adapter';
import type { FormSchemaExt } from '#/api';
#for(importClass : table.buildFormTsxImports())
#(importClass)
#end

import { reactive } from 'vue';

import { ActionEnum } from '@vben/constants';

import { useVbenForm } from '@vben/components/adapter';
import { getValidateRuleByVben } from '#/api/common/validate';
import { $t } from '#/locales';

export interface Emits {
  (e: 'success'): void;
}

export function useSchema(state: FormState): VbenFormSchema[] {
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

export interface FormState {
  type: ActionEnum;
  formData: Partial<#(table.buildEntityClassName())Type.#(voClassName)>;
}

export function useForm(emit: Emits) {
  const state = reactive<FormState>({
    type: ActionEnum.ADD,
    formData: {},
  });

  const [EditForm, formApi] = useVbenForm({
    schema: useSchema(state),
    showDefaultActions: false,
  });


  async function updateRules(
    Api: AxiosRequestConfig,
    customRules?: FormSchemaExt[],
  ) {
    const list = await getValidateRuleByVben({ Api, customRules });
    formApi.updateSchema(list);
  }

  async function loadFormData({ formData, type }: FormState) {
    state.type = type;
    state.formData = { ...formData };

    await formApi.setValues(state.formData);
    // 更新校验规则
    switch (type) {
      case ActionEnum.EDIT: {
        await updateRules(#(table.buildEntityClassName())Config.Update, []);
        break;
      }
      default: {
        await updateRules(#(table.buildEntityClassName())Config.Save, []);
        break;
      }
    }
  }

  async function handleSubmit(wrapperApi: ExtendedModalApi) {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    wrapperApi.lock();
    const data = await formApi.getValues<#(table.buildEntityClassName())Type.#(dtoClassName)>();
    try {
      await (state.type === ActionEnum.ADD
        ? #(table.buildEntityClassName())Api.save(data)
        : #(table.buildEntityClassName())Api.update(data));
      wrapperApi.close();
      emit('success');
    } finally {
      wrapperApi.unlock();
    }
  }
  return {
    EditForm,
    loadFormData,
    handleSubmit,
  };
}