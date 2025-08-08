package com.wb.wbrpc.loadbalancer;

import com.wb.wbrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

public interface LoadBalancerForHash extends LoadBalancer{

    /**
     * 只适用于一致性哈希算法
     * setIfChanged原子操作，当服务节点列表发生改变时，需要重新建立哈希环
     * @param requestParams  请求参数
     * @param serviceMetaInfoList 可用服务列表
     * @return
     */
    public void setIfChanged(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}
