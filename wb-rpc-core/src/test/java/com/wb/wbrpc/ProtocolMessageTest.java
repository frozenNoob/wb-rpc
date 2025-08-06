package com.wb.wbrpc;

import cn.hutool.core.util.IdUtil;
import com.wb.wbrpc.protocol.*;
import com.wb.wbrpc.constant.RpcConstant;
import com.wb.wbrpc.model.RpcRequest;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class ProtocolMessageTest {

    @Test
    public void testEncodeAndDecode() throws IOException {
        // 初始化消息
        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
        // 构造消息头
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.JDK.getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());// 说明消息类型为请求
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        header.setRequestId(IdUtil.getSnowflakeNextId());
        header.setBodyLength(0);
        // 构造消息体
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("myService");
        rpcRequest.setMethodName("myMethod");
        rpcRequest.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        rpcRequest.setParameterTypes(new Class[]{String.class});
        rpcRequest.setArgs(new Object[]{"aaa", "bbb"});
        // 构造消息
        protocolMessage.setHeader(header);
        protocolMessage.setBody(rpcRequest);

        // 验证编码解码
        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
        ProtocolMessage<?> message = ProtocolMessageDecoder.decode(encodeBuffer);

        Assert.assertNotNull(message);
        log.info("\n测试成功，请求头为：\n{}\n请求体为：\n{}",
                message.getHeader().toString(), message.getBody().toString());
    }

}
