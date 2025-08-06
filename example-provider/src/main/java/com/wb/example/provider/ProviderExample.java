package com.wb.example.provider;

import com.wb.example.provider.service.impl.UserServiceImpl;
import com.wb.example.common.service.UserService;
import com.wb.wbrpc.RpcApplication;
import com.wb.wbrpc.config.RegistryConfig;
import com.wb.wbrpc.config.RpcConfig;
import com.wb.wbrpc.model.ServiceMetaInfo;
import com.wb.wbrpc.registry.LocalRegistry;
import com.wb.wbrpc.registry.Registry;
import com.wb.wbrpc.registry.RegistryFactory;
import com.wb.wbrpc.server.HttpServer;
import com.wb.wbrpc.server.VertxHttpServer;
import com.wb.wbrpc.server.tcp.VertxTcpServer;

/**
 * 服务提供者示例
 */
public class ProviderExample {

    public static void main(String[] args) {
        // TODO：这里使用的固定名称，实际上应该是要通过某种方式来动态获取才对（参考Feign）
        String serviceName = UserService.class.getName();
        /**
         注册服务到第三方注册中心如etcd
         */
        // 通过懒加载获取配置
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        // 基于SPI通过实例名如"etcd"获取注册中心实例
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());

        // 注册服务到该注册中心实例上
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 注册服务到本地的注册中心
        LocalRegistry.register(serviceName, UserServiceImpl.class);
        // 启动 web 服务
        HttpServer httpServer = new VertxTcpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());

    }
}

