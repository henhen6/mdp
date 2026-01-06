package top.mddata.gateway.sop.service.validate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.gateway.sop.pojo.dto.ApiDto;
import top.mddata.gateway.sop.pojo.dto.AppDto;

/**
 * @author 六如
 */
@AllArgsConstructor
@Getter
public class ValidateReturn {
    private ApiDto apiDto;
    private AppDto appDto;
}
