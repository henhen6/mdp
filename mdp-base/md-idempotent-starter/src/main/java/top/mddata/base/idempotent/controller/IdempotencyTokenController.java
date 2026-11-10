package top.mddata.base.idempotent.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.base.constant.Constants;
import top.mddata.base.idempotent.properties.IdempotencyProperties;
import top.mddata.base.idempotent.service.IdempotencyTokenService;

/**
 * 幂等令牌签发接口。
 * <p>
 * 真实应用组件扫描会装配本类，须与自动配置同条件退避，保证 {@code mdp.idempotent.enabled=false} 时整体关闭。
 *
 * @author henhen6
 */
@RestController
@RequestMapping("/anyone/idempotent")
@ConditionalOnProperty(prefix = IdempotencyProperties.PREFIX,
        name = Constants.ENABLED, havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class IdempotencyTokenController {

    private final IdempotencyTokenService tokenService;

    /**
     * 签发当前身份专属的幂等令牌。
     *
     * @return 包含幂等令牌的统一响应
     */
    @GetMapping("/token")
    public R<String> token() {
        return R.success(tokenService.issue());
    }
}
