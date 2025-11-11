package top.mddata.base.mybatisflex.listener;

import top.mddata.base.base.entity.SuperEntity;
import top.mddata.base.utils.ContextUtil;
import top.mddata.base.utils.StrPool;
import cn.hutool.core.util.ReflectUtil;
import com.mybatisflex.annotation.UpdateListener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * 修改时数据填充监听器
 * @author henhen6
 * @since 2024年06月07日16:10:28
 */
public class DefaultUpdateListener implements UpdateListener {
    @Override
    public void onUpdate(Object param) {
        if (param instanceof SuperEntity entity) {
            if (entity.getUpdatedBy() == null) {
                Field updatedByField = ReflectUtil.getField(param.getClass(), SuperEntity.UPDATED_BY);
                Object userIdVal = ContextUtil.getUserId();
                if (updatedByField != null) {
                    userIdVal = StrPool.STRING_TYPE_NAME.equals(updatedByField.getGenericType().getTypeName()) ? String.valueOf(ContextUtil.getUserId()) : ContextUtil.getUserId();
                }

                entity.setUpdatedBy(userIdVal);
            }

            if (entity.getUpdatedAt() == null) {
                entity.setUpdatedAt(LocalDateTime.now());
            }
            return;
        }


        Field updatedByField = ReflectUtil.getField(param.getClass(), SuperEntity.UPDATED_BY);
        if (updatedByField != null) {
            Object fieldValue = ReflectUtil.getFieldValue(param, updatedByField);
            if (fieldValue == null) {
                Object userIdVal = StrPool.STRING_TYPE_NAME.equals(updatedByField.getGenericType().getTypeName()) ? String.valueOf(ContextUtil.getUserId()) : ContextUtil.getUserId();

                ReflectUtil.setFieldValue(param, updatedByField, userIdVal);
            }
        }

        Field updatedTimeField = ReflectUtil.getField(param.getClass(), SuperEntity.UPDATED_AT);
        if (updatedTimeField != null) {
            Object fieldValue = ReflectUtil.getFieldValue(param, updatedTimeField);
            if (fieldValue == null) {
                ReflectUtil.setFieldValue(param, updatedTimeField, LocalDateTime.now());
            }
        }
    }
}
