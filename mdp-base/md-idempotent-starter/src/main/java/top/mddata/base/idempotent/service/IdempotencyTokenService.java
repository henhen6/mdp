package top.mddata.base.idempotent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import top.mddata.base.constant.Constants;
import top.mddata.base.exception.BizException;
import top.mddata.base.idempotent.properties.IdempotencyProperties;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.util.ContextUtil;

import java.util.UUID;

/**
 * 幂等令牌签发服务，按当前身份（用户优先，其次应用）签发不可预测的幂等令牌。
 * <p>
 * 真实应用组件扫描会装配本类，须与自动配置同条件退避，保证 {@code mdp.idempotent.enabled=false} 时整体关闭。
 *
 * @author henhen6
 */
@Service
@ConditionalOnProperty(prefix = IdempotencyProperties.PREFIX,
        name = Constants.ENABLED, havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class IdempotencyTokenService {

    /** 匿名身份不可签发令牌的业务错误码。 */
    private static final int NO_SUBJECT_CODE = -16;

    private final IdempotencyRepository idempotencyRepository;

    /**
     * 签发归属当前身份的幂等令牌；仓储异常原样向上抛出。
     *
     * @return 随机幂等令牌
     */
    public String issue() {
        String token = UUID.randomUUID().toString();
        idempotencyRepository.issue(token, currentSubject());
        return token;
    }

    /**
     * 解析当前请求身份：已登录用户优先，其次第三方应用；两者皆无视为匿名并拒绝签发。
     *
     * @return user:&lt;用户ID&gt; 或 app:&lt;应用ID&gt;
     */
    public static String currentSubject() {
        Long userId = ContextUtil.getUserId();
        if (userId != null && userId > 0) {
            return "user:" + userId;
        }
        Long appId = ContextUtil.getAppId();
        if (appId != null && appId > 0) {
            return "app:" + appId;
        }
        throw new BizException(NO_SUBJECT_CODE, "当前身份不可签发幂等令牌");
    }
}
