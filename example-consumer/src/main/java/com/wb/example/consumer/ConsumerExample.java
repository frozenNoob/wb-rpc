package com.wb.example.consumer;

import com.wb.example.common.model.User;
import com.wb.example.common.service.UserService;
import com.wb.wbrpc.proxy.ServiceProxyFactory;

/**
 * 服务消费者示例
 */
public class ConsumerExample {

    public static void main(String[] args) throws InterruptedException {
        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("wb");
        // 调用3次
        User newUser = userService.getUser(user);
        newUser = userService.getUser(user);
        //System.out.println("开始测试缓存是否清空");
        //Thread.sleep(10*1000);
        //newUser = userService.getUser(user);

        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
        // 注意这里因为本质是调用代理对象的方法而非default方法！
        int number = userService.getNumber();
        System.out.println(number);
    }
}
