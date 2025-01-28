package com.lgzServer;

import com.lgzServer.handlers.ClientChannelManager;
import com.lgzServer.handlers.ExceptionHandler;
import com.lgzServer.handlers.MessageHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
public class ServerChannelInit extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline pipeline= socketChannel.pipeline();
        pipeline.addLast(new LengthFieldPrepender(2,0,false));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
        pipeline.addLast("decoder",new StringDecoder());//加入解码器 output 从后向前 input 从前向后
        pipeline.addLast("encoder",new StringEncoder());//加入编码器
        pipeline.addLast(new MessageHandler());
        pipeline.addLast(ClientChannelManager.instance);
        pipeline.addLast("exception",new ExceptionHandler());
    }
}
