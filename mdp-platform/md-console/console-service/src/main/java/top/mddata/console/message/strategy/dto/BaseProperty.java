package top.mddata.console.message.strategy.dto;

import lombok.Data;

/**
 * 基础接口属性
 * @author henhen
 */
@Data
public class BaseProperty {
    private Boolean debug;

    public boolean initAndValid() {
        if (this.debug == null) {
            this.debug = false;
        }
        return true;
    }
}
