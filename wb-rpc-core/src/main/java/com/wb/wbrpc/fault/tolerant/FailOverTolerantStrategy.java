package com.wb.wbrpc.fault.tolerant;

import com.github.rholder.retry.*;
import com.wb.wbrpc.model.RpcRequest;
import com.wb.wbrpc.model.RpcResponse;
import com.wb.wbrpc.model.ServiceMetaInfo;
import com.wb.wbrpc.server.tcp.VertxTcpClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 转移到其他服务节点 - 容错策略
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // 1. 获取传入的参数
        ServiceMetaInfo visitedServiceMetaInfo = (ServiceMetaInfo) context.get("visited");
        List<ServiceMetaInfo> serviceMetaInfoList = (List<ServiceMetaInfo>) context.get("nodeList");
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");

        RpcResponse rpcResponse;

        // 2. 固定时间间隔重试（访问参数可变）
        // AtomicInteger attemptCount = new AtomicInteger(serviceMetaInfoList.size());// 此处不需要原子类
        // 从内部类更改外部参数需要如此使用：
        final Integer[] attemptCount = {-1};
        // 2.1 定义Callable，动态获取参数
        Callable<RpcResponse> callable = () -> {
            int attempt = attemptCount[0]++ + 1;
            ServiceMetaInfo curService = serviceMetaInfoList.get(attempt);
            // 直接跳过之前已经访问过的崩溃节点
            if (curService.getServiceNodeKey()
                    .equals(visitedServiceMetaInfo.getServiceNodeKey())) {
                return null;
            }
            // 返回响应
            return VertxTcpClient.doRequest(rpcRequest, curService);
        };

        // 2.2 配置 Retryer
        // 添加抑制警告注解
        @SuppressWarnings("UnstableApiUsage")
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class) // 遇到异常重试
                .retryIfResult(Objects::isNull) // 结果为 null 重试
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS)) // 每次重试间隔 1 秒
                .withStopStrategy(StopStrategies.stopAfterAttempt(serviceMetaInfoList.size())) // 最大重试次数
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                })
                .build();
        // 2.3 开始重试
        try {
            rpcResponse = retryer.call(callable);
            rpcResponse.setException(e);
            rpcResponse.setMessage("通过FailOver策略重试");
        } catch (ExecutionException | RetryException ex) {
            log.error("调用其他节点时出现错误");
            throw new RuntimeException(ex);
        }

        return rpcResponse;
    }
}
