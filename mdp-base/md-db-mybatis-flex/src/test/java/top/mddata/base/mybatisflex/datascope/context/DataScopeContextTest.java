package top.mddata.base.mybatisflex.datascope.context;

import top.mddata.base.mybatisflex.datascope.annotation.DataScope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 数据权限上下文嵌套恢复测试：内层方法结束后必须恢复外层注解，
 * 否则外层查询会被内层编码污染。
 */
class DataScopeContextTest {

    @AfterEach
    void tearDown() {
        DataScopeContext.restore(null);
    }

    private static DataScope stub(String code) {
        return new DataScope() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DataScope.class;
            }

            @Override
            public String code() {
                return code;
            }

            @Override
            public String orgColumn() {
                return "";
            }

            @Override
            public String userColumn() {
                return "created_by";
            }

            @Override
            public String[] tableAliases() {
                return new String[0];
            }
        };
    }

    @Test
    void 未设置时返回null() {
        assertNull(DataScopeContext.get());
    }

    @Test
    void 嵌套进入覆盖_退出恢复外层() {
        DataScope outer = stub("menu:a");
        DataScope inner = stub("menu:b");

        DataScope prevOfOuter = DataScopeContext.setAndGetPrevious(outer);
        assertNull(prevOfOuter);
        assertSame(outer, DataScopeContext.get());

        DataScope prevOfInner = DataScopeContext.setAndGetPrevious(inner);
        assertSame(outer, prevOfInner);
        assertSame(inner, DataScopeContext.get());

        DataScopeContext.restore(prevOfInner);
        assertSame(outer, DataScopeContext.get());

        DataScopeContext.restore(prevOfOuter);
        assertNull(DataScopeContext.get());
    }
}
