# 开放平台重构（计划 3：web-open 日志自助查询 + 事件码修正 + 文档同步）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 开发者中心（web-open）新增「我的接口回调记录 / 事件推送记录 / 接口调用日志」3 个自助查询页（后端只读 API + 前端页面，强制按当前开发者应用过滤）；EventTypeCode.Workbench 事件码语义归位；文档同步。

**计划系列：** 计划 1（md-open 推送域，已确认）、计划 2（网关 + SDK，已确认）→ 本计划。Task 1 依赖计划 1 的包结构（service 已按域分包），若计划 1 未执行，请把文中的 `top.mddata.open.service.notify.callback.NotifyInfoService` 等 import 对应回 `top.mddata.open.service.admin.*`。

**Architecture:** 设计文档见 `docs/superpowers/specs/2026-10-13-open-platform-refactor-design.md` §6.2。后端在 open-web 新增 3 个 `/client/**` 只读 Controller，数据隔离参照 `ClientAppController.page` 的 `createdBy = ContextUtil.getUserId()` 模式；前端复刻 web-open 现有 app 页面骨架（useVbenVxeGrid + data/*.tsx + api/model 分层）。

**Tech Stack:** Spring Boot 3、MyBatis-Flex；Vue3 + TypeScript + vben（useVbenVxeGrid、ApiDict、$t i18n）。

## Global Constraints

- **禁止任何 git 操作**，检查点以「编译/构建通过」代替提交。
- 只读接口：不新增任何写操作端点；数据隔离必须在服务端强制（不依赖前端传参）。
- console 管理端页面**不动**。
- 前端新增代码跟随 web-open 现有风格（vben + JUnit 式 data/*.tsx 组织），不引入新组件库。

## 关键既有事实（实施时不必再查）

- 数据隔离模式（`ClientAppController.page:182`）：`entity.setCreatedBy(ContextUtil.getUserId())`；`ContextUtil.getUserId(): Long`（`top.mddata.base.util.ContextUtil`）。
- 应用归属：`mdo_app.created_by` = 开发者用户 id；`NotifyInfo/EventPush/ApiCallLog` 实体均有 `appId` 字段（已确认 ApiCallLogBase:38）。
- 可复用类：`NotifyInfoQuery/NotifyInfoVo`、`EventPushQuery/EventPushVo`、`ApiCallLogQuery/ApiCallLogVo` 均已存在于 open-pojo。
- 前端骨架：`api/open/client/<模块>/{api.ts,model.d.ts}` + `views/open/client/<页面>/{index.vue, data/index.tsx, modules/*.vue}`；请求用 `requestClient` + `ServicePrefixEnum.OPEN`；权限码常量 `#/constants/perm/open/client/<页面>.ts`；菜单为后台配置（部署动作）。
- 事件码调用点：`SsoUserServiceImpl` 6 处 `EventTypeCode.Console.USER_EDIT`（:125 updateProfile、:383 updatePhone、:412 updatePhoneByWxMa、:445 updateEmail、:475 updatePassword、:499 resetPwByEmail）；`EventTypeCode.Workbench` 命名空间为空。console 侧（OrgServiceImpl/UserServiceImpl）事件码正确，不动。

---

### Task 1: 后端 client 端日志只读 API

**Files:**
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/client/ClientAppHelper.java`
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/client/ClientNotifyInfoController.java`
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/client/ClientEventPushController.java`
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/client/ClientApiCallLogController.java`

**Interfaces:**
- Consumes: `NotifyInfoService`（计划 1 后在 `service.notify.callback`）、`EventPushService`（`service.notify.event`）、`ApiCallLogService`（`service.api`）、`AppService`（`service.app`）
- Produces:
  - `POST /client/notifyInfo/page`、`GET /client/notifyInfo/getById`
  - `POST /client/eventPush/page`、`GET /client/eventPush/getById`
  - `POST /client/apiCallLog/page`、`GET /client/apiCallLog/getById`
  - 全部强制 `appId in (当前开发者的应用)`，getById 校验归属

- [ ] **Step 1: 创建 `ClientAppHelper`（数据隔离唯一步骤）**

```java
package top.mddata.open.controller.client;

import com.gitee.sop.support.exception.OpenException;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.enumeration.StoryMessageEnum;
import top.mddata.open.entity.admin.App;
import top.mddata.open.service.app.AppService;

import java.util.List;

/**
 * client 端数据隔离支撑：开发者只能看到自己创建的应用及其产生的数据。
 */
@Component
@RequiredArgsConstructor
public class ClientAppHelper {

    private final AppService appService;

    /** 当前开发者拥有的应用 id 列表 */
    public List<Long> myAppIds() {
        Long userId = ContextUtil.getUserId();
        return appService.list(QueryWrapper.create()
                        .select(App::getId)
                        .eq(App::getCreatedBy, userId))
                .stream().map(App::getId).toList();
    }

    /** 校验记录归属：不属于当前开发者时直接失败 */
    public void checkOwner(Long appId) {
        if (appId == null || !myAppIds().contains(appId)) {
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "无权查看该记录");
        }
    }
}
```

- [ ] **Step 2: 创建 `ClientNotifyInfoController`**

```java
package top.mddata.open.controller.client;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.query.admin.NotifyInfoQuery;
import top.mddata.open.service.notify.callback.NotifyInfoService;
import top.mddata.open.vo.admin.NotifyInfoVo;

import java.util.List;

/**
 * 我的接口回调记录（开发者自助查询，只读）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "我的接口回调记录")
@RequestMapping("/client/notifyInfo")
@RequiredArgsConstructor
public class ClientNotifyInfoController {

    private final NotifyInfoService notifyInfoService;
    private final ClientAppHelper clientAppHelper;

    /**
     * 分页查询当前开发者的回调记录。即使用户传了 appId 查询条件，
     * 末尾追加的 in(myAppIds) 仍会把结果限制在本人应用内。
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询当前开发者的接口回调记录")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<NotifyInfoVo>> page(@RequestBody @Validated PageParams<NotifyInfoQuery> params) {
        Page<NotifyInfoVo> page = Page.of(params.getCurrent(), params.getSize());
        List<Long> appIds = clientAppHelper.myAppIds();
        if (appIds.isEmpty()) {
            return R.success(page);
        }
        NotifyInfo entity = BeanUtil.toBean(params.getModel(), NotifyInfo.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        wrapper.and(NotifyInfo::getAppId).in(appIds);
        notifyInfoService.pageAs(page, wrapper, NotifyInfoVo.class);
        return R.success(page);
    }

    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取回调记录（仅本人应用）")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<NotifyInfoVo> get(@RequestParam Long id) {
        NotifyInfo entity = notifyInfoService.getById(id);
        ArgumentAssert.notNull(entity, "记录不存在");
        clientAppHelper.checkOwner(entity.getAppId());
        return R.success(BeanUtil.toBean(entity, NotifyInfoVo.class));
    }
}
```

- [ ] **Step 3: 创建 `ClientEventPushController`**（与 Step 2 同构，替换点如下）

- 类名/路径：`ClientEventPushController` / `/client/eventPush`，Tag `我的事件推送记录`
- 类型替换：`NotifyInfo→EventPush`、`NotifyInfoQuery→EventPushQuery`、`NotifyInfoVo→EventPushVo`、`notifyInfoService→eventPushService`（import `top.mddata.open.service.notify.event.EventPushService`）
- 过滤列：`wrapper.and(EventPush::getAppId).in(appIds)`

- [ ] **Step 4: 创建 `ClientApiCallLogController`**（同构，替换点如下）

- 类名/路径：`ClientApiCallLogController` / `/client/apiCallLog`，Tag `我的接口调用日志`
- 类型替换：`NotifyInfo→ApiCallLog`、`NotifyInfoQuery→ApiCallLogQuery`、`NotifyInfoVo→ApiCallLogVo`、`notifyInfoService→apiCallLogService`（import `top.mddata.open.service.api.ApiCallLogService`）
- 过滤列：`wrapper.and(ApiCallLog::getAppId).in(appIds)`

- [ ] **Step 5: 编译验证**

```bash
mvn compile -pl mdp-apps/md-open/open-web -am
```

预期：BUILD SUCCESS。越权回归用例（环境允许时）：开发者 A 登录后请求开发者 B 应用的回调记录 id，应返回"无权查看该记录"。

---

### Task 2: 前端「我的接口回调记录」页面（完整模板）

**Files:**
- Create: `mdp-vben/apps/web-open/src/api/open/client/notifyInfo/api.ts`
- Create: `mdp-vben/apps/web-open/src/api/open/client/notifyInfo/model.d.ts`
- Create: `mdp-vben/apps/web-open/src/views/open/client/notifyInfo/index.vue`
- Create: `mdp-vben/apps/web-open/src/views/open/client/notifyInfo/data/index.tsx`
- Create: `mdp-vben/apps/web-open/src/constants/perm/open/client/notifyInfo.ts`
- Modify: web-open zh-CN 语言包（参照 app 页面词条所在文件追加，定位命令见 Step 5）

**Interfaces:**
- Consumes: Task 1 的 `/client/notifyInfo/page`、`/client/notifyInfo/getById`
- Produces: 本页是 Task 3 两个页面的复制模板

- [ ] **Step 1: `api/open/client/notifyInfo/api.ts`**

```typescript
import type { NotifyInfoType } from './model';

import type { PageParams, PageResult } from '#/api';

import { ServicePrefixEnum } from '@vben/constants';

import { requestClient } from '#/api/request';

const SERVICE_PREFIX = ServicePrefixEnum.OPEN;
const MODULAR = `${SERVICE_PREFIX}/client/notifyInfo`;

export namespace NotifyInfoApi {
  /**
   * 分页查询我的接口回调记录
   *
   * @param params 分页参数
   * @returns 每页数据
   */
  export const page = (params: PageParams<Partial<NotifyInfoType.NotifyInfoQuery>>) =>
    requestClient.post<PageResult<NotifyInfoType.NotifyInfoVo>>(`${MODULAR}/page`, params);

  /**
   * 单体查询
   *
   * @param id 主键
   * @returns 回调记录详情
   */
  export const getById = (id: string) =>
    requestClient.get<NotifyInfoType.NotifyInfoVo>(`${MODULAR}/getById`, {
      params: { id },
    });
}
```

- [ ] **Step 2: `api/open/client/notifyInfo/model.d.ts`**

```typescript
export namespace NotifyInfoType {
  /** 查询参数（字段与后端 NotifyInfoQuery 对齐，按需精简） */
  export interface NotifyInfoQuery {
    apiName?: string;
    appKey?: string;
    execStatus?: string;
    callLogId?: string;
  }

  /** 回调记录 */
  export interface NotifyInfoVo {
    id: string;
    callLogId: string;
    appId: string;
    appKey: string;
    apiName: string;
    apiVersion: string;
    notifyUrl: string;
    notifyEncryptionType?: number;
    requestData?: string;
    requestCnt?: number;
    maxRequestCnt?: number;
    lastRequestTime?: string;
    nextRequestTime?: string;
    execStatus?: string;
    remark?: string;
    createdAt?: string;
  }
}
```

- [ ] **Step 3: `constants/perm/open/client/notifyInfo.ts`**

```typescript
/**
 * 按钮权限定义
 * 注意：若代码生成器自动生成的权限码跟开发者在《菜单管理》中配置的不同，请手动修改一致
 */
export const notifyInfo = {
  detial: 'open:client:notifyInfo:detial',
};
```

- [ ] **Step 4: `views/open/client/notifyInfo/index.vue`**

```vue
<script lang="ts" setup>
import { Page } from '@vben/common-ui';

import { Tag } from 'antdv-next';

import DetailDrawer from './modules/detail.vue';
import { statusOf, useIndex } from './data';

defineOptions({
  name: 'ClientNotifyInfo',
  inheritAttrs: false,
});

const { Grid, detailVisible, detailRecord } = useIndex();
</script>
<template>
  <Page auto-content-height>
    <Grid>
      <template #execStatus="{ row }">
        <Tag :color="statusOf(row.execStatus).color">
          {{ statusOf(row.execStatus).text }}
        </Tag>
      </template>
    </Grid>
    <DetailDrawer v-model:visible="detailVisible" :record="detailRecord" />
  </Page>
</template>
```

- [ ] **Step 5: `views/open/client/notifyInfo/data/index.tsx`（完全贴合 app 页骨架）**

```tsx
import type { VbenFormSchema } from '@vben/common-ui';
import type {
  OnActionClickFn,
  OnActionClickParams,
  VxeTableGridOptions,
} from '@vben/components/adapter';

import type { NotifyInfoType } from '#/api/open/client/notifyInfo/model';

import { ref } from 'vue';

import { useVbenVxeGrid } from '@vben/components/adapter';

import { NotifyInfoApi } from '#/api/open/client/notifyInfo/api';
import { notifyInfo as permCode } from '#/constants/perm/open/client/notifyInfo';
import { $t } from '#/locales';
import { createdAtCodec } from '#/utils/createdAtCodec';

/**
 * 操作列类型枚举
 */
enum ActionType {
  VIEW = 'view',
}

/** 执行状态枚举（与后端 ExecStatusEnum 一致） */
const EXEC_STATUS_MAP: Record<string, { color: string; text: string }> = {
  '0': { color: 'blue', text: '待执行' },
  '1': { color: 'green', text: '执行成功' },
  '2': { color: 'red', text: '执行失败' },
  '3': { color: 'orange', text: '重试结束' },
  '4': { color: 'gray', text: '手动结束' },
};

export function statusOf(code?: string) {
  return EXEC_STATUS_MAP[code ?? ''] ?? { color: 'default', text: code ?? '-' };
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'apiName',
      label: $t('open.client.notifyInfo.apiName'),
    },
    {
      component: 'Input',
      fieldName: 'appKey',
      label: $t('open.client.notifyInfo.appKey'),
    },
    {
      component: 'Select',
      fieldName: 'execStatus',
      label: $t('open.client.notifyInfo.execStatus'),
      componentProps: {
        options: Object.entries(EXEC_STATUS_MAP).map(([value, item]) => ({
          label: item.text,
          value,
        })),
      },
    },
  ];
}

export function useColumns(
  onActionClick: OnActionClickFn<NotifyInfoType.NotifyInfoVo>,
): VxeTableGridOptions<NotifyInfoType.NotifyInfoVo>['columns'] {
  return [
    { field: 'apiName', title: $t('open.client.notifyInfo.apiName'), minWidth: 140 },
    { field: 'apiVersion', title: $t('open.client.notifyInfo.apiVersion'), width: 90 },
    { field: 'notifyUrl', title: $t('open.client.notifyInfo.notifyUrl'), minWidth: 200, showOverflow: true },
    {
      field: 'execStatus',
      title: $t('open.client.notifyInfo.execStatus'),
      width: 100,
      slots: { default: 'execStatus' },
    },
    { field: 'requestCnt', title: $t('open.client.notifyInfo.requestCnt'), width: 100 },
    { field: 'maxRequestCnt', title: $t('open.client.notifyInfo.maxRequestCnt'), width: 110 },
    { field: 'lastRequestTime', title: $t('open.client.notifyInfo.lastRequestTime'), width: 170 },
    { field: 'nextRequestTime', title: $t('open.client.notifyInfo.nextRequestTime'), width: 170 },
    {
      cellRender: {
        attrs: {
          nameField: 'apiName',
          nameTitle: $t('open.client.notifyInfo.title.table'),
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [{ code: ActionType.VIEW, auth: permCode.detial }],
      },
      field: 'operation',
      fixed: 'right',
      showOverflow: false,
      title: $t('ui.actionTitle.operation'),
      width: 100,
    },
  ];
}

export function useIndex() {
  const detailVisible = ref(false);
  const detailRecord = ref<NotifyInfoType.NotifyInfoVo>({} as NotifyInfoType.NotifyInfoVo);

  function onActionClick({ code, row }: OnActionClickParams<NotifyInfoType.NotifyInfoVo>) {
    if (code === ActionType.VIEW) {
      handleView(row);
    } else {
      throw new Error(`未知的操作类型: ${code}`);
    }
  }

  async function handleView(row: NotifyInfoType.NotifyInfoVo) {
    detailRecord.value = await NotifyInfoApi.getById(row.id);
    detailVisible.value = true;
  }

  const [Grid, gridApi] = useVbenVxeGrid({
    formOptions: {
      codec: createdAtCodec,
      schema: useGridFormSchema(),
      submitOnChange: true,
      collapsed: true,
      commonConfig: {
        labelWidth: 'auto',
      },
    },
    gridOptions: {
      columns: useColumns(onActionClick),
      height: 'auto',
      keepSource: true,
      proxyConfig: {
        ajax: {
          query: async (params, formValues) => {
            const { page, sorts } = params;
            const queryParams: any = { model: { ...formValues } };
            const firstSort = sorts[0];
            if (firstSort) {
              queryParams.sort = firstSort.field;
              queryParams.order = firstSort.order;
            }
            queryParams.size = page.pageSize;
            queryParams.current = page.currentPage;
            return await NotifyInfoApi.page(queryParams);
          },
        },
      },
    } as VxeTableGridOptions<NotifyInfoType.NotifyInfoVo>,
  });

  return {
    Grid,
    gridApi,
    detailVisible,
    detailRecord,
  };
}
```

- [ ] **Step 5.1: `views/open/client/notifyInfo/modules/detail.vue`（详情抽屉）**

```vue
<script lang="ts" setup>
import type { NotifyInfoType } from '#/api/open/client/notifyInfo/model';

import { computed } from 'vue';

import { Descriptions, DescriptionsItem, Drawer } from 'antdv-next';

const props = defineProps<{
  record: NotifyInfoType.NotifyInfoVo;
  visible: boolean;
}>();
const emit = defineEmits(['update:visible']);

const prettyRequestData = computed(() => {
  try {
    return JSON.stringify(JSON.parse(props.record?.requestData ?? ''), null, 2);
  } catch {
    return props.record?.requestData;
  }
});
</script>
<template>
  <Drawer
    :open="visible"
    title="回调详情"
    width="720"
    @close="emit('update:visible', false)"
  >
    <Descriptions :column="1" bordered size="small">
      <DescriptionsItem label="接口名称">{{ record.apiName }}</DescriptionsItem>
      <DescriptionsItem label="接口版本">{{ record.apiVersion }}</DescriptionsItem>
      <DescriptionsItem label="回调地址">{{ record.notifyUrl }}</DescriptionsItem>
      <DescriptionsItem label="请求次数">
        {{ record.requestCnt }} / {{ record.maxRequestCnt }}
      </DescriptionsItem>
      <DescriptionsItem label="最近请求时间">{{ record.lastRequestTime }}</DescriptionsItem>
      <DescriptionsItem label="下次请求时间">{{ record.nextRequestTime }}</DescriptionsItem>
      <DescriptionsItem label="请求报文">
        <pre style="white-space: pre-wrap;">{{ prettyRequestData }}</pre>
      </DescriptionsItem>
    </Descriptions>
  </Drawer>
</template>
```

i18n 补充 key（随 Step 6 一并加入）：`title.table: "回调记录列表"`。

- [ ] **Step 6: i18n 词条**

定位词条文件：`grep -rn "open.client.app" /Users/tangyh/gitee/mdp-vben/apps/web-open/src/locales`，在 app 词条同文件同结构追加：

```json
"notifyInfo": {
  "apiName": "接口名称",
  "apiVersion": "接口版本",
  "appKey": "应用Key",
  "notifyUrl": "回调地址",
  "execStatus": "执行状态",
  "requestCnt": "已请求次数",
  "maxRequestCnt": "最大请求次数",
  "lastRequestTime": "最近请求时间",
  "nextRequestTime": "下次请求时间",
  "detail": "回调详情"
}
```

（挂到与 `open.client.app` 同级的 `open.client` 节点下。）

- [ ] **Step 7: 前端构建验证**

```bash
cd /Users/tangyh/gitee/mdp-vben/apps/web-open && pnpm build
```

预期：构建成功，无 TS 错误。

---

### Task 3: 前端「我的事件推送记录」+「我的接口调用日志」页面

**Files:**
- Create: `api/open/client/eventPush/{api.ts,model.d.ts}`、`api/open/client/apiCallLog/{api.ts,model.d.ts}`
- Create: `views/open/client/eventPush/{index.vue,data/index.tsx}`、`views/open/client/apiCallLog/{index.vue,data/index.tsx}`
- Create: `constants/perm/open/client/eventPush.ts`、`apiCallLog.ts`

**Interfaces:**
- Consumes: `/client/eventPush/page|getById`、`/client/apiCallLog/page|getById`
- Produces: 两个页面，骨架与 Task 2 完全相同

- [ ] **Step 1: 复制 Task 2 四件套，逐点替换**

eventPush 页面替换点：
- 目录/文件：notifyInfo → eventPush；组件 name：`ClientEventPush`
- API 前缀：`${SERVICE_PREFIX}/client/eventPush`；namespace：`EventPushApi`；类型：`EventPushType.EventPushQuery/EventPushVo`
- `EventPushQuery` 字段：`eventCode?、appKey?、execStatus?、eventTriggerId?`
- `EventPushVo` 字段：`id、eventTriggerId、eventCode、appId、appKey、notifyUrl、notifyEncryptionType?、requestData?、requestCnt?、maxRequestCnt?、lastRequestTime?、nextRequestTime?、execStatus?、remark?、createdAt?`
- 列定义：`id / eventCode(事件编码) / notifyUrl / execStatus(同一状态映射) / requestCnt / maxRequestCnt / lastRequestTime / nextRequestTime / 操作列`
- 查询表单：`eventCode(Input)、appKey(Input)、execStatus(Select 同状态映射)`
- 权限码：`open:client:eventPush:detial`；i18n 前缀：`open.client.eventPush`，词条：`eventCode: "事件编码"`，其余与 notifyInfo 相同 key

apiCallLog 页面替换点：
- 目录/文件：notifyInfo → apiCallLog；组件 name：`ClientApiCallLog`
- API 前缀：`${SERVICE_PREFIX}/client/apiCallLog`；namespace：`ApiCallLogApi`；类型：`ApiCallLogType.ApiCallLogQuery/ApiCallLogVo`
- `ApiCallLogQuery` 字段：`apiName?、appKey?、execStatus?`
- `ApiCallLogVo` 字段：实施前先读 `ApiCallLogBase` 实际字段清单照搬（路径 `mdp-apps/md-open/open-pojo/.../entity/admin/base/ApiCallLogBase.java`），至少含 `id、appId、appKey、apiName、apiVersion、requestData?、responseData?、execStatus?、errorMsg?、createdAt?`
- 列定义：`id / apiName / apiVersion / execStatus(同一状态映射) / errorMsg(showOverflow) / createdAt / 操作列`
- 查询表单：`apiName(Input)、appKey(Input)、execStatus(Select 同状态映射)`
- 权限码：`open:client:apiCallLog:detial`；i18n 前缀：`open.client.apiCallLog`，词条：`errorMsg: "错误信息"`，其余相同

- [ ] **Step 2: 构建验证**

```bash
cd /Users/tangyh/gitee/mdp-vben/apps/web-open && pnpm build
```

预期：构建成功。

---

### Task 4: EventTypeCode.Workbench 事件码语义归位

**Files:**
- Modify: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/constant/EventTypeCode.java`
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/SsoUserServiceImpl.java`（:125、:383、:412、:445、:475、:499 共 6 处）

**Interfaces:**
- Consumes: 无
- Produces: `EventTypeCode.Workbench.USER_EDIT = "user:edit"`（**码值与 Console.USER_EDIT 相同**，订阅配置与推送报文零影响）

- [ ] **Step 1: 补充 Workbench 命名空间**

```java
interface Workbench {
    /** 用户资料变更（编辑资料、手机号、邮箱、密码等自助修改） */
    String USER_EDIT = "user:edit";
}
```

设计说明（写入注释）：码值与 Console.USER_EDIT 一致是有意的——订阅方关心的是「用户数据变了」这一事实，不关心变更来源；此处修改只是把 workbench 侧的调用归位到正确命名空间。

- [ ] **Step 2: 替换 6 处调用**

```bash
cd /Users/tangyh/gitee/mdp
sed -i '' 's/EventTypeCode\.Console\.USER_EDIT/EventTypeCode.Workbench.USER_EDIT/g' \
  mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/SsoUserServiceImpl.java
grep -c "EventTypeCode.Workbench.USER_EDIT" mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/SsoUserServiceImpl.java
```

预期：输出 6。

- [ ] **Step 3: 编译验证**

```bash
mvn compile -pl mdp-apps/md-public/md-common-pojo,mdp-apps/md-workbench/workbench-service -am
```

预期：BUILD SUCCESS。

---

### Task 5: 文档同步 + 菜单注册 + 收尾验证

**Files:**
- Modify: `mdp-apps/md-open/README.md`（重试描述修正）
- Modify: `mdp-docs` 中 http 未同步为 https 的页面（定位命令见 Step 2）
- Create/Modify: `mdp-docs/src/doc/integration/事件回调.md`（补充事件清单表）

- [ ] **Step 1: md-open README 修正**

「最多重试3次，分别是：5m,30m,6h」改为「最多重试 5 次，分别是：2m, 5m, 30m, 6h, 12h」（与 `NotifyProperties.timeLevel` 默认值一致）。

- [ ] **Step 2: mdp-docs http→https 检查**

```bash
grep -rn "http://console.mddata.top\|http://open.mddata.top\|http://api.mddata.top" /Users/tangyh/gitee/mdp-docs/src --include="*.md" | head -20
```

逐个替换为 https（主仓 b3216960 已完成同项改造，docs 同步）。

- [ ] **Step 3: 事件清单补充**

在 `mdp-docs/src/doc/integration/事件回调.md` 补充当前事件码表：

| 事件码 | 来源 | 触发场景 |
|---|---|---|
| `org:add` | console | 新增组织 |
| `org:edit` | console | 修改组织 |
| `org:delete` | console | 删除组织 |
| `user:add` | console | 新增用户（含批量导入） |
| `user:edit` | console / workbench | 修改用户（管理端编辑、用户自助改资料/手机号/邮箱/密码） |
| `user:delete` | console | 删除用户 |

- [ ] **Step 4: 菜单注册（部署动作，写入完成报告）**

在 console《菜单管理》为 web-open 应用新增 3 个菜单（参照《我的应用》菜单的配置方式）：

| 菜单名 | 路由路径 | 组件路径 | 权限码 |
|---|---|---|---|
| 接口回调记录 | /open/client/notify-info | open/client/notifyInfo/index | open:client:notifyInfo:detial |
| 事件推送记录 | /open/client/event-push | open/client/eventPush/index | open:client:eventPush:detial |
| 接口调用日志 | /open/client/api-call-log | open/client/apiCallLog/index | open:client:apiCallLog:detial |

- [ ] **Step 5: 全量验证**

```bash
mvn clean install -DskipTests -pl mdp-apps/md-open,mdp-apps/md-public/md-common-pojo,mdp-apps/md-workbench -am
cd /Users/tangyh/gitee/mdp-vben/apps/web-open && pnpm build
```

预期：后端 BUILD SUCCESS，前端构建成功。

- [ ] **Step 6: 编写实施完成报告**

汇报：3 个页面访问路径、越权校验结果、事件码替换计数（应为 6）、文档改动清单、菜单注册动作提醒。
