import type { FormState } from './form';

import { reactive } from 'vue';

import { useVbenForm } from '@vben/common-ui';
import { formToDetailBySchema } from '@vben/components/view';
import { ActionEnum } from '@vben/constants';

import { useSchema } from './form';

export function useDetail() {
  const state = reactive<FormState>({
    type: ActionEnum.ADD,
    formData: {},
  });

  const [DetailForm, formApi] = useVbenForm({
    schema: formToDetailBySchema(useSchema(state)),
    showDefaultActions: false,
  });

  async function loadFormData({ formData, type }: FormState) {
    state.type = type;
    state.formData = { ...formData };

    await formApi.setValues(state.formData);
  }

  return {
    DetailForm,
    state,
    loadFormData,
  };
}
