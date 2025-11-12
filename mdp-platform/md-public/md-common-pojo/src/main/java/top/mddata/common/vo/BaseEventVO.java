package top.mddata.common.vo;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.base.utils.ContextUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author henhen6
 * @version v1.0
 * @date 2022/7/29 10:04 PM
 * @create [2022/7/29 10:04 PM ] [henhen6] [初始创建]
 */
@Data
@Accessors(chain = true)
public class BaseEventVO {
    private Map<String, String> map;

    /**
     * 将线程变量副 暂存到map
     * 适用于：异步调用前
     *
     * @author henhen6
     * @date 2022/7/29 11:12 PM
     * @create [2022/7/29 11:12 PM ] [henhen6] [初始创建]
     */
    public BaseEventVO copy() {
        if (map == null) {
            map = new HashMap<>();
        }
        map.clear();
        map.putAll(ContextUtil.getLocalMap());
        return this;
    }

    /**
     * 将map写入线程变量
     * 适用于：异步执行一开始
     *
     * @author henhen6
     * @date 2022/7/29 11:12 PM
     * @create [2022/7/29 11:12 PM ] [henhen6] [初始创建]
     */
    public BaseEventVO write() {
        if (CollUtil.isNotEmpty(map)) {
            ContextUtil.setLocalMap(map);
        }
        return this;
    }

}
