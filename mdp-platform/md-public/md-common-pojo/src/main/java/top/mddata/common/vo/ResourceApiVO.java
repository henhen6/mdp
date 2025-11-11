package top.mddata.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 实体类
 * 资源接口
 * </p>
 *
 * @author henhen6
 * @since 2021-09-17
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(of = {"uri", "requestMethod"})
@Accessors(chain = true)
@AllArgsConstructor
public class ResourceApiVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 请求类型
     */
    private String requestMethod;

    /**
     * 接口路径;lamp-cloud版：uri需要拼接上gateway中路径前缀
     * lamp-boot版: uri需要不需要拼接前缀
     */
    private String uri;

    private String code;
}
