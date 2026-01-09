package top.mddata.api.oepn.message;

import com.gitee.sop.support.message.I18nMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 六如
 */
@AllArgsConstructor
@Getter
public enum StoryMessageEnum implements I18nMessage {
    /**
     * 业务异常
     */
    PARAM_VALIDATION("biz.param-validation");

    private final String configKey;

}
