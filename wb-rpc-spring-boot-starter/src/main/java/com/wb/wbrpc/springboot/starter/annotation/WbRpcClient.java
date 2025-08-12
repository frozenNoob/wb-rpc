package com.wb.wbrpc.springboot.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端注解（标注在类上），服务提供者使用此注解来决定需要暴露接口的类
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WbRpcClient {
    /**
     * 给服务提供者的本地注册中心的key
     * @return
     */
    String key() default "";
}
