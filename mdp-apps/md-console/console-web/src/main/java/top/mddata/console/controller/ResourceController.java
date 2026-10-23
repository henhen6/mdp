package top.mddata.console.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.fieldperm.model.FieldRule;
import top.mddata.base.fieldperm.spi.FieldPermProvider;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.entity.User;
import top.mddata.common.properties.IgnoreProperties;
import top.mddata.console.service.organization.UserService;
import top.mddata.console.service.permission.ResourceMenuService;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.vo.permission.ResourceMenuVo;
import top.mddata.console.vo.permission.UserAccessInfoVo;
import top.mddata.console.vo.permission.UserinfoVo;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "资源-菜单-应用")
public class ResourceController {
    private final ResourceMenuService resourceMenuService;
    private final RoleService roleService;
    private final UserService userService;
    private final IgnoreProperties ignoreProperties;
    private final FieldPermProvider fieldPermProvider;

    /**
     * 查询指定应用所有的菜单
     * 无需登录
     */
    @Operation(summary = "查询指定应用所有的菜单", description = "查询指定应用所有的菜单")
    @GetMapping("/anyUser/findAllMenu")
    @RequestLog(value = "查询指定应用所有菜单", logType = RequestLog.LogType.QUERY)
    public R<List<ResourceMenuVo>> findAllMenu(@RequestParam(value = "appId") Long appId) {
        return R.success(resourceMenuService.findAllMenu(appId));
    }

    /**
     * 查询指定应用所有的菜单
     * 需要登录
     */
    @Operation(summary = "查询用户可用的所有菜单路由", description = "查询用户可用的所有菜单路由")
    @GetMapping("/anyone/findUserRouter")
    @RequestLog(value = "查询用户可用的所有菜单路由", logType = RequestLog.LogType.QUERY)
    public R<List<ResourceMenuVo>> findUserRouter(@RequestParam(value = "appId") Long appId) {
        long userId = StpUtil.getLoginIdAsLong();
        return R.success(resourceMenuService.findUserRouter(appId, userId));
    }

    /**
     * 查询用户可用的所有资源
     * 需要登录
     */
    @Operation(summary = "查询用户可用的所有资源", description = "根据员工ID查询员工在某个应用下可用的资源")
    @GetMapping("/anyone/findUserResource")
    @RequestLog(value = "查询用户可用的所有资源", logType = RequestLog.LogType.QUERY)
    public R<UserAccessInfoVo> findUserResource(@RequestParam(value = "appId") Long appId) {
        long userId = StpUtil.getLoginIdAsLong();
        return R.success(UserAccessInfoVo.builder()
                .enabled(ignoreProperties.getAuthEnabled())
                .caseSensitive(ignoreProperties.getCaseSensitive())
                .roleList(roleService.findUserRoleCodes(userId))
                .resourceList(resourceMenuService.findUserResourceCode(appId, userId))
                .build());
    }

    /**
     * 查询当前请求中的用户信息
     * 需要登录
     */
    @Operation(summary = "查询当前请求中的用户信息", description = "查询当前请求中的用户信息")
    @GetMapping("/anyone/getUserinfo")
    @RequestLog(value = "查询当前请求中的用户信息", logType = RequestLog.LogType.QUERY)
    public R<UserinfoVo> getUserinfo() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getByIdCache(userId);
        return R.success(BeanUtil.toBean(user, UserinfoVo.class));
    }

    /**
     * 查询当前登录用户在指定菜单下的受限字段规则（前端列隐藏/脱敏渲染用）。
     *
     * @param menuId 菜单ID
     * @return property → 受限规则；无限制返回空Map
     */
    @GetMapping("/anyone/current")
    @Operation(summary = "查询当前用户受限字段", description = "查询当前用户在指定菜单下的受限字段规则")
    public R<Map<String, FieldRule>> current(@RequestParam Long menuId) {
        Long userId = ContextUtil.getUserId();
        if (userId == null) {
            return R.success(Map.of());
        }
        Map<String, FieldRule> rules = fieldPermProvider.findUserPerm(userId).rulesOf(menuId);
        return R.success(rules == null ? Map.of() : rules);
    }
}
