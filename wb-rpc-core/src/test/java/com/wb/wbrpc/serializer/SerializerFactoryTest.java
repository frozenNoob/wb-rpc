package com.wb.wbrpc.serializer;

import org.junit.Test;

import static org.junit.Assert.*;

public class SerializerFactoryTest {

    @Test
    public void getInstance() throws InterruptedException {
        Thread thread1 = new Thread(()->{
            SerializerFactory.getInstance("json");
        });
        Thread thread2 = new Thread(()->{
            SerializerFactory.getInstance("json");
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }
}