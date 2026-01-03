package com.gitee.sop.support.util;


import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.sop.support.dto.ApiConfig;
import com.gitee.sop.support.exception.SignException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.List;
import java.util.Map;

/**
 * 签名工具
 * @author zuihou
 */
public class SignUtil {

    private static final String INVALID = "40002";
    private static final String ISV_INVALID_SIGNATURE = "isv.invalid-signature";
    private static final String ISV_INVALID_SIGNATURE_TYPE = "isv.invalid-signature-type";
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
    private static final String CHARSET_GBK = "GBK";
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;


    /**
     * 获取签名内容
     *
     * @param params 签名参数
     * @return 签名内容
     */
    public static String getSignContent(Map<String, ?> params) {
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        int index = 0;
        for (String key : keys) {
            String value = String.valueOf(params.get(key));
            if (StrUtil.isAllNotEmpty(key, value)) {
                content.append(index == 0 ? "" : "&").append(key).append("=").append(value);
                index++;
            }
        }
        return content.toString();
    }

    /**
     * rsa内容签名
     *
     * @param content   内容
     * @param privateKey   签名秘钥
     * @param charset   字符集
     * @return 返回签名串
     */
    public static String rsaSign(String content, String privateKey, String charset,
                                 String signType) throws SignException {

        if (RSA.equals(signType)) {
            return rsaSign(content, privateKey, charset);
        } else if (RSA2.equals(signType)) {
            return rsa256Sign(content, privateKey, charset);
        } else {
            throw new SignException(INVALID, ISV_INVALID_SIGNATURE_TYPE);
        }

    }

    /**
     * sha256WithRsa 加签
     *
     * @param content    内容
     * @param privateKey 私钥
     * @param charset    字符集
     * @return 返回签名串
     */
    public static String rsa256Sign(String content, String privateKey,
                                    String charset) throws SignException {
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8(RSA, new ByteArrayInputStream(privateKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                    .getInstance(SHA256_WITH_RSA);

            signature.initSign(priKey);

            if (StrUtil.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            byte[] signed = signature.sign();

            return new String(Base64.encodeBase64(signed));
        } catch (Exception e) {
            throw new SignException(INVALID, ISV_INVALID_SIGNATURE, e);
        }

    }

    /**
     * sha1WithRsa 加签
     *
     * @param content   内容
     * @param privateKey  签名秘钥
     * @param charset   字符集
     * @return 返回签名串
     */
    public static String rsaSign(String content, String privateKey,
                                 String charset) throws SignException {
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8(RSA, new ByteArrayInputStream(privateKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                    .getInstance(SHA1_WITH_RSA);

            signature.initSign(priKey);

            if (StrUtil.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            byte[] signed = signature.sign();

            return new String(Base64.encodeBase64(signed));
        } catch (InvalidKeySpecException ie) {
            throw new SignException(INVALID, ISV_INVALID_SIGNATURE_TYPE, ie);
        } catch (Exception e) {
            throw new SignException(INVALID, ISV_INVALID_SIGNATURE, e);
        }
    }

    /**
     * sha1WithRsa 加签
     *
     * @param params 签名内容
     * @param key 私钥
     * @param charset 字符
     * @return 签名值
     * @throws SignException 签名异常
     */
    public static String rsaSign(Map<String, Object> params, String key,
                                 String charset, String signType) throws SignException {
        String signContent = getSignContent(params);

        return rsaSign(signContent, key, charset, signType);

    }

    /**
     * 从PKCS8获取私钥
     * @param algorithm 算法
     * @param ins 私钥流
     * @return 私钥
     * @throws Exception 异常
     */
    public static PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
        if (ins == null || StrUtil.isEmpty(algorithm)) {
            return null;
        }

        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
//        byte[] encodedKey = IoUtil.toStr(ins, StandardCharsets.UTF_8).getBytes();
        byte[] encodedKey = IoUtil.readBytes(ins);
        encodedKey = Base64.decodeBase64(encodedKey);

        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    /**
     * 获取签名校验内容  v1
     * @param params 参数
     * @return 签名内容
     */
    public static String getSignCheckContentV1(Map<String, String> params, ApiConfig apiConfig) {
        if (params == null) {
            return null;
        }

        params.remove(apiConfig.getSignName());
        params.remove(apiConfig.getSignTypeName());

        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (params.get(key) != null) {
                String value = SignConfig.wrapVal(params.get(key));
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
    public static String getSignCheckContentV2(Map<String, ?> params, ApiConfig apiConfig) {
        if (params == null) {
            return null;
        }

        params.remove(apiConfig.getSignName());

        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (params.get(key) != null) {
                String value = SignConfig.wrapVal(params.get(key));
                content.append(i == 0 ? "" : "&").append(key).append("=").append(value);
            }
        }

        return content.toString();
    }

    public static boolean rsaCheckV1(Map<String, String> params, String publicKey,
                                     String charset, ApiConfig apiConfig) throws SignException {
        String sign = params.get(apiConfig.getSignName());
        String content = getSignCheckContentV1(params, apiConfig);
        return rsaCheckContent(content, sign, publicKey, charset);
    }

    public static boolean rsaCheckV1(Map<String, String> params, String publicKey,
                                     String charset, String signType, ApiConfig apiConfig) throws SignException {
        String sign = params.get(apiConfig.getSignName());
        String content = getSignCheckContentV1(params, apiConfig);

        return rsaCheck(content, sign, publicKey, charset, signType);
    }

    /**
     * 验签
     * @param params 参数
     * @param publicKey 公钥
     * @param charset 字符
     * @return 是否成功
     * @throws SignException 验签异常
     */
    public static boolean rsaCheckV2(Map<String, String> params, String publicKey,
                                     String charset, ApiConfig apiConfig) throws SignException {
        String sign = params.get(apiConfig.getSignName());
        String content = getSignCheckContentV2(params, apiConfig);

        return rsaCheckContent(content, sign, publicKey, charset);
    }

    /**
     * 验签
     * @param params 参数
     * @param publicKey 公钥
     * @param charset 字符
     * @return 是否成功
     * @throws SignException 验签异常
     */
    public static boolean rsaCheckV2(Map<String, ?> params, String publicKey,
                                     String charset, String signType, ApiConfig apiConfig) throws SignException {
        String sign = String.valueOf(params.get(apiConfig.getSignName()));
        String content = getSignCheckContentV2(params, apiConfig);

        return rsaCheck(content, sign, publicKey, charset, signType);
    }

    /**
     * 验签
     * @param content 签名内容
     * @param publicKey 公钥
     * @param charset 字符
     * @return 是否成功
     * @throws SignException 验签异常
     */
    public static boolean rsaCheck(String content, String sign, String publicKey, String charset,
                                   String signType) throws SignException {
        if (RSA.equals(signType)) {
            return rsaCheckContent(content, sign, publicKey, charset);
        } else if (RSA2.equals(signType)) {
            return rsa256CheckContent(content, sign, publicKey, charset);
        } else {
            throw new SignException(INVALID, ISV_INVALID_SIGNATURE_TYPE);
        }
    }


    /**
     * 使用公钥验证签名
     *
     * @param content   原始数据
     * @param sign      签名值(Base64编码)
     * @param publicKey 公钥
     * @return 验证是否成功
     * @throws SignException 验证过程中的异常
     */
    public static boolean rsa256CheckContent(String content, String sign, String publicKey,
                                             String charset) throws SignException {
        try {
            PublicKey pubKey = getPublicKeyFromX509(RSA, new ByteArrayInputStream(publicKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                    .getInstance(SHA256_WITH_RSA);

            signature.initVerify(pubKey);

            if (StrUtil.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            return signature.verify(java.util.Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            throw new SignException(INVALID, ISV_INVALID_SIGNATURE, e);
        }
    }

    public static boolean rsaCheckContent(String content, String sign, String publicKey,
                                          String charset) throws SignException {
        try {
            PublicKey pubKey = getPublicKeyFromX509(RSA,
                    new ByteArrayInputStream(publicKey.getBytes()));

            java.security.Signature signature = java.security.Signature
                    .getInstance(SHA1_WITH_RSA);

            signature.initVerify(pubKey);

            if (StrUtil.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            return signature.verify(java.util.Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            throw new SignException(INVALID, ISV_INVALID_SIGNATURE, e);
        }
    }

    /**
     * 获取公钥
     * @param algorithm 算法
     * @param ins 公钥流
     * @return 公钥
     * @throws Exception 异常
     */
    public static PublicKey getPublicKeyFromX509(String algorithm,
                                                 InputStream ins) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);

        StringWriter writer = new StringWriter();
        IoUtil.copy(new InputStreamReader(ins), writer);

        byte[] encodedKey = writer.toString().getBytes();

        encodedKey = Base64.decodeBase64(encodedKey);

        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }

    /**
     * 验签并解密
     *
     * @param params          参数
     * @param alipayPublicKey 平台公钥
     * @param cusPrivateKey   商户私钥
     * @param isCheckSign     是否验签
     * @param isDecrypt       是否解密
     * @return 解密后明文，验签失败则异常抛出
     */
    public static String checkSignAndDecrypt(Map<String, String> params, String alipayPublicKey,
                                             String cusPrivateKey, boolean isCheckSign,
                                             boolean isDecrypt, ApiConfig apiConfig) throws SignException {
        String charset = params.get(apiConfig.getCharsetName());
        String bizContent = params.get(apiConfig.getBizContentName());
        if (isCheckSign) {
            if (!rsaCheckV2(params, alipayPublicKey, charset, apiConfig)) {
                throw new SignException(INVALID, ISV_INVALID_SIGNATURE);
            }
        }

        if (isDecrypt) {
            return rsaDecrypt(bizContent, cusPrivateKey, charset);
        }

        return bizContent;
    }

    /**
     * 验签并解密
     *
     * @param params          参数
     * @param alipayPublicKey 平台公钥
     * @param cusPrivateKey   商户私钥
     * @param isCheckSign     是否验签
     * @param isDecrypt       是否解密
     * @return 解密后明文，验签失败则异常抛出
     */
    public static String checkSignAndDecrypt(Map<String, String> params, String alipayPublicKey,
                                             String cusPrivateKey, boolean isCheckSign,
                                             boolean isDecrypt, String signType, ApiConfig apiConfig) throws SignException {
        String charset = params.get("charset");
        String bizContent = params.get("biz_content");
        if (isCheckSign) {
            if (!rsaCheckV2(params, alipayPublicKey, charset, signType, apiConfig)) {
                throw new SignException(INVALID, ISV_INVALID_SIGNATURE);
            }
        }

        if (isDecrypt) {
            return rsaDecrypt(bizContent, cusPrivateKey, charset);
        }

        return bizContent;
    }

    /**
     * 加密并签名<br>
     * <b>目前适用于公众号</b>
     *
     * @param bizContent      待加密、签名内容
     * @param alipayPublicKey 平台公钥
     * @param cusPrivateKey   商户私钥
     * @param charset         字符集，如UTF-8, GBK, GB2312
     * @param isEncrypt       是否加密，true-加密  false-不加密
     * @param isSign          是否签名，true-签名  false-不签名
     * @return 加密、签名后xml内容字符串
     * <p>
     * 返回示例：
     * <alipay>
     * <response>密文</response>
     * <encryption_type>RSA</encryption_type>
     * <sign>sign</sign>
     * <sign_type>RSA</sign_type>
     * </alipay>
     * </p>
     */
    public static String encryptAndSign(String bizContent, String alipayPublicKey,
                                        String cusPrivateKey, String charset, boolean isEncrypt,
                                        boolean isSign) throws SignException {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isEmpty(charset)) {
            charset = CHARSET_GBK;
        }
        sb.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>");
        if (isEncrypt) {
            // 加密
            sb.append("<alipay>");
            String encrypted = rsaEncrypt(bizContent, alipayPublicKey, charset);
            sb.append("<response>" + encrypted + "</response>");
            sb.append("<encryption_type>RSA</encryption_type>");
            if (isSign) {
                String sign = rsaSign(encrypted, cusPrivateKey, charset);
                sb.append("<sign>" + sign + "</sign>");
                sb.append("<sign_type>RSA</sign_type>");
            }
            sb.append("</alipay>");
        } else if (isSign) {
            // 不加密，但需要签名
            sb.append("<alipay>");
            sb.append("<response>" + bizContent + "</response>");
            String sign = rsaSign(bizContent, cusPrivateKey, charset);
            sb.append("<sign>" + sign + "</sign>");
            sb.append("<sign_type>RSA</sign_type>");
            sb.append("</alipay>");
        } else {
            // 不加密，不加签
            sb.append(bizContent);
        }
        return sb.toString();
    }

    /**
     * 加密并签名<br>
     * <b>目前适用于公众号</b>
     *
     * @param bizContent      待加密、签名内容
     * @param alipayPublicKey 平台公钥
     * @param cusPrivateKey   商户私钥
     * @param charset         字符集，如UTF-8, GBK, GB2312
     * @param isEncrypt       是否加密，true-加密  false-不加密
     * @param isSign          是否签名，true-签名  false-不签名
     * @return 加密、签名后xml内容字符串
     * <p>
     * 返回示例：
     * <alipay>
     * <response>密文</response>
     * <encryption_type>RSA</encryption_type>
     * <sign>sign</sign>
     * <sign_type>RSA</sign_type>
     * </alipay>
     * </p>
     */
    public static String encryptAndSign(String bizContent, String alipayPublicKey,
                                        String cusPrivateKey, String charset, boolean isEncrypt,
                                        boolean isSign, String signType) throws SignException {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isEmpty(charset)) {
            charset = CHARSET_GBK;
        }
        sb.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>");
        if (isEncrypt) {
            // 加密
            sb.append("<alipay>");
            String encrypted = rsaEncrypt(bizContent, alipayPublicKey, charset);
            sb.append("<response>" + encrypted + "</response>");
            sb.append("<encryption_type>RSA</encryption_type>");
            if (isSign) {
                String sign = rsaSign(encrypted, cusPrivateKey, charset, signType);
                sb.append("<sign>" + sign + "</sign>");
                sb.append("<sign_type>");
                sb.append(signType);
                sb.append("</sign_type>");
            }
            sb.append("</alipay>");
        } else if (isSign) {
            // 不加密，但需要签名
            sb.append("<alipay>");
            sb.append("<response>" + bizContent + "</response>");
            String sign = rsaSign(bizContent, cusPrivateKey, charset, signType);
            sb.append("<sign>" + sign + "</sign>");
            sb.append("<sign_type>");
            sb.append(signType);
            sb.append("</sign_type>");
            sb.append("</alipay>");
        } else {
            // 不加密，不加签
            sb.append(bizContent);
        }
        return sb.toString();
    }

    /**
     * 公钥加密
     *
     * @param content   待加密内容
     * @param publicKey 公钥
     * @param charset   字符集，如UTF-8, GBK, GB2312
     * @return 密文内容
     */
    public static String rsaEncrypt(String content, String publicKey,
                                    String charset) throws SignException {
        try {
            PublicKey pubKey = getPublicKeyFromX509(RSA,
                    new ByteArrayInputStream(publicKey.getBytes()));
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] data = StrUtil.isEmpty(charset) ? content.getBytes()
                    : content.getBytes(charset);
            int inputLen = data.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_ENCRYPT_BLOCK;
            }
            byte[] encryptedData = Base64.encodeBase64(out.toByteArray());
            out.close();

            return StrUtil.isEmpty(charset) ? new String(encryptedData)
                    : new String(encryptedData, charset);
        } catch (Exception e) {
            throw new SignException(INVALID, ISV_INVALID_SIGNATURE, e);
        }
    }

    /**
     * 私钥解密
     *
     * @param content   待解密内容
     * @param privateKey 私钥
     * @param charset   字符集，如UTF-8, GBK, GB2312
     * @return 明文内容
     */
    public static String rsaDecrypt(String content, String privateKey,
                                    String charset) throws SignException {
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS8(RSA, new ByteArrayInputStream(privateKey.getBytes()));
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            byte[] encryptedData = StrUtil.isEmpty(charset)
                    ? Base64.decodeBase64(content.getBytes())
                    : Base64.decodeBase64(content.getBytes(charset));
            int inputLen = encryptedData.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * MAX_DECRYPT_BLOCK;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();

            return StrUtil.isEmpty(charset) ? new String(decryptedData)
                    : new String(decryptedData, charset);
        } catch (Exception e) {
            throw new SignException(INVALID, ISV_INVALID_SIGNATURE, e);
        }
    }

}
