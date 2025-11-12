package top.mddata.common.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.common.entity.Org;

import java.util.List;

/**
 * 组织 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
@Repository
public interface OrgMapper extends SuperMapper<Org> {
    /**
     * 查询用户拥有的机构
     *
     * @param userId 用户id
     * @return java.util.List<java.lang.Long>
     */
    @Select({"""
             SELECT DISTINCT r.id
                    FROM mdc_org r INNER JOIN mdc_user_org_rel our on r.id = our.org_id
                    where  our.user_id = #{userId} and r.state = 1
            """})
    List<Long> selectOrgByUserId(@Param("userId") Long userId);
}
