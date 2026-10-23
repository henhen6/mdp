package top.mddata.sdk.core.sign;


import top.mddata.sdk.core.exception.SopSignException;
import top.mddata.sdk.core.util.Base64Util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 签名工具
 * @author henhen
 */
public class SignUtil {

    public static final String RSA = "RSA";

    /**
     * sha256WithRsa 算法请求类型
     */
    public static final String RSA2 = "RSA2";
    public static final String SHA1_WITH_RSA = "SHA1WithRSA";
    public static final String SHA256_WITH_RSA = "SHA256WithRSA";
    /**
     * GBK字符集
     **/
    public static final String CHARSET_GBK = "GBK";
    /**
     * RSA最大加密明文大小
     */
    public static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    public static final int MAX_DECRYPT_BLOCK = 128;


    /**
     * 获取签名内容
     * @param params 签名参数
     * @return 签名内容
     */
    public static String getSignContent(Map<String, ?> params) {
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        int index = 0;
        for (String key : keys) {
            if (params.get(key) != null) {
                String value = String.valueOf(params.get(key));
                content.append(index == 0 ? "" : "&").append(key).append("=").append(value);
                index++;
            }
        }
        return content.toString();
    }

    /**
     * rsa内容签名
     *
     * @param content 签名内容
     * @param privateKey 私钥
     * @param charset 字符
     * @return 签名值
     * @throws SopSignException 签名异常
     */
    public static String rsaSign(String content, String privateKey, String charset,
                                 String signType) throws SopSignException {

        if (RSA.equals(signType)) {
            return rsaSign(content, privateKey, charset);
        } else if (RSA2.equals(signType)) {
            return rsa256Sign(content, privateKey, charset);
        } else {
            throw new SopSignException("Sign Type is Not Support : signType=" + signType);
        }

    }

    /**
     * sha256WithRsa 加签
     *
     * @param content 签名内容
     * @param privateKey 私钥
     * @param charset 字符
     * @return 签名值
     * @throws SopSignException 签名异常
     */
    public static String rsa256Sign(String content, String privateKey,
                                    String charset) throws SopSignException {

        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8(RSA,
                    new ByteArrayInputStream(privateKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                    .getInstance(SHA256_WITH_RSA);

            signature.initSign(priKey);

            if (StringUtils.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            byte[] signed = signature.sign();

            return new String(Base64Util.encodeBase64(signed));
        } catch (Exception e) {
            throw new SopSignException("RSAcontent = " + content + "; charset = " + charset, e);
        }

    }

    /**
     * sha1WithRsa 加签
     *
     * @param content 签名内容
     * @param privateKey 私钥
     * @param charset 字符
     * @return 签名值
     * @throws SopSignException 签名异常
     */
    public static String rsaSign(String content, String privateKey,
                                 String charset) throws SopSignException {
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8(RSA,
                    new ByteArrayInputStream(privateKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                    .getInstance(SHA1_WITH_RSA);

            signature.initSign(priKey);

            if (StringUtils.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            byte[] signed = signature.sign();

            return new String(Base64Util.encodeBase64(signed));
        } catch (InvalidKeySpecException ie) {
            throw new SopSignException("RSA私钥格式不正确，请检查是否正确配置了PKCS8格式的私钥", ie);
        } catch (Exception e) {
            throw new SopSignException("RSAcontent = " + content + "; charset = " + charset, e);
        }
    }

    /**
     * sha1WithRsa 加签
     *
     * @param params 签名内容
     * @param privateKey 私钥
     * @param charset 字符
     * @return 签名值
     * @throws SopSignException 签名异常
     */
    public static String rsaSign(Map<String, String> params, String privateKey,
                                 String charset) throws SopSignException {
        String signContent = getSignContent(params);

        return rsaSign(signContent, privateKey, charset);

    }

    /**
     * 从PKCS8获取私钥
     * @param algorithm 算法
     * @param ins 私钥流
     * @return 私钥
     * @throws Exception 异常
     */
    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
        if (ins == null || StringUtils.isEmpty(algorithm)) {
            return null;
        }

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        byte[] encodedKey = StreamUtil.readText(ins).getBytes();

        encodedKey = Base64Util.decodeBase64(encodedKey);

        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    /**
     * 获取签名校验内容  v1
     * @param params 参数
     * @return 签名内容
     */
    public static String getSignCheckContentV1(Map<String, String> params) {
        if (params == null) {
            return null;
        }

        // 复制入参再剔除签名字段：不污染调用方的 Map（旧版直接 remove，调用方数据被篡改）
        Map<String, String> signParams = new HashMap<>(params);
        signParams.remove("sign");
        signParams.remove("sign_type");

        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<String>(signParams.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (signParams.get(key) != null) {
                String value = signParams.get(key);
                content.append(i == 0 ? "" : "&").append(key).append("=").append(value);
            }
        }

        return content.toString();
    }

    /**
     * 获取签名校验内容  v2
     * @param params 参数
     * @return 签名内容
     */
    public static String getSignCheckContentV2(Map<String, ?> params) {
        if (params == null) {
            return null;
        }

        Map<String, Object> signParams = new HashMap<>(params);
        signParams.remove("sign");

        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<String>(signParams.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (signParams.get(key) != null) {
                String value = String.valueOf(signParams.get(key));
                content.append(i == 0 ? "" : "&").append(key).append("=").append(value);
            }
        }

        return content.toString();
    }

    public static boolean rsaCheckV1(Map<String, String> params, String publicKey,
                                     String charset) throws SopSignException {
        String sign = params.get("sign");
        if (sign == null) {
            // sign 缺失直接判失败（与 rsaCheckV2 四参版语义对齐，不再深入解码 null）
            return false;
        }
        String content = getSignCheckContentV1(params);

        return rsaCheckContent(content, sign, publicKey, charset);
    }

    public static boolean rsaCheckV1(Map<String, String> params, String publicKey,
                                     String charset, String signType) throws SopSignException {
        String sign = params.get("sign");
        if (sign == null) {
            return false;
        }
        String content = getSignCheckContentV1(params);

        return rsaCheck(content, sign, publicKey, charset, signType);
    }

    /**
     * 验签
     * @param params 参数
     * @param publicKey 公钥
     * @param charset 字符
     * @return 是否成功
     * @throws SopSignException 验签异常
     */
    public static boolean rsaCheckV2(Map<String, String> params, String publicKey,
                                     String charset) throws SopSignException {
        String sign = params.get("sign");
        if (sign == null) {
            return false;
        }
        String content = getSignCheckContentV2(params);

        return rsaCheckContent(content, sign, publicKey, charset);
    }

    /**
     * 验签
     * @param params 参数
     * @param publicKey 公钥
     * @param charset 字符
     * @return 是否成功
     * @throws SopSignException 验签异常
     */
    public static boolean rsaCheckV2(Map<String, ?> params, String publicKey,
                                     String charset, String signType) throws SopSignException {
        Object signObj = params.get("sign");
        if (signObj == null) {
            // sign 缺失直接判失败（旧版 String.valueOf(null) 产生 "null" 字符串进入验签）
            return false;
        }
        String sign = String.valueOf(signObj);
        String content = getSignCheckContentV2(params);

        return rsaCheck(content, sign, publicKey, charset, signType);
    }

    /**
     * 验签
     * @param content 签名内容
     * @param publicKey 公钥
     * @param charset 字符
     * @return 是否成功
     * @throws SopSignException 验签异常
     */
    public static boolean rsaCheck(String content, String sign, String publicKey, String charset,
                                   String signType) throws SopSignException {

        if (RSA.equals(signType)) {

            return rsaCheckContent(content, sign, publicKey, charset);

        } else if (RSA2.equals(signType)) {

            return rsa256CheckContent(content, sign, publicKey, charset);

        } else {

            throw new SopSignException("Sign Type is Not Support : signType=" + signType);
        }

    }

    /**
     * SHA256  验签
     * @param content 参数
     * @param publicKey 公钥
     * @param charset 字符
     * @return 是否成功
     * @throws SopSignException 验签异常
     */
    public static boolean rsa256CheckContent(String content, String sign, String publicKey,
                                             String charset) throws SopSignException {
        try {
            PublicKey pubKey = getPublicKeyFromX509("RSA",
                    new ByteArrayInputStream(publicKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                    .getInstance(SHA256_WITH_RSA);

            signature.initVerify(pubKey);

            if (StringUtils.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            return signature.verify(java.util.Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            throw new SopSignException(
                    "RSAcontent = " + content + ",sign=" + sign + ",charset = " + charset, e);
        }
    }

    /**
     * SHA1 验签
     * @param content 参数
     * @param publicKey 公钥
     * @param charset 字符
     * @return 是否成功
     * @throws SopSignException 验签异常
     */
    public static boolean rsaCheckContent(String content, String sign, String publicKey,
                                          String charset) throws SopSignException {
        try {
            PublicKey pubKey = getPublicKeyFromX509("RSA",
                    new ByteArrayInputStream(publicKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                    .getInstance(SHA1_WITH_RSA);

            signature.initVerify(pubKey);

            if (StringUtils.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            return signature.verify(java.util.Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            throw new SopSignException(
                    "RSAcontent = " + content + ",sign=" + sign + ",charset = " + charset, e);
        }
    }

    /**
     * 获取公钥
     * @param algorithm 算法
     * @param ins 公钥流
     * @return 公钥
     * @throws Exception 异常
     */
    public static PublicKey getPublicKeyFromX509(String algorithm, InputStream ins) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        StringWriter writer = new StringWriter();
        StreamUtil.io(new InputStreamReader(ins), writer);

        byte[] encodedKey = writer.toString().getBytes();

        encodedKey = Base64Util.decodeBase64(encodedKey);

        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }
}
