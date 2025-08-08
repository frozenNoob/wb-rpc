package com.wb.wbrpc.proxy;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.wb.wbrpc.RpcApplication;
import com.wb.wbrpc.config.RpcConfig;
import com.wb.wbrpc.constant.RpcConstant;
import com.wb.wbrpc.loadbalancer.LoadBalancer;
import com.wb.wbrpc.loadbalancer.LoadBalancerFactory;
import com.wb.wbrpc.loadbalancer.LoadBalancerForHash;
import com.wb.wbrpc.loadbalancer.LoadBalancerKeys;
import com.wb.wbrpc.model.RpcRequest;
import com.wb.wbrpc.model.RpcResponse;
import com.wb.wbrpc.model.ServiceMetaInfo;
import com.wb.wbrpc.protocol.*;
import com.wb.wbrpc.registry.Registry;
import com.wb.wbrpc.registry.RegistryFactory;
import com.wb.wbrpc.serializer.JdkSerializer;
import com.wb.wbrpc.serializer.Serializer;
import com.wb.wbrpc.serializer.SerializerFactory;
import com.wb.wbrpc.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        try {
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
            if(loadBalancer instanceof LoadBalancerForHash){
                ((LoadBalancerForHash)loadBalancer).setIfChanged(requestParams, serviceMetaInfoList);
            }
            // 选取节点
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);

            // 发送 TCP 请求 和 得到响应
            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            return rpcResponse.getData();
        } catch (Exception e) {
            log.error("\n调用代理时出现错误");
            throw new RuntimeException(e.getMessage() + "\n调用代理时出现错误");
        }
    }
}

