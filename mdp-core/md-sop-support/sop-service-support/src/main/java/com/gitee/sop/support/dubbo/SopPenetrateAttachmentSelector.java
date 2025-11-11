package com.gitee.sop.support.dubbo;

import com.gitee.sop.support.constant.SopConstants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.PenetrateAttachmentSelector;
import org.apache.dubbo.rpc.RpcContextAttachment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 六如
 */
public class SopPenetrateAttachmentSelector implements PenetrateAttachmentSelector {
    @Override
    public Map<String, Object> select(Invocation invocation, RpcContextAttachment clientAttachment, RpcContextAttachment serverAttachment) {
        Map<String, Object> map = new HashMap<>(4);
        Object openContext = serverAttachment.getObjectAttachment(SopConstants.OPEN_CONTEXT);
        if (openContext != null) {
            map.put(SopConstants.OPEN_CONTEXT, openContext);
        }
        Object webContext = serverAttachment.getObjectAttachment(SopConstants.WEB_CONTEXT);
        if (webContext != null) {
            map.put(SopConstants.WEB_CONTEXT, webContext);
        }
        return map;
    }

    @Override
    public Map<String, Object> selectReverse(Invocation invocation, RpcContextAttachment clientResponseContext, RpcContextAttachment serverResponseContext) {
        return Collections.emptyMap();
    }
}
