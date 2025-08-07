package com.wb.wbrpc;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentNavigableMap;

public class FixedHashTest {
    public static void main(String[] args) {
        // 使用线程安全的ConcurrentSkipListMap
        ConcurrentNavigableMap<Integer, String> safeMap = new ConcurrentSkipListMap<>();

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                safeMap.put(i, "Value " + i);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                safeMap.put(i, "NewValue " + i);
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Map Size: " + safeMap.size()); // 总是1000
    }
}