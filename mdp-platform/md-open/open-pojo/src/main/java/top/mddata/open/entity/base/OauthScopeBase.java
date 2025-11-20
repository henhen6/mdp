package top.mddata.open.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * oauth2权限实体类。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OauthScopeBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_oauth_scope";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 介绍
     */
    private String intro;

    /**
     * 申请提示语
     */
    private String applyPrompt;

    /**
     * 确认提示语
     */
    private String confirmPrompt;

    /**
     * 权重
     */
    private Long weight;

    /**
     * 权限等级
     * [1-公开 2-特殊]
     */
    private Integer level;

}
