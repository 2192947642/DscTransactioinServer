package com.lgzServer.netty.handlers;

import com.lgzServer.types.Message;
import com.lgzServer.types.ReceiveContext;
import com.lgzServer.utils.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@ChannelHandler.Sharable
public class ClientChannelManager extends ChannelInboundHandlerAdapter {
    public static final ClientChannelManager instance=new ClientChannelManager();
    private ConcurrentHashMap<String, Channel> channelMap=new ConcurrentHashMap<>();
    private ConcurrentHashMap<Channel,String> addressMap=new ConcurrentHashMap<>();
    public Channel getChannel(String address){
        return channelMap.get(address);
    }
    public void putChannel(String address,Channel channel){
        channelMap.put(address,channel);
        addressMap.put(channel,address);
    }
    public void sendMessage(ReceiveContext<Message> receiveContext){
        Channel channel=getChannel(receiveContext.remoteAddress);
        if(channel==null||!channel.isActive()){//当前的通道并不是激活状态
            System.out.println("通道连接错误"+receiveContext.remoteAddress);
            return;
        }
        channel.writeAndFlush(JsonUtil.objToJson(receiveContext.message));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        System.out.println(ctx.channel().remoteAddress()+"连接成功");
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx)  {
        String address=addressMap.get(ctx.channel());
        System.out.println("通道断开连接"+address);
        if(address!=null){
            channelMap.remove(address);
            addressMap.remove(ctx.channel());
        }
    }
}
