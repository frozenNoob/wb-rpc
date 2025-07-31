package com.wb.example.consumer;

import com.wb.wbrpc.config.RpcConfig;
import com.wb.wbrpc.constant.RpcConstant;
import com.wb.wbrpc.utils.ConfigUtils;

/**
 * 服务消费者示例
 */
public class ConsumerExample {

    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);
    }
}
