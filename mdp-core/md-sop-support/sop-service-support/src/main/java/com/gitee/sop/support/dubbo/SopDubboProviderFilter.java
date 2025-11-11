package com.gitee.sop.support.dubbo;

import com.alibaba.fastjson2.JSON;
import com.gitee.sop.support.constant.SopConstants;
import com.gitee.sop.support.context.DefaultOpenContext;
import com.gitee.sop.support.context.DefaultWebContext;
import com.gitee.sop.support.util.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcContextAttachment;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.RpcServiceContext;
import org.springframework.core.env.Environment;

import java.net.SocketException;

/**
 * @author 六如
 */
@Slf4j
@Activate(group = {CommonConstants.PROVIDER})
public class SopDubboProviderFilter implements Filter {

    private Environment environment;

    private String ip;

    public void setEnvironment(Environment environment) {
        this.environment = environment;
        try {
            this.ip = NetUtil.getIntranetIp();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        DefaultOpenContext openContext = initOpenContext();
        DefaultWebContext webContext = initWebContext();

        RpcServiceContext serviceContext = RpcContext.getServiceContext();
        // 获取客户端IP
        String fromIP = serviceContext.getRemoteHost();
        // 获取客户端应用名称
        String fromApp = serviceContext.getRemoteApplicationName();
        // 获取调用的接口
        String interfaceName = invocation.getInvoker().getInterface().getName();
        // 获取调用的方法
        String methodName = serviceContext.getMethodName();
        // 调用参数值
        String param = JSON.toJSONString(serviceContext.getArguments());
        String currentApp = environment.getProperty(SopConstants.SPRING_APPLICATION_NAME);
        String currentIp = this.ip;
        String traceId = openContext == null ? "" : openContext.getTraceId();
        if (log.isInfoEnabled()) {
            log.info("[sop_trace][dubbo_server][{}] 收到请求, from={}({}) -> current={}({}), methodName={}.{}, param={}",
                    traceId, fromApp, fromIP, currentApp, currentIp, interfaceName, methodName, param);
        }

        long startTime = System.currentTimeMillis();
        try {
            return invoker.invoke(invocation);
        } finally {
            if (openContext != null) {
                openContext.remove();
            }
            if (webContext != null) {
                webContext.remove();
            }
            // 超过500毫秒告警
            long spend = System.currentTimeMillis() - startTime;
            if (log.isWarnEnabled() && spend > 500) {
                log.warn("[sop_trace][dubbo_server][time_warn][{}] Dubbo 耗时告警({}ms), from={}({}) -> current={}({}) , methodName={}.{}, param={}",
                        traceId, spend, fromApp, fromIP, currentApp, currentIp, interfaceName, methodName, param);
            }
        }
    }

    private DefaultOpenContext initOpenContext() {
        // 从 ServerAttachment 中读取的参数是从 Client 中传递过来的
        RpcContextAttachment serverAttachment = RpcContext.getServerAttachment();
        Object objectAttachment = serverAttachment.getObjectAttachment(SopConstants.OPEN_CONTEXT);
        if (objectAttachment instanceof DefaultOpenContext) {
            DefaultOpenContext openContext = (DefaultOpenContext) objectAttachment;
            openContext.initContext();
            return openContext;
        }
        return null;
    }

    private DefaultWebContext initWebContext() {
        RpcContextAttachment serverAttachment = RpcContext.getServerAttachment();
        Object objectAttachment = serverAttachment.getObjectAttachment(SopConstants.WEB_CONTEXT);
        if (objectAttachment instanceof DefaultWebContext) {
            DefaultWebContext webContext = (DefaultWebContext) objectAttachment;
            webContext.initContext();
            return webContext;
        }
        return null;
    }

}
