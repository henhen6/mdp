package top.mddata.open.service.admin;

import com.mybatisflex.core.paginate.Page;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.dto.client.AppDevInfoDto;
import top.mddata.open.dto.client.AppInfoUpdateDto;
import top.mddata.open.entity.admin.App;
import top.mddata.open.query.admin.AppQuery;
import top.mddata.open.vo.admin.AppVo;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
public interface AppService extends SuperService<App> {

    /**
     * 根据权限角色ID 分页查询角色拥有的应用 或者 没有的应用
     * @param page  分页对象
     * @param query 查询参数
     * @return 分页信息
     */
    Page<AppVo> pageByRoleTemplateId(Page<App> page, AppQuery query);

    Page<AppVo> pageByRoleId(Page<App> page, AppQuery query);

    /**
     * 查询需要 接收事件推送的应用
     *
     * @return 应用
     */
    List<AppVo> listNeedPushApp();

    /**
     * 查询角色已分配的应用（按权重降序）
     *
     * @param roleId 角色ID
     * @return 应用列表
     */
    List<AppVo> listByRoleId(Long roleId);

    /**
     * 根据应用标识查询应用
     *
     * @param appKey 应用标识
     * @return 应用
     */
    AppVo getAppByAppKey(String appKey);

    /**
     * 查询用户能访问的应用
     *
     * 1. 公开应用
     * 2. 已授权的应用
     *
     * @param userId 用户id
     * @return 应用列表
     */
    List<AppVo> listMyApp(Long userId);

    /**
     * 根据用户ID查询拥有的应用
     *
     * @param userId 用户ID
     * @return 应用列表
     */
    List<AppVo> listAppByUserId(Long userId);


    /**
     * 校验用户是否拥有该应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 是否拥有该应用
     */
    Boolean checkAppByUserId(Long userId, Long appId);

    /**
     * 修改应用信息
     *
     * @param dto 应用信息
     * @return 应用主键
     */
    Long updateInfoById(AppInfoUpdateDto dto);

    /**
     * 修改应用开发信息
     *
     * @param dto 应用开发信息
     * @return 应用主键
     */
    Long updateDevById(AppDevInfoDto dto);

}
