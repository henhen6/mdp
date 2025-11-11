package top.mddata.base.db.config;


import top.mddata.base.db.properties.DatabaseProperties;
import top.mddata.base.db.typehandler.FullLikeTypeHandler;
import top.mddata.base.db.typehandler.LeftLikeTypeHandler;
import top.mddata.base.db.typehandler.RightLikeTypeHandler;
import top.mddata.base.uid.dao.WorkerNodeDao;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.buffer.RejectedPutBufferHandler;
import com.baidu.fsg.uid.buffer.RejectedTakeBufferHandler;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.baidu.fsg.uid.impl.DefaultUidGenerator;
import com.baidu.fsg.uid.impl.HuToolUidGenerator;
import com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 持久层常用配置
 *
 * @author henhen6
 * @since 2018/10/24
 */
@Slf4j
public abstract class DbConfiguration {
    protected final DatabaseProperties databaseProperties;

    public DbConfiguration(final DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public UidGenerator getHuToolUidGenerator(WorkerNodeDao workerNodeDao) {
        switch (databaseProperties.getIdType()) {
            case CACHE -> {
                DisposableWorkerIdAssigner disposableWorkerIdAssigner = new DisposableWorkerIdAssigner(workerNodeDao);
                CachedUidGenerator uidGenerator = new CachedUidGenerator();
                DatabaseProperties.CacheId cacheId = databaseProperties.getCacheId();
                BeanUtil.copyProperties(cacheId, uidGenerator);
                if (cacheId.getRejectedPutBufferHandlerClass() != null) {
                    RejectedPutBufferHandler rejectedPutBufferHandler = ReflectUtil.newInstance(cacheId.getRejectedPutBufferHandlerClass());
                    uidGenerator.setRejectedPutBufferHandler(rejectedPutBufferHandler);
                }
                if (cacheId.getRejectedTakeBufferHandlerClass() != null) {
                    RejectedTakeBufferHandler rejectedTakeBufferHandler = ReflectUtil.newInstance(cacheId.getRejectedTakeBufferHandlerClass());
                    uidGenerator.setRejectedTakeBufferHandler(rejectedTakeBufferHandler);
                }
                uidGenerator.setWorkerIdAssigner(disposableWorkerIdAssigner);
                return uidGenerator;
            }
            case HU_TOOL -> {
                DatabaseProperties.HutoolId id = databaseProperties.getHutoolId();
                return new HuToolUidGenerator(id.getWorkerId(), id.getDataCenterId());
            }
            default -> {
                DisposableWorkerIdAssigner disposableWorkerIdAssigner = new DisposableWorkerIdAssigner(workerNodeDao);
                DefaultUidGenerator uidGenerator = new DefaultUidGenerator();
                BeanUtil.copyProperties(databaseProperties.getDefaultId(), uidGenerator);
                uidGenerator.setWorkerIdAssigner(disposableWorkerIdAssigner);
                return uidGenerator;
            }
        }
    }

    /**
     * Mybatis 自定义的类型处理器： 处理XML中  #{name,typeHandler=leftLike} 类型的参数
     * 用于左模糊查询时使用
     * <p>
     * eg：
     * and name like #{name,typeHandler=leftLike}
     *
     * @return 左模糊处理器
     */
    @Bean
    public LeftLikeTypeHandler getLeftLikeTypeHandler() {
        return new LeftLikeTypeHandler();
    }

    /**
     * Mybatis 自定义的类型处理器： 处理XML中  #{name,typeHandler=rightLike} 类型的参数
     * 用于右模糊查询时使用
     * <p>
     * eg：
     * and name like #{name,typeHandler=rightLike}
     *
     * @return 右模糊处理器
     */
    @Bean
    public RightLikeTypeHandler getRightLikeTypeHandler() {
        return new RightLikeTypeHandler();
    }

    /**
     * Mybatis 自定义的类型处理器： 处理XML中  #{name,typeHandler=fullLike} 类型的参数
     * 用于全模糊查询时使用
     * <p>
     * eg：
     * and name like #{name,typeHandler=fullLike}
     *
     * @return 全模糊处理器
     */
    @Bean
    public FullLikeTypeHandler getFullLikeTypeHandler() {
        return new FullLikeTypeHandler();
    }
}
