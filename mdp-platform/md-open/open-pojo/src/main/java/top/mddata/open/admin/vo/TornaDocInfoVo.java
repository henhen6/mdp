package top.mddata.open.admin.vo;

import lombok.Data;

/**
 * @author henhen6
 */
@Data
public class TornaDocInfoVo {
    private Long id;

    /**
     * 文档名称
     */
    private String name;

    /**
     * 文档概述
     */
    private String description;

    /**
     * 访问URL
     */
    private String url;

    /**
     * 版本号
     */
    private String version;

    /**
     * http方法
     */
    private String httpMethod;

    /**
     * contentType
     */
    private String contentType;

    /**
     * 文档类型,0:http,1:dubbo,2:富文本,3:Markdown
     */
    private Byte type;

    /**
     * 是否是分类，0：不是，1：是
     */
    private Integer isFolder;

    /**
     * 父节点
     */
    private Long parentId;

    /**
     * 是否显示
     */
    private Byte isShow;

}
