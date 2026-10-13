-- ----------------------------------------------------------------------------
-- 开放平台（控制台-开放平台，app_id=2）菜单重构
-- 1. 新增 5 个二级目录：应用 / 接口与文档 / 权限 / 事件中心 / 日志监控
-- 2. 14 个菜单按域重挂，同级 weight 按 10 间隔重排（事件域与日志域按业务链路连续）
-- 3. 术语统一：「接口调用日志」→「接口调用记录」（记录=任务主表，日志=尝试明细）
-- 4. 按域给已授权角色补充目录授权，防止 findUserRouter 精确匹配导致菜单断链消失
--    （MyTreeBuilder 会静默丢弃父节点不在授权集合内的菜单）
-- 执行前请备份：mysqldump <库> mdc_resource_menu mdc_role_resource_rel > backup.sql
-- ----------------------------------------------------------------------------

BEGIN;

-- 1. 新增 5 个二级目录（parent 为开放平台根 693083240537805824）
INSERT INTO `mdc_resource_menu` (`id`, `app_id`, `code`, `name`, `menu_type`, `remarks`, `path`, `component`, `redirect`, `state`, `data_scope_state`, `tree_path`, `parent_id`, `weight`, `meta`, `created_by`, `created_at`, `updated_by`, `updated_at`, `deleted_by`, `deleted_at`) VALUES
(693083240537805901, 2, 'open:admin:application', '应用', '10', '', '/manage/application', 'LAYOUT', '', b'1', b'0', '/693083240537805824/693083240537805901/', 693083240537805824, 10, '{"activeIcon":"","activePath":"","affixTab":null,"authority":[],"badge":"","badgeType":"","badgeVariants":"","component":"","fullPathKey":null,"hideChildrenInMenu":null,"hideInBreadcrumb":null,"hideInMenu":null,"hideInTab":null,"icon":"ant-design:appstore-outlined","iframeSrc":"","ignoreAccess":null,"ignoreLogin":null,"keepAlive":null,"link":"","menuVisibleWithForbidden":null,"openInNewWindow":null,"title":"应用"}', 680083598598475778, '2026-10-13 10:00:00.000', 680083598598475778, '2026-10-13 10:00:00.000', NULL, 0),
(693083240537805902, 2, 'open:admin:openApi', '接口与文档', '10', '', '/manage/openApi', 'LAYOUT', '', b'1', b'0', '/693083240537805824/693083240537805902/', 693083240537805824, 20, '{"activeIcon":"","activePath":"","affixTab":null,"authority":[],"badge":"","badgeType":"","badgeVariants":"","component":"","fullPathKey":null,"hideChildrenInMenu":null,"hideInBreadcrumb":null,"hideInMenu":null,"hideInTab":null,"icon":"carbon:api","iframeSrc":"","ignoreAccess":null,"ignoreLogin":null,"keepAlive":null,"link":"","menuVisibleWithForbidden":null,"openInNewWindow":null,"title":"接口与文档"}', 680083598598475778, '2026-10-13 10:00:00.000', 680083598598475778, '2026-10-13 10:00:00.000', NULL, 0),
(693083240537805903, 2, 'open:admin:auth', '权限', '10', '', '/manage/auth', 'LAYOUT', '', b'1', b'0', '/693083240537805824/693083240537805903/', 693083240537805824, 30, '{"activeIcon":"","activePath":"","affixTab":null,"authority":[],"badge":"","badgeType":"","badgeVariants":"","component":"","fullPathKey":null,"hideChildrenInMenu":null,"hideInBreadcrumb":null,"hideInMenu":null,"hideInTab":null,"icon":"carbon:security","iframeSrc":"","ignoreAccess":null,"ignoreLogin":null,"keepAlive":null,"link":"","menuVisibleWithForbidden":null,"openInNewWindow":null,"title":"权限"}', 680083598598475778, '2026-10-13 10:00:00.000', 680083598598475778, '2026-10-13 10:00:00.000', NULL, 0),
(693083240537805904, 2, 'open:admin:event', '事件中心', '10', '', '/manage/event', 'LAYOUT', '', b'1', b'0', '/693083240537805824/693083240537805904/', 693083240537805824, 40, '{"activeIcon":"","activePath":"","affixTab":null,"authority":[],"badge":"","badgeType":"","badgeVariants":"","component":"","fullPathKey":null,"hideChildrenInMenu":null,"hideInBreadcrumb":null,"hideInMenu":null,"hideInTab":null,"icon":"carbon:event","iframeSrc":"","ignoreAccess":null,"ignoreLogin":null,"keepAlive":null,"link":"","menuVisibleWithForbidden":null,"openInNewWindow":null,"title":"事件中心"}', 680083598598475778, '2026-10-13 10:00:00.000', 680083598598475778, '2026-10-13 10:00:00.000', NULL, 0),
(693083240537805905, 2, 'open:admin:log', '日志监控', '10', '', '/manage/log', 'LAYOUT', '', b'1', b'0', '/693083240537805824/693083240537805905/', 693083240537805824, 50, '{"activeIcon":"","activePath":"","affixTab":null,"authority":[],"badge":"","badgeType":"","badgeVariants":"","component":"","fullPathKey":null,"hideChildrenInMenu":null,"hideInBreadcrumb":null,"hideInMenu":null,"hideInTab":null,"icon":"carbon:document-audit","iframeSrc":"","ignoreAccess":null,"ignoreLogin":null,"keepAlive":null,"link":"","menuVisibleWithForbidden":null,"openInNewWindow":null,"title":"日志监控"}', 680083598598475778, '2026-10-13 10:00:00.000', 680083598598475778, '2026-10-13 10:00:00.000', NULL, 0);

-- 2. 菜单重挂与重排（路径 path 不变，前端路由与书签不受影响）
-- 应用
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805901, `weight` = 10, `tree_path` = '/693083240537805824/693083240537805901/693083240537805825/', `updated_at` = NOW(3) WHERE `id` = 693083240537805825; -- 应用管理
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805901, `weight` = 20, `tree_path` = '/693083240537805824/693083240537805901/27145464621658112/', `updated_at` = NOW(3) WHERE `id` = 27145464621658112; -- 应用审核
-- 接口与文档
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805902, `weight` = 10, `tree_path` = '/693083240537805824/693083240537805902/694452411687278593/', `updated_at` = NOW(3) WHERE `id` = 694452411687278593; -- 开放接口
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805902, `weight` = 20, `tree_path` = '/693083240537805824/693083240537805902/694452411687278594/', `updated_at` = NOW(3) WHERE `id` = 694452411687278594; -- 接口文档
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805902, `weight` = 30, `tree_path` = '/693083240537805824/693083240537805902/45451182173128704/', `updated_at` = NOW(3) WHERE `id` = 45451182173128704; -- 帮助文档
-- 权限
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805903, `weight` = 10, `tree_path` = '/693083240537805824/693083240537805903/694452411687278595/', `updated_at` = NOW(3) WHERE `id` = 694452411687278595; -- Oauth2权限
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805903, `weight` = 20, `tree_path` = '/693083240537805824/693083240537805903/694452411687278596/', `updated_at` = NOW(3) WHERE `id` = 694452411687278596; -- 权限组
-- 事件中心（按链路：定义 → 触发 → 推送）
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805904, `weight` = 10, `tree_path` = '/693083240537805824/693083240537805904/41382868891172868/', `updated_at` = NOW(3) WHERE `id` = 41382868891172868; -- 事件管理
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805904, `weight` = 20, `tree_path` = '/693083240537805824/693083240537805904/44703565805825027/', `updated_at` = NOW(3) WHERE `id` = 44703565805825027; -- 事件触发
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805904, `weight` = 30, `tree_path` = '/693083240537805824/693083240537805904/44703565805825028/', `updated_at` = NOW(3) WHERE `id` = 44703565805825028; -- 事件推送记录
UPDATE `mdc_resource_menu` SET `weight` = 10, `tree_path` = '/693083240537805824/693083240537805904/44703565805825028/44703565805825029/', `updated_at` = NOW(3) WHERE `id` = 44703565805825029; -- 事件推送日志（隐藏，parent 不变）
-- 日志监控
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805905, `weight` = 10, `tree_path` = '/693083240537805824/693083240537805905/44703565805825024/', `updated_at` = NOW(3) WHERE `id` = 44703565805825024; -- 接口调用记录（见第 3 步改名）
UPDATE `mdc_resource_menu` SET `parent_id` = 693083240537805905, `weight` = 20, `tree_path` = '/693083240537805824/693083240537805905/44703565805825025/', `updated_at` = NOW(3) WHERE `id` = 44703565805825025; -- 接口回调记录
UPDATE `mdc_resource_menu` SET `weight` = 10, `tree_path` = '/693083240537805824/693083240537805905/44703565805825025/44703565805825026/', `updated_at` = NOW(3) WHERE `id` = 44703565805825026; -- 接口回调日志（隐藏，parent 不变）

-- 3. 术语统一：接口调用日志 → 接口调用记录（name 与 meta.title 同步）
UPDATE `mdc_resource_menu` SET `name` = '接口调用记录', `meta` = REPLACE(`meta`, '接口调用日志', '接口调用记录'), `updated_at` = NOW(3) WHERE `id` = 44703565805825024;

-- 4. 按域给已授权角色补充目录授权（幂等，可重复执行）
-- 应用
INSERT INTO `mdc_role_resource_rel` (`id`, `role_id`, `app_id`, `resource_type`, `resource_id`, `created_by`, `created_at`)
SELECT UUID_SHORT(), t.role_id, 2, '', 693083240537805901, 680083598598475778, NOW(3)
FROM (SELECT DISTINCT role_id FROM `mdc_role_resource_rel` WHERE app_id = 2 AND resource_id IN (693083240537805825, 27145464621658112)) t
WHERE NOT EXISTS (SELECT 1 FROM `mdc_role_resource_rel` x WHERE x.role_id = t.role_id AND x.resource_id = 693083240537805901 AND x.app_id = 2);
-- 接口与文档
INSERT INTO `mdc_role_resource_rel` (`id`, `role_id`, `app_id`, `resource_type`, `resource_id`, `created_by`, `created_at`)
SELECT UUID_SHORT(), t.role_id, 2, '', 693083240537805902, 680083598598475778, NOW(3)
FROM (SELECT DISTINCT role_id FROM `mdc_role_resource_rel` WHERE app_id = 2 AND resource_id IN (694452411687278593, 694452411687278594, 45451182173128704)) t
WHERE NOT EXISTS (SELECT 1 FROM `mdc_role_resource_rel` x WHERE x.role_id = t.role_id AND x.resource_id = 693083240537805902 AND x.app_id = 2);
-- 权限
INSERT INTO `mdc_role_resource_rel` (`id`, `role_id`, `app_id`, `resource_type`, `resource_id`, `created_by`, `created_at`)
SELECT UUID_SHORT(), t.role_id, 2, '', 693083240537805903, 680083598598475778, NOW(3)
FROM (SELECT DISTINCT role_id FROM `mdc_role_resource_rel` WHERE app_id = 2 AND resource_id IN (694452411687278595, 694452411687278596)) t
WHERE NOT EXISTS (SELECT 1 FROM `mdc_role_resource_rel` x WHERE x.role_id = t.role_id AND x.resource_id = 693083240537805903 AND x.app_id = 2);
-- 事件中心
INSERT INTO `mdc_role_resource_rel` (`id`, `role_id`, `app_id`, `resource_type`, `resource_id`, `created_by`, `created_at`)
SELECT UUID_SHORT(), t.role_id, 2, '', 693083240537805904, 680083598598475778, NOW(3)
FROM (SELECT DISTINCT role_id FROM `mdc_role_resource_rel` WHERE app_id = 2 AND resource_id IN (41382868891172868, 44703565805825027, 44703565805825028, 44703565805825029)) t
WHERE NOT EXISTS (SELECT 1 FROM `mdc_role_resource_rel` x WHERE x.role_id = t.role_id AND x.resource_id = 693083240537805904 AND x.app_id = 2);
-- 日志监控
INSERT INTO `mdc_role_resource_rel` (`id`, `role_id`, `app_id`, `resource_type`, `resource_id`, `created_by`, `created_at`)
SELECT UUID_SHORT(), t.role_id, 2, '', 693083240537805905, 680083598598475778, NOW(3)
FROM (SELECT DISTINCT role_id FROM `mdc_role_resource_rel` WHERE app_id = 2 AND resource_id IN (44703565805825024, 44703565805825025, 44703565805825026)) t
WHERE NOT EXISTS (SELECT 1 FROM `mdc_role_resource_rel` x WHERE x.role_id = t.role_id AND x.resource_id = 693083240537805905 AND x.app_id = 2);

COMMIT;
