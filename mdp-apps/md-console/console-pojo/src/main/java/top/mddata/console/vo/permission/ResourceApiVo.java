package top.mddata.console.vo.permission;

import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.entity.permission.base.ResourceApiBase;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接口权限 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2026-09-27
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Schema(description = "接口权限")
@Table(ResourceApiBase.TABLE_NAME)
public class ResourceApiVo extends ResourceApiBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
