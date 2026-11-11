package top.mddata.common.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.common.entity.base.UserSocialAuthBase;

/**
 * 用户第三方身份绑定。
 * DO类：数据对象，可以在关联查询时再次添加字段，重新生成代码时忽略此文件。
 *
 * @author tangyh
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(UserSocialAuthBase.TABLE_NAME)
public class UserSocialAuth extends UserSocialAuthBase {
}
