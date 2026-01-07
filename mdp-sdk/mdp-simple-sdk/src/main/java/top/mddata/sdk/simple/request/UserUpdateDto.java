package top.mddata.sdk.simple.request;

import java.io.Serializable;

/**
 * 用户新增或修改
 *
 * @author henhen
 * @since 2025-10-19 09:45:12
 */
public class UserUpdateDto extends UserSaveDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    public Long getId() {
        return id;
    }

    public UserUpdateDto setId(Long id) {
        this.id = id;
        return this;
    }
}
