package top.mddata.common.mapper;

import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;
import top.mddata.common.entity.base.UserSocialAuthBase;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link UserSocialAuthMapper} 注解 SQL 回归测试。
 *
 * @author tangyh
 */
class UserSocialAuthMapperTest {

    @Test
    void 转移SQL的UPDATE与表名之间必须保留空白() throws NoSuchMethodException {
        String sql = updateSql("transferUser", Long.class, Long.class, Long.class,
                Long.class, LocalDateTime.class);

        assertUpdateTableSeparated(sql);
        assertTrue(sql.contains("SET user_id = #{targetUserId}, bound_at = #{now}"));
        assertTrue(sql.contains(
                "WHERE id = #{id} AND user_id = #{sourceUserId} AND deleted_at = 0"));
    }

    @Test
    void 解绑SQL的UPDATE与表名之间必须保留空白() throws NoSuchMethodException {
        String sql = updateSql("logicalDeleteByUser", Long.class, Long.class, Long.class,
                Long.class, LocalDateTime.class);

        assertUpdateTableSeparated(sql);
        assertTrue(sql.contains("SET deleted_at = #{deletedAt}, deleted_by = #{operatorId}"));
        assertTrue(sql.contains("WHERE id = #{id} AND user_id = #{userId} AND deleted_at = 0"));
    }

    @Test
    void 批量逻辑删除SQL的UPDATE与表名之间保留空白且含foreach() throws NoSuchMethodException {
        String sql = updateSql("logicalDeleteByUserIds", Collection.class, Long.class, Long.class,
                LocalDateTime.class);

        assertTrue(sql.contains("UPDATE " + UserSocialAuthBase.TABLE_NAME),
                () -> "UPDATE 与表名之间缺少空白：" + sql);
        assertTrue(sql.contains("SET deleted_at = #{deletedAt}, deleted_by = #{operatorId}"));
        assertTrue(sql.contains("WHERE deleted_at = 0"));
        assertTrue(sql.contains("user_id IN"));
        assertTrue(sql.contains("<foreach collection=\"userIds\" item=\"userId\""));
    }

    private String updateSql(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Update update = UserSocialAuthMapper.class
                .getMethod(methodName, parameterTypes)
                .getAnnotation(Update.class);
        return String.join("", update.value()).trim();
    }

    private void assertUpdateTableSeparated(String sql) {
        assertTrue(sql.startsWith("UPDATE " + UserSocialAuthBase.TABLE_NAME),
                () -> "UPDATE 与表名之间缺少空白：" + sql);
    }
}
