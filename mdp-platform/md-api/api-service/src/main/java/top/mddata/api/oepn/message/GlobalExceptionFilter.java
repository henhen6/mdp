package top.mddata.api.oepn.message;

import com.gitee.sop.support.exception.OpenException;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.filter.ExceptionFilter;
import top.mddata.base.exception.ArgumentException;

/**
 *
 * @author henhen
 * @since 2026/1/9 19:54
 */
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER}) // 激活为提供者/消费者端过滤器
public class GlobalExceptionFilter extends ExceptionFilter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 执行原调用逻辑
        Result result = invoker.invoke(invocation);
        // 处理正常响应中的异常（异步场景）
        if (result.hasException() && result.getException() != null) {
            Throwable e = result.getException();

            if (e instanceof ArgumentException) {
                OpenException openException = new OpenException(StoryMessageEnum.PARAM_VALIDATION, e.getMessage());
                result.setException(openException);
            }
        }
        return result;
    }


}
