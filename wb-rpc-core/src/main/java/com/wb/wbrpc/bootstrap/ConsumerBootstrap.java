package com.wb.wbrpc.bootstrap;

import com.wb.wbrpc.RpcApplication;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务消费者启动类（初始化）
 */
@Slf4j
public class ConsumerBootstrap {

    /**
     * 初始化
     */
    public static void init() {
        // RPC 框架初始化（配置和注册中心）
        RpcApplication.getRpcConfig();
        log.info("成功初始化服务消费者启动类");
    }

}
