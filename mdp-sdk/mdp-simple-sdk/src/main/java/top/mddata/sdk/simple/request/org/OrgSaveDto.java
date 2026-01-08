package top.mddata.sdk.simple.request.org;

import java.io.Serializable;


/**
 * 用户新增或修改
 *
 * @author henhen
 * @since 2025-10-19 09:45:12
 */

public class OrgSaveDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     * [10-单位 20-部门]
     */
    private String orgType;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 父组织
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer weight;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    private Boolean state;

    /**
     * 备注
     */
    private String remarks;

    public String getName() {
        return name;
    }

    public OrgSaveDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getOrgType() {
        return orgType;
    }

    public OrgSaveDto setOrgType(String orgType) {
        this.orgType = orgType;
        return this;
    }

    public String getShortName() {
        return shortName;
    }

    public OrgSaveDto setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public OrgSaveDto setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public OrgSaveDto setWeight(Integer weight) {
        this.weight = weight;
        return this;
    }

    public Boolean getState() {
        return state;
    }

    public OrgSaveDto setState(Boolean state) {
        this.state = state;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public OrgSaveDto setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }
}
