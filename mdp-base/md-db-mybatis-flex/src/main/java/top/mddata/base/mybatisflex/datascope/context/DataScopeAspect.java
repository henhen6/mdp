package top.mddata.base.mybatisflex.datascope.context;

import top.mddata.base.mybatisflex.datascope.annotation.DataScope;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * 数据权限切面：拦截带 @DataScope 的方法，把注解压入上下文，
 * 方法结束（含异常）恢复外层值。
 *
 * @author henhen
 * @since 2026年09月26日
 */
@Slf4j
@Aspect
public class DataScopeAspect {

    /** 当前方法覆盖的外层注解，随调用栈恢复 */
    private static final ThreadLocal<DataScope> PREVIOUS = new ThreadLocal<>();

    @Pointcut("@annotation(dataScope)")
    public void dataScopePointcut(DataScope dataScope) {
    }

    @Before("dataScopePointcut(dataScope)")
    public void beforeMethod(DataScope dataScope) {
        PREVIOUS.set(DataScopeContext.setAndGetPrevious(dataScope));
    }

    /**
     * @After 在正常与异常退出时都会执行（finally 语义），
     * 不能再配 @AfterThrowing 重复恢复，否则外层值会被二次清理
     */
    @After("dataScopePointcut(dataScope)")
    public void afterMethod(DataScope dataScope) {
        DataScopeContext.restore(PREVIOUS.get());
        PREVIOUS.remove();
    }
}
