package top.mddata.open.service.doc.impl;

import com.gitee.sop.support.dto.ApiConfig;
import com.gitee.sop.support.util.SignUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.mddata.base.exception.BizException;
import top.mddata.open.dto.client.DocDebugBuildParamDto;
import top.mddata.open.service.doc.DocInfoService;
import top.mddata.open.vo.admin.DocInfoViewVo;
import top.mddata.open.vo.admin.DocSettingVo;
import top.mddata.open.vo.admin.TornaDocInfoViewVo;
import top.mddata.open.vo.client.DocDebugBuildParamVo;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocDebugServiceImplTest {

    private DocInfoService docInfoService;
    private DocDebugServiceImpl service;

    @BeforeEach
    void setUp() {
        docInfoService = Mockito.mock(DocInfoService.class);
        service = new DocDebugServiceImpl(docInfoService);
    }

    private DocInfoViewVo mockDocView(String url, String docVersion, String sandboxUrl, String prodUrl) {
        TornaDocInfoViewVo doc = new TornaDocInfoViewVo();
        doc.setUrl(url);
        doc.setVersion(docVersion);
        DocSettingVo config = new DocSettingVo();
        config.setOpenSandboxUrl(sandboxUrl);
        config.setOpenProdUrl(prodUrl);
        DocInfoViewVo view = new DocInfoViewVo();
        view.setDocInfoView(doc);
        view.setDocInfoConfig(config);
        return view;
    }

    private DocDebugBuildParamDto baseDto() {
        return DocDebugBuildParamDto.builder()
                .docId(1L)
                .env("sandbox")
                .appKey("test-app-key")
                .bizContent("{\"orderNo\":\"123\"}")
                .build();
    }

    @Test
    void 正常组装_不签名() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "2.0", "http://sandbox.example.com/", "http://prod.example.com"));

        DocDebugBuildParamVo vo = service.buildParam(baseDto());

        // api 单入口：URL 只到 /api 前缀，接口名走 form 的 method 参数
        assertEquals("http://sandbox.example.com/api", vo.getUrl());
        Map<String, String> form = vo.getForm();
        assertEquals("user.page", form.get("method"));
        // 未传 version 时取文档版本
        assertEquals("2.0", form.get("version"));
        assertEquals("UTF-8", form.get("charset"));
        assertEquals("json", form.get("format"));
        assertEquals("RSA2", form.get("signType"));
        assertEquals("test-app-key", form.get("appKey"));
        assertEquals("{\"orderNo\":\"123\"}", form.get("bizContent"));
        assertTrue(form.containsKey("timestamp"));
        assertFalse(form.containsKey("sign"));
        assertFalse(form.containsKey("accessToken"));
    }

    @Test
    void version优先级_dto高于文档_文档为空时默认1点0() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "2.0", "http://s.example.com", "http://p.example.com"));
        DocDebugBuildParamDto dto = baseDto().setVersion("3.0");
        assertEquals("3.0", service.buildParam(dto).getForm().get("version"));

        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "", "http://s.example.com", "http://p.example.com"));
        assertEquals("1.0", service.buildParam(baseDto()).getForm().get("version"));
    }

    @Test
    void 生产环境取生产地址() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "", "http://s.example.com", "http://p.example.com"));
        DocDebugBuildParamVo vo = service.buildParam(baseDto().setEnv("prod"));
        assertEquals("http://p.example.com/api", vo.getUrl());
    }

    @Test
    void 私钥签名后可验签() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "", "http://s.example.com", "http://p.example.com"));

        DocDebugBuildParamVo vo = service.buildParam(baseDto().setPrivateKey(privateKey).setAccessToken("token-1"));

        Map<String, String> form = vo.getForm();
        assertTrue(form.containsKey("sign"));
        assertEquals("token-1", form.get("accessToken"));
        // 用服务端验签方法验证签名链路正确（与网关验签逻辑一致）
        assertTrue(SignUtil.rsaCheckV2(form, publicKey, "UTF-8", "RSA2", new ApiConfig()));
    }

    @Test
    void PEM格式私钥可正常签名() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        String base64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        // 模拟开发者从 PEM 文件复制的私钥：带头尾行与换行
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + base64.substring(0, 64) + "\n" + base64.substring(64) + "\n"
                + "-----END PRIVATE KEY-----";
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "", "http://s.example.com", "http://p.example.com"));

        DocDebugBuildParamVo vo = service.buildParam(baseDto().setPrivateKey(pem));

        assertTrue(SignUtil.rsaCheckV2(vo.getForm(), publicKey, "UTF-8", "RSA2", new ApiConfig()));
    }

    @Test
    void bizContent非法JSON时报错() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "", "http://s.example.com", "http://p.example.com"));
        DocDebugBuildParamDto dto = baseDto().setBizContent("not-json");
        assertThrows(BizException.class, () -> service.buildParam(dto));
    }

    @Test
    void 环境地址未配置时报错() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "", "未配置", "http://p.example.com"));
        assertThrows(BizException.class, () -> service.buildParam(baseDto()));
    }

    @Test
    void 文档url为空时报错() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("", "", "http://s.example.com", "http://p.example.com"));
        assertThrows(BizException.class, () -> service.buildParam(baseDto()));
    }

    @Test
    void 私钥格式错误时包装为业务异常() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "", "http://s.example.com", "http://p.example.com"));
        DocDebugBuildParamDto dto = baseDto().setPrivateKey("bad-key");
        assertThrows(BizException.class, () -> service.buildParam(dto));
    }

    @Test
    void 文档url为绝对地址时直接使用() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("http://other.example.com/api/user.page", "", "http://s.example.com", "http://p.example.com"));
        DocDebugBuildParamVo vo = service.buildParam(baseDto());
        assertEquals("http://other.example.com/api/user.page", vo.getUrl());
    }

    @Test
    void rest风格_form不含method和version_url带rest前缀和版本参数() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "2.0", "http://s.example.com", "http://p.example.com"));

        DocDebugBuildParamVo vo = service.buildParam(baseDto().setPathType("rest"));

        assertEquals("http://s.example.com/rest/user.page?v=2.0", vo.getUrl());
        assertFalse(vo.getForm().containsKey("method"));
        assertFalse(vo.getForm().containsKey("version"));
        // 其余公共参数不受影响
        assertEquals("test-app-key", vo.getForm().get("appKey"));
        assertTrue(vo.getForm().containsKey("timestamp"));
    }

    @Test
    void rest风格签名可通过验签() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "", "http://s.example.com", "http://p.example.com"));

        DocDebugBuildParamVo vo = service.buildParam(baseDto().setPathType("rest").setPrivateKey(privateKey));

        assertTrue(SignUtil.rsaCheckV2(vo.getForm(), publicKey, "UTF-8", "RSA2", new ApiConfig()));
    }

    @Test
    void pathType为空或非法值时按api风格组装() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("user.page", "", "http://s.example.com", "http://p.example.com"));

        DocDebugBuildParamVo nullType = service.buildParam(baseDto().setPathType(null));
        assertEquals("http://s.example.com/api", nullType.getUrl());
        assertEquals("user.page", nullType.getForm().get("method"));

        DocDebugBuildParamVo badType = service.buildParam(baseDto().setPathType("RESTFUL"));
        assertEquals("http://s.example.com/api", badType.getUrl());
        assertEquals("user.page", badType.getForm().get("method"));
    }

    @Test
    void rest风格_文档url为绝对地址时原样使用() {
        Mockito.when(docInfoService.getDocView(1L))
                .thenReturn(mockDocView("http://other.example.com/rest/user.page", "", "http://s.example.com", "http://p.example.com"));

        DocDebugBuildParamVo vo = service.buildParam(baseDto().setPathType("rest"));

        // 绝对地址视为完整自描述地址：不拼前缀、不加 ?v=
        assertEquals("http://other.example.com/rest/user.page", vo.getUrl());
    }
}
