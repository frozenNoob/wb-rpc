package com.wb.example.provider;

import com.wb.example.provider.service.impl.UserServiceImpl;
import com.wb.example.common.service.UserService;
import com.wb.wbrpc.RpcApplication;
import com.wb.wbrpc.registry.LocalRegistry;
import com.wb.wbrpc.server.HttpServer;
import com.wb.wbrpc.server.VertxHttpServer;

/**
 * 服务提供者示例
 */
public class ProviderExample {

    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}

