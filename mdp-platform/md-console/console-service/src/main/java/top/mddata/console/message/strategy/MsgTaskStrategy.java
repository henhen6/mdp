package top.mddata.console.message.strategy;

import top.mddata.console.message.strategy.dto.MsgResult;
import top.mddata.console.message.strategy.dto.MsgTaskParam;

/**
 * 消息任务执行策略
 * @author henhen
 */
public interface MsgTaskStrategy {


    /**
     * 执行发送
     *
     * @param msgTaskParam 消息任务参数
     * @throws Exception 异常
     */
    MsgResult exec(MsgTaskParam msgTaskParam) throws Exception;

    /**
     * 是否执行成功
     *
     * @param result 执行函数的返回值
     * @return true表示成功
     */
    default boolean isSuccess(MsgResult result) {
        return true;
    }
}
