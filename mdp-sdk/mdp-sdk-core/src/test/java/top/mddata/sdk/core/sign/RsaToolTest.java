package top.mddata.sdk.core.sign;

import org.junit.jupiter.api.Test;
import top.mddata.sdk.core.util.Base64Util;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RsaTool 单元测试，重点验证 JDK 原生实现的 PKCS8 → PKCS1 转换正确性
 */
class RsaToolTest {

    @Test
    void convertPkcs8ToPkcs1_转换结果包含原私钥modulus() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        RSAPrivateKey privateKey = (RSAPrivateKey) generator.generateKeyPair().getPrivate();

        byte[] pkcs1 = Base64Util.decodeBase64(RsaTool.convertPkcs8ToPkcs1(privateKey.getEncoded()));

        // PKCS1 RSAPrivateKey 是 DER SEQUENCE，首字节必为 0x30
        assertEquals(0x30, pkcs1[0] & 0xFF, "PKCS1 私钥应以 SEQUENCE(0x30) 开头");
        // DER INTEGER 编码与 BigInteger.toByteArray 一致（均带符号位前导零），modulus 必为子序列
        assertTrue(containsSubArray(pkcs1, privateKey.getModulus().toByteArray()),
                "转换结果应包含原私钥的 modulus");
    }

    @Test
    void convertPkcs8ToPkcs1_非法输入时快速失败() {
        assertThrows(IllegalArgumentException.class,
                () -> RsaTool.convertPkcs8ToPkcs1(new byte[]{1, 2, 3}));
    }

    private static boolean containsSubArray(byte[] data, byte[] sub) {
        outer:
        for (int i = 0; i <= data.length - sub.length; i++) {
            for (int j = 0; j < sub.length; j++) {
                if (data[i + j] != sub[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }
}
