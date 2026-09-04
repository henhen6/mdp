package top.mddata.gateway.sop.controller;

import com.gitee.sop.support.message.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import top.mddata.gateway.sop.request.ApiRequest;
import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.gateway.sop.service.ParamExecutor;
import top.mddata.gateway.sop.service.RouteService;
import top.mddata.gateway.sop.util.RequestUtil;

import java.io.IOException;

/**
 * 开放平台入口
 *
 * @author henhen
 */
@Controller
public class IndexController {

    @Resource
    @Qualifier("routeService")
    private RouteService routeService;

    @Resource
    @Qualifier("restRouteService")
    private RouteService restRouteService;

    @Value("${mdp.gateway.rest}")
    private String restPrefix;

    @Resource
    private ParamExecutor<HttpServletRequest, HttpServletResponse> paramExecutor;

    @GetMapping("/")
    public void home(HttpServletResponse response) throws IOException {
        response.getWriter().write("Open Platform");
        // 跳转到网站首页
        // response.sendRedirect("https://www.baidu.com");
    }

    /**
     * 请求入口
     *
     * @apiNote 参数描述
     * <pre>
     * 参数	            类型	    是否必填	    最大长度	    描述	            示例值
     * appKey	        String	是	        32	    平台分配给开发者的应用ID	2014072300007148
     * method	        String	是	        128	    接口名称	alipay.trade.fastpay.refund.query
     * version	        String	是	        3	    调用的接口版本，固定为：1.0	1.0
     * format	        String	否	        40	    仅支持JSON	JSON
     * charset	        String	是	        10	    请求使用的编码格式，如utf-8,gbk,gb2312等	utf-8
     * signType	    String	视接口配置	10	    应用生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2。仅接口要求签名（needSign=true）时必填	RSA2
     * sign	        String	视接口配置	344	    应用请求参数的签名串，详见签名。仅接口要求签名（needSign=true）时必填	详见示例
     * timestamp	    String	是	        19	    发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"	2014-07-24 03:07:50
     * accessToken	    String	否	        512	    访问令牌，通过 accessToken.get 接口获取
     * bizContent	    String	是		请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文档
     * </pre>
     */
    @RequestMapping(value = "${mdp.gateway.path:/api}", method = {RequestMethod.GET, RequestMethod.POST})
    public void index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ApiRequestContext apiRequestContext = paramExecutor.build(request);
        Response apiResponse = routeService.route(apiRequestContext);
        paramExecutor.write(apiRequestContext, apiResponse, response);
    }

    /**
     * restful请求
     * <p>
     *     格式：http://<网关host>:<网关port>/rest/<value>[?v=<版本号>]
     * </p>
     * @param request 请求
     * @param response 响应
     * @param v  版本号
     * @throws IOException
     */
    @RequestMapping(value = "${mdp.gateway.rest}/**")
    public void rest0(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "v", required = false, defaultValue = "1.0") String v
    ) throws IOException {
        ApiRequestContext apiRequestContext = paramExecutor.build(request);
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        String apiName = getApiName(request);
        apiRequest.setMethod(apiName);
        apiRequest.setVersion(v);
        apiRequestContext.setIsRest(true);
        apiRequestContext.setWebContext(RequestUtil.buildWebContext(request));
        Response apiResponse = restRouteService.route(apiRequestContext);
        paramExecutor.write(apiRequestContext, apiResponse, response);
    }

    private String getApiName(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String apiName = requestURI.substring(restPrefix.length());
        return StringUtils.trimLeadingCharacter(apiName, '/');
    }


}
