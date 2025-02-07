package com.lgzServer.netty;

import com.alibaba.nacos.api.exception.NacosException;
import com.lgzServer.netty.nacos.NettyNacosRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
public class NettyServer {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    ServerBootstrap bootstrap;
    @Value("${transact.netty.port}")
    private Integer port;

    @PostConstruct
    public void init(){
        bossGroup=new NioEventLoopGroup(1);
        workerGroup=new NioEventLoopGroup();
        bootstrap=new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup)//设置两个线程组
                .channel(NioServerSocketChannel.class)//使用NioSocketChannel 作为服务器的通道实现
                .option(ChannelOption.SO_BACKLOG,128)//设置线程队列得到连接的数量
                .childHandler(new ServerChannelInit());
        new Thread(()->{
            try {
                startServer(this);
            } catch (NacosException | InterruptedException e) {
                log.error(e.getMessage());
            };
        });
    }
    public ChannelFuture start() throws InterruptedException {
        ChannelFuture channelFuture=bootstrap.bind(this.port).sync();
        return channelFuture;
    }
    public static void startServer(NettyServer nettyServer) throws NacosException, InterruptedException {
        Channel channel=nettyServer.start().channel();
        NettyNacosRegistry nettyNacosRegistry =new NettyNacosRegistry();
        nettyNacosRegistry.register();
        channel.closeFuture().sync();
    }

}
