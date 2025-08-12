package com.wb.examplespringbootconsumer;

import com.wb.wbrpc.springboot.starter.annotation.EnableRpc;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.util.Set;

public class ScanAnnotationTest {
    /**
     * 查找包下的被注解@EnableRpc修饰的接口/类
     */
    @Test
    void testScanAnnotation(){
        Reflections reflections = new Reflections("com.wb.examplespringbootconsumer");

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(EnableRpc.class);
        for (Class<?> clazz : annotated) {
            EnableRpc ann = clazz.getAnnotation(EnableRpc.class);
            System.out.println(clazz.getName() + " : " + ann.getClass());
        }
    }
}
