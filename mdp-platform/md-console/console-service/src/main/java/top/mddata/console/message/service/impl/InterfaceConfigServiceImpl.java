package top.mddata.console.message.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.message.dto.InterfaceConfigDto;
import top.mddata.console.message.dto.InterfaceConfigSettingDto;
import top.mddata.console.message.entity.InterfaceConfig;
import top.mddata.console.message.entity.InterfaceStat;
import top.mddata.console.message.enumeration.InterfaceExecModeEnum;
import top.mddata.console.message.mapper.InterfaceConfigMapper;
import top.mddata.console.message.service.InterfaceConfigService;
import top.mddata.console.message.service.InterfaceStatService;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * 接口 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:47
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InterfaceConfigServiceImpl extends SuperServiceImpl<InterfaceConfigMapper, InterfaceConfig> implements InterfaceConfigService {
    private final InterfaceStatService interfaceStatService;

    @Override
    protected InterfaceConfig updateBefore(Object updateDto) {
        InterfaceConfigDto dto = (InterfaceConfigDto) updateDto;
        if (InterfaceExecModeEnum.MAGIC_API.eq(dto.getExecMode())) {
            ArgumentAssert.notEmpty(dto.getMagicApiId(), "请填写MagicApi实现类");
        } else if (InterfaceExecModeEnum.IMPL_CLASS.eq(dto.getExecMode())) {
            ArgumentAssert.notEmpty(dto.getImplClass(), "请填写实现类");
        } else {
            ArgumentAssert.notEmpty(dto.getScript(), "请填写实现脚本");
        }
        return super.updateBefore(updateDto);
    }

    @Override
    protected InterfaceConfig saveBefore(Object save) {
        InterfaceConfigDto dto = (InterfaceConfigDto) save;
        if (InterfaceExecModeEnum.MAGIC_API.eq(dto.getExecMode())) {
            ArgumentAssert.notEmpty(dto.getMagicApiId(), "请填写MagicApi实现类");
        } else if (InterfaceExecModeEnum.IMPL_CLASS.eq(dto.getExecMode())) {
            ArgumentAssert.notEmpty(dto.getImplClass(), "请填写实现类");
        } else {
            ArgumentAssert.notEmpty(dto.getScript(), "请填写实现脚本");
        }

        InterfaceConfig entity = BeanUtil.toBean(save, InterfaceConfig.class);
        entity.setId(null);
        entity.setConfigJson(JSON.toJSONString(Collections.emptyList()));
        return entity;
    }

    @Override
    protected void saveAfter(Object save, InterfaceConfig entity) {

        // ID一致
        InterfaceStat stat = BeanUtil.toBean(entity, InterfaceStat.class);
        interfaceStatService.save(stat);
    }

    @Override
    protected void updateAfter(Object updateDto, InterfaceConfig entity) {
        InterfaceStat stat = new InterfaceStat();
        stat.setId(entity.getId());
        stat.setName(entity.getName());
        interfaceStatService.updateById(stat);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateConfigById(InterfaceConfigSettingDto dto) {
        InterfaceConfig interfaceConfig = UpdateEntity.of(InterfaceConfig.class, dto.getId());
        if (CollUtil.isNotEmpty(dto.getConfigJsonList())) {
            interfaceConfig.setConfigJson(JSON.toJSONString(dto.getConfigJsonList()));
        } else {
            interfaceConfig.setConfigJson(JSON.toJSONString(Collections.emptyList()));
        }
        updateById(interfaceConfig);
        return interfaceConfig.getId();
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        interfaceStatService.removeByIds(idList);
        return super.removeByIds(idList);
    }

    @Override
    public boolean removeById(InterfaceConfig entity) {
        interfaceStatService.removeById(entity.getId());
        return super.removeById(entity);
    }

    @Override
    public boolean removeById(Serializable id) {
        interfaceStatService.removeById(id);
        return super.removeById(id);
    }
}
