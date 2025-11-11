package top.mddata.base.validator.mateconstraint.impl;


import top.mddata.base.annotation.constraints.NotEmptyPattern;
import top.mddata.base.validator.mateconstraint.IConstraintConverter;
import top.mddata.base.validator.utils.ValidatorConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * 正则校验规则
 *
 * @author henhen6
 */
public class RegExConstraintConverter extends BaseConstraintConverter implements IConstraintConverter {
    @Override
    protected String getType(Class<? extends Annotation> type) {
        return "RegEx";
    }

    @Override
    protected List<Class<? extends Annotation>> getSupport() {
        return Arrays.asList(Pattern.class, Email.class, URL.class, NotEmptyPattern.class);
    }

    @Override
    protected List<String> getMethods() {
        return Arrays.asList("regexp", ValidatorConstants.MESSAGE);
    }
}
