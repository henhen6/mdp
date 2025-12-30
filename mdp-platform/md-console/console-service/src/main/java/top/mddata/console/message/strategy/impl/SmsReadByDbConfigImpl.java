package top.mddata.console.message.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.aliyun.config.AlibabaConfig;
import org.dromara.sms4j.baidu.config.BaiduConfig;
import org.dromara.sms4j.budingyun.config.BudingV2Config;
import org.dromara.sms4j.chuanglan.config.ChuangLanConfig;
import org.dromara.sms4j.cloopen.config.CloopenConfig;
import org.dromara.sms4j.comm.constant.SupplierConstant;
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.ctyun.config.CtyunConfig;
import org.dromara.sms4j.danmi.config.DanMiConfig;
import org.dromara.sms4j.dingzhong.config.DingZhongConfig;
import org.dromara.sms4j.emay.config.EmayConfig;
import org.dromara.sms4j.huawei.config.HuaweiConfig;
import org.dromara.sms4j.jdcloud.config.JdCloudConfig;
import org.dromara.sms4j.jg.config.JgConfig;
import org.dromara.sms4j.lianlu.config.LianLuConfig;
import org.dromara.sms4j.luosimao.config.LuoSiMaoConfig;
import org.dromara.sms4j.mas.config.MasConfig;
import org.dromara.sms4j.netease.config.NeteaseConfig;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.dromara.sms4j.qiniu.config.QiNiuConfig;
import org.dromara.sms4j.submail.config.SubMailConfig;
import org.dromara.sms4j.tencent.config.TencentConfig;
import org.dromara.sms4j.unisms.config.UniConfig;
import org.dromara.sms4j.yixintong.config.YiXintongConfig;
import org.dromara.sms4j.yunpian.config.YunpianConfig;
import org.dromara.sms4j.zhutong.config.ZhutongConfig;
import org.springframework.stereotype.Component;
import top.mddata.console.message.dto.InterfaceConfigJsonDto;
import top.mddata.console.message.entity.InterfaceConfig;
import top.mddata.console.message.enumeration.MsgTypeEnum;
import top.mddata.console.message.mapper.InterfaceConfigMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信接口类配置
 *
 * @author henhen
 * @since 2025/12/29 11:23
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SmsReadByDbConfigImpl implements SmsReadConfig {
    private final InterfaceConfigMapper interfaceConfigMapper;

    private static BaseConfig getBaseConfig(InterfaceConfig interfaceConfig) {
        List<InterfaceConfigJsonDto> configJsonList = interfaceConfig.getConfigJson();
        if (CollUtil.isEmpty(configJsonList)) {
            return null;
        }
        Map<String, String> configParam = new HashMap<>();
        configJsonList.forEach(configJson -> {
            configParam.put(configJson.getKey(), configJson.getValue());
        });
        BaseConfig config = switch (interfaceConfig.getKey()) {
            case SupplierConstant.ALIBABA -> new AlibabaConfig();
            case SupplierConstant.BAIDU -> new BaiduConfig();
            case SupplierConstant.BUDING_V2 -> new BudingV2Config();
            case SupplierConstant.CHUANGLAN -> new ChuangLanConfig();
            case SupplierConstant.CLOOPEN -> new CloopenConfig();
            case SupplierConstant.CTYUN -> new CtyunConfig();
            case SupplierConstant.DAN_MI -> new DanMiConfig();
            case SupplierConstant.DINGZHONG -> new DingZhongConfig();
            case SupplierConstant.EMAY -> new EmayConfig();
            case SupplierConstant.HUAWEI -> new HuaweiConfig();
            case SupplierConstant.JDCLOUD -> new JdCloudConfig();
            case SupplierConstant.JIGUANG -> new JgConfig();
            case SupplierConstant.LIANLU -> new LianLuConfig();
            case SupplierConstant.LUO_SI_MAO -> new LuoSiMaoConfig();
            case SupplierConstant.MAS -> new MasConfig();
            case SupplierConstant.NETEASE -> new NeteaseConfig();
            case SupplierConstant.QINIU -> new QiNiuConfig();
            case SupplierConstant.MY_SUBMAIL -> new SubMailConfig();
            case SupplierConstant.TENCENT -> new TencentConfig();
            case SupplierConstant.UNISMS -> new UniConfig();
            case SupplierConstant.YIXINTONG -> new YiXintongConfig();
            case SupplierConstant.YUNPIAN -> new YunpianConfig();
            case SupplierConstant.ZHUTONG -> new ZhutongConfig();
            default -> new ChuangLanConfig();
        };

        BeanUtil.fillBeanWithMap(configParam, config, true);
        return config;
    }

    @Override
    public BaseConfig getSupplierConfig(String configId) {
        InterfaceConfig interfaceConfig = interfaceConfigMapper.selectOneByQuery(QueryWrapper.create().eq(InterfaceConfig::getKey, configId));
        if (interfaceConfig == null) {
            return null;
        }
        return getBaseConfig(interfaceConfig);

    }

    @Override
    public List<BaseConfig> getSupplierConfigList() {
        List<InterfaceConfig> list = interfaceConfigMapper.selectListByQuery(QueryWrapper.create().eq(InterfaceConfig::getMsgType, MsgTypeEnum.SMS.getCode()));
        List<BaseConfig> configList = new ArrayList<>();
        list.forEach(interfaceConfig -> {
            BaseConfig baseConfig = getBaseConfig(interfaceConfig);
            if (baseConfig != null) {
                configList.add(baseConfig);
            }
        });
        return configList;
    }
}
