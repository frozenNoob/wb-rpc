package com.wb.wbrpc.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 配置工具类
 */
public class ConfigUtils {

    /**
     * 加载配置对象
     *
     * @param tClass
     * @param prefix
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象，支持区分环境
     *
     * @param tClass-返回对应类实例
     * @param prefix-前缀，比如rpc.name=wbrpc中，前缀就是rpc,然后name就会作为属性封装到tClass对象上。
     * @param environment
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");
        Props props = new Props(configFileBuilder.toString());
        // 这个函数默认是从调用这个函数的jar包开始查找文件，匹配则直接返回。不匹配才会从其他jar包中查找。
        return props.toBean(tClass, prefix);
    }

       /* *//**
         * 加载配置对象（支持嵌套对象和区分环境）
         *
         * @param tClass       返回对应类实例
         * @param prefix       属性前缀 (如 "rpc" 对应 rpc.name=wbrpc)
         * @param environment  环境标识 (如 "dev", "prod")
         * @param <T>          返回类型
         * @return 配置对象实例
         *//*
        public static <T > T loadConfig(Class < T > tClass, String prefix, String environment) {
            // 1. 构建配置文件名称
            StringBuilder configFileBuilder = new StringBuilder("application");
            if (StrUtil.isNotBlank(environment)) {
                configFileBuilder.append("-").append(environment);
            }
            configFileBuilder.append(".properties");

            // 2. 加载配置文件
            Props props = new Props(configFileBuilder.toString());

            // 3. 创建目标Bean实例
            try {
                T configBean = tClass.getDeclaredConstructor().newInstance();

                // 4. 处理带前缀的配置项
                String actualPrefix = StrUtil.isBlank(prefix) ? "" : prefix + ".";
                Map<String, Object> configMap = new LinkedHashMap<>();

                // 5. 提取带前缀的配置项并移除前缀
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    String key = (String) entry.getKey();
                    if (key.startsWith(actualPrefix)) {
                        String nestedKey = key.substring(actualPrefix.length());
                        configMap.put(nestedKey, entry.getValue());
                    }
                }

                // 6. 使用BeanUtil填充嵌套属性（核心修改）
                if (!configMap.isEmpty()) {
                    BeanUtil.fillBeanWithMap(configMap, configBean, true);
                }

                return configBean;
            } catch (Exception e) {
                throw new RuntimeException("创建配置对象失败: " + tClass.getName(), e);
            }
        }*/
    }
