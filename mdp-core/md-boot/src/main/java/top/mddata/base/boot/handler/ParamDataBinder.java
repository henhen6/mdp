package top.mddata.base.boot.handler;

import jakarta.servlet.ServletRequest;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import java.util.Map;

/**
 *
 * @author henhen6
 * @since 2025/9/3 12:29
 */
public class ParamDataBinder extends ExtendedServletRequestDataBinder {
    private final Map<String, String> renameMapping;

    public ParamDataBinder(Object target, String objectName, Map<String, String> renameMapping) {
        super(target, objectName);
        this.renameMapping = renameMapping;
    }

    @Override
    protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
        super.addBindValues(mpvs, request);
        for (Map.Entry<String, String> entry : renameMapping.entrySet()) {
            String from = entry.getKey();
            String to = entry.getValue();
            if (mpvs.contains(from)) {
                mpvs.add(to, mpvs.getPropertyValue(from).getValue());
            }
        }
    }

}
