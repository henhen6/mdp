package top.mddata.sdk.simple.request.org;

import java.io.Serializable;

/**
 * 用户新增或修改
 *
 * @author henhen
 * @since 2025-10-19 09:45:12
 */
public class OrgUpdateDto extends OrgSaveDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    public Long getId() {
        return id;
    }

    public OrgUpdateDto setId(Long id) {
        this.id = id;
        return this;
    }
}
