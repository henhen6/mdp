package top.mddata.open.vo.admin;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.open.entity.admin.base.EventPushBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件推送任务 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2026-01-12 21:28:36
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "事件推送任务Vo")
@Table(EventPushBase.TABLE_NAME)
public class EventPushVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private Long id;

    /**
     * 事件触发ID
     */
    @Schema(description = "事件触发ID")
    private Long eventTriggerId;

    /**
     * 事件类型
     */
    @Schema(description = "事件类型")
    private String eventCode;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 应用秘钥
     */
    @Schema(description = "应用秘钥")
    private String appKey;

    /**
     * 回调url
     */
    @Schema(description = "回调url")
    private String notifyUrl;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String requestData;

    /**
     * 最后请求时间
     */
    @Schema(description = "最后请求时间")
    private LocalDateTime lastRequestTime;

    /**
     * 下次请求时间
     */
    @Schema(description = "下次请求时间")
    private LocalDateTime nextRequestTime;

    /**
     * 最大请求次数
     */
    @Schema(description = "最大请求次数")
    private Integer maxRequestCnt;

    /**
     * 已请求次数
     */
    @Schema(description = "已请求次数")
    private Integer requestCnt;

    /**
     * 执行状态
     * [0-待执行 1-执行成功 2-执行失败 3-重试结束 4-手动结束]
     */
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

    /**
     * 修改人id
     */
    @Schema(description = "修改人id")
    private Long updatedBy;

}
