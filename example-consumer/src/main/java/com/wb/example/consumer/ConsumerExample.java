package com.wb.example.consumer;

import com.wb.example.common.model.User;
import com.wb.examplespringbootprovider.service.client.UserService;
import com.wb.wbrpc.bootstrap.ConsumerBootstrap;
import com.wb.wbrpc.proxy.ServiceProxyFactory;

/**
 * 服务消费者示例
 */
public class ConsumerExample {

    public static void main(String[] args) throws InterruptedException {
        // 服务提供者初始化
        ConsumerBootstrap.init();

        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("wb");
        // 调用
        User newUser = userService.getUser(user);
        for(int i=0;i<100;i++)
            newUser = userService.getUser(user);

        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
