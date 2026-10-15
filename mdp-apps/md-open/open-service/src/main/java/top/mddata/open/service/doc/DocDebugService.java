package top.mddata.open.service.doc;

import top.mddata.open.dto.client.DocDebugBuildParamDto;
import top.mddata.open.vo.client.DocDebugBuildParamVo;

/**
 * 接口在线调试
 */
public interface DocDebugService {

    /**
     * 按 mdp-sdk 规则组装并签名调试请求参数（与 OpenClient.execute 的组装逻辑一致），
     * 最终请求由开发者浏览器直连网关
     *
     * @param dto 调试参数
     * @return 可直接提交的 url 与表单参数
     */
    DocDebugBuildParamVo buildParam(DocDebugBuildParamDto dto);
}
