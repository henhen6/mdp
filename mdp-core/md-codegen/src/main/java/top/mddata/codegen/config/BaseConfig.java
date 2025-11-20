package top.mddata.codegen.config;

import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.codegen.constant.GenerationStrategyEnum;

import java.io.Serializable;

/**
 *
 * @author henhen6
 * @since 2024/7/7 21:26
 */
@Data
@Accessors(chain = true)
public class BaseConfig implements Serializable {
    /**
     * 生成策略。
     */
    private GenerationStrategyEnum generationStrategy = GenerationStrategyEnum.OVERWRITE;
}
