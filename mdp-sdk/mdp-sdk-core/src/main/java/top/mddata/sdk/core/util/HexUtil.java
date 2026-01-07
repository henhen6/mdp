package top.mddata.sdk.core.util;

/**
 * hex工具类
 * @author 六如
 */
public class HexUtil {
    private static final String ZERO = "0";
    private static final String CHARS = "0123456789ABCDEF";

    /**
     * 二进制转十六进制字符串
     *
     * @param bytes 二进制
     * @return 十六进制字符串
     */
    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append(ZERO);
            }
            sign.append(hex);
        }
        return sign.toString();
    }

    /**
     * 十六进制字符串转二进制
     *
     * @param hexString the hex string
     * @return byte[] 字节
     */
    public static byte[] hex2bytes(String hexString) {
        if (hexString == null || "".equalsIgnoreCase(hexString)) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) CHARS.indexOf(c);
    }
}
