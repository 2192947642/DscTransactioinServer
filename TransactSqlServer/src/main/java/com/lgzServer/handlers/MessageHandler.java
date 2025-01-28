package com.lgzServer.handlers;
import com.lgzServer.redis.RedisHelper;
import com.lgzServer.types.*;
import com.lgzServer.types.status.GlobalStatus;
import com.lgzServer.types.status.LocalStatus;
import com.lgzServer.types.status.MessageTypeEnum;
import com.lgzServer.utils.JsonUtil;
import com.lgzServer.utils.TimeUtil;
import io.netty.channel.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;

public class MessageHandler extends SimpleChannelInboundHandler<String> {
    RedisHelper redisHelper=RedisHelper.getInstance();

    public MessageHandler() throws IOException {
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String string) throws Exception {
        Message message=JsonUtil.jsonToObject(string, Message.class);
        if(message.type.equals(MessageTypeEnum.LocalNotice)){
            handleLocalNotice(message);
        }else if(message.type.equals(MessageTypeEnum.ServerAddress)){
            handleServerAddress(message,channelHandlerContext.channel());
        }
    }
    private void handleServerAddress(Message msg,Channel channel){
        ServerAddress address= JsonUtil.jsonToObject(msg.content, ServerAddress.class);
        ClientChannelManager.instance.putChannel(address.serverAddress, channel);
    }
    //处理本地事务的请求
    private  void handleLocalNotice(Message message){
        String string=message.content;
        LocalNotice localNotice= JsonUtil.jsonToObject(string, LocalNotice.class);
        GlobalType globalType=redisHelper.getGlobalType(localNotice.globalId);
        if(localNotice.isSuccess){//如果为请求成功,那么就查找是否全部的都请求完了
            if(globalType.status==GlobalStatus.wait) return;
            if(globalType.status==GlobalStatus.success){
                sendNotice(globalType.status,localNotice.globalId,globalType.localTypeMap);
            }
        }else{//如果为失败
            //如果请求失败那么就直接向所有的本地事务发送失败的信息
            sendNotice(GlobalStatus.fail,localNotice.globalId,globalType.localTypeMap);
        }
    }
    private void sendNotice(GlobalStatus globalStatus,String globalId,Map<String,LocalType> maps){//全局事务的id
        if(globalStatus==GlobalStatus.wait) return;//如果当前全局事务还有为执行完的
        Set<String> keys = maps.keySet();
        GlobalNotice globalNotice=new GlobalNotice();
        if(globalStatus==GlobalStatus.fail){
            globalNotice.isSuccess=false;
        }else if(globalStatus==GlobalStatus.success){
            globalNotice.isSuccess=true;
        }
        //向所有的本地事务 发送通知
        for(String localId:keys){
           LocalType localType=maps.get(localId);
           globalNotice.localType=localType;//设置localType
           Message message=new Message(MessageTypeEnum.GlobalNotice, JsonUtil.objToJson(globalNotice), TimeUtil.getLocalTime());
           ReceiveContext receiveContext=new ReceiveContext<Message>(localType.serverAddress,message);
           ClientChannelManager.instance.sendMessage(receiveContext,true);
        }
        //redisHelper.deleteGlobalTransaction(globalId);//删除redis中存储的事务
    }
}
