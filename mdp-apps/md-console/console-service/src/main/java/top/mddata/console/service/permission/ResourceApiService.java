package top.mddata.console.service.permission;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.dto.permission.ResourceApiBindDto;
import top.mddata.console.dto.permission.ResourceApiSaveDto;
import top.mddata.console.entity.permission.ResourceApi;
import top.mddata.console.vo.permission.ResourceApiVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 接口权限 服务层。
 *
 * @author henhen6
 * @since 2026-09-27
 */
public interface ResourceApiService extends SuperService<ResourceApi> {

    /**
     * 按资源查询已配置接口列表。
     *
     * @param resourceId 资源id（菜单表id）
     * @return 接口列表
     */
    List<ResourceApiVo> listByResource(Long resourceId);

    /**
     * 手动录入一条接口配置。
     *
     * @param dto 录入参数
     * @return 是否成功
     */
    Boolean saveManual(ResourceApiSaveDto dto);

    /**
     * 批量绑定扫描得到的接口，自动跳过已关联项。
     *
     * @param dto 绑定参数
     * @return 新增关联条数
     */
    Integer bind(ResourceApiBindDto dto);

    /**
     * 按主键批量删除接口配置。
     *
     * @param ids 主键集合
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> ids);

    /**
     * 按资源删除全部关联接口（菜单删除时级联调用；按钮即菜单行，随菜单树级联）。
     *
     * @param resourceIds 资源id集合
     * @return 是否成功
     */
    Boolean deleteByResource(Collection<? extends Serializable> resourceIds);
}
