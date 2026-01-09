package top.mddata.api.oepn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


/**
 * 用户新增或修改
 *
 * @author henhen
 * @since 2025-10-19 09:45:12
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户批量新增")
public class UserBatchSaveDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Valid
    private List<UserSaveDto> list;
}
