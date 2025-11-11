import type { #(table.buildEntityClassName())Type } from './model';

import type { PageParams, PageResult } from '#/api';

import { ContentTypeEnum, RequestEnum, ServicePrefixEnum } from '@vben/constants';

import { requestClient } from '#/api/request';

const SERVICE_PREFIX = "/#(packageConfig.subSystem)";
const MODULAR = `${SERVICE_PREFIX}#(table.buildControllerRequestMappingPrefix())/#(table.buildEntityVarName())`;

export const #(table.buildEntityClassName())Config = {
  Save: {
    url: `${MODULAR}/save`,
    method: RequestEnum.POST,
  },
  Update: {
    url: `${MODULAR}/update`,
    method: RequestEnum.POST,
  }
};

export namespace #(table.buildEntityClassName())Api {
  /**
   * 分页查询
   *
   * @param params 分页参数
   * @returns 每页数据
   */
  export const page = (params: PageParams<Partial<#(table.buildEntityClassName())Type.#(queryClassName)>>) =>
    requestClient.post<PageResult<#(table.buildEntityClassName())Type.#(voClassName)>>(
      `${MODULAR}/page`,
      params,
    );

  /**
   * 批量查询
   *
   * @param params 查询参数
   * @returns 所有数据
   */
  export const list = (params: Partial<#(table.buildEntityClassName())Type.#(queryClassName)>) =>
    requestClient.post<#(table.buildEntityClassName())Type.#(voClassName)[]>(
      `${MODULAR}/list`,
      params,
    );

  /**
   * 单体查询
   *
   * @param id id
   * @returns 单个对象
   */
  export const getById = (id: string) =>
    requestClient.get<#(table.buildEntityClassName())Type.#(voClassName)>(`${MODULAR}/getById`, {
      params: { id },
    });

  /**
   * 保存数据
   *
   * @param params 参数
   * @returns 主键ID
   */
  export const save = (params: Partial<#(table.buildEntityClassName())Type.#(dtoClassName)>) =>
    requestClient.post<string>(#(table.buildEntityClassName())Config.Save.url as string, params);

  /**
   * 修改数据
   *
   * @param params 参数
   * @returns 主键ID
   */
  export const update = (params: Partial<#(table.buildEntityClassName())Type.#(dtoClassName)>) =>
    requestClient.post<string>(#(table.buildEntityClassName())Config.Update.url as string, params);

  /**
   * 批量删除
   *
   * @param ids id
   * @returns 是否成功
   */
  export const remove = (ids: string[]) =>
    requestClient.post<boolean>(`${MODULAR}/delete`, ids);
#if(globalConfig.getFrontTreeGenerateEnable())

  /**
   * 批量查询 Tree
   *
   * @param params 查询参数
   * @returns 树结构数据
   */
  export const tree = (params: Partial<#(table.buildEntityClassName())Type.#(queryClassName)>) =>
    requestClient.post<#(table.buildEntityClassName())Type.#(voClassName)[]>(`${MODULAR}/tree`, params);

  /**
   * 移动
   * @param sourceId 待移动的id
   * @param targetId 目标id
   * @returns true
   */
  export async function move(sourceId: string, targetId?: string) {
    return requestClient.post<boolean>(
      `${MODULAR}/move`,
      {
        sourceId,
        targetId,
      },
      {
        headers: { 'Content-Type': ContentTypeEnum.FORM_URLENCODED },
      },
    );
  }
#end
}