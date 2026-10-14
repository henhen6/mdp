package top.mddata.open.controller.client;

import com.gitee.sop.support.exception.OpenException;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.StoryMessageEnum;
import top.mddata.open.entity.admin.App;
import top.mddata.open.service.app.AppService;

import java.util.List;

/**
 * client 端数据隔离支撑：开发者只能看到自己创建的应用及其产生的数据。
 */
@Component
@RequiredArgsConstructor
public class ClientAppHelper {

    private final AppService appService;

    /** 当前开发者拥有的应用 id 列表 */
    public List<Long> myAppIds() {
        Long userId = ContextUtil.getUserId();
        // 纵深防御：MyBatis-Flex 会忽略 eq(null) 条件，userId 为空时 eq(createdBy, null) 会查全表，
        // 数据隔离不能依赖上游鉴权配置永远正确，必须在代码内 fail fast
        ArgumentAssert.notNull(userId, "未登录或登录已过期");
        return appService.list(QueryWrapper.create()
                        .select(App::getId)
                        .eq(App::getCreatedBy, userId))
                .stream().map(App::getId).toList();
    }

    /** 校验记录归属：不属于当前开发者时直接失败 */
    public void checkOwner(Long appId) {
        if (appId == null || !myAppIds().contains(appId)) {
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "无权查看该记录");
        }
    }
}
