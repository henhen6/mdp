package top.mddata.open.service.doc.impl;

import com.alibaba.fastjson2.JSON;
import com.gitee.sop.support.constant.SopConstants;
import com.gitee.sop.support.dto.ApiConfig;
import com.gitee.sop.support.exception.SignException;
import com.gitee.sop.support.util.SignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.mddata.base.exception.BizException;
import top.mddata.open.dto.client.DocDebugBuildParamDto;
import top.mddata.open.service.doc.DocDebugService;
import top.mddata.open.service.doc.DocInfoService;
import top.mddata.open.vo.admin.DocInfoViewVo;
import top.mddata.open.vo.admin.DocSettingVo;
import top.mddata.open.vo.admin.TornaDocInfoViewVo;
import top.mddata.open.vo.client.DocDebugBuildParamVo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 接口在线调试
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DocDebugServiceImpl implements DocDebugService {

    /** 默认请求格式，与 SDK 侧 OpenConfig.formatType 默认值一致 */
    private static final String DEFAULT_FORMAT = "json";

    private final DocInfoService docInfoService;

    /** API 单入口前缀，与网关 mdp.gateway.path 对应；默认值兜底单测手动 new 的场景 */
    @Value("${mdp.open.debug.api-prefix:/api}")
    private String apiPrefix = "/api";

    /** REST 入口前缀，与网关 mdp.gateway.rest 对应；默认值兜底单测手动 new 的场景 */
    @Value("${mdp.open.debug.rest-prefix:/rest}")
    private String restPrefix = "/rest";

    @Override
    public DocDebugBuildParamVo buildParam(DocDebugBuildParamDto dto) {
        // getDocView 内部已校验文档存在且已发布，不存在时抛 BizException("文档不存在")
        DocInfoViewVo docView = docInfoService.getDocView(dto.getDocId());
        TornaDocInfoViewVo doc = docView.getDocInfoView();
        if (doc == null || !StringUtils.hasText(doc.getUrl())) {
            throw new BizException("文档不存在或未配置接口地址");
        }

        ApiConfig config = new ApiConfig();
        String charset = StringUtils.hasText(dto.getCharset()) ? dto.getCharset() : SopConstants.UTF8;
        String signType = StringUtils.hasText(dto.getSignType()) ? dto.getSignType() : SignUtil.RSA2;
        String format = StringUtils.hasText(dto.getFormat()) ? dto.getFormat() : DEFAULT_FORMAT;
        String version = resolveVersion(dto.getVersion(), doc.getVersion(), SopConstants.DEFAULT_VERSION);

        String bizContent = dto.getBizContent().trim();
        if (!JSON.isValid(bizContent)) {
            throw new BizException("业务参数 bizContent 不是合法的 JSON");
        }

        boolean rest = "rest".equalsIgnoreCase(dto.getPathType());

        Map<String, String> form = new LinkedHashMap<>();
        // rest 风格下接口名与版本由网关从 URL 提取，form 中不放 method/version，签名内容也随之减少
        if (!rest) {
            form.put(config.getApiName(), doc.getUrl());
            form.put(config.getVersionName(), version);
        }
        form.put(config.getCharsetName(), charset);
        form.put(config.getFormatName(), format);
        form.put(config.getSignTypeName(), signType);
        String timestamp = new SimpleDateFormat(config.getTimestampPattern()).format(new Date());
        form.put(config.getTimestampName(), timestamp);
        form.put(config.getAppKeyName(), dto.getAppKey());
        if (StringUtils.hasText(dto.getAccessToken())) {
            form.put(config.getAccessTokenName(), dto.getAccessToken());
        }
        form.put(config.getBizContentName(), bizContent);

        if (StringUtils.hasText(dto.getPrivateKey())) {
            form.put(config.getSignName(), sign(form, dto.getPrivateKey(), charset, signType));
        }

        String url = buildUrl(dto.getEnv(), docView.getDocInfoConfig(), doc.getUrl(), rest, version);
        return DocDebugBuildParamVo.builder().url(url).form(form).build();
    }

    /**
     * version 优先级：调试页填写 > 文档自带 > SDK 默认
     */
    private String resolveVersion(String dtoVersion, String docVersion, String defaultVersion) {
        if (StringUtils.hasText(dtoVersion)) {
            return dtoVersion;
        }
        if (StringUtils.hasText(docVersion)) {
            return docVersion;
        }
        return defaultVersion;
    }

    private String sign(Map<String, String> form, String privateKey, String charset, String signType) {
        try {
            String content = SignUtil.getSignContent(form);
            return SignUtil.rsaSign(content, normalizePrivateKey(privateKey), charset, signType);
        } catch (SignException e) {
            // 私钥格式错误是开发者填写问题，包装为业务异常给出明确提示（异常详情含签名内容，不向上抛出）
            log.warn("调试参数签名失败: {}", e.getMessage());
            throw new BizException("私钥格式错误或签名算法不支持，请检查私钥与 signType");
        }
    }

    /**
     * 规范化私钥：签名工具的 getPrivateKeyFromPKCS8 只接受纯 Base64 内容，
     * 而开发者从 PEM 文件复制的私钥通常带 BEGIN/END 头尾行与换行，直接传入会解码失败
     */
    private String normalizePrivateKey(String privateKey) {
        return privateKey.replaceAll("-----[^-]+-----", "").replaceAll("\\s", "");
    }

    /**
     * 拼接完整请求地址，规则与前端调试面板 buildEnvUrl 一致。
     * 环境地址为网关根地址（不含入口前缀），入口前缀（api/rest）由本方法拼接；
     * api 风格为单入口（接口名走 form 的 method 参数），URL 只到入口前缀；
     * rest 风格时接口名拼在路径上，版本号以 ?v= 形式放入 query（网关从 query 读取版本）。
     */
    private String buildUrl(String env, DocSettingVo config, String path, boolean rest, String version) {
        boolean prod = "prod".equals(env);
        String base = prod ? config.getOpenProdUrl() : config.getOpenSandboxUrl();
        if (!StringUtils.hasText(base) || "未配置".equals(base)) {
            throw new BizException((prod ? "生产" : "沙箱") + "环境网关地址未配置");
        }
        if (path.matches("(?i)^https?://.*")) {
            return path;
        }
        String prefix = rest ? restPrefix : apiPrefix;
        String root = base.replaceAll("/+$", "") + "/" + prefix.replaceAll("^/+|/+$", "");
        if (!rest) {
            return root;
        }
        return root + "/" + path.replaceAll("^/+", "") + "?v=" + version;
    }
}
