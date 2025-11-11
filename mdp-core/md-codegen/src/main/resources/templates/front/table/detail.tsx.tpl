import type { FormState } from './form';

import type { DescItem } from '@vben/components/description';

import { reactive, ref } from 'vue';

import { schemaToDetailForm } from '@vben/components/utils';
import { ActionEnum } from '@vben/constants';

import { useSchema } from './form';

export function useDetail() {
  const state = reactive<FormState>({
    type: ActionEnum.VIEW,
    formData: {},
  });
  const schemaGroup = ref<DescItem[]>([]);

  async function loadFormData({ formData, type }: FormState) {
    state.type = type;
    state.formData = formData;

    schemaGroup.value = schemaToDetailForm(useSchema(), formData);
  }

  return { schemaGroup, state, loadFormData };
}