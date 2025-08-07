package com.wb.wbrpc.server.tcp;

import com.wb.wbrpc.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 装饰者模式（使用 recordParser 对原有的 buffer 处理能力进行增强）
 * 该模式的必要条件：1. 通过构造函数获得一个属于具体组件的成员实例。 2. 在具体装饰器中重写该实例需要被装饰的方法，即在原方法执行前后加上一些其他行为
 * 但是这个案例比较特殊，是通过设置具体组件的一些成员（size和eventHandler），从而把“在原方法执行前后加上一些其他行为”这些代码转移给这些成员执行。
 * <br><br>
 * 也就是实际执行的方法handle的流程如下：
 *      具体装饰器TcpBufferHandlerWrapper执行handle=>具体组件RecordParserImpl执行handle
 *          =>执行通过构造函数中的方法initRecordParser获取具体组件时，方法setOutput记录的匿名类中重写的方法handle
 *          =>而这个方法中，装饰了initRecordParser的形参传进来的方法（在该方法执行前后加了一些新的行为）
 *      结果是达到了装饰者模式的效果。
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    private final RecordParser recordParser;

    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        recordParser = initRecordParser(bufferHandler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // 构造 parser，固定尺寸以完整读取一个消息头
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            int size = -1;
            // 一次完整的读取（头 + 体）
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (-1 == size) {
                    // 读取消息体长度,从给定的位置13开始读取4个字节并将他们组合为一个int值返回
                    // 对应位置13~17，这个位置是存储消息头中的 消息体的长度。
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);
                    // 写入头信息到结果
                    resultBuffer.appendBuffer(buffer);
                } else {
                    // 写入体信息到结果
                    resultBuffer.appendBuffer(buffer);
                    // 已拼接为完整 Buffer，执行处理
                    bufferHandler.handle(resultBuffer);
                    // 重置一轮
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });

        return parser;
    }
}
