package top.mddata.console.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.CollHelper;
import top.mddata.base.utils.ContextUtil;
import top.mddata.common.cache.console.system.ConfigCacheKeyBuilder;
import top.mddata.common.cache.console.system.ConfigUniqKeyCacheKeyBuilder;
import top.mddata.common.enumeration.system.DataTypeEnum;
import top.mddata.console.system.entity.Config;
import top.mddata.console.system.mapper.ConfigMapper;
import top.mddata.console.system.service.ConfigService;
import top.mddata.console.system.vo.ConfigVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 系统配置 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigServiceImpl extends SuperServiceImpl<ConfigMapper, Config> implements ConfigService {
    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new ConfigCacheKeyBuilder();
    }

    @Override
    protected Config updateBefore(Object updateDto) {
        Config sysParam = super.updateBefore(updateDto);

        Config old = this.getById(sysParam.getId());
        if (!StrUtil.equals(old.getUniqKey(), sysParam.getUniqKey())) {
            CacheKey uniqKey = ConfigUniqKeyCacheKeyBuilder.builder(old.getUniqKey());
            cacheOps.del(uniqKey);
        }

        // 当前机构id
        Long currentCompanyId = ContextUtil.getCurrentCompanyId();
        if (ContextUtil.getTopCompanyIsAdmin()) {
            // 若是当前组织性质是运维管理员，则不填写机构id
            sysParam.setOrgId(null);
        } else {
            sysParam.setOrgId(currentCompanyId);
        }
        // 不同的机构下，参数可以重复，取值时，优先取当前机构的
        ArgumentAssert.isFalse(check(sysParam.getUniqKey(), sysParam.getOrgId(), sysParam.getId()), "参数标识不能重复");

        return sysParam;
    }

    @Override
    protected Config saveBefore(Object save) {
        Config sysParam = super.saveBefore(save);
        Long currentCompanyId = ContextUtil.getCurrentCompanyId();
        if (ContextUtil.getTopCompanyIsAdmin()) {
            // 若是当前组织性质是运维管理员，则不填写机构id
            sysParam.setOrgId(null);
        } else {
            sysParam.setOrgId(currentCompanyId);
        }

        ArgumentAssert.isFalse(check(sysParam.getUniqKey(), sysParam.getOrgId(), null), "参数标识不能重复");

        return sysParam;
    }

    @Override
    protected void saveAfter(Object save, Config entity) {
        CacheKey key = ConfigUniqKeyCacheKeyBuilder.builder(entity.getUniqKey());
        cacheOps.del(key);
    }

    @Override
    protected void updateAfter(Object update, Config entity) {
        CacheKey key = ConfigUniqKeyCacheKeyBuilder.builder(entity.getUniqKey());
        cacheOps.del(key);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean check(String uniqKey, Long orgId, Long id) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (orgId == null) {
            wrapper.isNull(Config::getOrgId);
        } else {
            wrapper.eq(Config::getOrgId, orgId);
        }
        wrapper.eq(Config::getUniqKey, uniqKey);
        wrapper.ne(Config::getId, id);
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        if (CollUtil.isEmpty(idList)) {
            return true;
        }
        List<Config> sysParams = listByIds(idList);
        if (CollUtil.isEmpty(sysParams)) {
            return true;
        }
        boolean flag = super.removeByIds(idList);

        // 同时清理2种缓存
        List<CacheKey> uniqKeyCacheKeys = sysParams.stream().map(Config::getUniqKey).map(ConfigUniqKeyCacheKeyBuilder::builder).toList();
        cacheOps.del(uniqKeyCacheKeys);
        delCache(idList);
        return flag;
    }

    @Override
    @Transactional(readOnly = true)
    public Config getByUniqKey(String uniqKey) {
        CacheKey key = ConfigUniqKeyCacheKeyBuilder.builder(uniqKey);
        CacheResult<Long> uniqKeyResult = cacheOps.get(key, k -> {
            Config param = this.getOne(new QueryWrapper().eq(Config::getUniqKey, uniqKey));
            return param != null ? param.getId() : null;
        });
        Long id = uniqKeyResult.getValue();
        return this.getByIdCache(id);
    }


    @Override
    @Transactional(readOnly = true)
    public ConfigVo getConfig(String uniqKey) {
        Config sysParam = getByUniqKey(uniqKey);
        return BeanUtil.toBean(sysParam, ConfigVo.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLong(String uniqKey, Long defaultValue) {
        Config sysParam = getByUniqKey(uniqKey);
        if (sysParam == null) {
            return defaultValue;
        }
        ArgumentAssert.isTrue(DataTypeEnum.INTEGER.eq(sysParam.getDataType()), "系统参数[{}]不是[{}]", uniqKey, DataTypeEnum.INTEGER.getDesc());

        return Convert.toLong(sysParam.getValue(), defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getInteger(String uniqKey, Integer defaultValue) {
        Config sysParam = getByUniqKey(uniqKey);
        if (sysParam == null) {
            return defaultValue;
        }
        ArgumentAssert.isTrue(DataTypeEnum.INTEGER.eq(sysParam.getDataType()), "系统参数[{}]不是[{}]", uniqKey, DataTypeEnum.INTEGER.getDesc());

        return Convert.toInt(sysParam.getValue(), defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public String getString(String uniqKey, String defaultValue) {
        Config sysParam = getByUniqKey(uniqKey);
        if (sysParam == null) {
            return defaultValue;
        }
        ArgumentAssert.isTrue(DataTypeEnum.STRING.eq(sysParam.getDataType()), "系统参数[{}]不是[{}]", uniqKey, DataTypeEnum.INTEGER.getDesc());

        return Convert.toStr(sysParam.getValue(), defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean getBoolean(String uniqKey, Boolean defaultValue) {
        Config sysParam = getByUniqKey(uniqKey);
        if (sysParam == null) {
            return defaultValue;
        }
        ArgumentAssert.isTrue(DataTypeEnum.BOOLEAN.eq(sysParam.getDataType()), "系统参数[{}]不是[{}]", uniqKey, DataTypeEnum.INTEGER.getDesc());

        return Convert.toBool(sysParam.getValue(), defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, ConfigVo> findConfigByUniqKey(List<String> uniqKeys) {
        if (CollUtil.isEmpty(uniqKeys)) {
            return Collections.emptyMap();
        }
        List<ConfigVo> sysParamVos = super.listAs(QueryWrapper.create().in(Config::getUniqKey, uniqKeys).isNull(Config::getOrgId).eq(Config::getState, true), ConfigVo.class);

        return CollHelper.buildMap(sysParamVos, ConfigVo::getUniqKey, item -> item);
    }
}
