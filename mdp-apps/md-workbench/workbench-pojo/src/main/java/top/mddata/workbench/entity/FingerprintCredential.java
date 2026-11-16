package top.mddata.workbench.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.workbench.entity.base.FingerprintCredentialBase;

/**
 * 指纹登录凭证
 *
 * @author tangyh
 * @since 2026-11-13
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(FingerprintCredentialBase.TABLE_NAME)
public class FingerprintCredential extends FingerprintCredentialBase {
}
