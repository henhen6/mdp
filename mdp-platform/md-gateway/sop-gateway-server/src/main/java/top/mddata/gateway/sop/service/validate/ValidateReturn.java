package top.mddata.gateway.sop.service.validate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.gateway.sop.common.ApiDto;
import top.mddata.gateway.sop.common.AppDto;

/**
 * @author 六如
 */
@AllArgsConstructor
@Getter
public class ValidateReturn {
    private ApiDto apiDto;
    private AppDto appDto;
}
