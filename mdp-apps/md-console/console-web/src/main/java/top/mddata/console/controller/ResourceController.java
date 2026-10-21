package top.mddata.console.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
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
import top.mddata.common.entity.User;
import top.mddata.common.properties.IgnoreProperties;
import top.mddata.console.service.organization.OrgService;
import top.mddata.console.service.organization.PositionService;
import top.mddata.console.service.organization.UserService;
import top.mddata.console.service.permission.ResourceMenuService;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.vo.permission.ResourceMenuVo;
import top.mddata.console.vo.permission.UserAccessInfoVo;
import top.mddata.console.vo.permission.UserinfoVo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private final PositionService positionService;
    private final OrgService orgService;

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
        UserinfoVo userinfo = BeanUtil.toBean(user, UserinfoVo.class);
        // 岗位、组织仅作展示，id 换名称复用内部查询（findByIds 返回 Map<id, name>）
        if (user.getPositionId() != null) {
            Object positionName = positionService.findByIds(Set.of(user.getPositionId())).get(user.getPositionId());
            userinfo.setPositionName(Convert.toStr(positionName));
        }
        if (CollUtil.isNotEmpty(user.getOrgIdList())) {
            Map<Serializable, Object> orgNameMap = orgService.findByIds(new HashSet<>(user.getOrgIdList()));
            userinfo.setOrgNameList(user.getOrgIdList().stream()
                    .map(orgNameMap::get).filter(Objects::nonNull).map(String::valueOf).toList());
        }
        return R.success(userinfo);
    }
}
