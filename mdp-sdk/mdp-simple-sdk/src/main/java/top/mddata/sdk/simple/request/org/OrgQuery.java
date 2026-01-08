package top.mddata.sdk.simple.request.org;

/**
 * 分页查询用户信息 参数
 *
 * @author henhen
 */
public class OrgQuery {



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

    public Long getId() {
        return id;
    }

    public OrgQuery setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public OrgQuery setName(String name) {
        this.name = name;
        return this;
    }

    public String getOrgType() {
        return orgType;
    }

    public OrgQuery setOrgType(String orgType) {
        this.orgType = orgType;
        return this;
    }

    public String getShortName() {
        return shortName;
    }

    public OrgQuery setShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public Long getParentId() {
        return parentId;
    }

    public OrgQuery setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getTreePath() {
        return treePath;
    }

    public OrgQuery setTreePath(String treePath) {
        this.treePath = treePath;
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public OrgQuery setWeight(Integer weight) {
        this.weight = weight;
        return this;
    }

    public Boolean getState() {
        return state;
    }

    public OrgQuery setState(Boolean state) {
        this.state = state;
        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public OrgQuery setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }
}
