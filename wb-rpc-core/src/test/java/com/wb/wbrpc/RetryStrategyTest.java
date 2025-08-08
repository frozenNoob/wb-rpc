package com.wb.wbrpc;

import com.wb.wbrpc.fault.retry.NoRetryStrategy;
import com.wb.wbrpc.fault.retry.RetryStrategy;
import com.wb.wbrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * 重试策略测试
 */
@Slf4j
public class RetryStrategyTest {

    RetryStrategy retryStrategy = new NoRetryStrategy();

    @Test
    public void doRetry() {
        log.error("执行此方法不会中断，因为只有抛出异常时才会中断");
        try {
            RpcResponse rpcResponse = retryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败");
            });
            System.out.println("继续进行");
            System.out.println(rpcResponse);
        } catch (Exception e) {
            System.out.println("重试多次失败");
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
