package top.mddata.sdk.simple.response;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
public class OrgVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

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
     * 树路径
     */
    private String treePath;

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

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


    /**
     * 修改时间
     */
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public OrgVo setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public OrgVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getOrgType() {
        return orgType;
    }

    public OrgVo setOrgType(String orgType) {
        this.orgType = orgType;
        return this;
    }

    public String getShortName() {
        return shortName;
    }

    public OrgVo setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public OrgVo setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getTreePath() {
        return treePath;
    }

    public OrgVo setTreePath(String treePath) {
        this.treePath = treePath;
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public OrgVo setWeight(Integer weight) {
        this.weight = weight;
        return this;
    }

    public Boolean getState() {
        return state;
    }

    public OrgVo setState(Boolean state) {
        this.state = state;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public OrgVo setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrgVo setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OrgVo setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
