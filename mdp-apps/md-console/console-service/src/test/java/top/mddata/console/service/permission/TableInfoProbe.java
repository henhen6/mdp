package top.mddata.console.service.permission;

import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.table.IdInfo;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import org.junit.jupiter.api.Test;
import top.mddata.console.entity.permission.ResourceApi;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 实体主键识别回归测试。
 *
 * <p>ResourceApiBase 曾因错误声明为泛型 SuperEntity&lt;T&gt;，导致 id 被解析为
 * 实体自身类型、MyBatis-Flex 不识别主键（idCount=0），插入报
 * "Field 'id' doesn't have a default value"。此测试防止该类错误复发。</p>
 */
class TableInfoProbe {
    @Test
    void resourceApi主键应被识别且使用uid生成器() {
        TableInfo info = TableInfoFactory.ofEntityClass(ResourceApi.class);
        IdInfo[] ids = info.getPrimaryKeyList().toArray(new IdInfo[0]);
        assertEquals(1, ids.length, "ResourceApi 必须识别出 1 个主键");
        assertEquals("id", ids[0].getProperty());
        assertEquals(KeyType.Generator, ids[0].getKeyType());
        assertEquals("uid", ids[0].getValue());
    }
}
