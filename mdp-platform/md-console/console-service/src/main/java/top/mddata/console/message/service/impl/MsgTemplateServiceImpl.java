package top.mddata.console.message.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.Kv;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.StrPool;
import top.mddata.console.message.entity.InterfaceConfig;
import top.mddata.console.message.entity.MsgTemplate;
import top.mddata.console.message.mapper.MsgTemplateMapper;
import top.mddata.console.message.service.InterfaceConfigService;
import top.mddata.console.message.service.MsgTemplateService;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息模板 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MsgTemplateServiceImpl extends SuperServiceImpl<MsgTemplateMapper, MsgTemplate> implements MsgTemplateService {
    /**
     * 解析占位符 ${xxx}
     */
    private static final Pattern REG_EX = Pattern.compile("\\$\\{([^}]+)}");
    private final InterfaceConfigService interfaceConfigService;

    /**
     * 解析参数
     * @param title 标题
     * @param content 内容
     */
    private static String parseParam(String title, String content) {
        // 查找字符串中是否有匹配正则表达式的字符/字符串//有序， 目的是为了兼容 腾讯云参数
        Set<Kv> list = new LinkedHashSet<>();
        if (StrUtil.isNotEmpty(title)) {
            //忽略大小写的写法
            Matcher matcher = REG_EX.matcher(title);
            while (matcher.find()) {
                String key = matcher.group(1);
                list.add(Kv.builder().key(key).value(StrPool.EMPTY).build());
            }
        }

        if (StrUtil.isNotEmpty(content)) {
            Matcher matcher = REG_EX.matcher(content);
            while (matcher.find()) {
                String key = matcher.group(1);
                list.add(Kv.builder().key(key).value(StrPool.EMPTY).build());
            }
        }
        return JSON.toJSONString(list);
    }

    @Override
    @Transactional(readOnly = true)
    public MsgTemplate getByTemplateKey(String templateKey) {
        return getOne(QueryWrapper.create().eq(MsgTemplate::getKey, templateKey, true));
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean check(String key, Long id) {
        ArgumentAssert.notEmpty(key, "请填写模板标识");
        return count(QueryWrapper.create().eq(MsgTemplate::getKey, key).ne(MsgTemplate::getId, id)) > 0;
    }

    @Override
    protected MsgTemplate updateBefore(Object update) {
        MsgTemplate entity = UpdateEntity.of(getEntityClass());
        BeanUtil.copyProperties(update, entity);
        InterfaceConfig interfaceConfig = interfaceConfigService.getById(entity.getInterfaceConfigId());
        ArgumentAssert.notNull(interfaceConfig, "接口不存在");
        ArgumentAssert.isFalse(StrUtil.isNotBlank(entity.getKey()) && check(entity.getKey(), entity.getId()), "模板标识{}已存在", entity.getKey());
        entity.setParam(parseParam(entity.getTitle(), entity.getContent()));
        entity.setMsgType(interfaceConfig.getMsgType());
        return entity;
    }

    @Override
    protected MsgTemplate saveBefore(Object save) {
        MsgTemplate entity = BeanUtil.toBean(save, getEntityClass());
        InterfaceConfig interfaceConfig = interfaceConfigService.getById(entity.getInterfaceConfigId());
        ArgumentAssert.notNull(interfaceConfig, "接口不存在");
        entity.setId(null);
        ArgumentAssert.isFalse(StrUtil.isNotBlank(entity.getKey()) && check(entity.getKey(), entity.getId()), "模板标识{}已存在", entity.getKey());
        entity.setParam(parseParam(entity.getTitle(), entity.getContent()));
        entity.setMsgType(interfaceConfig.getMsgType());
        return entity;
    }
}
