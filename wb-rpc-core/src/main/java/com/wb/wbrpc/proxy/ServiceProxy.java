package com.wb.wbrpc.proxy;


import cn.hutool.core.collection.CollUtil;
import com.wb.wbrpc.RpcApplication;
import com.wb.wbrpc.config.RpcConfig;
import com.wb.wbrpc.constant.RpcConstant;
import com.wb.wbrpc.fault.retry.RetryStrategy;
import com.wb.wbrpc.fault.retry.RetryStrategyFactory;
import com.wb.wbrpc.fault.tolerant.TolerantStrategy;
import com.wb.wbrpc.fault.tolerant.TolerantStrategyFactory;
import com.wb.wbrpc.fault.tolerant.TolerantStrategyKeys;
import com.wb.wbrpc.loadbalancer.LoadBalancer;
import com.wb.wbrpc.loadbalancer.LoadBalancerFactory;
import com.wb.wbrpc.loadbalancer.LoadBalancerForHash;
import com.wb.wbrpc.model.RpcRequest;
import com.wb.wbrpc.model.RpcResponse;
import com.wb.wbrpc.model.ServiceMetaInfo;
import com.wb.wbrpc.registry.Registry;
import com.wb.wbrpc.registry.RegistryFactory;
import com.wb.wbrpc.server.tcp.VertxTcpClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务代理（JDK 动态代理）
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {


    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 从注册中心获取服务提供者请求地址
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("暂无服务地址");
        }

        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        // 将调用方法名（请求路径）作为负载均衡参数
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        // 如果算法是一致性哈希的话，需要额外建立哈希环
        if (loadBalancer instanceof LoadBalancerForHash) {
            ((LoadBalancerForHash) loadBalancer).setIfChanged(requestParams, serviceMetaInfoList);
        }
        // 选取节点
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);

        RpcResponse rpcResponse;
        try {
            // 使用重试机制发送TCP请求和得到响应
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );

        } catch (Exception e) {
            log.error("\n重试失败，将采用容错机制");
            // 1. 选取容错机制
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(RpcApplication.getRpcConfig().getTolerantStrategy());
            Map<String, Object> contextAboutTolerant = null;
            // 1.1 Fail-Back策略
            if (TolerantStrategyKeys.FAIL_BACK.equals(RpcApplication.getRpcConfig().getTolerantStrategy())) {
                contextAboutTolerant = new HashMap<>() {{
                    put("method", method);
                    put("args", args);
                }};
            }
            // 1.2 Fail-Over策略
            if (TolerantStrategyKeys.FAIL_OVER.equals(RpcApplication.getRpcConfig().getTolerantStrategy())) {
                contextAboutTolerant = new HashMap<>() {{
                    put("visited", selectedServiceMetaInfo);// 已经访问过的节点
                    put("nodeList", serviceMetaInfoList); // 所有的节点
                    put("rpcRequest", rpcRequest); //
                }};
            }
            // 2. 使用容错机制
            rpcResponse = tolerantStrategy.doTolerant(contextAboutTolerant, e);

            if (rpcResponse == null) {
                log.error("返回的响应为空");
                throw new Exception(e);
            }
        }

        if (rpcResponse.getData() == null) {
            log.warn("返回的响应的数据为空");
        }
        return rpcResponse.getData();
    }
}

