package top.mddata.api.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.aes.AesException;
import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import com.gitee.sop.support.message.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.base.util.StrPool;
import top.mddata.common.constant.EventTypeCode;
import top.mddata.open.enumeration.admin.EventTypeEnum;
import top.mddata.open.enumeration.admin.NotifyEncryptionTypeEnum;
import top.mddata.open.facade.admin.AppKeysFacade;
import top.mddata.open.vo.admin.AppKeysVo;

/**
 * 回调接收控制器（开发者参考实现）。
 * 支持明文模式、兼容模式、安全模式三种加密类型的消息接收和解密。
 *
 * <p>接收流程：
 * <ol>
 *   <li>从URL参数获取加密类型、签名、时间戳、随机数</li>
 *   <li>根据加密类型选择验签和解密方式</li>
 *   <li>根据type字段区分事件推送/回调任务</li>
 *   <li>根据method字段分发到具体业务处理器</li>
 * </ol>
 *
 * @author henhen
 * @since 2025/12/18 10:04
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class NotifyCallbackController {
    private final AppKeysFacade appKeysFacade;

    /**
     * 回调接收接口。
     * 支持明文模式、兼容模式、安全模式。
     *
     * @param request HTTP请求（包含URL参数：signature/msg_signature/encrypt_type/timestamp/nonce）
     * @param content 请求体
     * @return 响应结果，code=0表示成功
     */
    @PostMapping("/anyUser/notify/callback")
    public ApiResponse callback(HttpServletRequest request, @RequestBody String content) {
        log.info("收到回调通知, content={}", content);

        // 从URL参数获取签名相关信息（小写驼峰命名）
        // 加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数
        String signature = request.getParameter("signature");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        // 消息签名，MDP服务器会验证签名
        String msgSignature = request.getParameter("msgSignature");
        // 加密类型
        String encryptType = request.getParameter("encryptType");

        JSONObject body = JSON.parseObject(content);
        String appKey = extractAppKey(body, encryptType);
        if (StrUtil.isBlank(appKey)) {
            return ApiResponse.error("无法识别应用标识");
        }

        // 通过 appKey 查询应用的加密配置
        R<AppKeysVo> result = appKeysFacade.getByAppKey(appKey);
        AppKeysVo appKeys = result != null ? result.getData() : null;
        if (appKeys == null) {
            return ApiResponse.error("应用不存在：" + appKey);
        }

        // 第三方开发者应该将 token/encodingAesKey/encryptionMode 储存在自己项目的 配置文件或nacos中妥善保管，防止泄露
        String token = appKeys.getNotifyToken();
        String encodingAesKey = appKeys.getNotifyEncodingAesKey();
        // 加密模式可以根据你在MDP后台配置的固定值，只实现一种加密模式。
        Integer encryptionMode = appKeys.getNotifyEncryptionType();
        if (encryptionMode == null) {
            encryptionMode = NotifyEncryptionTypeEnum.PLAINTEXT.getCode();
        }

        // 构建加解密实例（appKey 与加密端保持一致）
        MdpBizMsgCrypt crypt;
        try {
            crypt = new MdpBizMsgCrypt(token, encodingAesKey, appKey);
        } catch (AesException e) {
            return ApiResponse.error("加密配置错误：" + e.getMessage());
        }

        // 根据加密模式解析明文
        String plaintext;
        try {
            // 验签
            verifySignature(body, encryptType, signature, crypt, timestamp, nonce, encryptionMode);
            // 解密内容
            plaintext = decryptBody(body, encryptType, encryptionMode, crypt, timestamp, nonce, msgSignature);
        } catch (Exception e) {
            log.error("消息解密或验签失败", e);
            return ApiResponse.error("消息验证失败：" + e.getMessage());
        }

        // 解析业务数据
        JSONObject bizData = JSON.parseObject(plaintext);
        String type = bizData.getString("type");
        String method = bizData.getString("method");
        log.info("消息类型={}, 方法={}, 明文={}", type, method, plaintext);

        // 根据类型分发处理
        try {
            if (EventTypeEnum.EVENT_PUSH.name().equals(type)) {
                handleEventPush(bizData, method);
            } else if (EventTypeEnum.CALLBACK.name().equals(type)) {
                handleCallback(bizData, method);
            } else {
                log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("业务处理异常, type={}, method={}", type, method, e);
            return ApiResponse.error("业务处理失败：" + e.getMessage());
        }

        // 返回 code=0 表示成功
        return ApiResponse.success("");
    }

    /**
     * 根据加密模式验证签名
     */
    private void verifySignature(JSONObject body, String encryptType, String signature,
                                 MdpBizMsgCrypt crypt, String timestamp, String nonce,
                                 Integer encryptionMode) {
        if (NotifyEncryptionTypeEnum.PLAINTEXT.getCode().equals(encryptionMode) && StrUtil.isBlank(encryptType)) {
            // 明文模式：验证 signature = SHA1(sort(token, timestamp, nonce, ""))
            boolean verified = crypt.verifySignature(timestamp, nonce, StrPool.EMPTY, signature);
            if (!verified) {
                throw new RuntimeException("明文模式签名验证失败");
            }
            return;
        }

        if ("aes".equals(encryptType)) {
            String encrypt = body.getString("encrypt");
            if (StrUtil.isBlank(encrypt)) {
                throw new RuntimeException("密文为空");
            }
            // 密文模式：验证 msgSignature = SHA1(sort(token, timestamp, nonce, encrypt))
            // 注：密文模式的完整验证在 decryptMsg 中自动完成（验签+解密）
            return;
        }

        throw new RuntimeException("不支持的加密类型: " + encryptType);
    }

    /**
     * 根据加密模式解密消息体。
     * 密文模式由 MdpBizMsgCrypt.decryptMsg 完成验签+解密；明文模式直接返回 body。
     */
    private String decryptBody(JSONObject body, String encryptType, Integer encryptionMode,
                               MdpBizMsgCrypt crypt, String timestamp,
                               String nonce, String msgSignature) {
        if (NotifyEncryptionTypeEnum.PLAINTEXT.getCode().equals(encryptionMode)
            && StrUtil.isBlank(encryptType)) {
            return body.toJSONString();
        }

        if ("aes".equals(encryptType)) {
            try {
                String encrypt = body.getString("encrypt");
                return crypt.decryptMsg(encrypt, timestamp, nonce, msgSignature);
            } catch (AesException e) {
                throw new RuntimeException("消息解密失败：" + e.getMessage(), e);
            }
        }

        throw new RuntimeException("不支持的加密类型: " + encryptType);
    }

    /**
     * 从请求体中提取 appKey（所有模式下字段名均为 appKey）。
     */
    private String extractAppKey(JSONObject body, String encryptType) {
        return body.getString("appKey");
    }

    /**
     * 处理事件推送
     */
    private void handleEventPush(JSONObject bizData, String method) {
        String bizContent = bizData.getString("bizContent");
        Long eventTriggerId = bizData.getLong("eventTriggerId");
        log.info("[事件推送] method={}, eventTriggerId={}, bizContent={}", method, eventTriggerId, bizContent);

        switch (method) {
            // 事件编码与发布侧 EventTypeCode 保持一致（冒号格式）
            case EventTypeCode.Console.ORG_EDIT:
                log.info("处理组织编辑事件: {}", bizContent);
                break;
            case EventTypeCode.Console.USER_ADD:
                log.info("处理用户新增事件: {}", bizContent);
                break;
            default:
                log.info("处理未定义的事件: method={}, content={}", method, bizContent);
        }
    }

    /**
     * 处理回调任务
     */
    private void handleCallback(JSONObject bizData, String method) {
        String bizContent = bizData.getString("bizContent");
        Long callLogId = bizData.getLong("callLogId");
        String version = bizData.getString("version");
        log.info("[回调任务] method={}, version={}, callLogId={}, bizContent={}", method, version, callLogId, bizContent);

        switch (method) {
            case "shop.order.create":
                log.info("处理订单创建回调: {}", bizContent);
                break;
            case "shop.order.close":
                log.info("处理订单关闭回调: {}", bizContent);
                break;
            case "org.save":
                log.info("处理组织保存回调: {}", bizContent);
                break;
            default:
                log.info("处理未定义的回调: method={}, content={}", method, bizContent);
        }
    }
}
