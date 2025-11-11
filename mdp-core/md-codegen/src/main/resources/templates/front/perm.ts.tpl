/**
 * 按钮权限定义
 * 注意：若代码生成器自动生成的权限码跟开发者在《菜单管理》中配置的不同，请手动修改一致
 */
export const #(firstCharToLowerCase(table.buildEntityClassName())) = {
  add: '#(packageConfig.getSubSystem()):#(packageConfig.getModule()):#(firstCharToLowerCase(table.buildEntityClassName())):add',
  delete: '#(packageConfig.getSubSystem()):#(packageConfig.getModule()):#(firstCharToLowerCase(table.buildEntityClassName())):delete',
  edit: '#(packageConfig.getSubSystem()):#(packageConfig.getModule()):#(firstCharToLowerCase(table.buildEntityClassName())):edit',
  view: '#(packageConfig.getSubSystem()):#(packageConfig.getModule()):#(firstCharToLowerCase(table.buildEntityClassName())):view',
};
