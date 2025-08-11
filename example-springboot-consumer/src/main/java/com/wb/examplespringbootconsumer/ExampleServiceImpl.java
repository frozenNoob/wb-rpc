package com.wb.examplespringbootconsumer;

import com.wb.example.common.model.User;
import com.wb.example.common.service.UserService;
import com.wb.wbrpc.springboot.starter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {
    /**
     * 使用RPC框架注入
     */
    @RpcReference
    private UserService userService;

    public void test(){
        User user = new User();
        user.setName("wb");
        User resultUser = userService.getUser(user);
        System.out.println(resultUser.getName());
    }
}
