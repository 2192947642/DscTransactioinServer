package com.lgzServer;

import com.alibaba.nacos.api.exception.NacosException;
import com.lgzServer.nacos.NacosRegistry;
import com.lgzServer.redis.RedisHelper;
import com.lgzServer.service.ResendService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.InetAddress;

public class NettyServer {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    ServerBootstrap bootstrap;
    private int port;
    NettyServer(){
        this.port=ServerConfig.instance.serverPort;//设置端口
        bossGroup=new NioEventLoopGroup(1);
        workerGroup=new NioEventLoopGroup();
        bootstrap=new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup)//设置两个线程组
                .channel(NioServerSocketChannel.class)//使用NioSocketChannel 作为服务器的通道实现
                .option(ChannelOption.SO_BACKLOG,128)//设置线程队列得到连接的数量
                .childHandler(new ServerChannelInit());
    }
    public ChannelFuture start() throws InterruptedException {
        ChannelFuture channelFuture=bootstrap.bind(this.port).sync();
        return channelFuture;
    }
    public static void main(String[] args) throws InterruptedException, IOException, NacosException {
        RedisHelper.init();
        ResendService.instance.startResendService();
        NettyServer nettyServer=new NettyServer();
        Channel channel=nettyServer.start().channel();
        NacosRegistry nacosRegistry=new NacosRegistry();
        nacosRegistry.register();
        channel.closeFuture().sync();
    }
}
