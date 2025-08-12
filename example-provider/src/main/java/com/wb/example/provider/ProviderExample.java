package com.wb.example.provider;

import com.wb.examplespringbootprovider.service.client.UserService;
import com.wb.example.provider.service.impl.UserServiceImpl;
import com.wb.wbrpc.bootstrap.ProviderBootstrap;
import com.wb.wbrpc.model.ServiceRegisterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务提供者示例
 */
public class ProviderExample {

    public static void main(String[] args) {
        // 要注册的服务
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        // 注意这里导入的是公共API模块中的接口，所以给出泛型 ? 即可
        ServiceRegisterInfo<?> serviceRegisterInfo = new ServiceRegisterInfo<>(UserService.class.getName(), UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);
        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }
}

