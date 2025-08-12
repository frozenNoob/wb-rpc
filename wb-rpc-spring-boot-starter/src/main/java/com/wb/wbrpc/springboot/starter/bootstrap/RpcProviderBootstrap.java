package com.wb.wbrpc.springboot.starter.bootstrap;

import com.wb.wbrpc.RpcApplication;
import com.wb.wbrpc.config.RegistryConfig;
import com.wb.wbrpc.config.RpcConfig;
import com.wb.wbrpc.model.ServiceMetaInfo;
import com.wb.wbrpc.registry.LocalRegistry;
import com.wb.wbrpc.registry.Registry;
import com.wb.wbrpc.registry.RegistryFactory;
import com.wb.wbrpc.springboot.starter.annotation.RpcService;
import com.wb.wbrpc.springboot.starter.annotation.WbRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Set;

/**
 * Rpc 服务提供者启动
 */
@Slf4j
public class RpcProviderBootstrap implements BeanPostProcessor {

    /**
     * 从注解 @WbRpcClient中获取本地注册所需key
     */
    public String getLocalRegistryKeyFromAnnotation(String basePackage, String className) throws ClassNotFoundException {

        // 在basePackage路径下进行搜索
        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(WbRpcClient.class);
        // 获取本地注册所需key
        String localRegistryKey =null;
        for (Class<?> clazz : annotated) {
            WbRpcClient annotation = clazz.getAnnotation(WbRpcClient.class);
            // 获取到指定实现类对应的接口名字
            if(annotation.key().equals(className)){
                localRegistryKey = clazz.getName();
                break;
            }
        }
        return localRegistryKey;
    }
    /**
     * 每个 Bean 初始化后执行，注册服务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 对每个Bean对象都查询是否被该注解修饰，
        // 如果是则获取该注解进而获取具体的成员值（即为注解赋的值，否则采用默认值）。
        // 不是则返回null
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            // 需要注册服务
            // 1. 获取服务基本信息
            Class<?> interfaceClass = rpcService.interfaceClass();
            // 1.1 默认值处理（默认值是Bean对应的实现类实现的第一个接口的class对象）
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];
            }
            // 1.2 根据服务接口和服务实现类类名获取服务名
            String serviceName = null;
            try {
                // 包名需要后面都加上client
                String basePackage = interfaceClass.getPackageName() + ".client";
                // 所以是需要同包名(即同package)，然后才能根据实现类的类名去作匹配
                serviceName = getLocalRegistryKeyFromAnnotation(basePackage, beanClass.getSimpleName());
            } catch (ClassNotFoundException e) {
                log.error("未扫描到该包名下的接口或者实现类类名匹配失败！");
                throw new RuntimeException(e);
            }
            String serviceVersion = rpcService.serviceVersion();
            // 2. 注册服务
            // 本地注册
            LocalRegistry.register(serviceName, beanClass);

            // 全局配置
            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
