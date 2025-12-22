package top.mddata.base.utils;

import cn.hutool.crypto.digest.DigestUtil;
import freemarker.cache.MruCacheStorage;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;
import java.util.Map;

/**
 * 模板引擎工具类
 *
 * @author tangyh
 * @version v1.0
 * @date 2022/7/25 12:24 PM
 * @create [2022/7/25 12:24 PM ] [tangyh] [初始创建]
 */
@Slf4j
public class FreeMarkerUtil {
    private static final Configuration FREEMARKER_CFG;
    private static final StringTemplateLoader SL;

    static {
        FREEMARKER_CFG = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        SL = new StringTemplateLoader();
        FREEMARKER_CFG.setBooleanFormat("c");
        FREEMARKER_CFG.setNumberFormat("0.##");
        generateSharedVariable();
        FREEMARKER_CFG.setCacheStorage(new MruCacheStorage(2000, Integer.MAX_VALUE));
        FREEMARKER_CFG.setTemplateUpdateDelayMilliseconds(6000000L);
        TemplateLoader[] loaders = new TemplateLoader[]{SL};
        MultiTemplateLoader mt = new MultiTemplateLoader(loaders);
        FREEMARKER_CFG.setTemplateLoader(mt);
    }

    private static void generateSharedVariable() {
        try {
            BeansWrapper wrapper = new BeansWrapper(Configuration.VERSION_2_3_30);
            TemplateHashModel staticModels = wrapper.getStaticModels();
            TemplateHashModel strPool = (TemplateHashModel) staticModels.get(StrPool.class.getName());
            FREEMARKER_CFG.setSharedVariable("StrPool", strPool);
            TemplateHashModel dateUtils = (TemplateHashModel) staticModels.get(DateUtils.class.getName());
            FREEMARKER_CFG.setSharedVariable("DateUtils", dateUtils);
            TemplateHashModel argumentAssert = (TemplateHashModel) staticModels.get(ArgumentAssert.class.getName());
            FREEMARKER_CFG.setSharedVariable("ArgumentAssert", argumentAssert);
            TemplateHashModel beanPlusUtil = (TemplateHashModel) staticModels.get(BeanPlusUtil.class.getName());
            FREEMARKER_CFG.setSharedVariable("BeanPlusUtil", beanPlusUtil);
            TemplateHashModel collHelper = (TemplateHashModel) staticModels.get(CollHelper.class.getName());
            FREEMARKER_CFG.setSharedVariable("CollHelper", collHelper);
            TemplateHashModel springUtils = (TemplateHashModel) staticModels.get(SpringUtils.class.getName());
            FREEMARKER_CFG.setSharedVariable("SpringUtils", springUtils);
            TemplateHashModel strHelper = (TemplateHashModel) staticModels.get(StrHelper.class.getName());
            FREEMARKER_CFG.setSharedVariable("StrHelper", strHelper);
            TemplateHashModel treeUtil = (TemplateHashModel) staticModels.get(MyTreeUtil.class.getName());
            FREEMARKER_CFG.setSharedVariable("MyTreeUtil", treeUtil);
        } catch (TemplateModelException e) {
            log.error(e.getMessage(), e);
        }
    }

    @SneakyThrows
    public static String generateString(String strTemplate, Map<String, Object> parameters) {
        String templateName = DigestUtil.md5Hex(strTemplate);
        if (SL.findTemplateSource(templateName) == null) {
            SL.putTemplate(templateName, strTemplate);
        }

        StringWriter writer = new StringWriter();
        Template template = FREEMARKER_CFG.getTemplate(templateName, StrPool.UTF8);
        template.process(parameters, writer);
        return writer.toString();
    }
}
