package top.mddata.base.mybatisflex.listener;

import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.base.entity.SuperEntity;
import top.mddata.base.utils.ContextUtil;
import top.mddata.base.utils.StrPool;
import cn.hutool.core.util.ReflectUtil;
import com.mybatisflex.annotation.InsertListener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * 新增时数据填充监听器
 * @author henhen6
 * @since 2024年06月07日16:10:28
 */
public class DefaultInsertListener implements InsertListener {
    @Override
    public void onInsert(Object param) {

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
        }

        if (param instanceof BaseEntity entity) {

            if (entity.getCreatedBy() == null) {

                Field createdByField = ReflectUtil.getField(param.getClass(), BaseEntity.CREATED_BY);
                Object userIdVal = ContextUtil.getUserId();
                if (createdByField != null) {
                    userIdVal = StrPool.STRING_TYPE_NAME.equals(createdByField.getGenericType().getTypeName()) ? String.valueOf(ContextUtil.getUserId()) : ContextUtil.getUserId();
                }
                entity.setCreatedBy(userIdVal);
            }
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(LocalDateTime.now());
            }
            return;
        }

        Field createdByField = ReflectUtil.getField(param.getClass(), BaseEntity.CREATED_BY);
        if (createdByField != null) {
            Object fieldValue = ReflectUtil.getFieldValue(param, createdByField);
            if (fieldValue == null) {
                Object userIdVal = StrPool.STRING_TYPE_NAME.equals(createdByField.getGenericType().getTypeName()) ? String.valueOf(ContextUtil.getUserId()) : ContextUtil.getUserId();
                ReflectUtil.setFieldValue(param, createdByField, userIdVal);
            }
        }

        Field createdTimeField = ReflectUtil.getField(param.getClass(), BaseEntity.CREATED_AT);
        if (createdTimeField != null) {
            Object fieldValue = ReflectUtil.getFieldValue(param, createdTimeField);
            if (fieldValue == null) {
                ReflectUtil.setFieldValue(param, createdTimeField, LocalDateTime.now());
            }
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
