package com.wb.examplespringbootprovider;

import com.wb.example.common.model.User;
import com.wb.example.common.service.UserService;
import com.wb.wbrpc.springboot.starter.annotation.RpcService;

/**
 * 用户服务实现类
 */
@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public User getUser(User user) {
        System.out.println("用户名；"+ user.getName());
        return user;
    }
}
