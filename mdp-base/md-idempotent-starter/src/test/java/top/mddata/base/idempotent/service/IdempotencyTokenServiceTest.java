package top.mddata.base.idempotent.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import top.mddata.base.exception.BizException;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.util.ContextUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * {@link IdempotencyTokenService} 的身份绑定与令牌签发行为测试。
 *
 * @author henhen6
 */
class IdempotencyTokenServiceTest {

    @AfterEach
    void clearContext() {
        ContextUtil.remove();
    }

    @Test
    void userIdentityTakesPrecedence() {
        ContextUtil.setUserId(9L);
        ContextUtil.setAppId(3L);
        IdempotencyRepository repository = mock(IdempotencyRepository.class);
        IdempotencyTokenService service = new IdempotencyTokenService(repository);

        String token = service.issue();

        assertFalse(token.isBlank());
        verify(repository).issue(eq(token), eq("user:9"));
    }

    @Test
    void applicationIdentityIsSupported() {
        ContextUtil.setAppId(3L);
        assertEquals("app:3", IdempotencyTokenService.currentSubject());
    }

    @Test
    void anonymousRequestIsRejected() {
        BizException error = assertThrows(BizException.class,
                IdempotencyTokenService::currentSubject);
        assertEquals(-16, error.getCode());
    }

    @Test
    void issuedTokensAreUnpredictable() {
        ContextUtil.setUserId(9L);
        IdempotencyTokenService service = new IdempotencyTokenService(mock(IdempotencyRepository.class));

        assertNotEquals(service.issue(), service.issue());
    }

    @Test
    void repositoryIssueFailurePropagates() {
        ContextUtil.setUserId(9L);
        IdempotencyRepository repository = mock(IdempotencyRepository.class);
        BizException failure = new BizException(-10, "令牌仓储签发失败");
        doThrow(failure).when(repository).issue(anyString(), eq("user:9"));
        IdempotencyTokenService service = new IdempotencyTokenService(repository);

        BizException thrown = assertThrows(BizException.class, service::issue);
        assertSame(failure, thrown);
    }
}
