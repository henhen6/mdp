package top.mddata.workbench.service.twofactor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;
import top.mddata.workbench.enumeration.TwoFactorStrategyEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 策略解析器单元测试：缓存命中与异常回退
 *
 * @author tangyh
 */
class TwoFactorStrategyResolverTest {

    private ConfigFacade configFacade;
    private TwoFactorStrategyResolver resolver;

    @BeforeEach
    void setUp() {
        configFacade = Mockito.mock(ConfigFacade.class);
        resolver = new TwoFactorStrategyResolver(configFacade);
    }

    @Test
    void resolve_shouldCacheWithin60Seconds() {
        when(configFacade.getString("TWO_FACTOR_STRATEGY", "OFF")).thenReturn("FORCE");

        assertEquals(TwoFactorStrategyEnum.FORCE, resolver.resolve());
        assertEquals(TwoFactorStrategyEnum.FORCE, resolver.resolve());

        verify(configFacade, times(1)).getString(anyString(), anyString());
    }

    @Test
    void resolve_shouldFallbackOff_whenFacadeThrows() {
        when(configFacade.getString("TWO_FACTOR_STRATEGY", "OFF")).thenThrow(new RuntimeException("db down"));

        assertEquals(TwoFactorStrategyEnum.OFF, resolver.resolve());
    }

    @Test
    void resolve_shouldParseOffExplicitly() {
        when(configFacade.getString("TWO_FACTOR_STRATEGY", "OFF")).thenReturn("OFF");

        assertEquals(TwoFactorStrategyEnum.OFF, resolver.resolve());
    }

    @Test
    void resolveForceChannel_shouldCacheWithin60Seconds() {
        when(configFacade.getString("TWO_FACTOR_FORCE_CHANNEL", "BOTH")).thenReturn("PHONE");

        assertEquals(TwoFactorChannelEnum.PHONE, resolver.resolveForceChannel());
        assertEquals(TwoFactorChannelEnum.PHONE, resolver.resolveForceChannel());

        verify(configFacade, times(1)).getString("TWO_FACTOR_FORCE_CHANNEL", "BOTH");
    }

    @Test
    void resolveForceChannel_shouldFallbackBoth_whenFacadeThrows() {
        when(configFacade.getString("TWO_FACTOR_FORCE_CHANNEL", "BOTH"))
                .thenThrow(new RuntimeException("db down"));

        assertEquals(TwoFactorChannelEnum.BOTH, resolver.resolveForceChannel());
    }

    @Test
    void resolveForceChannel_shouldFallbackBoth_whenValueIllegal() {
        when(configFacade.getString("TWO_FACTOR_FORCE_CHANNEL", "BOTH")).thenReturn("SMS");

        assertEquals(TwoFactorChannelEnum.BOTH, resolver.resolveForceChannel());
    }
}
