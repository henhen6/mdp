package top.mddata.open.entity.admin;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.open.entity.admin.base.AppScopeApplyBase;

import java.io.Serial;

/**
 * 权限组申请实体类。
 *
 * @author henhen6
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Table(AppScopeApplyBase.TABLE_NAME)
public class AppScopeApply extends AppScopeApplyBase {
    @Serial
    private static final long serialVersionUID = 1L;
}
