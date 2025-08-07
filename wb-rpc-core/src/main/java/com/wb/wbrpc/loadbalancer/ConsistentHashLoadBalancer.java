package com.wb.wbrpc.loadbalancer;

import com.wb.wbrpc.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * （几乎）线程安全的一致性哈希负载均衡器
 * 基于公平读写锁+双重检测锁 实现`addIfAbsentAndGet`操作（近似于原子操作）来保证一致性哈希算法的线程安全
 */
@Slf4j
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一致性 Hash 环，存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    /**
     * 公平的读写锁保证线程安全
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * 记录上次使用的服务列表快照
     * 这里可以不用static的，因为在一个java进程中，SPI加载的是同一个实例（因为用到单例模式）。
     */
    private List<ServiceMetaInfo> lastServiceMetaInfoList = Collections.emptyList();

    /**
     * 虚拟节点数
     */
    private static final int VIRTUAL_NODE_NUM = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }

        // 检查服务列表是否变化
        if (hasServiceListChanged(serviceMetaInfoList)) {
            // 获取写锁重建哈希环
            lock.writeLock().lock();
            try {
                // 双重检查避免重复重建
                if (hasServiceListChanged(serviceMetaInfoList)) {
                    rebuildVirtualNodes(serviceMetaInfoList);
                    lastServiceMetaInfoList = new ArrayList<>(serviceMetaInfoList);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        // 获取读锁执行查询
        lock.readLock().lock();
        // 检查服务列表是否变化，变化则记录warn，因为后续有兜底或重试机制，所以其实这里不用太严格
        if (hasServiceListChanged(serviceMetaInfoList)) {
            log.warn("此时的服务列表已经发生变化");
            //throw new RuntimeException("此时的服务列表已经发生变化");
        }
        try {
            int hash = getHash(requestParams);
            Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
            if (entry == null) {
                entry = virtualNodes.firstEntry();
            }
            return entry.getValue();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 重建虚拟节点环
     */
    private void rebuildVirtualNodes(List<ServiceMetaInfo> serviceMetaInfoList) {
        virtualNodes.clear();
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }
    }

    /**
     * 检查服务列表是否发生变化，最坏时间复杂度为O(n)，其中n为列表长度
     */
    private boolean hasServiceListChanged(List<ServiceMetaInfo> newList) {
        if (lastServiceMetaInfoList.size() != newList.size()) {
            return true;
        }

        // 创建服务地址快照用于比较
        Set<String> oldAddresses = new HashSet<>();
        for (ServiceMetaInfo service : lastServiceMetaInfoList) {
            oldAddresses.add(service.getServiceAddress());
        }

        Set<String> newAddresses = new HashSet<>();
        for (ServiceMetaInfo service : newList) {
            newAddresses.add(service.getServiceAddress());
        }

        return !oldAddresses.equals(newAddresses);
    }

    /**
     * Hash 算法
     */
    private int getHash(Object key) {
        return key.hashCode();
    }
}