package top.mddata.base.cache.redis;

import java.io.Serial;
import java.io.Serializable;

/**
 * 空值
 * 解决缓存穿透
 *
 * @author henhen6
 * @date 2020/9/17 2:30 下午
 */
public class NullVal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
