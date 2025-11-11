package com.gitee.sop.support.message;


import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 负责构建错误消息
 *
 * @author 六如
 */
public class OpenMessageFactory {

    public static final String SYS_ERR = "系统错误";
    private static final String SOLUTION = ".solution";
    private static final String I18N_FOLDER = "i18n";
    private static final String I18N_CLASSPATH = "classpath*:" + I18N_FOLDER + "/**/*.properties";
    private static final Set<String> NO_MODULE_CACHE = new HashSet<>();
    static Logger logger = Logger.getLogger(OpenMessageFactory.class.getName());
    /**
     * 错误信息的国际化信息
     */
    private static MessageSourceAccessor errorMessageSourceAccessor;


    private OpenMessageFactory() {
    }

    public static void initMessage() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = resolver.getResources(I18N_CLASSPATH);
            Set<String> isvModuleList = new LinkedHashSet<>();
            for (Resource resource : resources) {
                String path = resource.getURL().getPath();
                if (path == null) {
                    continue;
                }
                int j = path.lastIndexOf(I18N_FOLDER);
                // i18n/open/error_zh_CN.properties
                String shortPath = path.substring(j);
                int i = shortPath.indexOf("_");
                if (i < 0) {
                    continue;
                }
                String module = shortPath.substring(0, i);
                // i18n/open/code,i18n/open/error
                isvModuleList.add(module);
            }
            OpenMessageFactory.initMessageSource(new ArrayList<>(isvModuleList));
        } catch (IOException e) {
            logger.warning("初始化i18n模块错误:" + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * 设置国际化资源信息
     */
    public static void initMessageSource(List<String> isvModules) {
        HashSet<String> baseNamesSet = new HashSet<>();

        if (isvModules != null) {
            baseNamesSet.addAll(isvModules);
        }

        String[] totalBaseNames = baseNamesSet.toArray(new String[0]);

        logger.info("加载错误码国际化资源：" + StringUtils.arrayToCommaDelimitedString(totalBaseNames));
        ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
        bundleMessageSource.setBasenames(totalBaseNames);
        MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(bundleMessageSource);
        setErrorMessageSourceAccessor(messageSourceAccessor);
    }

    /**
     * 通过ErrorMeta，Locale，params构建国际化错误消息
     *
     * @param openError 错误信息
     * @param locale    本地化
     * @param params    参数
     * @return 如果没有配置国际化消息，则直接返回errorMeta中的信息
     */
    public static OpenMessage getMessage(I18nMessage openError, Locale locale, Object... params) {
        if (locale == null) {
            locale = Locale.SIMPLIFIED_CHINESE;
        }
        String subCode = openError.getConfigKey();
        // isp.unknow-error=Service is temporarily unavailable
        String subMsg = getErrorMessage(subCode, locale, params);
        if (ObjectUtils.isEmpty(subMsg)) {
            subMsg = SYS_ERR;
        }
        // isp.unknow-error.solution=Service is temporarily unavailable
        String solution = getErrorMessage(subCode + SOLUTION, locale, params);
        if (ObjectUtils.isEmpty(solution)) {
            solution = "";
        }
        return new DefaultOpenMessage(subCode, subMsg, solution);
    }


    private static void setErrorMessageSourceAccessor(MessageSourceAccessor errorMessageSourceAccessor) {
        OpenMessageFactory.errorMessageSourceAccessor = errorMessageSourceAccessor;
    }

    /**
     * 返回本地化信息
     *
     * @param module 错误模块
     * @param locale 本地化
     * @param params 参数
     * @return 返回信息
     */
    private static String getErrorMessage(String module, Locale locale, Object... params) {
        if (NO_MODULE_CACHE.contains(module)) {
            return null;
        }
        try {
            return errorMessageSourceAccessor.getMessage(module, params, locale);
        } catch (Exception e) {
            NO_MODULE_CACHE.add(module);
            return null;
        }
    }

}
