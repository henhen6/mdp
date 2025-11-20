package com.gitee.sop.support.doc;

import com.gitee.sop.support.doc.constants.OpenAnnotationConstants;
import com.gitee.sop.support.doc.helper.ParamsBuildHelper;
import com.ly.doc.builder.ProjectDocConfigBuilder;
import com.ly.doc.constants.DocGlobalConstants;
import com.ly.doc.constants.DocTags;
import com.ly.doc.constants.ParamTypeConstants;
import com.ly.doc.model.ApiConfig;
import com.ly.doc.model.ApiParam;
import com.ly.doc.model.RpcJavaMethod;
import com.ly.doc.model.annotation.FrameworkAnnotations;
import com.ly.doc.template.RpcDocBuildTemplate;
import com.ly.doc.utils.DocClassUtil;
import com.ly.doc.utils.DocUtil;
import com.ly.doc.utils.JavaClassUtil;
import com.ly.doc.utils.JavaClassValidateUtil;
import com.ly.doc.utils.JavaFieldUtil;
import com.power.common.util.StringUtil;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.expression.AnnotationValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 解析文档
 * <p>
 * 参考:<a href="https://smart-doc-group.github.io/zh/guide/advanced/expand">smart-doc扩展</a>
 * </p>
 *
 * @author 六如
 */
public class SopDocBuildTemplate extends RpcDocBuildTemplate {

    public static final String DEFAULT_VERSION = "1.0";

    private final ParamsBuildHelper paramsBuildHelper = new ParamsBuildHelper();


    @Override
    public boolean supportsFramework(String s) {
        return "sop".equalsIgnoreCase(s);
    }

    @Override
    public List<ApiParam> requestParams(final JavaMethod javaMethod, ProjectDocConfigBuilder builder,
                                        AtomicInteger atomicInteger, Map<String, JavaType> actualTypesMap) {
        boolean isStrict = builder.getApiConfig().isStrict();
        boolean isShowJavaType = builder.getApiConfig().getShowJavaType();
        boolean isShowValidation = builder.getApiConfig().isShowValidation();
        String className = javaMethod.getDeclaringClass().getCanonicalName();
        Map<String, String> paramTagMap = DocUtil.getCommentsByTag(javaMethod, DocTags.PARAM, className);
        List<JavaParameter> parameterList = javaMethod.getParameters();
        if (parameterList.isEmpty()) {
            return null;
        }
        ClassLoader classLoader = builder.getApiConfig().getClassLoader();
        List<ApiParam> paramList = new ArrayList<>();
        for (JavaParameter parameter : parameterList) {
            boolean required = false;
            String paramName = parameter.getName();
            String typeName = this.replaceTypeName(parameter.getType().getGenericCanonicalName(), actualTypesMap,
                    Boolean.FALSE);
            String simpleName = this.replaceTypeName(parameter.getType().getValue(), actualTypesMap, Boolean.FALSE)
                    .toLowerCase();
            String fullTypeName = this.replaceTypeName(parameter.getType().getFullyQualifiedName(), actualTypesMap,
                    Boolean.FALSE);
            String paramPre = "";
            if (!paramTagMap.containsKey(paramName) && JavaClassValidateUtil.isPrimitive(fullTypeName) && isStrict) {
                throw new RuntimeException("ERROR: Unable to find javadoc @param for actual param \"" + paramName
                                           + "\" in method " + javaMethod.getName() + " from " + className);
            }
            StringBuilder comment = new StringBuilder(this.paramCommentResolve(paramTagMap.get(paramName)));
            String mockValue = JavaFieldUtil.createMockValue(paramTagMap, paramName, typeName, typeName);
            JavaClass javaClass = builder.getJavaProjectBuilder().getClassByName(fullTypeName);
            List<JavaAnnotation> annotations = parameter.getAnnotations();
            for (JavaAnnotation a : annotations) {
                if (JavaClassValidateUtil.isJSR303Required(a.getType().getValue())) {
                    required = true;
                }
            }
            comment.append(JavaFieldUtil.getJsrComment(isShowValidation, classLoader, annotations));
            Set<String> groupClasses = JavaClassUtil.getParamGroupJavaClass(annotations,
                    builder.getJavaProjectBuilder());
            Set<String> paramJsonViewClasses = JavaClassUtil.getParamJsonViewClasses(annotations, builder);
            if (JavaClassValidateUtil.isCollection(fullTypeName) || JavaClassValidateUtil.isArray(fullTypeName)) {
                if (JavaClassValidateUtil.isCollection(typeName)) {
                    typeName = typeName + "<T>";
                }
                String[] gicNameArr = DocClassUtil.getSimpleGicName(typeName);
                String gicName = gicNameArr[0];
                if (JavaClassValidateUtil.isArray(gicName)) {
                    gicName = gicName.substring(0, gicName.indexOf("["));
                }
                if (JavaClassValidateUtil.isPrimitive(gicName)) {
                    String processedType = isShowJavaType ? JavaClassUtil.getClassSimpleName(typeName)
                            : DocClassUtil.processTypeNameForParams(simpleName);
                    ApiParam param = ApiParam.of()
                            .setId(atomicInteger.incrementAndGet())
                            .setField(paramName)
                            .setDesc(comment + "   (children type : " + gicName + ")")
                            .setRequired(required)
                            .setType(processedType);
                    paramList.add(param);
                } else {
                    paramList.addAll(paramsBuildHelper.buildParams(gicNameArr[0], paramPre, 0, "true", Boolean.FALSE,
                            new HashMap<>(16), builder, groupClasses, paramJsonViewClasses, 0, Boolean.FALSE,
                            atomicInteger));
                }
            } else if (JavaClassValidateUtil.isPrimitive(fullTypeName)) {
                ApiParam param = ApiParam.of()
                        .setId(atomicInteger.incrementAndGet())
                        .setField(paramName)
                        .setType(JavaClassUtil.getClassSimpleName(typeName))
                        .setDesc(comment.toString())
                        .setRequired(required)
                        .setMaxLength(JavaFieldUtil.getParamMaxLength(parameter.getAnnotations()))
                        .setValue(mockValue)
                        .setVersion(DocGlobalConstants.DEFAULT_VERSION);
                paramList.add(param);
            } else if (JavaClassValidateUtil.isMap(fullTypeName)) {
                if (JavaClassValidateUtil.isMap(typeName)) {
                    ApiParam apiParam = ApiParam.of()
                            .setId(atomicInteger.incrementAndGet())
                            .setField(paramName)
                            .setType(typeName)
                            .setDesc(comment.toString())
                            .setRequired(required)
                            .setVersion(DocGlobalConstants.DEFAULT_VERSION);
                    paramList.add(apiParam);
                    continue;
                }
                String[] gicNameArr = DocClassUtil.getSimpleGicName(typeName);
                paramList.addAll(paramsBuildHelper.buildParams(gicNameArr[1], paramPre, 0, "true", Boolean.FALSE,
                        new HashMap<>(16), builder, groupClasses, paramJsonViewClasses, 0, Boolean.FALSE,
                        atomicInteger));
            } else if (javaClass.isEnum()) {
                ApiParam param = ApiParam.of()
                        .setId(atomicInteger.incrementAndGet())
                        .setField(paramName)
                        .setType(ParamTypeConstants.PARAM_TYPE_ENUM)
                        .setRequired(required)
                        .setDesc(comment.toString())
                        .setVersion(DocGlobalConstants.DEFAULT_VERSION);
                paramList.add(param);
            } else {
                List<ApiParam> aTrue = paramsBuildHelper.buildParams(typeName, paramPre, 0, "true", Boolean.FALSE, new HashMap<>(16),
                        builder, groupClasses, paramJsonViewClasses, 0, Boolean.FALSE, atomicInteger);

                paramList.addAll(
                        aTrue);
            }
        }
        return paramList;
    }

    @Override
    public RpcJavaMethod convertToJavadocJavaMethod(ApiConfig apiConfig, JavaMethod method, Map<String, JavaType> actualTypesMap) {
        RpcJavaMethod rpcJavaMethod = super.convertToJavadocJavaMethod(apiConfig, method, actualTypesMap);
        Optional<OpenAnnotationInfo> openAnnotationInfoOpt = getOpenAnnotationInfo(method);
        rpcJavaMethod.setVersion(openAnnotationInfoOpt.map(OpenAnnotationInfo::getVersion).orElse(DEFAULT_VERSION));

        String desc = DocUtil.getEscapeAndCleanComment(method.getComment());
        if (StringUtil.isEmpty(desc)) {
            desc = openAnnotationInfoOpt.map(OpenAnnotationInfo::getValue).orElse("");
        }
        rpcJavaMethod.setDesc(desc);
        return rpcJavaMethod;
    }

    // 返回接口名称+版本号
    // 格式:接口名称#版本号
    @Override
    public String methodDefinition(JavaMethod method, Map<String, JavaType> actualTypesMap) {
        return getOpenAnnotationInfo(method)
                .map(this::buildUrl)
                .orElseThrow(() -> new RuntimeException("未指定@Open"));
    }

    protected String buildUrl(OpenAnnotationInfo openAnnotationInfo) {
        return openAnnotationInfo.getValue() + "#" + openAnnotationInfo.getVersion();
    }

    protected String getVersion(JavaMethod method) {
        return getOpenAnnotationInfo(method).map(OpenAnnotationInfo::getVersion)
                .orElse(DEFAULT_VERSION);
    }

    protected Optional<OpenAnnotationInfo> getOpenAnnotationInfo(JavaMethod method) {
        List<JavaAnnotation> annotations = method.getAnnotations();
        for (JavaAnnotation annotation : annotations) {
            String simpleAnnotationName = annotation.getType().getValue();
            if (OpenAnnotationConstants.OPEN_ANNOTATION_NAME.equalsIgnoreCase(simpleAnnotationName)) {
                AnnotationValue value = annotation.getProperty(OpenAnnotationConstants.PROP_VALUE);
                AnnotationValue version = annotation.getProperty(OpenAnnotationConstants.PROP_VERSION);
                OpenAnnotationInfo openAnnotationInfo = new OpenAnnotationInfo();
                openAnnotationInfo.setValue(StringUtil.removeQuotes(value.toString()));
                openAnnotationInfo.setVersion(Optional.ofNullable(version)
                        .map(annotationValue -> StringUtil.removeQuotes(annotationValue.toString()))
                        .orElse(DEFAULT_VERSION));
                return Optional.of(openAnnotationInfo);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean skipMethod(JavaClass cls, JavaMethod method, ApiConfig apiConfig, ProjectDocConfigBuilder projectBuilder) {
        return !this.getOpenAnnotationInfo(method).isPresent();
    }

    @Override
    public boolean isEntryPoint(JavaClass javaClass, FrameworkAnnotations frameworkAnnotations) {
        return javaClass.isInterface();
    }

}
