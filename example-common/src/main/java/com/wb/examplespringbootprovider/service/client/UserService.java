package com.wb.examplespringbootprovider.service.client;

import com.wb.example.common.model.User;
import com.wb.wbrpc.springboot.starter.annotation.WbRpcClient;

/**
 * 用户服务
 */
@WbRpcClient(key = "UserServiceImpl")
public interface UserService {

    /**
     * 获取用户
     *
     * @param user
     * @return
     */
    User getUser(User user);

    default int getNumber(){
        return 1;
    }
}
