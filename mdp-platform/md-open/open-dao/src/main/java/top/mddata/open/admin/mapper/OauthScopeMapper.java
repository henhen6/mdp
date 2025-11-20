package top.mddata.open.admin.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.admin.entity.OauthScope;

import java.util.List;

/**
 * oauth2权限 映射层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Repository
public interface OauthScopeMapper extends SuperMapper<OauthScope> {
    /**
     * 根据应用id查询应用拥有的权限
     *
     * @param appId 应用id
     * @param level         公开权限
     * @return 权限
     */
    @Select({"""
            select * from mdo_oauth_scope os
            where os.level = 1
            or exists (
                select 1 from mdo_group_scope_rel gs
                inner join mdo_app_group_rel ag 
                  on ag.group_id = gs.group_id 
                where ag.app_id =  #{appId}
                  and gs.scope_id = os.id      
            )
            """})
    List<OauthScope> selectByAppId(@Param("appId") Long appId, @Param("level") Integer level);
}
