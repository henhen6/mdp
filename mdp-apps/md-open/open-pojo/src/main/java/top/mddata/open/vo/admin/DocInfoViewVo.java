package top.mddata.open.vo.admin;

import lombok.Data;

/**
 * @author henhen6
 */
@Data
public class DocInfoViewVo {

    /**
     * 文档详情
     */
    private TornaDocInfoViewVo docInfoView;

    /**
     * 文档配置
     */
    private DocSettingVo docInfoConfig;

    /**
     * 接口是否需要 token（来自接口注册表, null 表示接口未注册, 前端按"需要"回落展示）
     */
    private Integer needToken;

    /**
     * 接口是否需要签名（来自接口注册表, null 表示接口未注册, 前端按"需要"回落展示）
     */
    private Integer needSign;

}
