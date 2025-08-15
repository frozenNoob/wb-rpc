package com.wb.wbrpc.fault.tolerant;

import com.wb.wbrpc.model.RpcResponse;
import com.wb.wbrpc.proxy.MockServiceProxy;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 降级到其他服务 - 容错策略
 */
@Slf4j
public class FailBackTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        RpcResponse rpcResponse = new RpcResponse();
        try {
            Method method = (Method) context.get("method");
            Object[] args = (Object[]) context.get("args");
            MockServiceProxy mockServiceProxy = new MockServiceProxy();
            // 执行该方法，这里传入的第一个参数proxy是根本用不到的，因为我是手动执行invoke方法，而不是通过正常的代理执行。
            // 在正常的代理执行时，第一个参数proxy是默认赋值为生成的代理对象，可以通过该对象访问接口名等等（不过一般用不上？）。
            Object result = mockServiceProxy.invoke(mockServiceProxy, method, args);
            // 设置回响应的结果中
            rpcResponse.setData(result);
            rpcResponse.setMessage("返回Mock作为降级策略");
            rpcResponse.setException(e);
        } catch (Throwable ex) {
            log.error("执行FailBack容错策略失败");
            throw new RuntimeException(ex);
        }
        return rpcResponse;
    }
}
