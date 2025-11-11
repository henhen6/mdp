package top.mddata.base.validator.component.extract;

import top.mddata.base.validator.model.FieldValidatorDesc;
import top.mddata.base.validator.model.ValidConstraint;

import java.util.Collection;
import java.util.List;


/**
 * 提取指定表单验证规则
 *
 * @author henhen6
 * @date 2019-06-12
 */
public interface IConstraintExtract {

    /**
     * 提取指定表单验证规则
     *
     * @param constraints 限制条件
     * @return 验证规则
     * @throws Exception 异常
     */
    Collection<FieldValidatorDesc> extract(List<ValidConstraint> constraints) throws Exception;
}
