package com.wb.wbrpc.serializer;

import com.wb.wbrpc.spi.SpiLoader;

/**
 * 序列化器工厂（用于获取序列化器对象）
 */
public class SerializerFactory {

    /**
     * 序列化映射（用于实现饿汉式单例模式）
     */
    private volatile static boolean finishLoad;

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        if(!finishLoad){
            synchronized (SerializerFactory.class){
                if(!finishLoad){
                    // 懒加载工厂所需信息
                    SpiLoader.load(Serializer.class);
                    finishLoad = true;
                }
            }
        }
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
