package top.mddata.api.oepn.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户批量返回")
public class UserListVo {
    /**
     * 新增数据
     */
    @Schema(description = "新增数据")
    private List<UserVo> saveList;
    /**
     * 修改数据
     */
    @Schema(description = "修改数据")
    private List<UserVo> updateList;
    /**
     * 重复数据
     */
    @Schema(description = "重复数据")
    private List<UserVo> existList;
}
