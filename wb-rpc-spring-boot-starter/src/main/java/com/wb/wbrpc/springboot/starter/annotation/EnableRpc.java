package com.wb.wbrpc.springboot.starter.annotation;

import com.wb.wbrpc.springboot.starter.bootstrap.RpcConsumerBootstrap;
import com.wb.wbrpc.springboot.starter.bootstrap.RpcInitBootstrap;
import com.wb.wbrpc.springboot.starter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用 Rpc 注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {

    /**
     * 需要启动 server
     *
     * @return
     */
    boolean needServer() default true;
}
