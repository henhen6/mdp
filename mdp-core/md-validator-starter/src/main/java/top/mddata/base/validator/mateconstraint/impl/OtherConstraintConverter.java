package top.mddata.base.validator.mateconstraint.impl;

import top.mddata.base.validator.mateconstraint.IConstraintConverter;
import top.mddata.base.validator.utils.ValidatorConstants;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 其他 转换器
 *
 * @author henhen6
 * @date 2019-07-25 15:15
 */
public class OtherConstraintConverter extends BaseConstraintConverter implements IConstraintConverter {

    @Override
    protected String getType(Class<? extends Annotation> type) {
        return type.getSimpleName();
    }

    @Override
    protected List<Class<? extends Annotation>> getSupport() {
        return new ArrayList<>();
    }


    @Override
    protected List<String> getMethods() {
        return Collections.singletonList(ValidatorConstants.MESSAGE);
    }
}
