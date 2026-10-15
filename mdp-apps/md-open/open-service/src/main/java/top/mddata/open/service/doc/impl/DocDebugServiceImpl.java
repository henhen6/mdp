package top.mddata.open.service.doc.impl;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import top.mddata.sdk.core.common.OpenConfig;
import top.mddata.sdk.core.exception.SopSignException;
import top.mddata.sdk.core.sign.SignUtil;

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

    private final DocInfoService docInfoService;

    @Override
    public DocDebugBuildParamVo buildParam(DocDebugBuildParamDto dto) {
        // getDocView 内部已校验文档存在且已发布，不存在时抛 BizException("文档不存在")
        DocInfoViewVo docView = docInfoService.getDocView(dto.getDocId());
        TornaDocInfoViewVo doc = docView.getDocInfoView();
        if (doc == null || !StringUtils.hasText(doc.getUrl())) {
            throw new BizException("文档不存在或未配置接口地址");
        }

        OpenConfig config = new OpenConfig();
        String charset = StringUtils.hasText(dto.getCharset()) ? dto.getCharset() : config.getCharset();
        String signType = StringUtils.hasText(dto.getSignType()) ? dto.getSignType() : config.getSignType();
        String format = StringUtils.hasText(dto.getFormat()) ? dto.getFormat() : config.getFormatType();
        String version = resolveVersion(dto.getVersion(), doc.getVersion(), config.getDefaultVersion());

        String bizContent = dto.getBizContent().trim();
        if (!JSON.isValid(bizContent)) {
            throw new BizException("业务参数 bizContent 不是合法的 JSON");
        }

        Map<String, String> form = new LinkedHashMap<>();
        form.put(config.getMethodName(), doc.getUrl());
        form.put(config.getVersionName(), version);
        form.put(config.getCharsetName(), charset);
        form.put(config.getFormatName(), format);
        form.put(config.getSignTypeName(), signType);
        String timestamp = new SimpleDateFormat(config.getTimestampPattern()).format(new Date());
        form.put(config.getTimestampName(), timestamp);
        form.put(config.getAppKeyName(), dto.getAppKey());
        if (StringUtils.hasText(dto.getAccessToken())) {
            form.put(config.getAccessTokenName(), dto.getAccessToken());
        }
        form.put(config.getDataName(), bizContent);

        if (StringUtils.hasText(dto.getPrivateKey())) {
            form.put(config.getSignName(), sign(form, dto.getPrivateKey(), charset, signType));
        }

        String url = buildUrl(dto.getEnv(), docView.getDocInfoConfig(), doc.getUrl());
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
        } catch (SopSignException e) {
            // 私钥格式错误是开发者填写问题，包装为业务异常给出明确提示（异常详情含签名内容，不向上抛出）
            log.warn("调试参数签名失败: {}", e.getMessage());
            throw new BizException("私钥格式错误或签名算法不支持，请检查私钥与 signType");
        }
    }

    /**
     * 规范化私钥：SDK 的 getPrivateKeyFromPKCS8 只接受纯 Base64 内容，
     * 而开发者从 PEM 文件复制的私钥通常带 BEGIN/END 头尾行与换行，直接传入会解码失败
     */
    private String normalizePrivateKey(String privateKey) {
        return privateKey.replaceAll("-----[^-]+-----", "").replaceAll("\\s", "");
    }

    /**
     * 拼接完整请求地址，规则与前端文档页 currentUrl 一致
     */
    private String buildUrl(String env, DocSettingVo config, String path) {
        boolean prod = "prod".equals(env);
        String base = prod ? config.getOpenProdUrl() : config.getOpenSandboxUrl();
        if (!StringUtils.hasText(base) || "未配置".equals(base)) {
            throw new BizException((prod ? "生产" : "沙箱") + "环境网关地址未配置");
        }
        if (path.matches("(?i)^https?://.*")) {
            return path;
        }
        return base.replaceAll("/+$", "") + "/" + path.replaceAll("^/+", "");
    }
}
