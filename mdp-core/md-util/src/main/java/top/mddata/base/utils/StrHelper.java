package top.mddata.base.utils;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

/**
 * 字符串帮助类
 *
 * @author henhen6
 * @since 2019-07-16 22:09
 */
@Slf4j
public final class StrHelper {
    private StrHelper() {
    }

    public static String getOrDef(String val, String def) {
        return DefValueHelper.getOrDef(val, def);
    }

    /**
     * 有 任意 一个 Blank
     *
     * @param css CharSequence
     * @return boolean
     */
    public static boolean isAnyBlank(final CharSequence... css) {
        if (ObjectUtil.isEmpty(css)) {
            return true;
        }
        return Stream.of(css).anyMatch(StrUtil::isBlank);
    }

    /**
     * 是否全非 Blank
     *
     * @param css CharSequence
     * @return boolean
     */
    public static boolean isNoneBlank(final CharSequence... css) {
        if (ObjectUtil.isEmpty(css)) {
            return false;
        }
        return Stream.of(css).allMatch(StrUtil::isNotBlank);
    }


    /**
     * 将下划线命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name) {
        // 快速检查
        if (StrUtil.isEmpty(name)) {
            // 没必要转换
            return StrPool.EMPTY;
        } else if (!name.contains(StrPool.UNDERSCORE)) {
            // 不含下划线，仅将首字母大写
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        // 用下划线将原始字符串分割
        String[] camels = name.split(StrPool.UNDERSCORE);
        StringBuilder result = new StringBuilder();
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (StrUtil.isEmpty(camel)) {
                continue;
            }
            // 首字母大写
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * 驼峰转连字符
     * <p>StringUtils.camelToHyphen( "managerAdminUserService" ) = manager-admin-user-service</p>
     *
     * @param input ignore
     * @return 以'-'分隔
     * @see <a href="https://github.com/krasa/StringManipulation">document</a>
     */
    public static String camelToHyphen(String input) {
        return wordsToHyphenCase(wordsAndHyphenAndCamelToConstantCase(input));
    }

    private static String wordsAndHyphenAndCamelToConstantCase(String input) {
        StringBuilder buf = new StringBuilder();
        char previousChar = ' ';
        char[] chars = input.toCharArray();
        for (char c : chars) {
            boolean isUpperCaseAndPreviousIsLowerCase = (Character.isLowerCase(previousChar)) && (Character.isUpperCase(c));

            boolean previousIsWhitespace = Character.isWhitespace(previousChar);
            boolean lastOneIsNotUnderscore = (buf.length() > 0) && (buf.charAt(buf.length() - 1) != CharPool.UNDERLINE);
            boolean isNotUnderscore = c != CharPool.UNDERLINE;
            if (lastOneIsNotUnderscore && (isUpperCaseAndPreviousIsLowerCase || previousIsWhitespace)) {
                buf.append(StrPool.UNDERSCORE);
            } else if ((Character.isDigit(previousChar) && Character.isLetter(c))) {
                buf.append(StrPool.UNDERSCORE);
            }
            if ((shouldReplace(c)) && (lastOneIsNotUnderscore)) {
                buf.append(StrPool.UNDERSCORE);
            } else if (!Character.isWhitespace(c) && (isNotUnderscore || lastOneIsNotUnderscore)) {
                buf.append(Character.toUpperCase(c));
            }
            previousChar = c;
        }
        if (Character.isWhitespace(previousChar)) {
            buf.append(StrPool.UNDERSCORE);
        }
        return buf.toString();
    }

    private static boolean shouldReplace(char c) {
        return (c == CharPool.DOT) || (c == CharPool.UNDERLINE) || (c == CharPool.DASHED);
    }

    private static String wordsToHyphenCase(String s) {
        StringBuilder buf = new StringBuilder();
        char lastChar = 'a';
        for (char c : s.toCharArray()) {
            if ((Character.isWhitespace(lastChar)) && (!Character.isWhitespace(c))
                && (CharPool.DASHED != c) && (buf.length() > 0)
                && (buf.charAt(buf.length() - 1) != CharPool.DASHED)) {
                buf.append(StrPool.DASH);
            }
            if (CharPool.UNDERLINE == c) {
                buf.append(StrPool.DASH);
            } else if (CharPool.DOT == c) {
                buf.append(StrPool.DASH);
            } else if (!Character.isWhitespace(c)) {
                buf.append(Character.toLowerCase(c));
            }
            lastChar = c;
        }
        if (Character.isWhitespace(lastChar)) {
            buf.append(StrPool.DASH);
        }
        return buf.toString();
    }
}
