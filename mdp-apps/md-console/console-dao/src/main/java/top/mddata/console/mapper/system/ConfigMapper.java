package top.mddata.console.mapper.system;

import com.mybatisflex.core.FlexConsts;
import com.mybatisflex.core.provider.EntitySqlProvider;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.console.entity.system.Config;

import java.util.List;

/**
 * 系统配置 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Repository
public interface ConfigMapper extends SuperMapper<Config> {


    /**
     * 根据查询条件查询配置列表
     *
     * @param queryWrapper 查询条件
     * @return 配置列表
     */
    @SelectProvider(type = EntitySqlProvider.class, method = "selectListByQuery")
    @Nullable List<Config> selectListByQuery(@Param(FlexConsts.QUERY) @NonNull QueryWrapper queryWrapper);

}
