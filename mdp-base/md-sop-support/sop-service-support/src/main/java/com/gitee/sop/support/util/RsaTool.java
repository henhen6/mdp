package com.gitee.sop.support.util;

import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

/**
 * RSA加解密工具。 公私钥生成工具
 *
 * @author henhen6
 */
public class RsaTool {
    private static final String RSA_ALGORITHM = "RSA";

    private final KeyFormat keyFormat;
    private final KeyLength keyLength;

    public RsaTool(KeyFormat keyFormat, KeyLength keyLength) {
        this.keyFormat = keyFormat;
        this.keyLength = keyLength;
    }

    /**
     * 将PKCS8编码的私钥转换为PKCS1编码
     * @param privateKeyData PKCS8编码的私钥
     * @return PKCS1编码的私钥
     * @throws Exception 异常
     */
    public static String convertPkcs8ToPkcs1(byte[] privateKeyData) throws Exception {
        return Base64.encodeBase64String(extractPrivateKeyFromPkcs8(privateKeyData));
    }

    /**
     * 从 PKCS8 DER 结构中取出内嵌的 PKCS1 私钥。
     * PKCS8 结构：SEQUENCE { INTEGER(版本), SEQUENCE(算法标识), OCTET STRING(PKCS1私钥) }，
     * 最内层 OCTET STRING 的内容即 PKCS1 编码，无需完整 ASN.1 库即可提取。
     */
    private static byte[] extractPrivateKeyFromPkcs8(byte[] pkcs8) {
        int[] offset = {0};
        // 外层 SEQUENCE，只消费头部，offset 停在内容开始处
        readHeader(pkcs8, offset, 0x30);
        // 版本 INTEGER，跳过
        readElement(pkcs8, offset, 0x02);
        // 算法标识 SEQUENCE，跳过
        readElement(pkcs8, offset, 0x30);
        // OCTET STRING，其内容为 PKCS1 私钥
        return readElement(pkcs8, offset, 0x04);
    }

    /**
     * 读取 DER 元素的 tag 与长度（头部），offset 推进到内容开始处，返回内容长度。
     */
    private static int readHeader(byte[] der, int[] offset, int expectedTag) {
        if (offset[0] >= der.length || (der[offset[0]] & 0xFF) != expectedTag) {
            throw new IllegalArgumentException(
                    "私钥不是合法的 PKCS8 DER 结构：偏移 " + offset[0] + " 处期望 tag 0x"
                            + Integer.toHexString(expectedTag));
        }
        offset[0]++;
        return readLength(der, offset);
    }

    /**
     * 读取一个 DER 元素，校验 tag 后返回其内容，offset 推进到下一元素。
     */
    private static byte[] readElement(byte[] der, int[] offset, int expectedTag) {
        int length = readHeader(der, offset, expectedTag);
        if (offset[0] + length > der.length) {
            throw new IllegalArgumentException("私钥不是合法的 PKCS8 DER 结构：元素长度超出数据范围");
        }
        byte[] content = new byte[length];
        System.arraycopy(der, offset[0], content, 0, length);
        offset[0] += length;
        return content;
    }

    /**
     * 读取 DER 长度字段，支持短形式（<0x80）与长形式。
     */
    private static int readLength(byte[] der, int[] offset) {
        if (offset[0] >= der.length) {
            throw new IllegalArgumentException("私钥不是合法的 PKCS8 DER 结构：长度字段缺失");
        }
        int first = der[offset[0]++] & 0xFF;
        if (first < 0x80) {
            return first;
        }
        int numBytes = first & 0x7F;
        if (numBytes == 0 || numBytes > 4 || offset[0] + numBytes > der.length) {
            throw new IllegalArgumentException("私钥不是合法的 PKCS8 DER 结构：长度字段非法");
        }
        int length = 0;
        for (int i = 0; i < numBytes; i++) {
            length = (length << 8) | (der[offset[0]++] & 0xFF);
        }
        return length;
    }

    /**
     * ASCII码转BCD码
     */
    public static byte[] asciiToBcd(byte[] ascii, int asc_len) {
        byte[] bcd = new byte[asc_len / 2];
        int j = 0;
        for (int i = 0; i < (asc_len + 1) / 2; i++) {
            bcd[i] = ascToBcd(ascii[j++]);
            bcd[i] = (byte) (((j >= asc_len) ? 0x00 : ascToBcd(ascii[j++]) & 0xff) + (bcd[i] << 4));
        }
        return bcd;
    }

    /**
     * ASC转换为BCD
     * @param asc asc
     * @return bcd
     */
    public static byte ascToBcd(byte asc) {
        byte bcd;
        if ((asc >= '0') && (asc <= '9')) {
            bcd = (byte) (asc - '0');
        } else if ((asc >= 'A') && (asc <= 'F')) {
            bcd = (byte) (asc - 'A' + 10);
        } else if ((asc >= 'a') && (asc <= 'f')) {
            bcd = (byte) (asc - 'a' + 10);
        } else {
            bcd = (byte) (asc - 48);
        }
        return bcd;
    }

    /**
     * 创建公钥私钥
     *
     * @return 返回公私钥对
     * @throws Exception 异常
     */
    public KeyStore createKeys() throws Exception {
        KeyPairGenerator keyPairGeno = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGeno.initialize(keyLength.getLength());
        KeyPair keyPair = keyPairGeno.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        KeyStore keyStore = new KeyStore();
        if (this.keyFormat == KeyFormat.PKCS1) {
            keyStore.setPublicKey(Base64.encodeBase64String(publicKey.getEncoded()));
            keyStore.setPrivateKey(convertPkcs8ToPkcs1(privateKey.getEncoded()));
        } else {
            keyStore.setPublicKey(Base64.encodeBase64String(publicKey.getEncoded()));
            keyStore.setPrivateKey(Base64.encodeBase64String(privateKey.getEncoded()));
        }
        return keyStore;
    }

    /**
     * 获取公钥对象
     *
     * @param pubKeyData 公钥
     * @return 返回公钥对象
     * @throws Exception 异常
     */
    public RSAPublicKey getPublicKey(byte[] pubKeyData) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyData);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * 获取公钥对象
     *
     * @param pubKey 公钥
     * @return 返回私钥对象
     * @throws Exception 异常
     */
    public RSAPublicKey getPublicKey(String pubKey) throws Exception {
        return getPublicKey(Base64.decodeBase64(pubKey));

    }

    /**
     * 获取私钥对象
     *
     * @param priKey 私钥
     * @return 私钥对象
     * @throws Exception 异常
     */
    public RSAPrivateKey getPrivateKey(String priKey) throws Exception {
        return getPrivateKey(Base64.decodeBase64(priKey));
    }

    /**
     * 通过私钥byte[]将公钥还原，适用于RSA算法
     *
     * @param keyBytes key字节数据
     * @return 返回私钥
     * @throws Exception 异常
     */
    public RSAPrivateKey getPrivateKey(byte[] keyBytes) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

    }


    /**
     * 拆分数组
     */
    public byte[][] splitArray(byte[] data, int len) {
        int x = data.length / len;
        int y = data.length % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        byte[][] arrays = new byte[x + z][];
        byte[] arr;
        for (int i = 0; i < x + z; i++) {
            arr = new byte[len];
            if (i == x + z - 1 && y != 0) {
                System.arraycopy(data, i * len, arr, 0, y);
            } else {
                System.arraycopy(data, i * len, arr, 0, len);
            }
            arrays[i] = arr;
        }
        return arrays;
    }

    public enum KeyLength {
        /**
         * 秘钥长度：1024
         */
        LENGTH_1024(1024),
        /**
         * 秘钥长度：2048
         */
        LENGTH_2048(2048);
        private final int length;

        KeyLength(int length) {
            this.length = length;
        }

        public int getLength() {
            return length;
        }
    }

    public enum KeyFormat {
        /**
         * java适用
         */
        PKCS8(1, "RSA", "PKCS8(Java适用)"),
        /**
         * 非java适用
         */
        PKCS1(2, "RSA/ECB/PKCS1Padding", "PKCS1(非Java适用)");

        private final Integer code;
        private final String cipherAlgorithm;
        private final String desc;

        KeyFormat(Integer code, String cipherAlgorithm, String desc) {
            this.code = code;
            this.cipherAlgorithm = cipherAlgorithm;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getCipherAlgorithm() {
            return cipherAlgorithm;
        }

        public String getDesc() {
            return desc;
        }

        public static KeyFormat of(Integer value) {
            for (KeyFormat keyFormat : KeyFormat.values()) {
                if (Objects.equals(value, keyFormat.code)) {
                    return keyFormat;
                }
            }
            return PKCS8;
        }
    }

    public static class KeyStore {
        private String publicKey;
        private String privateKey;

        public String getPublicKey() {
            return publicKey;
        }

        public KeyStore setPublicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public KeyStore setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        @Override
        public String toString() {
            return "KeyStore{publicKey='" + publicKey + "', privateKey='" + privateKey + "'}";
        }
    }


}
