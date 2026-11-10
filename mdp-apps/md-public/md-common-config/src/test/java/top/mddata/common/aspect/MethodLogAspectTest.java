package top.mddata.common.aspect;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.service.IdempotencyTokenService;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.properties.SystemProperties;
import top.mddata.common.service.OrdinaryLoggingService;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 真实 Spring AOP 下令牌返回值脱敏与普通 Service 日志行为回归。 */
class MethodLogAspectTest {

    private AnnotationConfigApplicationContext context;
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private boolean originalAdditivity;

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
        if (logger != null) {
            logger.detachAppender(appender);
            logger.setAdditive(originalAdditivity);
        }
        if (context != null) {
            context.close();
        }
    }

    @Test
    void tokenResultNeverLoggedWhileOrdinaryServiceResultKeepsLogging() {
        SystemProperties properties = mock(SystemProperties.class);
        when(properties.getRecordLog()).thenReturn(true);
        when(properties.getRecordArgs()).thenReturn(true);
        when(properties.getRecordResult()).thenReturn(true);
        context = new AnnotationConfigApplicationContext();
        context.registerBean(RefreshScope.class);
        context.registerBean(SystemProperties.class, () -> properties);
        context.registerBean(IdempotencyRepository.class, () -> mock(IdempotencyRepository.class));
        context.register(TestAopConfiguration.class);
        context.refresh();

        logger = (Logger) LoggerFactory.getLogger(MethodLogAspect.class);
        originalAdditivity = logger.isAdditive();
        logger.setAdditive(false);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        ContextUtil.setUserId(9L);
        String issuedToken = context.getBean(IdempotencyTokenService.class).issue();
        context.getBean(OrdinaryLoggingService.class).value();

        String messages = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
        assertFalse(messages.contains(issuedToken));
        assertTrue(messages.contains("OrdinaryLoggingService#value"));
        assertTrue(messages.contains("ordinary-result"));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class TestAopConfiguration {
        @Bean
        MethodLogAspect methodLogAspect(SystemProperties properties) {
            return new MethodLogAspect(properties);
        }

        @Bean
        IdempotencyTokenService idempotencyTokenService(IdempotencyRepository repository) {
            return new IdempotencyTokenService(repository);
        }

        @Bean
        OrdinaryLoggingService ordinaryLoggingService() {
            return new OrdinaryLoggingService();
        }
    }
}
