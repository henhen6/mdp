package top.mddata.base.utils;

import top.mddata.base.exception.BizException;

import java.security.MessageDigest;

/**
 * 常见加密算法工具类
 *
 * @author henhen6
 */
public class UaSecureUtil {

    /**
     * md5加密
     *
     * @param str 指定字符串
     * @return 加密后的字符串
     */
    public static String md5(String str) {
        str = (str == null ? "" : str);
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] btInput = str.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char[] strA = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                strA[k++] = hexDigits[byte0 >>> 4 & 0xf];
                strA[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(strA);
        } catch (Exception e) {
            throw new BizException("生成md5密码失败");
        }
    }

    /**
     * md5加盐加密: md5(md5(str) + md5(salt))
     *
     * @param str  字符串
     * @param salt 盐
     * @return 加密后的字符串
     */
    public static String md5BySalt(String str, String salt) {
        return md5(md5(str) + md5(salt));
    }

}
