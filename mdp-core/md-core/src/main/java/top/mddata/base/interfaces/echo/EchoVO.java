package top.mddata.base.interfaces.echo;

import java.io.Serializable;
import java.util.Map;

/**
 * 注入VO 父类
 *
 * @author henhen6
 * @date 2021/3/22 2:22 下午
 */
public interface EchoVO extends Serializable {

    /**
     * 回显值 集合
     *
     * @return 回显值 集合
     */
    Map<String, Object> getEchoMap();
}
