package top.mddata.base.mybatisflex.keygen;

import cn.hutool.extra.spring.SpringUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.keygen.IKeyGenerator;

/**
 * mybatis-flex 的自定义id生成器实现类
 * @author henhen6
 * @since 2024年06月07日09:00:08
 */
public class UidKeyGenerator implements IKeyGenerator {
    private static UidGenerator uidGenerator;

    @Override
    public Object generate(Object entity, String keyColumn) {
        if (uidGenerator == null) {
            uidGenerator = SpringUtil.getBean(UidGenerator.class);
        }
        return uidGenerator.getUid();
    }
}
