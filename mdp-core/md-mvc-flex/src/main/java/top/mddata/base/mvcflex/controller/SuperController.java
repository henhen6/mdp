package top.mddata.base.mvcflex.controller;

import cn.hutool.core.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.service.SuperService;

/**
 * 简单的实现了BaseController，为了获取注入 Service 和 实体类型
 * <p>
 * 基类该类后，没有任何方法。
 * 可以让业务Controller继承 SuperSimpleController 后，按需实现 *Controller 接口
 *
 * @param <S>      Service
 * @param <Entity> 实体
 * @author henhen6
 * @since 2020年03月07日22:08:27
 */
public abstract class SuperController<S extends SuperService<Entity>, Entity extends BaseEntity<?>>
        implements BaseController<Entity> {
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    protected S superService;
    protected Class<Entity> entityClass = (Class<Entity>) TypeUtil.getTypeArgument(this.getClass(), 1);

    @Override
    public Class<Entity> getEntityClass() {
        return this.entityClass;
    }

    @Override
    public SuperService<Entity> getSuperService() {
        return superService;
    }


}
