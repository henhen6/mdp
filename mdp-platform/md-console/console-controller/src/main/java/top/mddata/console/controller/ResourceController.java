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
import top.mddata.base.base.R;
import top.mddata.common.entity.User;
import top.mddata.common.properties.IgnoreProperties;
import top.mddata.console.organization.service.UserService;
import top.mddata.console.permission.service.ResourceMenuService;
import top.mddata.console.permission.vo.ResourceMenuVo;
import top.mddata.console.permission.vo.UserinfoVo;
import top.mddata.console.permission.vo.VisibleResourceVO;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "资源-菜单-应用")
public class ResourceController {
    private final ResourceMenuService resourceMenuService;
    private final UserService userService;
    private final IgnoreProperties ignoreProperties;

    /**
     * 查询指定应用所有的菜单
     */
    @Operation(summary = "查询指定应用所有的菜单", description = "查询指定应用所有的菜单")
    @GetMapping("/anyUser/findAllMenu")
    public R<List<ResourceMenuVo>> findAllMenu(@RequestParam(value = "applicationId") Long applicationId) {
        return R.success(resourceMenuService.findAllMenu(applicationId));
    }

    /**
     * 查询用户可用的所有资源
     */
    @Operation(summary = "查询用户可用的所有资源", description = "根据员工ID查询员工在某个应用下可用的资源")
    @GetMapping("/anyone/visible/resource")
    public R<VisibleResourceVO> visible(@RequestParam(value = "applicationId") Long applicationId) {
        return R.success(VisibleResourceVO.builder()
                .enabled(ignoreProperties.getAuthEnabled())
                .caseSensitive(ignoreProperties.getCaseSensitive())
                .roleList(Collections.singletonList("*"))
                .resourceList(Collections.singletonList("*"))
                .build());
    }

    /**
     * 查询当前请求中的用户信息
     */
    @Operation(summary = "查询当前请求中的用户信息", description = "查询当前请求中的用户信息")
    @GetMapping("/anyone/getUserinfo")
    public R<UserinfoVo> getUserinfo() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getByIdCache(userId);
        return R.success(BeanUtil.toBean(user, UserinfoVo.class));
    }
}
