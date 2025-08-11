package com.wb.wbrpc.bootstrap;

import com.wb.wbrpc.RpcApplication;
import com.wb.wbrpc.config.RegistryConfig;
import com.wb.wbrpc.config.RpcConfig;
import com.wb.wbrpc.model.ServiceMetaInfo;
import com.wb.wbrpc.model.ServiceRegisterInfo;
import com.wb.wbrpc.registry.LocalRegistry;
import com.wb.wbrpc.registry.Registry;
import com.wb.wbrpc.registry.RegistryFactory;
import com.wb.wbrpc.server.tcp.VertxTcpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 服务提供者初始化
 */
@Slf4j
public class ProviderBootstrap {

    /**
     * 初始化
     */
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        // RPC 框架初始化（配置和注册中心），获得全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            // 本地注册
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }

        // 启动服务器
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());

        log.info("成功初始化服务提供者启动类");
    }
}
