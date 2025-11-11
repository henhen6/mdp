package com.gitee.sop.support.message;

/**
 * 定义错误返回
 *
 * @author 六如
 */
public interface OpenMessage {

    /**
     * @return 明细返回码
     */
    String getCode();

    /**
     * @return 明细信息
     */
    String getMsg();

    /**
     * @return 解决方案
     */
    String getSolution();


}
