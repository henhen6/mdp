package com.gitee.sop.support.dubbo;

import com.alibaba.fastjson2.JSON;
import com.gitee.sop.support.constant.SopConstants;
import com.gitee.sop.support.context.OpenContext;
import com.gitee.sop.support.util.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.RpcServiceContext;
import org.springframework.core.env.Environment;

import java.net.SocketException;
import java.util.Optional;

/**
 * @author 六如
 */
@Slf4j
@Activate(group = {CommonConstants.CONSUMER})
public class SopDubboConsumerFilter implements Filter {

    private Environment environment;

    private String ip = "unknown_ip";

    public void setEnvironment(Environment environment) {
        this.environment = environment;
        try {
            this.ip = NetUtil.getIntranetIp();
        } catch (SocketException e) {
            log.warn("获取内网IP失败", e);
        }
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcServiceContext serviceContext = RpcContext.getServiceContext();
        // 获取最后一次调用的提供方IP地址
        String targetIP = serviceContext.getRemoteHost();
        // 获取服务端应用名称
        String targetApp = serviceContext.getRemoteApplicationName();
        // 调用的接口
        String interfaceName = serviceContext.getInterfaceName();
        // 获取调用的方法
        String methodName = serviceContext.getMethodName();
        String param = JSON.toJSONString(serviceContext.getArguments());

        String currentApp = environment.getProperty(SopConstants.SPRING_APPLICATION_NAME);
        String currentIp = this.ip;

        OpenContext current = OpenContext.current();
        String traceId = Optional.ofNullable(current).map(OpenContext::getTraceId).orElse("");

        if (log.isInfoEnabled()) {
            log.info("[sop_trace][dubbo_client][{}] 发送请求, current={}({}) -> target={}({}), methodName={}.{}, param={}",
                    traceId, currentApp, currentIp, targetApp, targetIP, interfaceName, methodName, param);
        }

        return invoker.invoke(invocation);
    }

}
