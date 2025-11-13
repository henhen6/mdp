package top.mddata.workbench.sso.config;

import cn.dev33.satoken.sso.config.SaSsoClientModel;
import cn.dev33.satoken.sso.template.SaSsoServerTemplate;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.open.manage.facade.AppFacade;
import top.mddata.open.manage.vo.AppVo;

import java.util.List;

/**
 * 重写 SaSsoServerTemplate 部分方法，增强功能
 *
 * @author henhen6
 * @since 2022-12-6
 */
@Component
public class CustomSaSsoServerTemplate extends SaSsoServerTemplate {
    @Autowired
    private AppFacade appFacade;

    /**
     * 把数据库中的 SysClient 实体类，转化为 Sa-Token SSO 框架所需要的 SaSsoClientModel 对象
     */
    public SaSsoClientModel convertToSaSsoClientModel(AppVo client) {
        // 构建 SaSsoClientModel
        SaSsoClientModel model = new SaSsoClientModel();
        model.setClient(client.getAppId());    // Client 标识
        model.setSecretKey(client.getAppSecret());   // 单独使用的秘钥
        model.setAllowUrl(client.getAllowUrl());    // 所有允许的授权地址，多个用逗号隔开
        model.setIsPush(client.getIsPush());  // 是否接收消息推送
        model.setPushUrl(client.getPushUrl());   // 推送消息地址
        model.setIsSlo(client.getIsSlo());   // 是否接收单点注销回调的消息推送
        return model;
    }

    // 根据 client 标识获取 SaSsoClientModel 对象
    @Override
    public SaSsoClientModel getClient(String clientSn) {
        R<AppVo> result = appFacade.getByAppId(clientSn);
        if (result.getIsSuccess()) {
            AppVo opApplication = result.getData();
            if (opApplication == null) {
                throw new BizException(StrUtil.format("应用无效：{} ", clientSn));
            }
            if (!opApplication.getState()) {
                throw new BizException("该应用已被封禁，无法授权认证");
            }
            return convertToSaSsoClientModel(opApplication);
        }
        return super.getClient(clientSn);

    }

    // 获取目前系统所有需要 push 消息的 Clients
    @Override
    public List<SaSsoClientModel> getNeedPushClients() {
        R<List<AppVo>> result = appFacade.listNeedPushApp();
        if (result.getIsSuccess()) {
            List<AppVo> list = result.getData();
            return list.stream().map(this::convertToSaSsoClientModel).toList();
        }
        // 转换
        return super.getNeedPushClients();
    }

}
