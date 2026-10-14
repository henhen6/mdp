package top.mddata.sdk.core.aes;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import top.mddata.sdk.core.sign.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * 消息推送加解密主类。
 * 参考微信 WXBizMsgCrypt，支持明文模式、兼容模式、安全模式。
 *
 * <p>加密流程：
 * <ol>
 *   <li>构造明文：random(16B) + msgLen(4B网络字节序) + msg + appKey</li>
 *   <li>AES-CBC 加密，PKCS7 填充（块大小 32 字节）</li>
 *   <li>Base64 编码得到密文</li>
 *   <li>计算签名：SHA1(sort(token, timestamp, nonce, encrypt))</li>
 * </ol>
 *
 * <p>AESKey = Base64Decode(encodingAesKey + "=")，32 字节；IV = AESKey 的前 16 字节。
 *
 * @author henhen6
 * @since 2026-07-01
 */
public final class MdpBizMsgCrypt {

    /** 明文模式 */
    public static final int MODE_PLAINTEXT = 0;
    /** 兼容模式（明文+密文共存） */
    public static final int MODE_COMPATIBLE = 1;
    /** 安全模式（纯密文） */
    public static final int MODE_ENCRYPTED = 2;

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final byte[] aesKey;
    private final String token;
    private final String appKey;

    /**
     * 构造函数。
     *
     * @param token          签名令牌
     * @param encodingAesKey 43 字符的 EncodingAESKey
     * @param appKey         应用标识
     * @throws AesException encodingAesKey 非法时抛出
     */
    public MdpBizMsgCrypt(String token, String encodingAesKey, String appKey) throws AesException {
        if (encodingAesKey == null || encodingAesKey.length() != 43) {
            throw new AesException(AesException.ILLEGAL_AES_KEY);
        }
        this.token = token;
        this.appKey = appKey;
        this.aesKey = decodeAesKey(encodingAesKey);
    }

    /**
     * 加密消息并构建请求体。
     * 签名随返回值返回（不再写实例字段，实例可安全共享给多线程使用）。
     *
     * @param plaintext      明文 JSON 字符串
     * @param timestamp      时间戳
     * @param nonce          随机字符串
     * @param encryptionMode 加密模式（{@link #MODE_PLAINTEXT} / {@link #MODE_COMPATIBLE} / {@link #MODE_ENCRYPTED}）
     * @return 报文体与签名
     * @throws AesException 加密失败时抛出
     */
    public EncryptResult encryptMsg(String plaintext, String timestamp, String nonce, int encryptionMode)
            throws AesException {
        String encrypt;
        if (encryptionMode == MODE_PLAINTEXT) {
            encrypt = "";
        } else {
            encrypt = encrypt(plaintext);
        }

        // 计算签名
        String signature = SHA1.getSHA1(token, timestamp, nonce, "");
        String msgSignature = encryptionMode == MODE_PLAINTEXT
                ? null
                : SHA1.getSHA1(token, timestamp, nonce, encrypt);

        // 构建请求体
        JSONObject body = new JSONObject();
        if (encryptionMode == MODE_PLAINTEXT) {
            // 明文模式：直接发送明文
            body = JSON.parseObject(plaintext);
        } else if (encryptionMode == MODE_COMPATIBLE) {
            // 兼容模式：明文展开 + 密文 + appKey
            JSONObject plainData = JSON.parseObject(plaintext);
            body.putAll(plainData);
            body.put("encrypt", encrypt);
            body.put("appKey", appKey);
        } else {
            // 安全模式：仅密文 + appKey
            body.put("encrypt", encrypt);
            body.put("appKey", appKey);
        }
        return new EncryptResult(body, signature, msgSignature);
    }

    /**
     * 解密消息。从 JSON 请求体中提取密文，验证签名后解密得到明文。
     *
     * @param encrypt      加密提
     * @param timestamp    时间戳（URL 参数）
     * @param nonce        随机字符串（URL 参数）
     * @param msgSignature 消息签名（URL 参数，密文模式需传入）
     * @return 解密后的明文 JSON 字符串
     * @throws AesException 验签失败或解密失败时抛出
     */
    public String decryptMsg(String encrypt, String timestamp, String nonce, String msgSignature)
            throws AesException {
        if (StringUtils.isEmpty(encrypt)) {
            throw new AesException(AesException.PARSE_JSON_ERROR);
        }

        // 验证签名
        String calculated = SHA1.getSHA1(token, timestamp, nonce, encrypt);
        if (!calculated.equals(msgSignature)) {
            throw new AesException(AesException.VALIDATE_SIGNATURE_ERROR);
        }

        return decrypt(encrypt);
    }

    /**
     * 验证 URL（仅验签不解密）。
     *
     * @param msgSignature 签名串
     * @param timestamp    时间戳
     * @param nonce        随机字符串
     * @param echoStr      待验证的字符串
     * @return 解密后的 echoStr
     * @throws AesException 验签失败时抛出
     */
    public String verifyUrl(String msgSignature, String timestamp, String nonce, String echoStr)
            throws AesException {
        String calculated = SHA1.getSHA1(token, timestamp, nonce, echoStr);
        if (!calculated.equals(msgSignature)) {
            throw new AesException(AesException.VALIDATE_SIGNATURE_ERROR);
        }
        return decrypt(echoStr);
    }

    // ============ 内部加解密 ============

    /**
     * AES-CBC 加密（PKCS7 填充）。
     * 明文格式：random(16B) + msgLen(4B) + msg + appKey
     */
    private String encrypt(String text) throws AesException {
        try {
            ByteGroup byteCollector = new ByteGroup();
            byte[] randomStrBytes = getRandomStr().getBytes(CHARSET);
            byte[] textBytes = text.getBytes(CHARSET);
            byte[] networkBytesOrder = getNetworkBytesOrder(textBytes.length);
            byte[] appKeyBytes = appKey.getBytes(CHARSET);

            byteCollector.addBytes(randomStrBytes);
            byteCollector.addBytes(networkBytesOrder);
            byteCollector.addBytes(textBytes);
            byteCollector.addBytes(appKeyBytes);

            byte[] padBytes = PKCS7Encoder.encode(byteCollector.size());
            byteCollector.addBytes(padBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(aesKey, 0, 16);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(byteCollector.toBytes());

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new AesException(AesException.ENCRYPT_AES_ERROR);
        }
    }

    /**
     * AES-CBC 解密（PKCS7 去填充）。
     */
    private String decrypt(String text) throws AesException {
        byte[] original;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = Base64.getDecoder().decode(text);
            original = cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new AesException(AesException.DECRYPT_AES_ERROR);
        }

        try {
            byte[] bytes = PKCS7Encoder.decode(original);
            byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);
            int msgLength = recoverNetworkBytesOrder(networkOrder);
            String msg = new String(Arrays.copyOfRange(bytes, 20, 20 + msgLength), CHARSET);
            String fromAppKey = new String(Arrays.copyOfRange(bytes, 20 + msgLength, bytes.length), CHARSET);
            if (!appKey.equals(fromAppKey)) {
                throw new AesException(AesException.VALIDATE_APPID_ERROR);
            }
            return msg;
        } catch (AesException e) {
            throw e;
        } catch (Exception e) {
            throw new AesException(AesException.ILLEGAL_BUFFER);
        }
    }

    // ============ 静态工具方法 ============

    /**
     * 计算消息签名。
     * 将 token、timestamp、nonce、encrypt 四个参数字典序排序后拼接，再做 SHA-1。
     *
     * @param token     签名令牌
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param encrypt   密文（明文模式下传空字符串）
     * @return 40 字符十六进制 SHA-1 签名
     */
    public static String calcSignature(String token, String timestamp, String nonce, String encrypt) {
        try {
            return SHA1.getSHA1(token, timestamp, nonce, encrypt);
        } catch (AesException e) {
            throw new RuntimeException("签名计算失败", e);
        }
    }

    /**
     * 验证消息签名（静态方法，需要传入 token）。
     */
    public static boolean verifySignature(String token, String timestamp, String nonce,
                                          String encrypt, String msgSignature) {
        String calculated = calcSignature(token, timestamp, nonce, encrypt);
        return calculated.equals(msgSignature);
    }

    /**
     * 验证消息签名（实例方法，使用构造时保存的 token）。
     *
     * @param timestamp    时间戳
     * @param nonce        随机字符串
     * @param encrypt      密文（明文模式传空字符串）
     * @param msgSignature 待验证的签名
     * @return 签名是否匹配
     */
    public boolean verifySignature(String timestamp, String nonce, String encrypt, String msgSignature) {
        return verifySignature(this.token, timestamp, nonce, encrypt, msgSignature);
    }

    /** 生成 16 字符随机字符串 */
    public static String generateNonce() {
        return StringUtils.randomString(16);
    }

    /** 获取当前秒级时间戳 */
    public static String generateTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 生成 EncodingAESKey（43 字符）。
     * 算法：32 字节随机 → Base64（44字符） → 去掉末尾 '='（43字符）。
     */
    public static String generateEncodingAesKey() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).replace("=", "");
    }

    /** 生成 32 字符 Token */
    public static String generateToken() {
        return StringUtils.randomString(32);
    }

    /**
     * 拼接 URL 参数。
     *
     * @param baseUrl     基础 URL
     * @param signature   签名（所有模式）
     * @param msgSignature 消息签名（加密模式，明文模式传 null）
     * @param timestamp   时间戳
     * @param nonce       随机字符串
     * @param encryptType 加密类型（"aes" 或 null）
     * @return 拼接后的 URL
     */
    public static String appendUrlParams(String baseUrl, String signature, String msgSignature,
                                         String timestamp, String nonce, String encryptType) {
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append(baseUrl.contains("?") ? "&" : "?");
        if (!StringUtils.isEmpty(signature)) {
            sb.append("signature=").append(signature).append("&");
        }
        if (!StringUtils.isEmpty(msgSignature)) {
            sb.append("msgSignature=").append(msgSignature).append("&");
        }
        sb.append("timestamp=").append(timestamp).append("&");
        sb.append("nonce=").append(nonce);
        if (!StringUtils.isEmpty(encryptType)) {
            sb.append("&encryptType=").append(encryptType);
        }
        return sb.toString();
    }

    // ============ 内部辅助 ============

    /** 生成 4 字节网络字节序 */
    private static byte[] getNetworkBytesOrder(int sourceNumber) {
        byte[] orderBytes = new byte[4];
        orderBytes[3] = (byte) (sourceNumber & 0xFF);
        orderBytes[2] = (byte) (sourceNumber >> 8 & 0xFF);
        orderBytes[1] = (byte) (sourceNumber >> 16 & 0xFF);
        orderBytes[0] = (byte) (sourceNumber >> 24 & 0xFF);
        return orderBytes;
    }

    /** 还原 4 字节网络字节序 */
    private static int recoverNetworkBytesOrder(byte[] orderBytes) {
        int sourceNumber = 0;
        for (int i = 0; i < 4; i++) {
            sourceNumber <<= 8;
            sourceNumber |= orderBytes[i] & 0xff;
        }
        return sourceNumber;
    }

    /** 随机生成 16 位字符串 */
    private static String getRandomStr() {
        return StringUtils.randomString(16);
    }

    /** 将 43 字符 EncodingAESKey 解码为 32 字节 AESKey */
    private static byte[] decodeAesKey(String encodingAesKey) throws AesException {
        try {
            byte[] decoded = Base64.getDecoder().decode(encodingAesKey + "=");
            if (decoded.length != 32) {
                throw new AesException(AesException.ILLEGAL_AES_KEY);
            }
            return decoded;
        } catch (AesException e) {
            throw e;
        } catch (Exception e) {
            throw new AesException(AesException.ILLEGAL_AES_KEY);
        }
    }
}
