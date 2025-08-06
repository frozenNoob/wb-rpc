package com.wb.wbrpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.wb.wbrpc.RpcApplication;
import com.wb.wbrpc.model.RpcRequest;
import com.wb.wbrpc.model.RpcResponse;
import com.wb.wbrpc.model.ServiceMetaInfo;
import com.wb.wbrpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class VertxTcpClient {

    public void start() {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8888, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("Connected to TCP server");
                io.vertx.core.net.NetSocket socket = result.result();
                for (int i = 0; i < 1000; i++) {
                    // 发送数据
                    Buffer buffer = Buffer.buffer();
                    String str = "Hello, server!Hello, server!Hello, server!Hello, server!";
                    buffer.appendInt(0);
                    buffer.appendInt(str.getBytes().length);
                    buffer.appendBytes(str.getBytes());
                    socket.write(buffer);
                }
                // 接收响应
                socket.handler(buffer -> {
                    System.out.println("Received response from server: " + buffer.toString());
                });
            } else {
                System.err.println("Failed to connect to TCP server");
            }
        });
    }

    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws InterruptedException, ExecutionException {
        // 发送TCP请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();// 为了同步而用
        //  Vert.x 提供的请求处理器是异步的
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(),
                result -> {
                    if (result.succeeded()) {
                        log.info("Connected to TCP server");
                        NetSocket socket = result.result();
                        // 发送数据
                        // 构造消息
                        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                        ProtocolMessage.Header header = new ProtocolMessage.Header();
                        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                        header.setSerializer((byte) ProtocolMessageSerializerEnum
                                .getEnumByValue(RpcApplication.getRpcConfig().getSerializer())
                                .getKey());
                        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                        // 生成全局请求ID
                        header.setRequestId(IdUtil.getSnowflakeNextId());

                        protocolMessage.setHeader(header);
                        protocolMessage.setBody(rpcRequest);
                        // 编码请求
                        try {
                            Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                            socket.write(encodeBuffer);// 发送请求
                        } catch (IOException e) {
                            throw new RuntimeException("TCP协议消息编码错误");
                        }

                        // 接收响应
                        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper((buffer) -> {
                            try {
                                ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                // 完成了响应
                                responseFuture.complete(rpcResponseProtocolMessage.getBody());// 另一线程执行任务，手动设置成功结果
                            } catch (IOException e) {
                                throw new RuntimeException("协议消息解码错误");
                            }
                        });
                        socket.handler(bufferHandlerWrapper);
                    } else {
                        log.error("Failed to connect to TCP server");
                    }
                });

        // 阻塞，直到响应完成，才会继续向下执行
        RpcResponse rpcResponse = responseFuture.get();// 阻塞该线程，等待另一线程执行完任务
        // 记得关闭连接
        netClient.close();
        return rpcResponse;
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
