package top.mddata.sdk.core.util;

import java.security.MessageDigest;

/**
 * MD5工具类
 * @author 六如
 */
public class MD5Util {

    private static final String MD5 = "MD5";

    /**
     * 生成md5,全部大写
     *
     * @param input
     * @return
     */
    public static String encryptUpper(String input) {
        return encrypt(input).toUpperCase();
    }

    /**
     * 生成md5,全部大写
     *
     * @param input
     * @return
     */
    public static String encryptUpper(byte[] input) {
        return encrypt(input).toUpperCase();
    }

    /**
     * 生成md5,全部小写
     *
     * @param input
     * @return
     */
    public static String encrypt(String input) {
        if(input == null || "".equals(input)) {
            throw new IllegalArgumentException("The argument input can not be empty.");
        }
        return encrypt(input.getBytes());
    }

    /**
     * 返回长度16串,小写
     * @param input
     * @return
     */
    public static String encrypt16(String input) {
        return encrypt(input).substring(8,24);
    }

    /**
     * 生成md5,全部小写
     *
     * @param input 文件数据
     * @return md5
     */
    public static String encrypt(byte[] input) {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("The argument input can not be empty.");
        }
        try {
            // 创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance(MD5);
            // 计算后获得字节数组,这就是那128位了
            byte[] buff = md.digest(input);

            // 把数组每一字节（一个字节占八位）换成16进制连成md5字符串
            return HexUtil.byte2hex(buff);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
